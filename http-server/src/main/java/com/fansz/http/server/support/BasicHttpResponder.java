package com.fansz.http.server.support;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HttpResponder实现类，用于向客户端产生响应内容
 */
public class BasicHttpResponder {

    private Logger LOG = LoggerFactory.getLogger(BasicHttpResponder.class);

    private final Channel channel;

    private final boolean keepAlive;

    private final AtomicBoolean responded;

    public BasicHttpResponder(Channel channel, boolean keepAlive) {
        this.channel = channel;
        this.keepAlive = keepAlive;
        responded = new AtomicBoolean(false);
    }

    public void sendJson(HttpResponseStatus status, Object object) {
        ByteBuf buffer = null;
        try {
            String content = object instanceof String ? (String) object : JSON.toJSONString(object);
            buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
            sendContent(status, buffer, "application/json", new HashMap<String, String>());
        } catch (Exception e) {
            throw e;
        } finally {
            if (buffer != null) {
                buffer.release();
            }
        }
    }

    public void sendContent(HttpResponseStatus status, ByteBuf content, String contentType,
                            Map<String, String> headers) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);

        setCustomHeaders(response, headers);

        if (content != null) {
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
            response.content().writeBytes(content);
        } else {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }

        boolean responseKeepAlive = setResponseKeepAlive(response);
        ChannelFuture future = channel.writeAndFlush(response);
        if (!responseKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    public void sendStatus(HttpResponseStatus status) {
        sendContent(status, null, null, new HashMap<String, String>());
    }

    public boolean isCommitted() {
        return responded.get();
    }

    /**
     * 设置Http Header
     *
     * @param response
     * @param headers
     */
    private void setCustomHeaders(HttpResponse response, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                response.headers().set(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean setResponseKeepAlive(HttpResponse response) {
        boolean closeConn = HttpHeaders.Values.CLOSE.equalsIgnoreCase(response.headers().get(HttpHeaders.Names.CONNECTION));
        boolean responseKeepAlive = this.keepAlive && !closeConn;

        if (responseKeepAlive) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }

        return responseKeepAlive;
    }

}
