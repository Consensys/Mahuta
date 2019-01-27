FROM openjdk:8-jre
ADD target/mahuta-http-api-exec.jar mahuta-http-api.jar
EXPOSE 8040
ENV CONF ""
CMD ["java", "-jar", "mahuta-http-api.jar", "--spring.config.additional-location=${CONF}"]