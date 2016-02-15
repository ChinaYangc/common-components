package com.fansz.event.search;

import com.fansz.pub.utils.CollectionTools;
import com.fansz.pub.utils.StringTools;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/2/3.
 */
public class SearchClientFactoryBean implements FactoryBean<Client>, DisposableBean {

    private Logger logger = LoggerFactory.getLogger(SearchClientFactoryBean.class);

    private Map<String, String> props;

    private String addresses;

    private TransportClient client;


    @Override
    public Client getObject() throws Exception {
        // 设置client.transport.sniff为true来使客户端去嗅探整个集群的状态，把集群中其它机器的ip地址加到客户端中，
        Settings settings = Settings.settingsBuilder().put(props).put("client.transport.sniff", true).build();
        client = TransportClient.builder().settings(settings).build();
        InetSocketTransportAddress[] nodes = resolveAddress(addresses);
        if (CollectionTools.isNotNullOrEmptyArray(nodes)) {
            client.addTransportAddresses(nodes);
        } else {
            logger.error("please configure elasticsearch node address,current is {}", addresses);
        }
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return Client.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    private InetSocketTransportAddress[] resolveAddress(String addresses) {
        List<InetSocketTransportAddress> nodes = new ArrayList<>();
        if (StringTools.isNotBlank(addresses)) {
            for (String addr : addresses.split(",")) {
                String[] hostAndAddr = addr.split(":");
                String host = hostAndAddr[0];
                int port = Integer.valueOf(hostAndAddr[1]);
                nodes.add(new InetSocketTransportAddress(new InetSocketAddress(host, port)));
            }
        }
        return nodes.toArray(new InetSocketTransportAddress[0]);
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }
}
