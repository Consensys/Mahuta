FROM openjdk:8-jre

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait /wait
RUN chmod +x /wait


## Add and configure the java executable
ADD target/mahuta-http-api-exec.jar mahuta-http-api.jar
ENV CONF ""
EXPOSE 8040


## Run
CMD /wait && java -jar mahuta-http-api.jar if [ "$CONF" != "" ] ; then echo " --spring.config.additional-location=$CONF" ; fi