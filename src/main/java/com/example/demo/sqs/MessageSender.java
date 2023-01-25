package com.example.demo.sqs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Service
@RequiredArgsConstructor
public class MessageSender {

    private static final String ENDPOINT_FIFO = "https://sqs.ap-northeast-1.amazonaws.com/836511517717/TaskQueue.fifo";

    private final QueueMessagingTemplate queueMessagingTemplate;

    @GetMapping("/send/{message}")
    public void send(@PathVariable(value = "message") String message){
        Map<String, Object> headers = new HashMap<>();
        headers.put("message-group-id", "groupId");
        queueMessagingTemplate.convertAndSend(ENDPOINT_FIFO, message, headers);
        log.info("Message sent: {}", message);
    }

    public void addTaskIdToSQS(String taskId){
        Map<String, Object> headers = new HashMap<>();
        headers.put("message-group-id", "groupId");
        queueMessagingTemplate.convertAndSend(ENDPOINT_FIFO, taskId, headers);
        log.info("Message sent: {}", taskId);
    }
}
