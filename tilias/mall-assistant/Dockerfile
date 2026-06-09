FROM maven:3.8.6-openjdk-8-slim AS build
WORKDIR /app
COPY pom.xml .
COPY mall-common mall-common/
COPY mall-mbg mall-mbg/
COPY mall-security mall-security/
COPY mall-admin mall-admin/
COPY mall-portal mall-portal/
COPY mall-search mall-search/
COPY tilias tilias/
RUN mvn install -pl mall-common,mall-mbg -am -DskipTests -Ddocker.skip=true -Dmaven.javadoc.skip=true --batch-mode
RUN mvn package -f tilias/mall-assistant/pom.xml -DskipTests -Ddocker.skip=true -Dmaven.javadoc.skip=true --batch-mode

FROM eclipse-temurin:8-jre-alpine
RUN apk add --no-cache tzdata curl
ENV TZ=Asia/Shanghai
COPY --from=build /app/tilias/mall-assistant/target/mall-assistant-1.0-SNAPSHOT.jar /app.jar
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD curl -f http://localhost:8086/assistant/health || exit 1
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=dev"]
