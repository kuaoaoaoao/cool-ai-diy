package com.coolai.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Queue names
    public static final String DOCUMENT_PROCESS_QUEUE = "coolai.document.process";
    public static final String VOICE_TRAIN_QUEUE = "coolai.voice.train";
    public static final String SINGING_QUEUE = "coolai.singing";
    public static final String VIDEO_QUEUE = "coolai.video";
    public static final String DEEPFAKE_QUEUE = "coolai.deepfake";

    // Exchange
    public static final String COOLAI_EXCHANGE = "coolai.exchange";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(10);
        return factory;
    }

    // Exchange
    @Bean
    public TopicExchange coolaiExchange() {
        return new TopicExchange(COOLAI_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue documentProcessQueue() {
        return QueueBuilder.durable(DOCUMENT_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DOCUMENT_PROCESS_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue voiceTrainQueue() {
        return QueueBuilder.durable(VOICE_TRAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", VOICE_TRAIN_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue singingQueue() {
        return QueueBuilder.durable(SINGING_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SINGING_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue videoQueue() {
        return QueueBuilder.durable(VIDEO_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", VIDEO_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue deepfakeQueue() {
        return QueueBuilder.durable(DEEPFAKE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DEEPFAKE_QUEUE + ".dlq")
                .build();
    }

    // Bindings
    @Bean
    public Binding documentProcessBinding() {
        return BindingBuilder.bind(documentProcessQueue())
                .to(coolaiExchange())
                .with("coolai.document.*");
    }

    @Bean
    public Binding voiceTrainBinding() {
        return BindingBuilder.bind(voiceTrainQueue())
                .to(coolaiExchange())
                .with("coolai.voice.*");
    }

    @Bean
    public Binding singingBinding() {
        return BindingBuilder.bind(singingQueue())
                .to(coolaiExchange())
                .with("coolai.singing.*");
    }

    @Bean
    public Binding videoBinding() {
        return BindingBuilder.bind(videoQueue())
                .to(coolaiExchange())
                .with("coolai.video.*");
    }

    @Bean
    public Binding deepfakeBinding() {
        return BindingBuilder.bind(deepfakeQueue())
                .to(coolaiExchange())
                .with("coolai.deepfake.*");
    }
}
