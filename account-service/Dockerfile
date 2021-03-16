FROM openjdk:8-jdk-alpine
EXPOSE 8081
COPY build/libs/account-service-1.0-SNAPSHOT.jar .
ENV CONFIG_SERVER_URL="http://config-service:8001"
ENV SPR_PROFILE="docker"
ENV CONFIG_SERVICE_URL="http://registry:8761/eureka/"
ENV DATASOURCE_URL="jdbc:postgresql://192.168.1.55:5432/account_service_database"
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "gateway:8989", "--timeout=70", "--", "java", "-jar", "account-service-1.0-SNAPSHOT.jar"]
#FROM openjdk:8-jdk-alpine
#EXPOSE 8081
#COPY build/libs/account-service-1.0-SNAPSHOT.jar .
#ENV CONFIG_SERVER_URL="http://192.168.1.55:8001"
#ENV SPR_PROFILE="docker"
#ENV CONFIG_SERVICE_URL="http://192.168.1.55:8761/eureka/"
#ENV DATASOURCE_URL="jdbc:postgresql://192.168.1.55:5432/account_service_database"
#ENTRYPOINT ["java", "-jar", "account-service-1.0-SNAPSHOT.jar"]