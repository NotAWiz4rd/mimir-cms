package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.DisplayableException;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * This controller offers an HTTP interface for manipulating artifacts (e.g. deleting, creating etc.)
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class ArtifactWebController {
    private static final String PREVIEW = "?preview";
    private static final String DOWNLOAD = "?download";

    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactWebController.class);

    private ArtifactService artifactService;
    private FolderService folderService;
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     * @param artifactService
     * @param folderService
     * @param applicationBaseUrl
     */
    public ArtifactWebController(ArtifactService artifactService, FolderService folderService, @Value("${application.base.url}") String applicationBaseUrl) {
        this.artifactService = artifactService;
        this.folderService = folderService;
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * The user can do an initial checkin of an artifact by calling this interface.
     *
     * @param principal
     * @param currentUrl
     * @param name
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/artifact")
    public String initialCheckin(Principal principal, @RequestParam("currentUrl") String currentUrl, @RequestParam("name") String name, @RequestParam("file") MultipartFile file) throws IOException {
        Assert.isTrue(currentUrl.endsWith("/"), "currentUrl must end with a slash");
        Assert.isTrue(folderService.exists(currentUrl), "currentUrl must be valid");

        Artifact artifact = artifactService.initialCheckin(name, file, currentUrl, principal);
        String parentUrl = UriUtils.encodePath(artifact.getParentUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + parentUrl;
    }

    /**
     * The user can rename an artifact by calling this interface.
     * This method will perform some checks (is the artifact not locked? is the name not dangerous?).
     * @param id
     * @param newName
     * @return
     */
    @PutMapping(value = "/artifact/{id}/name")
    public String rename(@PathVariable long id, @RequestParam("newName") String newName) {
        Artifact artifact = artifactService.findById(id).orElseThrow(EntityNotFoundException::new);
        Artifact renamedArtifact = artifactService.rename(id, newName);
        String totalUrl = UriUtils.encodePath(renamedArtifact.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl + PREVIEW;
    }

    /**
     * The user can delete an artifact by calling this interface.
     * @param id
     * @return
     */
    @DeleteMapping(value = "/artifact/{id}")
    public String delete(@PathVariable long id) {
        Artifact artifact = artifactService.findById(id).orElseThrow(EntityNotFoundException::new);
        artifactService.delete(artifact);
        String parentUrl = UriUtils.encodePath(artifact.getParentUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + parentUrl;
    }

    /**
     * The user can checkin an existing artifact by calling this interface.
     * @param principal
     * @param id
     * @param newFile
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/artifact/{id}/file")
    public String checkin(Principal principal, @PathVariable long id, @RequestParam("newFile") MultipartFile newFile) throws IOException {
        Artifact artifact = artifactService.checkin(id, newFile, principal);
        String totalUrl = UriUtils.encodePath(artifact.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl + PREVIEW;
    }

    /**
     * The user can checkout an artifact by calling this interface.
     * @param principal
     * @param id
     * @return
     */
    @PutMapping(value = "/artifact/{id}/checkout")
    public String checkout(Principal principal, @PathVariable long id) {
        Artifact artifact = artifactService.findById(id).orElseThrow(EntityNotFoundException::new);
        Artifact lockedArtifact = artifactService.checkout(artifact, principal);
        String totalUrl = UriUtils.encodePath(lockedArtifact.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl + DOWNLOAD;
    }

    /**
     * The user can force an unlock of an artifact by calling this interface.
     * @param id
     * @return
     */
    @PutMapping(value = "/artifact/{id}/unlock")
    @PreAuthorize("hasRole(T(de.seprojekt.se2019.g4.mimir.content.ContentWebController).ADMIN_ROLE)")
    public String forceUnlock(@PathVariable long id) {
        Artifact artifact = artifactService.findById(id).orElseThrow(EntityNotFoundException::new);
        Artifact unlockArtifact = artifactService.forceUnlock(artifact);
        String totalUrl = UriUtils.encodePath(unlockArtifact.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl + PREVIEW;
    }

    /**
     * This method will be called when a {@link DisplayableException} or {@link EntityNotFoundException} is thrown
     * in the controller methods.
     * The method will add the exception message to the error/5xx template and display it.
     * @param model
     * @param exception
     * @return
     */
    @ExceptionHandler({DisplayableException.class, EntityNotFoundException.class})
    public String handle(Model model, Exception exception) {
        String message = null;
        if (exception instanceof DisplayableException) {
            message = exception.getMessage();
        } else if (exception instanceof EntityNotFoundException){
            message = "Artefakt existiert nicht.";
        }

        model.addAttribute("errorMessage", message);
        return "error/5xx";
    }
}
