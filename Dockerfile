FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache ca-certificates
RUN ln -sf /etc/ssl/certs/java/cacerts $JAVA_HOME/lib/security/cacerts
WORKDIR /app
COPY M3Almacenamiento-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]