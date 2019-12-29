FROM openjdk:11
#  Definiere Version 1.0.0 als Default Version
ARG MIMIR_VERSION=1.0.0
#  Da Build Args nicht im ENTRYPOINT benutzt werden können, erzeugen wir aus der Build Arg eine environment variable
ENV ENV_MIMIR_VERSION=$MIMIR_VERSION
#  Verwende einen LDAP Server als Backend zur Benutzer Authentifizierung
ENV USER_AUTHENTICATION_METHOD=ldap_server
#  Definiere den Speicherpfad für die Artefakte von Mimir
#  gemäß https://github.com/paulcwarren/spring-content/issues/11
ENV SPRING_CONTENT_FS_FILESYSTEM_ROOT=/srv/mimir
#  Datenbank Initialisierung
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update

#  Erzeuge einen dedizierten Benutzer für Mimir
RUN useradd --home /srv/mimir --comment "Mimir user" --create-home --system --shell /sbin/nologin mimir
#  Installiere Maven
RUN apt-get update && apt-get install maven -y
#  Kopiere die Maven Konfiguration in den Container und wechsle in diesen Ordner
COPY ./pom.xml /usr/src/mimir/pom.xml
WORKDIR /usr/src/mimir
#  Installiere allen Abhängigkeiten von Mimir
#  Dieser Schritt wird bei einem erneuten Build des Containers nur ausgeführt,
#  wenn die pom.xml sich verändert hat.
#  Bei einer Veränderung des Quelltexts ansich, wird der Docker Cache genutzt,
#  um den Build zu beschleunigen
RUN mvn dependency:go-offline
#  Kopiere den Quelltext in den Container
COPY ./src /usr/src/mimir/src
#  Erzeuge mittels maven eine JAR-Datei
RUN mvn package -DskipTests
#  Verschiebe den von Maven erzeugten Ordner aus dem Quelltext-Ordner
RUN mv /usr/src/mimir/target /opt/mimir && \
    chown -R mimir:mimir /opt/mimir
#  Wechsle in den verschobenen Ordner
WORKDIR /opt/mimir

#  Gib Port 80 aus dem Container frei
EXPOSE 80
#  Wechsle zu Mimir Benutzer
USER mimir
#  Starte den Java Server
ENTRYPOINT java -jar "mimir-$ENV_MIMIR_VERSION.jar"
