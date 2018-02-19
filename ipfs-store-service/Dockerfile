FROM openjdk:8-jre
ADD target/ipfs-store-exec.jar app.jar
EXPOSE 8040
CMD ["java", "-jar", "app.jar"]