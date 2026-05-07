FROM eclipse-temurin:17

WORKDIR /app


COPY target/Mediation-1.0-SNAPSHOT.jar app.jar

RUN mkdir -p /app/archive /app/temp
# for logging env 
ENV JAVA_OPTS=""


CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]