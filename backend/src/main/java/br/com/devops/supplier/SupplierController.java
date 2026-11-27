package br.com.devops.supplier;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    public List<Supplier> list(@RequestParam(defaultValue = "") String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public Supplier find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping
    public ResponseEntity<Supplier> create(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = service.create(request);
        return ResponseEntity.created(URI.create("/api/suppliers/" + supplier.id)).body(supplier);
    }
}
