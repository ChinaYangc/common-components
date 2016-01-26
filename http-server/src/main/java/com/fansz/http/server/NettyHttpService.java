package com.fansz.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * HTTP服务提供类
 *
 * @author yanyanming
 */
public final class NettyHttpService {

    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpService.class);

    private InetSocketAddress bindAddress;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private final int httpChunkLimit;

    private int readTimeOut;//读超时时间

    private int writeTimeOut;//写超时时间

    private int rwTimeOut;

    private SimpleChannelInboundHandler<FullHttpRequest> httpRequestRouter;

    /**
     * 初始化NettyHttpService
     *
     * @param httpRequestRouter
     * @param bindAddress       HTTP服务绑定的地址.
     * @param httpChunkLimit    上传文件大小限制
     */
    private NettyHttpService(SimpleChannelInboundHandler<FullHttpRequest> httpRequestRouter,
                             InetSocketAddress bindAddress, int httpChunkLimit,
                             int readTimeOut, int writeTimeOut, int rwTimeOut) {
        this.httpRequestRouter = httpRequestRouter;
        this.bindAddress = bindAddress;
        this.httpChunkLimit = httpChunkLimit;
        this.readTimeOut = readTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.rwTimeOut = rwTimeOut;
    }


    public static Builder builder() {
        return new Builder();
    }

    public void startUp() throws Exception {
        try {
            LOG.info("Starting http service on address {}...", bindAddress);
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            // 默认的worker线程数为 2 * numberOfCpuCores
            final LoggingHandler logging = new LoggingHandler();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("logging", logging);
                            ch.pipeline().addLast("idleStateHandler",
                                    new IdleStateHandler(readTimeOut, writeTimeOut, rwTimeOut));
                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            pipeline.addLast("decoder", new HttpRequestDecoder());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(httpChunkLimit));
                            // pipeline.addLast("compressor", new HttpContentCompressor());
                            pipeline.addLast("router", httpRequestRouter);
                        }

                    }).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_BACKLOG, 256 * 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_RCVBUF, 256 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            ChannelFuture f = b.bind(bindAddress).sync();
            LOG.info("Http service is running on address {}", bindAddress);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("error to start http server", e);
        } finally {
            shutDown();
        }
    }

    /**
     * @return Http服务绑定的地址
     */
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void shutDown() throws Exception {
        LOG.info("Stopping service on address {}...", bindAddress);
        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } finally {
        }
        LOG.info("Done stopping service on address {}", bindAddress);
    }

    /**
     * NettyHttpService构建工具类
     */
    public static class Builder {

        private static final int DEFAULT_HTTP_CHUNK_LIMIT = 2 * 1024 * 1024;// 默认最大2M

        private static final int DEFAULT_LISTEN_PORT = 80;// 默认绑定80端口
        private static final int IDEL_TIME_OUT = 300;

        private static final int READ_IDEL_TIME_OUT = 300;

        private static final int WRITE_IDEL_TIME_OUT = 300;

        private String host;

        private int port;


        private int httpChunkLimit;

        private int readTimeOut;//读超时时间

        private int writeTimeOut;//写超时时间

        private int rwTimeOut;// 读写超时时间

        private SimpleChannelInboundHandler<FullHttpRequest> httpRequestRouter;

        // 避免外部类通过new直接创建Builder对象
        private Builder() {
            port = DEFAULT_LISTEN_PORT;
            httpChunkLimit = DEFAULT_HTTP_CHUNK_LIMIT;
            readTimeOut = READ_IDEL_TIME_OUT;
            writeTimeOut = WRITE_IDEL_TIME_OUT;
            rwTimeOut = IDEL_TIME_OUT;
        }


        public Builder setHttpChunkLimit(int httpChunkLimit) {
            this.httpChunkLimit = httpChunkLimit;
            return this;
        }

        public Builder setHttpRequestRouter(SimpleChannelInboundHandler<FullHttpRequest> httpRequestRouter) {
            this.httpRequestRouter = httpRequestRouter;
            return this;
        }

        /**
         * 设置监听端口，默认监听80端口
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * 设置绑定地址，默认绑定所有地址
         *
         * @param host bindAddress for the service.
         * @return instance of {@code Builder}.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * 当超过readTimeOut(单位为s)未读取到数据时,netty关闭连接
         *
         * @param readTimeOut
         */
        public void setReadTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
        }

        /**
         * 当超过writeTimeOut(单位为s)未写数据时,netty关闭连接
         *
         * @param writeTimeOut
         */
        public void setWriteTimeOut(int writeTimeOut) {
            this.writeTimeOut = writeTimeOut;
        }

        /**
         * 当超过rwTimeOut(单位为s)未读写数据时,netty关闭连接
         *
         * @param rwTimeOut
         */
        public void setRwTimeOut(int rwTimeOut) {
            this.rwTimeOut = rwTimeOut;
        }

        /**
         * @return {@code NettyHttpService}实例
         */
        public NettyHttpService build() {
            if (httpRequestRouter == null) {
                throw new NullPointerException("httpRequestRouter cann't be null!");
            }
            InetSocketAddress bindAddress;
            if (host == null) {
                bindAddress = new InetSocketAddress(port);
            } else {
                bindAddress = new InetSocketAddress(host, port);
            }

            return new NettyHttpService(httpRequestRouter, bindAddress, httpChunkLimit, readTimeOut, writeTimeOut, rwTimeOut);
        }
    }
}
