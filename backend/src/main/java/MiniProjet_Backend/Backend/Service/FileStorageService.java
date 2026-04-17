package MiniProjet_Backend.Backend.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "ppt", "pptx", "doc", "docx", "xls", "xlsx", "txt", "zip", "png", "jpg", "jpeg"
    );

    private final Path uploadDirectory;

    public FileStorageService(@Value("${app.file-storage.upload-dir:uploads/course-materials}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est obligatoire");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille maximale autorisée est 10MB");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Type de fichier non autorisé: " + extension);
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = uploadDirectory.resolve(storedName).normalize();
        if (!target.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Chemin de fichier invalide");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(storedName, originalName, file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de stocker le fichier", ex);
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path filePath = uploadDirectory.resolve(storedName).normalize();
            if (!filePath.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("Chemin de fichier invalide");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Fichier introuvable");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Fichier introuvable", ex);
        }
    }

    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }

        Path filePath = uploadDirectory.resolve(storedName).normalize();
        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Chemin de fichier invalide");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de supprimer le fichier", ex);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }
        return Paths.get(fileName).getFileName().toString().replaceAll("[\\r\\n]", "").trim();
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new IllegalArgumentException("Le fichier doit avoir une extension");
        }
        return fileName.substring(index + 1);
    }

    public record StoredFile(String storedName, String originalName, String contentType, long size) {
    }
}
