package com.example.demo.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSqs
public class Config {
    public static final String ACCESS_KEY = "AKIA4FQ7YZQKUP6TJ5OB";
    public static final String SECRET_KEY = "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";

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(){
        return new QueueMessagingTemplate(amazonSQSAsync());
    }

    @Bean
    @Primary
    public AmazonSQSAsync amazonSQSAsync() {
        BasicAWSCredentials credentials=new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

       return AmazonSQSAsyncClientBuilder
               .standard()
               .withRegion(Regions.AP_NORTHEAST_1)
               .withCredentials(new AWSStaticCredentialsProvider(credentials))
               .build();
    }


    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSQSAsync) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSQSAsync);
        factory.setAutoStartup(true);
        factory.setMaxNumberOfMessages(10);
        factory.setQueueMessageHandler(queueMessageHandler(amazonSQSAsync));
        return factory;
    }

    @Bean
    public QueueMessageHandler queueMessageHandler(AmazonSQSAsync amazonSQSAsync) {
        QueueMessageHandlerFactory queueMsgHandlerFactory = new QueueMessageHandlerFactory();
        queueMsgHandlerFactory.setAmazonSqs(amazonSQSAsync);
        QueueMessageHandler queueMessageHandler = queueMsgHandlerFactory.createQueueMessageHandler();
        List<HandlerMethodArgumentResolver> list = new ArrayList<>();
        HandlerMethodArgumentResolver resolver = new PayloadMethodArgumentResolver(new MappingJackson2MessageConverter());
        list.add(resolver);
        queueMessageHandler.setArgumentResolvers(list);
        return queueMessageHandler;
    }
}