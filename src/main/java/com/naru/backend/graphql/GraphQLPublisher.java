package com.naru.backend.graphql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxProcessor;

@Component
public class GraphQLPublisher {
    private final Map<String, FluxProcessor<Object, Object>> processors = new ConcurrentHashMap<>();

    /**
     * 특정 토픽으로 데이터를 발행하는 메서드
     *
     * @param topic   발행할 토픽 이름
     * @param payload 발행할 데이터
     */
    public <T> void publish(String topic, T payload) {
        processors.computeIfAbsent(topic, key -> DirectProcessor.create().serialize()).onNext(payload);
    }

    /**
     * 특정 토픽에 대한 Publisher 반환
     *
     * @param topic 토픽 이름
     * @param type  반환할 데이터 타입
     * @return Publisher<T>
     */
    public <T> Publisher<T> getPublisher(String topic, Class<T> type) {
        return processors
                .computeIfAbsent(topic, key -> DirectProcessor.create().serialize())
                .map(type::cast);
    }
}