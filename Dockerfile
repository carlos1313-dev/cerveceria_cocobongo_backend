# --- Etapa 1: Construcción (Build) ---
# Usa una imagen de Maven con el JDK para compilar el código.
# Reemplaza '3.9.0-eclipse-temurin-17-alpine' con la versión de tu Maven y JDK [citation:7].
FROM maven:3.9.16-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia el archivo pom.xml primero para aprovechar la caché de Docker.
# Si no cambian las dependencias, Docker no volverá a descargarlas.
COPY pom.xml .
# Si tu proyecto es multi-módulo, necesitas copiar los pom.xml de cada módulo [citation:12].
# COPY ./modulo1/pom.xml ./modulo1/
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente y compila.
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: Ejecución (Runtime) ---
# Usa una imagen JRE más ligera para ejecutar la aplicación [citation:2][citation:7].
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia el archivo JAR generado en la etapa de construcción.
# El nombre del JAR dependerá de tu configuración en el pom.xml.
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto que usará tu aplicación (8080 es el predeterminado de Spring Boot).
EXPOSE 8080

# Comando para ejecutar la aplicación.
# Especifica el perfil 'prod' o 'render' para usar configuraciones de producción [citation:2][citation:4].
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=render"]