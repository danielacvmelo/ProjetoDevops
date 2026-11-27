package br.com.devops.product;

import br.com.devops.shared.Text;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> list(String query) {
        return repository.findBySearchKeyContainingOrderByNameAsc(Text.normalize(query));
    }

    public Product find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    @Transactional
    public Product create(ProductRequest request) {
        return save(new Product(), request);
    }

    @Transactional
    public Product update(UUID id, ProductRequest request) {
        return save(find(id), request);
    }

    private Product save(Product product, ProductRequest request) {
        String codeKey = Text.normalize(request.code());
        boolean duplicate = !codeKey.isEmpty() && (product.id == null
                ? repository.existsByCodeKey(codeKey) : repository.existsByCodeKeyAndIdNot(codeKey, product.id));
        if (duplicate) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este código já está cadastrado em outro produto.");
        product.code = Text.clean(request.code());
        product.codeKey = codeKey.isEmpty() ? null : codeKey;
        product.name = Text.clean(request.name());
        product.category = Text.clean(request.category());
        product.unit = Text.clean(request.unit());
        product.purchasePrice = request.purchasePrice();
        product.salePrice = request.salePrice();
        product.stock = request.stock();
        product.minimumStock = request.minimumStock();
        product.status = request.status();
        product.searchKey = Text.normalize(product.name + " " + product.code + " " + product.category);
        return repository.saveAndFlush(product);
    }
}
