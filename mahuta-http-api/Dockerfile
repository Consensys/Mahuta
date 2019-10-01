FROM openjdk:8-jre

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.1/wait /wait
RUN chmod +x /wait


## Add and configure the java executable
ADD target/mahuta-http-api-exec.jar mahuta-http-api.jar
ENV CONF "classpath:///application.yml"
EXPOSE 8040


## Run
CMD /wait && java -jar mahuta-http-api.jar --spring.config.additional-location=${CONF}