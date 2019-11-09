Rest-Schnittstelle wurde implementiert:

    /folder/root -> liefert alle Root-Folder (parentFolder ist null), initial wird ein Root-Folder erstellt
    /folder/{id} -> liefert den Content (Artifacts / Folder) des Folders mit der angegebenen ID
    /artifact/{id} -> liefert nur das Artifact zurück
    /artifact/{id}?download -> startet den Download des Artifacts

Außerdem können Folder und Artifacts über POST-Request (siehe Code) vom "ROOT-USER" erstellt und über DELETE-Requests gelöscht werden.