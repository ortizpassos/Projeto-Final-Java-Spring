FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/monitor-ellas-api-1.0.0.jar app.jar

EXPOSE 3001

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV MONGO_URI="mongodb://localhost:27017/production-monitor"
ENV JWT_SECRET="segredo_super_secreto"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
