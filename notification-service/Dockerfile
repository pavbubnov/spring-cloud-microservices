FROM openjdk:8-jdk-alpine
EXPOSE 9999
COPY build/libs/notification-service-1.0-SNAPSHOT.jar .
ENV CONFIG_SERVER_URL="http://config-service:8001"
ENV SPR_PROFILE="docker"
ENV CONFIG_SERVICE_URL="http://registry:8761/eureka/"
ENV CONFIG_HOST="rabbitmq"
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "bill-service:8082", "--timeout=110", "--", "java", "-jar", "notification-service-1.0-SNAPSHOT.jar"]
#FROM openjdk:8-jdk-alpine
#EXPOSE 9999
#COPY build/libs/notification-service-1.0-SNAPSHOT.jar .
#ENV CONFIG_SERVER_URL="http://192.168.1.55:8001"
#ENV SPR_PROFILE="docker"
#ENV CONFIG_SERVICE_URL="http://192.168.1.55:8761/eureka/"
#ENV CONFIG_HOST="192.168.1.55"
#ENTRYPOINT ["java", "-jar", "notification-service-1.0-SNAPSHOT.jar"]