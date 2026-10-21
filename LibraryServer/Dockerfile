# Build context is the repository root (the module needs the parent pom.xml and Common):
#   docker build -f LibraryServer/Dockerfile .

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
# The parent pom lists every module, so all module poms are needed; only these two modules are built
COPY pom.xml .
COPY Common/pom.xml Common/
COPY LibraryServer/pom.xml LibraryServer/
COPY DiscoveryServer/pom.xml DiscoveryServer/
COPY AccountingServer/pom.xml AccountingServer/
COPY SupplierServer/pom.xml SupplierServer/
COPY Library/pom.xml Library/
COPY Supplier/pom.xml Supplier/
COPY User/pom.xml User/
RUN mvn -B -q -pl LibraryServer -am dependency:go-offline
COPY Common/src Common/src
COPY LibraryServer/src LibraryServer/src
RUN mvn -B -q -pl LibraryServer -am -DskipTests package

FROM tomcat:9.0-jre17-temurin
# Relative paths in app.properties (users, ZIP files, logs) resolve against this directory
WORKDIR /usr/local/tomcat
COPY --from=build /src/LibraryServer/target/LibraryServer.war webapps/LibraryServer.war
EXPOSE 8080
