FROM openjdk:8-jdk-alpine
EXPOSE 9090
COPY build/libs/deposit-service-1.0-SNAPSHOT.jar .
ENV CONFIG_SERVER_URL="http://config-service:8001"
ENV SPR_PROFILE="docker"
ENV CONFIG_SERVICE_URL="http://registry:8761/eureka/"
ENV DATASOURCE_URL="jdbc:postgresql://192.168.1.55:5432/deposit_service_database"
ENV CONFIG_HOST="rabbitmq"
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "notification-service:9999", "--timeout=110", "--", "java", "-jar", "deposit-service-1.0-SNAPSHOT.jar"]
