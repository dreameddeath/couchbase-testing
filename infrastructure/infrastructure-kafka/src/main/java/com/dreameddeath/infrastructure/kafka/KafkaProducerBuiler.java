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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Properties;

/**
 * Created by Christophe Jeunesse on 18/05/2015.
 */
public class KafkaProducerBuiler<K,V> {
    private String brokerCnxString;
    private String clientId=null;
    private AckMode ackMode = AckMode.MASTER_ACK;
    private CompressionMode compressionMode = CompressionMode.NONE;
    private Serializer<K> keySerializer;
    private Serializer<V> valueSerializer;
    private Class<Serializer<K>> keySerializerClass;
    private Class<Serializer<V>> valueSerializerClass;

    public enum AckMode{
        NO_ACK("0"), MASTER_ACK("1"), ALL_ACK("all");
        private String value;
        AckMode(String value){this.value = value;}
        @Override public String toString(){ return value;}
    }


    public enum CompressionMode{
        NONE("none"), GZIP("gzip"), SNAPPY("snappy");
        private String value;
        CompressionMode(String value){this.value = value;}
        @Override public String toString(){return value;}
    }

    public KafkaProducerBuiler<K,V> withClientId(String clientId){
        this.clientId = clientId;
        return this;
    }

    public KafkaProducerBuiler<K,V> withBrokerCnx(String brokerCnxString){
        this.brokerCnxString = brokerCnxString;
        return this;
    }

    public KafkaProducerBuiler<K,V> withKeySerializerClass(Class<Serializer<K>> keySerializerClass){
        this.keySerializerClass = keySerializerClass;
        return this;
    }

    public KafkaProducerBuiler<K,V> withValueSerializerClass(Class<Serializer<V>> valueSerializerClass){
        this.valueSerializerClass = valueSerializerClass;
        return this;
    }

    public KafkaProducerBuiler<K,V> withKeySerializer(Serializer<K> keySerializer){
        this.keySerializer = keySerializer;
        return this;
    }

    public KafkaProducerBuiler<K,V> withValueSerializer(Serializer<V> valueSerializer){
        this.valueSerializer = valueSerializer;
        return this;
    }

    public KafkaProducerBuiler<K,V> withAckMode(AckMode mode){
        this.ackMode = mode;
        return this;
    }

    public Producer<K,V> build() throws Exception{
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerCnxString);
        props.put(ProducerConfig.ACKS_CONFIG, ackMode.toString());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,compressionMode.toString());

        if(clientId!=null){
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        }

        if((keySerializer!=null) && (valueSerializer!=null)){
            return new KafkaProducer<>(props,keySerializer,valueSerializer);
        }
        else if((keySerializerClass!=null) && (valueSerializerClass!=null)){
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass.getName());
            return new KafkaProducer<>(props);
        }
        else{
            throw new IllegalArgumentException("At least one of the serializers are missing");
        }
    }
}
