#FROM openjdk:8-jdk-alpine
#EXPOSE 9876
#COPY build/libs/deposit-service-1.0-SNAPSHOT.jar .
#RUN apk add --no-cache bash
#COPY wait-for.sh /wait-for.sh
#RUN chmod +x /wait-for.sh
#ENTRYPOINT ["/wait-for.sh", "deposit-service:9090", "--timeout=110", "--", "java", "-jar",
#"payment-service-1.0-SNAPSHOT.jar"]
FROM openjdk:8-jdk-alpine
EXPOSE 9876
COPY build/libs/payment-service-1.0-SNAPSHOT.jar .
ENV CONFIG_SERVER_URL="http://192.168.1.55:8001"
ENV SPR_PROFILE="docker"
ENV CONFIG_SERVICE_URL="http://192.168.1.55:8761/eureka/"
ENV DATASOURCE_URL="jdbc:postgresql://192.168.1.55:5432/payment_service_database"
ENV CONFIG_HOST="192.168.1.55"
ENTRYPOINT ["java", "-jar", "payment-service-1.0-SNAPSHOT.jar"]