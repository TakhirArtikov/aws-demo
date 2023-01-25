FROM linux/amd64
FROM amazoncorretto:17
COPY /target/aws-demo.jar aws-demo.jar
ENTRYPOINT ["java", "-jar", "aws-demo.jar"]