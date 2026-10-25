# ----
# build
# ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copies Maven wrapper and pom.xml first
COPY .mvn/ .mvn/
COPY --chmod=0755 mvnw ./
COPY pom.xml ./

# Downloads dependencies
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copies source code and build the executable JAR
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -B

# ----
# production
# ----
FROM eclipse-temurin:25-jre-alpine AS production
WORKDIR /app

# Creates a restricted non-root user 
RUN addgroup -S spring && adduser -S spring -G spring

# Copies onl the compiled JAR from the build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Switch to non-root user
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]