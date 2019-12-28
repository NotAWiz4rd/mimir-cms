Entwicklung
---

    * Server starten: `mvn spring-boot:run -Dserver.port=8080`
    * Tests starten: `mvn test`

Rest-Schnittstelle:

    /login?username=...&password=... -> GET liefert JWT für User

    /space/{id} -> GET liefert Space nach Frontend Muster
    /space/{id}?force -> DELETE löscht Space und gesamten Inhalt
    /space/?name -> POST erstellt einen neuen Space
    
    /folder -> POST erstellt neuen Folder
    /folder/{id} -> GET liefert nur den Folder zurück
    /folder/{id}?name=... -> PUT benennt Folder um
    /folder/{id}?force -> DELETE löscht Folder und gesamten Inhalt
    /folder/{id}/download -> GET startet den Download des Folders
    /folder/download/{id} -> GET liefert JWT zum Downloaden eines Folders
    /folder/share/{id} -> GET liefert JWT zum Teilen eines Folders
    
    /artifact -> POST erstellt neues Artifact
    /artifact/{id} -> GET liefert nur das Artifact zurück
    /artifact/{id} -> DELETE löscht das Artifact
    /artifact/{id}?name=... -> PUT benennt Artifact um
    /artifact/{id}/download -> GET startet den Download des Artifacts
    /artifact/{id}/raw -> GET liefert content eines Artifacts
    /artifact/download/{id} -> GET liefert JWT zum Downloaden eines Artifacts
    /artifact/share/{id} -> GET liefert JWT zum Teilen eines Artifacts
    
    /comments -> POST erstellt einen Kommentar
    /comments?artifactId=123 -> GET liefert alle Kommentare
    /comments/{id} -> DELETE löscht Kommentar
    
    /thumbnail/{id} -> GET liefert Thumbnail oder Icon für Artifact

Docker Deployment
---
### Starten
`docker-compose up` oder `docker-compose up -d`
### Stoppen
`docker-compose down`
### Logs
- `docker-compose logs mimir`
- `docker-compose logs mail`
