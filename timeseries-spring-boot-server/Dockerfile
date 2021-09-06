## check still exists at https://hub.docker.com/ if have issues
FROM jjosef/11-jdk-slim-docker
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8901 8092


