FROM openjdk:8-jdk-alpine
EXPOSE 9876
COPY build/libs/deposit-service-1.0-SNAPSHOT.jar .
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "deposit-service:9090", "--timeout=110", "--", "java", "-jar",
"payment-service-1.0-SNAPSHOT.jar"]