package com.example.demo.aws;

import com.example.demo.dynamodb.TaskEntity;
import com.example.demo.dynamodb.TaskRepository;
import com.example.demo.dynamodb.TaskState;
import com.example.demo.sqs.MessageSender;
import com.example.demo.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSService {

    private static final String BUCKET= "tahir-aws-s3";
    private final TaskRepository taskRepository;
    private final MessageSender messageSender;
    private final ImageValidator imageValidator;

    private static final Region region = Region.AP_NORTHEAST_1;
    private static final S3Client s3 = S3Client.builder()
            .region(region)
            .build();

    @Transactional(readOnly = true)
    public String getTaskStateOrLinkRotatedImageIfReady(String taskId){
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(taskId);
        if(taskEntityOptional.isPresent()){
            TaskEntity taskEntity = taskEntityOptional.get();
            if(taskEntity.getTaskState() == TaskState.DONE) return taskEntity.getRotatedFilePath();
            return taskEntity.getTaskState().toString();
        }
        else return "TaskId is invalid";
    }

    public void getObjectBytes(String key) {
        File myFile = new File(key);
        try(OutputStream os = new FileOutputStream(myFile)) {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(key)
                    .bucket(BUCKET)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();
            os.write(data);
            log.info("Successfully obtained bytes from an S3 object");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    @Transactional
    public String uploadFileToBucket(MultipartFile file) throws InterruptedException {
        if(!imageValidator.validate(file.getOriginalFilename())) throw new IllegalStateException("This is not image file!");
        File myFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try(OutputStream os = new FileOutputStream(myFile)){
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(file.getOriginalFilename())
                    .build();


            os.write(file.getBytes());
            log.info("Successfully obtained bytes from an S3 object");
            s3.putObject(objectRequest, RequestBody.fromFile(myFile));

            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setFileName(file.getOriginalFilename());
            taskEntity.setTaskState(TaskState.CREATED);
            taskEntity.setOriginalFilePath(makeObjectUrl(file.getOriginalFilename()));
            taskEntity.setRotatedFilePath(null);
            taskRepository.save(taskEntity);
            messageSender.addTaskIdToSQS(taskEntity.getTaskId());
            return taskEntity.getTaskId();
        } catch (S3Exception | NoSuchElementException | IOException e) {
            log.error(e.getMessage());
            System.exit(1);
            throw new InterruptedException("");
        }
    }

    private String makeObjectUrl(String fileName){
        return "https://" + BUCKET + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
