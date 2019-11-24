Entwicklung
---

    * Server starten: `mvn spring-boot:run -Dserver.port=8080`
    * Tests starten: `mvn test`

Rest-Schnittstelle:

    /spaces -> liefert alle Spaces
    /space/{id} -> liefert Space nach Frontend Muster
    /folder/{id} -> liefert nur den Folder zurück
    /artifact/{id} -> liefert nur das Artifact zurück
    /artifact/{id}?download -> startet den Download des Artifacts
    /folder/{id}?download -> startet den Download des Folders

Außerdem können Spaces, Folder und Artifacts über POST-Request (siehe Code) vom "ROOT-USER" erstellt und über DELETE-Requests gelöscht werden.

Docker Deployment
---
### Starten
`docker-compose up` oder `docker-compose up -d`
### Stoppen
`docker-compose down`
### Logs
- `docker-compose logs mimir`
- `docker-compose logs mail`
