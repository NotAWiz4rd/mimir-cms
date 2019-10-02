package de.seprojekt.se2019.g4.mimir.content;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * This controller offers an HTTP interface for displaying folders and artifacts
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class ContentWebController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ContentWebController.class);
    public static final String ADMIN_ROLE = "CMS-ADMINS";

    private ArtifactService artifactService;
    private FolderService folderService;
    private ContentService contentService;
    private UrlPathHelper urlPathHelper;
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param artifactService
     * @param folderService
     * @param contentService
     * @param applicationBaseUrl
     */
    public ContentWebController(ArtifactService artifactService, FolderService folderService, ContentService contentService, @Value("${application.base.url}") String applicationBaseUrl) {
        this.artifactService = artifactService;
        this.folderService = folderService;
        this.contentService = contentService;
        this.applicationBaseUrl = applicationBaseUrl;
        this.urlPathHelper = new UrlPathHelper();
    }

    /**
     * Redirect user from typical start urls to the root folder view.
     *
     * @return
     */
    @GetMapping(value = {"", "/", "${application.base.url}"})
    public String redirectToRoot() {
        return "redirect:" + applicationBaseUrl + "/";
    }

    /**
     * Display the preview for an artifact.
     *
     * @param model
     * @param principal
     * @param request
     * @return
     * @throws URISyntaxException
     */
    @GetMapping(value = {"${application.base.url}/**"}, params = "preview")
    public Object preview(Model model, Principal principal, HttpServletRequest request) throws URISyntaxException {
        String contentPath = getContentPath(request);
        if (contentPath.endsWith("/")) {
            throw new DisplayableException("Es gibt keine Preview von Ordnern.");
        }
        return showPreview(model, contentPath, request, principal);
    }

    /**
     * Generate a download of an artifact when a user visit this url.
     *
     * @param request
     * @return
     */
    @GetMapping(value = {"${application.base.url}/**"}, params = "download")
    public Object download(HttpServletRequest request) {
        String contentPath = getContentPath(request);
        Artifact artifact = artifactService.findByTotalUrl(contentPath).orElseThrow(EntityNotFoundException::new);

        HttpHeaders headers = new HttpHeaders();
        String fileName = artifactService.calculateFileName(artifact);
        headers.set("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

        InputStream inputStream = artifactService.findArtifact(artifact);
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(artifact.getContentType())
                .contentLength(artifact.getContentLength())
                // InputStreamResource will close the InputStream
                .body(resource);
    }

    /**
     * Display the content of an folder or the raw view of an artifact.
     *
     * @param model
     * @param principal
     * @param request
     * @return
     * @throws URISyntaxException
     */
    @GetMapping(value = {"${application.base.url}/", "${application.base.url}/**"})
    public Object show(Model model, Principal principal, HttpServletRequest request) throws URISyntaxException {
        String contentPath = getContentPath(request);
        if (contentPath.endsWith("/")) {
            return showFolder(model, contentPath, principal);
        }
        return showFile(contentPath);
    }

    /**
     * Prepare the model to contain all necessary information to execute the content template.
     *
     * @param model
     * @param contentPath
     * @param principal
     * @return
     * @throws URISyntaxException
     */
    private Object showFolder(Model model, String contentPath, Principal principal) throws URISyntaxException {
        List<Content> contentForFolder = contentService.findContentForFolder(contentPath).orElseThrow(EntityNotFoundException::new);
        boolean isRootFolder = "/".equals(contentPath);
        Optional<Folder> folder = folderService.findByTotalUrl(contentPath);

        model.addAttribute("currentUrl", contentPath);
        model.addAttribute("parentUrl", folder.map(Folder::getParentUrl).orElse("/"));
        model.addAttribute("content", contentForFolder);
        model.addAttribute("isRootFolder", isRootFolder);
        model.addAttribute("folder", folder.orElse(null));
        model.addAttribute("currentUser", principal.getName());
        model.addAttribute("applicationBaseUrl", applicationBaseUrl);
        return "content";
    }

    /**
     * Return only the artifact with the correct contentType etc.
     *
     * @param contentUrl
     * @return
     */
    private Object showFile(String contentUrl) {
        Artifact artifact = artifactService.findByTotalUrl(contentUrl).orElseThrow(EntityNotFoundException::new);
        InputStream inputStream = artifactService.findArtifact(artifact);
        // InputStreamResource will close the InputStream
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .contentType(artifact.getContentType())
                .contentLength(artifact.getContentLength())
                .body(resource);
    }

    /**
     * Prepare the model to contain all necessary information to execute the preview template.
     *
     * @param model
     * @param url
     * @param request
     * @param principal
     * @return
     * @throws URISyntaxException
     */
    private Object showPreview(Model model, String url, HttpServletRequest request, Principal principal) throws URISyntaxException {
        Artifact artifact = artifactService.findByTotalUrl(url).orElseThrow(EntityNotFoundException::new);
        model.addAttribute("currentUrl", UriUtils.encodePath(url, StandardCharsets.UTF_8));
        model.addAttribute("currentUser", principal.getName());
        model.addAttribute("parentUrl", artifact.getParentUrl());
        model.addAttribute("artifact", artifact);
        model.addAttribute("isLockedByCurrentUser", artifactService.isLockedByCurrentUser(artifact));
        model.addAttribute("isUserAdmin", request.isUserInRole(ADMIN_ROLE));
        model.addAttribute("fileSize", artifactService.byteCountToDisplaySize(artifact));
        model.addAttribute("applicationBaseUrl", applicationBaseUrl);
        return "preview";
    }

    /**
     * Return the folder url (e.g. http://localhost/mimir/aaa/ => /aaa/)
     *
     * @param httpServletRequest
     * @return
     */
    private String getContentPath(HttpServletRequest httpServletRequest) {
        String pathInApp = urlPathHelper.getPathWithinApplication(httpServletRequest);
        String contentPath = pathInApp.substring(applicationBaseUrl.length());
        return contentPath;
    }

    /**
     * This method will be called when a {@link DisplayableException} or {@link EntityNotFoundException} is thrown
     * in the controller methods.
     * The method will add the exception message to the error/5xx template and display it.
     *
     * @param model
     * @param exception
     * @return
     */
    @ExceptionHandler({DisplayableException.class, EntityNotFoundException.class})
    public String handle(Model model, Exception exception) {
        String message = null;
        if (exception instanceof DisplayableException) {
            message = exception.getMessage();
        } else if (exception instanceof EntityNotFoundException) {
            message = "Ordner oder Artefakt existiert nicht.";
        }

        model.addAttribute("errorMessage", message);
        return "error/5xx";
    }
}
