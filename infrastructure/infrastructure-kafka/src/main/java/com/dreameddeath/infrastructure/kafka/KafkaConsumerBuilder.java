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
    private String _brokerCnxString;
    private String _clientId=null;
    private Deserializer<K> _keyDeserializer;
    private Deserializer<V> _valueDeserializer;
    private ConsumerRebalanceCallback _consumerRebalanceCallback = null;
    private Class<Deserializer<K>> _keyDeserializerClass;
    private Class<Deserializer<V>> _valueDeserializerClass;

    
    public KafkaConsumerBuilder<K,V> withClientId(String clientId){
        _clientId = clientId;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withBrokerCnx(String brokerCnxString){
        _brokerCnxString = brokerCnxString;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withKeyDeserializerClass(Class<Deserializer<K>> keySerializerClass){
        _keyDeserializerClass = keySerializerClass;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withValueDeserializerClass(Class<Deserializer<V>> valueSerializerClass){
        _valueDeserializerClass = valueSerializerClass;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withKeyDeserializer(Deserializer<K> keySerializer){
        _keyDeserializer = keySerializer;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withValueDeserializer(Deserializer<V> valueSerializer){
        _valueDeserializer = valueSerializer;
        return this;
    }

    public KafkaConsumerBuilder <K,V> withRebalanceCallback(ConsumerRebalanceCallback consumerRebalanceCallback){
        _consumerRebalanceCallback = consumerRebalanceCallback;
        return this;
    }

    public Consumer<K,V> build() throws Exception{
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, _brokerCnxString);
    
        if(_clientId!=null){
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, _clientId);
        }

        if((_keyDeserializer !=null) && (_valueDeserializer !=null)){
            return new KafkaConsumer<>(props,_consumerRebalanceCallback, _keyDeserializer, _valueDeserializer);
        }
        else if((_keyDeserializerClass !=null) && (_valueDeserializerClass !=null)){
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, _keyDeserializerClass.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, _valueDeserializerClass.getName());
            return new KafkaConsumer<>(props,_consumerRebalanceCallback);
        }
        else{
            throw new IllegalArgumentException("At least one of the deserializers are missing");
        }
    }
}
