package br.com.devops.product;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findBySearchKeyContainingOrderByNameAsc(String query);
    boolean existsByCodeKeyAndIdNot(String codeKey, UUID id);
    boolean existsByCodeKey(String codeKey);
}
