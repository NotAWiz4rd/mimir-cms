package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Optional;

import static org.imgscalr.Scalr.Method.AUTOMATIC;
import static org.springframework.http.MediaType.*;

/**
 * This class abstract the complex logic for generating thumbnails.
 */
@Service
public class ThumbnailGenerator {
    private static final int THUMBNAIL_SIZE = 2 * 200;
    private static final MediaType VIDEO = MediaType.valueOf("video/*");
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbnailGenerator.class);

    /**
     * Returns if the {@link ThumbnailGenerator} should be able to generate a thumbnail.
     * @param mimeType
     * @return
     */
    static boolean supportMimeType(MimeType mimeType) {
        return IMAGE_JPEG.isCompatibleWith(mimeType) ||
                IMAGE_GIF.isCompatibleWith(mimeType) ||
                IMAGE_PNG.isCompatibleWith(mimeType) ||
                APPLICATION_PDF.isCompatibleWith(mimeType) ||
                VIDEO.isCompatibleWith(mimeType);
    }

    /**
     * Try to generate a thumbnail for the given source file and return it. If the mime type is not supported or an
     * exception occurs, the method will return an empty optional.
     * @param source
     * @param mediaType
     * @return
     */
    public Optional<InputStream> generateThumbnail(InputStream source, MediaType mediaType) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(mediaType, "mediaType must not be null");
        if (IMAGE_JPEG.isCompatibleWith(mediaType) || IMAGE_GIF.isCompatibleWith(mediaType)) {
            return generateFromImage(source);
        }

        if (IMAGE_PNG.isCompatibleWith(mediaType)) {
            return generateFromPng(source);
        }

        if (APPLICATION_PDF.isCompatibleWith(mediaType)) {
            return generateFromPdf(source);
        }

        if (VIDEO.isCompatibleWith(mediaType)) {
            return generateFromVideo(source);
        }

        return Optional.empty();
    }

    /**
     * Try to generate a thumbnail for JPGs/GIFs.
     * @param source
     * @return
     */
    private Optional<InputStream> generateFromImage(InputStream source) {
        try {
            BufferedImage original = ImageIO.read(source);
            return resizeImage(original);
        } catch (IOException e) {
            LOGGER.error("Can't create thumbnail for jpg/gif", e);
            return Optional.empty();
        }
    }

    /**
     * Try to generate a thumbnail for PNGs.
     * Taken from: https://memorynotfound.com/convert-png-to-jpg-image-file-using-java/
     * @param source
     * @return
     */
    private Optional<InputStream> generateFromPng(InputStream source) {
        try {
            BufferedImage original = ImageIO.read(source);
            BufferedImage converted = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            converted.createGraphics().drawImage(original, 0, 0, Color.WHITE, null);
            return resizeImage(converted);
        } catch (IOException e) {
            LOGGER.error("Can't create thumbnail for png", e);
            return Optional.empty();
        }
    }

    /**
     * Try to render the pdf document, use the first page as an image and generate a thumbnail for this image.
     * Taken from: https://www.tutorialspoint.com/pdfbox/pdfbox_extracting_image.htm
     * @param source
     * @return
     */
    private Optional<InputStream> generateFromPdf(InputStream source) {
        try {
            PDDocument document = PDDocument.load(source);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage firstPageImage = renderer.renderImage(0);
            document.close();
            return resizeImage(firstPageImage);
        } catch (IOException e) {
            LOGGER.error("Can't resize pdf thumbnail", e);
            return Optional.empty();
        }
    }

    /**
     * Try to read the video with jcodec, extract the 24th frame from the video and generate a thumbnail
     * for this image.
     * This works only for the video encodings: AVC, H.264 in MP4, ISO BMF, Quicktime container
     * Accessing a non existing frame will not cause an error (the last frame of the video will be used instead)
     * @param inputStream
     * @return
     */
    private Optional<InputStream> generateFromVideo(InputStream inputStream) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("video_thumbnail", ".tmp");
            tempFile.deleteOnExit();
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            Picture thumbnail = FrameGrab.getFrameFromFile(tempFile, 24);
            BufferedImage bufferedThumbnail = AWTUtil.toBufferedImage(thumbnail);
            tempFile.delete();
            return resizeImage(bufferedThumbnail);
        } catch (IOException | JCodecException e) {
            if (tempFile != null) {
                tempFile.delete();
            }
            LOGGER.error("Can't create thumbnail from video", e);
            return Optional.empty();
        }
    }

    /**
     * Try to resize the given BufferedImage to a maximum height or width without changing the ratio.
     * @param original
     * @return
     */
    private Optional<InputStream> resizeImage(BufferedImage original) {
        try {
            Scalr.Mode mode = original.getWidth() < original.getHeight() ? Scalr.Mode.FIT_TO_WIDTH : Scalr.Mode.FIT_TO_HEIGHT;
            BufferedImage thumbnail = Scalr.resize(original, AUTOMATIC, mode, THUMBNAIL_SIZE, Scalr.OP_ANTIALIAS);
            BufferedImage quadraticThumbnail = Scalr.crop(thumbnail, THUMBNAIL_SIZE, THUMBNAIL_SIZE);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(quadraticThumbnail, "jpg", output);
            byte[] bytes = output.toByteArray();

            if (bytes.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOGGER.error("Can't resize original image", e);
            return Optional.empty();
        }
    }
}