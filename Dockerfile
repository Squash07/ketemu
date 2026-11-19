# Stage 1: build the WAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Stage 2: run it on Tomcat 10.1
FROM tomcat:10.1-jre17
ENV TZ=Asia/Jakarta \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -Xss512k -Duser.timezone=Asia/Jakarta -Djava.security.egd=file:/dev/./urandom"
RUN rm -rf /usr/local/tomcat/webapps/* \
 && sed -i 's/<Server port="8005"/<Server port="-1"/' /usr/local/tomcat/conf/server.xml
COPY --from=build /src/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
