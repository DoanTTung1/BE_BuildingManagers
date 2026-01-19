# 1. Dùng Maven để build file .jar (Dùng JDK 17 hoặc 21 tuỳ project của bạn)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Chạy ứng dụng bằng JDK rút gọn cho nhẹ
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]