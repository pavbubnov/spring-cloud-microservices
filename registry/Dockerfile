FROM openjdk:8-jdk-alpine
EXPOSE 8761
COPY build/libs/registry-1.0-SNAPSHOT.jar .
ENV CONFIG_SERVER_URL="http://config-service:8001"
ENV SPR_PROFILE="docker"
ENV CONFIG_SERVICE_URL="http://registry:8761/eureka/"
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "config-service:8001", "--timeout=70", "--", "java", "-jar", "registry-1.0-SNAPSHOT.jar"]
