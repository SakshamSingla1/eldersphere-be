FROM maven:3.9.6-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/eldersphere-be-0.0.1-SNAPSHOT.jar eldersphere-be.jar
RUN mkdir -p /uploads
EXPOSE 8080
ENTRYPOINT ["java","-jar","eldersphere-be.jar"]
