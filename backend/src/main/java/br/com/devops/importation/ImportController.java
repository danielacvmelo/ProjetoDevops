package br.com.devops.importation;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportView> upload(@RequestParam UUID supplierId, @RequestParam MultipartFile file) throws IOException {
        ImportView view = service.upload(supplierId, file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.created(URI.create("/api/imports/" + view.batch().id)).body(view);
    }

    @GetMapping
    public List<ImportBatch> list(@RequestParam(required = false) UUID supplierId) {
        return service.list(supplierId);
    }

    @GetMapping("/{id}")
    public ImportView view(@PathVariable UUID id) {
        return service.view(id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> file(@PathVariable UUID id) {
        ImportBatch batch = service.find(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("importacao-" + id + ".pdf").build().toString())
                .body(batch.fileData);
    }

    @PutMapping("/{id}/items/{itemId}")
    public ImportView update(@PathVariable UUID id, @PathVariable UUID itemId, @Valid @RequestBody ItemRequest request) {
        return service.update(id, itemId, request);
    }

    @PostMapping("/{id}/confirm")
    public ImportView confirm(@PathVariable UUID id) {
        return service.confirm(id);
    }

    @GetMapping("/history")
    public List<PriceHistory> history(@RequestParam(required = false) UUID productId,
                                     @RequestParam(required = false) UUID supplierId,
                                     @RequestParam(required = false) UUID importId) {
        return service.history(productId, supplierId, importId);
    }
}
