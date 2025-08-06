package com.example.payment_service.config;

import com.example.payment_service.dto.event.OrderEventDto;
import com.example.payment_service.dto.event.PaymentEventDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Generic props
    private Map<String, Object> commonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    // --- OrderEvent Producer Beans ---
    @Bean
    public ProducerFactory<Long, OrderEventDto> orderProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerProps());
    }

    @Bean(name = "orderKafkaTemplate")
    public KafkaTemplate<Long, OrderEventDto> orderKafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    // --- PaymentEvent Producer Beans ---
    @Bean
    public ProducerFactory<Long, PaymentEventDto> paymentProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerProps());
    }

    @Bean(name = "paymentKafkaTemplate")
    public KafkaTemplate<Long, PaymentEventDto> paymentKafkaTemplate() {
        return new KafkaTemplate<>(paymentProducerFactory());
    }

    // --- Consumer configuration (for PaymentEventDto) ---
    @Bean
    public ConsumerFactory<Long, PaymentEventDto> paymentConsumerFactory(
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        JsonDeserializer<PaymentEventDto> deserializer = new JsonDeserializer<>(PaymentEventDto.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, PaymentEventDto> paymentKafkaListenerContainerFactory(
            ConsumerFactory<Long, PaymentEventDto> paymentConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, PaymentEventDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory);
        return factory;
    }
}
