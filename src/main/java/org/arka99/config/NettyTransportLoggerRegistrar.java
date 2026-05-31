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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NettyTransportLoggerRegistrar implements BeanCreatedEventListener<NettyEmbeddedServer> {

    @Override
    public NettyEmbeddedServer onCreated(BeanCreatedEvent<NettyEmbeddedServer> event) {
        NettyEmbeddedServer server = event.getBean();
        server.register(new NettyTransportLoggerCustomizer());
        return server;
    }

    private static final class NettyTransportLoggerCustomizer implements NettyServerCustomizer {

        private static final Logger log = LoggerFactory.getLogger(NettyTransportLoggerCustomizer.class);

        @Override
        public NettyServerCustomizer specializeForChannel(Channel channel, ChannelRole role) {
            if (role == ChannelRole.LISTENER) {
                return NettyServerCustomizer.super.specializeForChannel(channel, role);
            }
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
            installHandler("initial");
        }

        @Override
        public void onStreamPipelineBuilt() {
            installHandler("stream");
        }

        private void installHandler(String phase) {
            if (channel.pipeline().get(handlerName) == null) {
                channel.pipeline().addFirst(handlerName, new NettyTransportHandler(role));
            }
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
                log.info(
                    "[netty] inbound {} {} role={} thread={}",
                    request.method().name(),
                    request.uri(),
                    role,
                    Thread.currentThread().getName()
                );
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
            if (msg instanceof HttpResponse response) {
                log.info(
                    "[netty] received application response status={} role={} thread={}",
                    response.status().code(),
                    role,
                    Thread.currentThread().getName()
                );
                log.info(
                    "[netty] writing response status={} to client role={} thread={}",
                    response.status().code(),
                    role,
                    Thread.currentThread().getName()
                );
            } else if (msg instanceof LastHttpContent) {
                log.info(
                    "[netty] writing last response content type={} role={} thread={}",
                    msg.getClass().getSimpleName(),
                    role,
                    Thread.currentThread().getName()
                );
            } else if (msg instanceof HttpContent) {
                log.info(
                    "[netty] writing response content type={} role={} thread={}",
                    msg.getClass().getSimpleName(),
                    role,
                    Thread.currentThread().getName()
                );
            } else {
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
