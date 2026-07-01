FROM maven:3.8.8-eclipse-temurin-8 AS builder

WORKDIR /build

COPY settings.xml /usr/share/maven/conf/settings.xml
COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2/repository mvn -B -ntp clean package -DskipTests

FROM maven:3.8.8-eclipse-temurin-8

WORKDIR /app

COPY --from=builder /build/target/exam-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
