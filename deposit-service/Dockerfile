FROM openjdk:8-jdk-alpine
EXPOSE 9090
COPY build/libs/deposit-service-1.0-SNAPSHOT.jar .
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "notification-service:9999", "--timeout=110", "--", "java", "-jar",
"deposit-service-1.0-SNAPSHOT.jar"]