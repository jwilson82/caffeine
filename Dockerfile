FROM maven:3.6.3-openjdk-15 AS MAVEN

COPY ./pom.xml .
COPY ./src/ ./src/

RUN mvn clean package

FROM openjdk:15-slim

COPY --from=MAVEN ./target/caffeine-*.jar /caffeine.jar

CMD ["java", "-jar", "/caffeine.jar"]
