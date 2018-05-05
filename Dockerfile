FROM openjdk:alpine
ENV LIQUIBASE_URL=https://github.com/liquibase/liquibase/releases/download/liquibase-parent-3.5.3/liquibase-3.5.3-bin.tar.gz \
    SQLITE_DRIVER_URL=http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.21.0.1/sqlite-jdbc-3.21.0.1.jar \
    START_SCRIPT="java -version && java -jar /opt/liquibase/liquibase.jar --classpath=/opt/liquibase/lib/sqlitedriver.jar --driver=org.sqlite.JDBC --url=jdbc:sqlite:file:/var/lib/bote/bote.sqlite --changeLogFile=/opt/bote/database/changelog-master.xml update && java -jar bote.jar /etc/bote/application.properties"

# Install liquibase with sqlite driver
RUN mkdir -p /opt/liquibase && \
    cd /opt/liquibase && \
    apk update && apk add ca-certificates && update-ca-certificates && apk add openssl && \
    wget -c -O liquibase.tar.gz $LIQUIBASE_URL && \
    tar -xf liquibase.tar.gz && \
    cd lib && \
    wget -c -O sqlitedriver.jar $SQLITE_DRIVER_URL && \
    rm ../liquibase.tar.gz

# Copy Database Changelogs
COPY database /opt/bote/database

# Copy applikation JAR
COPY target/bote-*-with-dependencies.jar /opt/bote/bote.jar

WORKDIR /opt/bote

# Volumes to store database and config
VOLUME ["/var/lib/bote" "/etc/bote"]

# Create start script
RUN echo -e $START_SCRIPT > /opt/bote/start-bote.sh

EXPOSE 8080

CMD ["sh", "start-bote.sh"]
