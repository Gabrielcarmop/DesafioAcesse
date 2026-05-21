package com.example.desafio.infra.config;

import com.example.desafio.infra.kafka.event.ChunkConcluidoEvent;
import com.example.desafio.infra.kafka.event.LoteFinalizadoEvent;
import com.example.desafio.infra.kafka.event.LoteIniciadoEvent;
import com.example.desafio.infra.kafka.producer.LoteEventProducer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração explícita do Kafka para ter controle total sobre serialização,
 * trusted packages e o ack-mode do listener.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ── Producer ─────────────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        // 🔧 MUDANÇA 1: Ativar cabeçalhos de tipo para o Producer
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);  // ALTERADO: false -> true
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ── Consumer ─────────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.desafio.infra.kafka.event");

        // 🔧 MUDANÇA 2: Ativar uso de cabeçalhos de tipo e remover VALUE_DEFAULT_TYPE
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);  // ALTERADO: false -> true

        // 🔧 MUDANÇA 3: Adicionar mapeamento de tipos (fallback caso headers não funcionem)
        props.put(JsonDeserializer.TYPE_MAPPINGS,
                "loteIniciadoEvent:com.example.desafio.infra.kafka.event.LoteIniciadoEvent," +
                        "loteFinalizadoEvent:com.example.desafio.infra.kafka.event.LoteFinalizadoEvent," +
                        "chunkConcluidoEvent:com.example.desafio.infra.kafka.event.ChunkConcluidoEvent"
        );

        // REMOVER esta linha (está causando o problema):
        // props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3);
        return factory;
    }

    // ── Tópicos (criados automaticamente na inicialização) ───────────────────

    @Bean
    public NewTopic topicLoteIniciado() {
        return TopicBuilder.name(LoteEventProducer.TOPIC_LOTE_INICIADO)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicChunkConcluido() {
        return TopicBuilder.name(LoteEventProducer.TOPIC_CHUNK_CONCLUIDO)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicLoteFinalizado() {
        return TopicBuilder.name(LoteEventProducer.TOPIC_LOTE_FINALIZADO)
                .partitions(3).replicas(1).build();
    }
}