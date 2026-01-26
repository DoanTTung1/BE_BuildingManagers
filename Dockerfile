# =========================================================
# STAGE 1: BUILD (Dùng Maven với JDK 21)
# =========================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ source code vào
COPY . .

# Build ra file .jar và BỎ QUA TEST (Tránh lỗi môi trường trên Render)
RUN mvn clean package -DskipTests

# =========================================================
# STAGE 2: RUN (Dùng JDK 21 Alpine cho nhẹ)
# =========================================================
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy file .jar từ bước build sang bước chạy
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java","-jar","app.jar"]