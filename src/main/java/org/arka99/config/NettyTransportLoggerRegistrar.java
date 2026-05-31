package org.arka99.config;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.server.netty.NettyEmbeddedServer;
import io.micronaut.http.server.netty.NettyServerCustomizer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@Singleton
public class NettyTransportLoggerRegistrar implements BeanCreatedEventListener<NettyEmbeddedServer> {

    @Override
    public NettyEmbeddedServer onCreated(BeanCreatedEvent<NettyEmbeddedServer> event) {
        NettyEmbeddedServer server = event.getBean();
        // Register the transport customizer before the server starts.
        //
        // Why this listener exists:
        // - the higher-level Micronaut filter/controller/service logs only show execution once the
        //   request is already inside application handling
        // - they do not show the earliest Netty transport boundary where the request first enters
        //   the server or the point where the response is written back to the client
        //
        // NettyServerCustomizer is Micronaut's supported extension point for hooking into the
        // Netty pipeline setup process. Registering here ensures our transport handler is available
        // when connection/request pipelines are created.
        server.register(new NettyTransportLoggerCustomizer());
        return server;
    }

    private static final class NettyTransportLoggerCustomizer implements NettyServerCustomizer {

        private static final Logger log = LoggerFactory.getLogger(NettyTransportLoggerCustomizer.class);

        @Override
        public NettyServerCustomizer specializeForChannel(Channel channel, ChannelRole role) {
            // Skip listener channels intentionally.
            //
            // A LISTENER channel represents the bound server socket itself, not an actual HTTP
            // request-processing channel. Adding per-request transport handlers there is noisy,
            // does not give meaningful request traces, and can lead to duplicate handler issues.
            //
            // We only care about channels that participate in actual request/response flow.
            if (role == ChannelRole.LISTENER) {
                return NettyServerCustomizer.super.specializeForChannel(channel, role);
            }

            // Log specialization so we know which channel roles Micronaut is using at runtime.
            // This is especially useful because HTTP/1 and HTTP/2 can surface different channel
            // structures such as CONNECTION or REQUEST_STREAM.
            log.info("[netty] specialized channel id={} role={} thread={}", channel.id().asShortText(), role,
                Thread.currentThread().getName());
            return new PerChannelNettyTransportLogger(channel, role);
        }
    }

    private static final class PerChannelNettyTransportLogger implements NettyServerCustomizer {

        private final Channel channel;
        private final NettyServerCustomizer.ChannelRole role;
        private final String handlerName;

        private PerChannelNettyTransportLogger(Channel channel, NettyServerCustomizer.ChannelRole role) {
            this.channel = channel;
            this.role = role;
            this.handlerName = NettyTransportHandler.class.getName() + "-" + role + "-" + channel.id().asShortText();
        }

        @Override
        public void onInitialPipelineBuilt() {
            // The initial pipeline is the earliest stable point where we can attach our logger
            // before inbound traffic starts flowing through the connection pipeline.
            installHandler("initial");
        }

        @Override
        public void onStreamPipelineBuilt() {
            // The stream pipeline represents the final request-processing setup.
            // Keeping the hook here too helps in cases where Micronaut finalizes request routing
            // in a later phase than the initial connection pipeline.
            installHandler("stream");
        }

        private void installHandler(String phase) {
            if (channel.pipeline().get(handlerName) == null) {
                // Add the handler at the front of the pipeline.
                //
                // Why addFirst:
                // - we want to observe inbound traffic as close as possible to Netty reception
                // - we want to observe outbound traffic before later handlers obscure the original
                //   message type or flow
                //
                // A per-channel handler name prevents duplicate registration in pipelines that may
                // trigger customization callbacks more than once during setup.
                channel.pipeline().addFirst(handlerName, new NettyTransportHandler(role));
            }

            // Log the pipeline phase so it is clear when our handler became active for the channel.
            LoggerFactory.getLogger(PerChannelNettyTransportLogger.class).info(
                "[netty] {} pipeline ready channel={} role={} thread={}",
                phase,
                channel.id().asShortText(),
                role,
                Thread.currentThread().getName()
            );
        }
    }

    private static final class NettyTransportHandler extends ChannelDuplexHandler {

        private static final Logger log = LoggerFactory.getLogger(NettyTransportHandler.class);

        private final NettyServerCustomizer.ChannelRole role;

        private NettyTransportHandler(NettyServerCustomizer.ChannelRole role) {
            this.role = role;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest request) {
                // This is the lowest-level request trace in the application.
                //
                // It shows the Netty thread that first observes the decoded HTTP request before
                // Micronaut application code runs. Comparing this thread with the later Micronaut
                // filter/controller logs reveals whether request processing stayed on the event loop
                // or was dispatched elsewhere.
                log.info(
                    "[netty] inbound {} {} role={} thread={}",
                    request.method().name(),
                    request.uri(),
                    role,
                    Thread.currentThread().getName()
                );

                // This second log marks the handoff point conceptually: after this handler passes
                // the message downstream, Micronaut's HTTP server pipeline can route it into the
                // application layer.
                log.info(
                    "[netty] dispatching inbound {} {} to Micronaut role={} thread={}",
                    request.method().name(),
                    request.uri(),
                    role,
                    Thread.currentThread().getName()
                );
            }
            super.channelRead(ctx, msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            switch (msg) {
                case HttpResponse response -> {
                    // A plain HttpResponse marks the point where application output re-enters Netty's
                    // outbound pipeline. This is the counterpart to the inbound dispatch log above.
                    log.info(
                        "[netty] received application response status={} role={} thread={}",
                        response.status().code(),
                        role,
                        Thread.currentThread().getName()
                    );

                    // This log highlights the moment Netty is about to send the response toward the
                    // client, which helps compare the application response thread with the transport
                    // write thread.
                    log.info(
                        "[netty] writing response status={} to client role={} thread={}",
                        response.status().code(),
                        role,
                        Thread.currentThread().getName()
                    );
                }
                case LastHttpContent lastHttpContent ->
                    // Netty may emit the terminal content chunk separately from the initial response
                    // headers. Logging it helps explain cases where no single full HttpResponse object
                    // appears to represent the entire outbound response flow.
                    log.info(
                        "[netty] writing last response content type={} role={} thread={}",
                        msg.getClass().getSimpleName(),
                        role,
                        Thread.currentThread().getName()
                    );
                case HttpContent httpContent ->
                    // Chunked or streaming responses often travel through Netty as HttpContent pieces.
                    // This log makes those partial outbound writes visible instead of assuming every
                    // response is represented by one HttpResponse instance.
                    log.info(
                        "[netty] writing response content type={} role={} thread={}",
                        msg.getClass().getSimpleName(),
                        role,
                        Thread.currentThread().getName()
                    );
                default ->
                    // Keep a fallback for unknown outbound message types.
                    // This is useful because Netty/Micronaut internals may evolve and start writing a
                    // different message class in future versions. Logging the concrete type keeps the
                    // transport behavior observable instead of silently dropping that information.
                    log.info(
                        "[netty] writing outbound message type={} role={} thread={}",
                        msg.getClass().getName(),
                        role,
                        Thread.currentThread().getName()
                    );
            }
            super.write(ctx, msg, promise);
        }
    }
}
