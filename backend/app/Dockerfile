FROM eclipse-temurin:17-alpine
VOLUME /tmp
COPY build/libs/*T.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
