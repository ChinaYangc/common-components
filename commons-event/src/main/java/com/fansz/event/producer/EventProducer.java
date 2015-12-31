package com.fansz.event.producer;


import com.fansz.event.type.AsyncEventType;
import com.fansz.pub.utils.JsonHelper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by allan on 15/12/16.
 */
public class EventProducer {

    private Properties kafkaProp;

    Producer<String, String> producer;

    @PostConstruct
    private void init() {
        if (kafkaProp == null) {
            throw new NullPointerException("kafka配置文件路径错误");
        }
        producer = new KafkaProducer(kafkaProp);
    }

    public void produce(AsyncEventType event, Object val) {
        producer.send(new ProducerRecord<String, String>(event.getName(), event.getCode(), JsonHelper.convertObject2JSONString(val)));

    }

    public void close() {
        if (producer != null) producer.close();
    }

    public void setKafkaProp(Properties kafkaProp) {
        this.kafkaProp = kafkaProp;
    }
}
