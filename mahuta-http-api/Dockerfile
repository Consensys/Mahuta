FROM openjdk:8-jre
ADD target/mahuta-http-api-exec.jar mahuta-http-api.jar
ENV CONF ""
EXPOSE 8040
CMD ["java", "-jar", "mahuta-http-api.jar", "--spring.config.additional-location=${CONF}"]