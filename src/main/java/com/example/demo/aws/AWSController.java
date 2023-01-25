package com.example.demo.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/aws")
@RequiredArgsConstructor
public class AWSController {

    private final AWSService service;

    @GetMapping("/hello")
    public String hello(){
        return "Hello from AWS Demo App!";
    }

    @GetMapping("/")
    public void getObjectBytes (@RequestParam String key){
        service.getObjectBytes(key);
    }

    @PostMapping("/upload-image")
    public String uploadFile(@ModelAttribute MultipartFile file) throws InterruptedException {
        return service.uploadFileToBucket(file);
    }

    @GetMapping("/task-state")
    public String getTaskStateOrLinkRotatedImageIfReady(@RequestParam String taskId){
        return service.getTaskStateOrLinkRotatedImageIfReady(taskId);
    }
}
