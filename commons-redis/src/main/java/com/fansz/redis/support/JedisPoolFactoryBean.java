package com.fansz.redis.support;

import com.fansz.pub.utils.StringTools;
import com.wandoulabs.jodis.JedisPoolAdaptor;
import com.wandoulabs.jodis.JedisResourcePool;
import com.wandoulabs.jodis.RoundRobinJedisPool;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by allan on 16/1/6.
 */
public class JedisPoolFactoryBean implements FactoryBean {

    private String zkAddress;

    private String proxyDir;

    private int zkSessionTimeoutMs = 30000;

    private boolean useCodis = true;

    private JedisPoolConfig poolConfig;

    private String host;

    private int port = 6379;

    private int timeout = 2000;

    private String password;

    @Override
    public Object getObject() throws Exception {
        if (useCodis) {
            RoundRobinJedisPool.Builder builder = RoundRobinJedisPool.create();
            builder.curatorClient(zkAddress, zkSessionTimeoutMs).zkProxyDir(proxyDir);
            if (poolConfig != null) {
                builder.poolConfig(poolConfig);
            }
            if (StringTools.isNotBlank(password)) {
                builder.password(password);
            }
            return builder.build();
        } else {
            if (poolConfig == null) {
                poolConfig = new JedisPoolConfig();
            }
            return new JedisPoolAdaptor(poolConfig, host, port, timeout, password);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return JedisResourcePool.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }


    public void setProxyDir(String proxyDir) {
        this.proxyDir = proxyDir;
    }


    public void setZkSessionTimeoutMs(int zkSessionTimeoutMs) {
        this.zkSessionTimeoutMs = zkSessionTimeoutMs;
    }

    public void setUseCodis(boolean useCodis) {
        this.useCodis = useCodis;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
