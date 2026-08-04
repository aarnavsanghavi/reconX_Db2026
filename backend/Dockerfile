FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# Build and install the audit starter
COPY recon-audit-starter /workspace/recon-audit-starter
WORKDIR /workspace/recon-audit-starter
RUN chmod +x mvnw
RUN ./mvnw -q -B clean install

# Build the backend
WORKDIR /workspace/backend
COPY backend /workspace/backend

RUN chmod +x mvnw
RUN ./mvnw -q -B -DskipTests package && \
    mv target/reconx-*.jar app.jar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S reconx && adduser -S reconx -G reconx
USER reconx

WORKDIR /app

COPY --from=build /workspace/backend/app.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]