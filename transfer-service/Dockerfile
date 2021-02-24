FROM openjdk:8-jdk-alpine
EXPOSE 5431
COPY build/libs/deposit-service-1.0-SNAPSHOT.jar .
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "payment-service:9876", "--timeout=110", "--", "java", "-jar",
"transfer-service-1.0-SNAPSHOT.jar"]