# ==========================
# 🏗️ Build Stage
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -e -DskipTests clean package

# ==========================
# 🚀 Run Stage
# ==========================
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Lightweight JVM flags for Render Free Tier
ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=$PORT"]
