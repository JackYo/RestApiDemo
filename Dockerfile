FROM maven AS build
COPY . .
RUN mvn package

FROM lpicanco/java11-alpine
ARG JAR_FILE=target/*.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
