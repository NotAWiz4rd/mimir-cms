Entwicklung
---

    * Server starten: `mvn spring-boot:run -Dserver.port=8080`
    * Tests starten: `mvn test`

Rest-Schnittstelle:

    /spaces -> liefert alle Spaces
    /space/{id} -> liefert Space nach Frontend Muster
    /space/{id}?force -> DELETE löscht Space und gesamten Inhalt
    /folder/{id} -> liefert nur den Folder zurück
    /artifact/{id} -> liefert nur das Artifact zurück
    /artifact/{id}?download -> startet den Download des Artifacts
    /folder/{id}?download -> startet den Download des Folders
    /folder/{id}?force -> DELETE löscht Folder und gesamten Inhalt

Außerdem können Spaces, Folder und Artifacts über POST-Request (siehe Code) vom "ROOT-USER" erstellt und über DELETE-Requests gelöscht werden.<br>
Über PUT an Folder und Artifact können diese umbenannt werden.

Docker Deployment
---
### Starten
`docker-compose up` oder `docker-compose up -d`
### Stoppen
`docker-compose down`
### Logs
- `docker-compose logs mimir`
- `docker-compose logs mail`
