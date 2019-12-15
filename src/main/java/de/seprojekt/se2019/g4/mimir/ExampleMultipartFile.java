package de.seprojekt.se2019.g4.mimir;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is a helper class to upload file in the {@link ExampleMultipartFile} (because the {@link
 * ArtifactService} only accepts MultipartFile.
 */
public class ExampleMultipartFile implements MultipartFile {

  private String name;
  private MediaType contentType;
  private ClassPathResource classPathResource;

  public ExampleMultipartFile(String name, MediaType contentType,
      ClassPathResource classPathResource) {
    this.name = name;
    this.contentType = contentType;
    this.classPathResource = classPathResource;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return classPathResource.getFilename();
  }

  @Override
  public String getContentType() {
    return contentType.toString();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public long getSize() {
    try {
      return classPathResource.getInputStream().readAllBytes().length;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public byte[] getBytes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return classPathResource.getInputStream();
  }

  @Override
  public void transferTo(File dest) throws IllegalStateException {
    throw new UnsupportedOperationException();
  }
}
