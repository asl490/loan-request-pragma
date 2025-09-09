FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

# Crear usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el jar desde la ruta de construcción de Gradle, renombrarlo a app.jar y establecer permisos.
# El comodín (*) lo hace más robusto si el nombre exacto del JAR cambia.
COPY --chown=appuser:appgroup ../applications/app-service/build/libs/*.jar app.jar

# Config JVM (opciones válidas para Temurin/HotSpot)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

# Usar el nombre estandarizado app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]