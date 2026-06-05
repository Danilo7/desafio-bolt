# ============================================================================
# Dockerfile multi-stage: compila com Maven e roda só com o JRE (imagem menor).
# ============================================================================

# ---- Estágio 1: BUILD (compila o projeto e gera o .jar) --------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia primeiro o pom.xml e baixa as dependências (aproveita cache do Docker:
# só refaz o download se o pom mudar, não a cada alteração de código).
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copia o código-fonte e empacota (pulando testes para um build mais rápido).
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Estágio 2: RUNTIME (imagem final, só com o necessário para rodar) ------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia apenas o .jar gerado no estágio de build.
COPY --from=build /app/target/*.jar app.jar

# A aplicação roda na porta 8082 (exigência do desafio).
EXPOSE 8082

# Comando que sobe a aplicação quando o contêiner inicia.
ENTRYPOINT ["java", "-jar", "app.jar"]
