FROM eclipse-temurin:17

WORKDIR /app

# ─────────────────────────────
# COPY APPLICATION JAR
# ─────────────────────────────
COPY target/Mediation-1.0-SNAPSHOT.jar app.jar

# ─────────────────────────────
# REQUIRED DIRECTORIES
# ─────────────────────────────
RUN mkdir -p /app/archive \
             /app/temp \
             /app/cdr

# ─────────────────────────────
# JVM CONFIG (OPTIONAL BUT CLEAN)
# ─────────────────────────────
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# ─────────────────────────────
# RUN APPLICATION
# ─────────────────────────────
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]