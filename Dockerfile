FROM openjdk:8-jdk-alpine
MAINTAINER Moonesh Kachroo "mooneshkachroo@hotmail.com"
VOLUME /tmp
ADD target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]