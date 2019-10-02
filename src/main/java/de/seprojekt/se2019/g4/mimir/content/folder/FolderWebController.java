package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.DisplayableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;

/**
 * This controller offers an HTTP interface for manipulating folders (e.g. deleting, creating etc.)
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class FolderWebController {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderWebController.class);
    private FolderService folderService;
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     * @param folderService
     * @param applicationBaseUrl
     */
    public FolderWebController(FolderService folderService, @Value("${application.base.url}") String applicationBaseUrl) {
        this.folderService = folderService;
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * The user can create a new folder by calling this interface.
     * @param model
     * @param name
     * @param currentUrl
     * @return
     */
    @PostMapping(value = "/folder")
    public String create(Model model, @RequestParam("name") String name, @RequestParam("currentUrl") String currentUrl) {
        Assert.isTrue(folderService.exists(currentUrl), "currentUrl must be an existing folder");
        Folder createdFolder = folderService.create(currentUrl, name);
        String totalUrl = UriUtils.encodePath(createdFolder.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl;
    }

    /**
     * The user can rename a folder by calling this interface.
     * @param model
     * @param id
     * @param newName
     * @return
     */
    @PutMapping(value = "/folder/{id}")
    public String rename(Model model, @PathVariable("id") long id, @RequestParam("newName") String newName) {
        Folder folder = folderService.findById(id).orElseThrow(EntityExistsException::new);
        Folder renamedFolder = folderService.rename(folder, newName);
        String totalUrl = UriUtils.encodePath(renamedFolder.getTotalUrl(), StandardCharsets.UTF_8);
        return "redirect:" + applicationBaseUrl + totalUrl;
    }

    /**
     * The user can delete a folder by calling this interface.
     * @param model
     * @param id
     * @return
     */
    @DeleteMapping(value = "/folder/{id}")
    public String delete(Model model, @PathVariable("id") long id) {
        Folder folder = folderService.findById(id).orElseThrow(EntityExistsException::new);
        String parentUrl = UriUtils.encodePath(folder.getParentUrl(), StandardCharsets.UTF_8);
        folderService.delete(folder);
        return "redirect:" + applicationBaseUrl + parentUrl;
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
            message = "Ordner existiert nicht.";
        }

        model.addAttribute("errorMessage", message);
        return "error/5xx";
    }
}
