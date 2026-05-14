FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
COPY . .
RUN ./mvnw clean package -DskipTests -pl ${SERVICE_NAME} -am -q

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG SERVICE_NAME=coolai-auth
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
