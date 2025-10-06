package com.spmorangle.common.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Exchange names (topic exchanges for routing flexibility)
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    
    // Queue names
    public static final String COMMENT_QUEUE = "notification.comment.queue";
    public static final String TASK_QUEUE = "notification.task.queue";
    public static final String USER_QUEUE = "notification.user.queue";
    public static final String PROJECT_QUEUE = "notification.project.queue";
    
    // Routing keys
    public static final String COMMENT_ROUTING_KEY = "notification.comment.*";
    public static final String TASK_ROUTING_KEY = "notification.task.*";
    public static final String USER_ROUTING_KEY = "notification.user.*";
    public static final String PROJECT_ROUTING_KEY = "notification.project.*";
    
    // Dead Letter Queue
    public static final String DLQ_EXCHANGE = "notification.dlq.exchange";
    public static final String DLQ_QUEUE = "notification.dlq.queue";
    
    // Exchange
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    
    // Queues with DLQ
    @Bean
    public Queue commentQueue() {
        return QueueBuilder.durable(COMMENT_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }
    
    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(TASK_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }
    
    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(USER_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }
    
    @Bean
    public Queue projectQueue() {
        return QueueBuilder.durable(PROJECT_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }
    
    // Dead Letter Queue
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }
    
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }
    
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlqExchange()).with("dlq");
    }
    
    // Bindings (Connect queues to exchange)
    @Bean
    public Binding commentBinding() {
        return BindingBuilder.bind(commentQueue())
            .to(notificationExchange())
            .with(COMMENT_ROUTING_KEY);
    }
    
    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(taskQueue())
            .to(notificationExchange())
            .with(TASK_ROUTING_KEY);
    }
    
    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue())
            .to(notificationExchange())
            .with(USER_ROUTING_KEY);
    }
    
    @Bean
    public Binding projectBinding() {
        return BindingBuilder.bind(projectQueue())
            .to(notificationExchange())
            .with(PROJECT_ROUTING_KEY);
    }
    
    // JSON Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
