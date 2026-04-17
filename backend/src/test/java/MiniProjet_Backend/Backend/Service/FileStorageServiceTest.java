package MiniProjet_Backend.Backend.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {
    @TempDir
    private Path tempDir;

    @Test
    void storeAcceptsAllowedCourseMaterial() {
        FileStorageService service = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cours.pdf",
                "application/pdf",
                "content".getBytes()
        );

        FileStorageService.StoredFile stored = service.store(file);
        Resource resource = service.loadAsResource(stored.storedName());

        assertEquals("cours.pdf", stored.originalName());
        assertEquals("application/pdf", stored.contentType());
        assertNotNull(resource);
    }

    @Test
    void storeRejectsForbiddenExtension() {
        FileStorageService service = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.store(file)
        );

        assertEquals("Type de fichier non autorisé: exe", exception.getMessage());
    }
}
