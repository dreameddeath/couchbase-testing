/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.infrastructure.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceCallback;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Properties;

/**
 * Created by Christophe Jeunesse on 18/05/2015.
 */
public class KafkaConsumerBuilder<K,V> {
    private String brokerCnxString;
    private String clientId=null;
    private Deserializer<K> keyDeserializer;
    private Deserializer<V> valueDeserializer;
    private ConsumerRebalanceCallback consumerRebalanceCallback = null;
    private Class<Deserializer<K>> keyDeserializerClass;
    private Class<Deserializer<V>> valueDeserializerClass;

    
    public KafkaConsumerBuilder<K,V> withClientId(String clientId){
        this.clientId = clientId;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withBrokerCnx(String brokerCnxString){
        this.brokerCnxString = brokerCnxString;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withKeyDeserializerClass(Class<Deserializer<K>> keyDeserializerClass){
        this.keyDeserializerClass = keyDeserializerClass;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withValueDeserializerClass(Class<Deserializer<V>> valueDeserializerClass){
        this.valueDeserializerClass = valueDeserializerClass;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withKeyDeserializer(Deserializer<K> keyDeserializer){
        this.keyDeserializer = keyDeserializer;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withValueDeserializer(Deserializer<V> valueDeserializer){
        this.valueDeserializer = valueDeserializer;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withRebalanceCallback(ConsumerRebalanceCallback consumerRebalanceCallback){
        this.consumerRebalanceCallback = consumerRebalanceCallback;
        return this;
    }

    public Consumer<K,V> build() throws Exception{
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerCnxString);
    
        if(clientId!=null){
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        }

        if((keyDeserializer !=null) && (valueDeserializer !=null)){
            return new KafkaConsumer<>(props,consumerRebalanceCallback, keyDeserializer, valueDeserializer);
        }
        else if((keyDeserializerClass !=null) && (valueDeserializerClass !=null)){
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass.getName());
            return new KafkaConsumer<>(props,consumerRebalanceCallback);
        }
        else{
            throw new IllegalArgumentException("At least one of the deserializers are missing");
        }
    }
}
