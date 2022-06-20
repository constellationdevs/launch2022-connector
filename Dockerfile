FROM adoptopenjdk/openjdk11
VOLUME /d/tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
EXPOSE 9154
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]