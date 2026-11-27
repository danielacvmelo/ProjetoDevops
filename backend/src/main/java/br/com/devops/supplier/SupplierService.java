package br.com.devops.supplier;

import br.com.devops.shared.Text;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class SupplierService {
    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    public List<Supplier> list(String query) {
        return repository.findByNameKeyContainingOrderByNameAsc(Text.normalize(query));
    }

    public Supplier find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado."));
    }

    @Transactional
    public Supplier create(SupplierRequest request) {
        String nameKey = Text.normalize(request.name());
        if (repository.existsByNameKey(nameKey)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este fornecedor já está cadastrado.");
        Supplier supplier = new Supplier();
        supplier.name = Text.clean(request.name());
        supplier.nameKey = nameKey;
        supplier.email = Text.clean(request.email());
        supplier.phone = Text.clean(request.phone());
        return repository.saveAndFlush(supplier);
    }
}
