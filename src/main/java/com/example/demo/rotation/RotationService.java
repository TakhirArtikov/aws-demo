package com.example.demo.rotation;

import com.example.demo.dynamodb.TaskEntity;
import com.example.demo.dynamodb.TaskRepository;
import com.example.demo.dynamodb.TaskState;
import com.example.demo.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RotationService {

    private static final String BUCKET= "tahir-aws-s3";
    private final TaskRepository taskRepository;
    private final ImageValidator imageValidator;

    private static final Region region = Region.AP_NORTHEAST_1;
    private static final S3Client s3 = S3Client.builder()
            .region(region)
            .build();

    @Transactional
    public void processTask(String taskId){
        Optional<TaskEntity> taskEntityOptional = taskRepository.findByTaskId(taskId);
        if(taskEntityOptional.isPresent() && taskEntityOptional.get().getTaskState() != TaskState.DONE){
            TaskEntity taskEntity = taskEntityOptional.get();
            taskEntity.setTaskState(TaskState.IN_PROGRESS);
            taskRepository.save(taskEntity);
            String rotateFile = getObjectBytes(taskEntity.getFileName());
            if(rotateFile != null) {
                taskEntity.setTaskState(TaskState.DONE);
                taskEntity.setRotatedFilePath(rotateFile);
                taskRepository.save(taskEntity);
            }
        }
    }

    public String getObjectBytes(String key) {
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

            BufferedImage image = ImageIO.read(myFile);
            BufferedImage rotatedImage = rotate(image);
            File rotatedImageFile = new File("rotated-" + key);
            ImageIO.write(rotatedImage, imageValidator.getImageFileExtension(key), rotatedImageFile);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(rotatedImageFile.getName())
                    .build();
            s3.putObject(putObjectRequest, RequestBody.fromFile(rotatedImageFile));
            String rotateFileS3Path = makeObjectUrl(rotatedImageFile.getName());
            myFile.deleteOnExit();
            rotatedImageFile.deleteOnExit();
            return rotateFileS3Path;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

    public BufferedImage rotate(BufferedImage img) {

        // Getting Dimensions of image
        double width = img.getWidth();
        double height = img.getHeight();

        // Creating a new buffered image
        BufferedImage newImage = new BufferedImage(
                img.getWidth(), img.getHeight(), img.getType());

        // creating Graphics in buffered image
        Graphics2D g2 = newImage.createGraphics();

        g2.rotate(Math.toRadians(180), width / 2, height / 2);
        g2.drawImage(img, null, 0, 0);

        // Return rotated buffer image
        return newImage;
    }

    private String makeObjectUrl(String fileName){
        return "https://" + BUCKET + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
