package com.example.demo.sqs;

import com.example.demo.rotation.RotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReceiver {

    private final RotationService rotationService;

    @SqsListener(value = "TaskQueue.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void receive(String taskId){
        log.info("Message (taskId) received: {}", taskId);
        rotationService.processTask(taskId);
    }
}
