package br.com.devops.importation;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRepository extends JpaRepository<ImportBatch, UUID> {
    Optional<ImportBatch> findFirstBySupplierIdAndHashAndDuplicateOfIsNullOrderByCreatedAtAsc(UUID supplierId, String hash);
    List<ImportBatch> findAllByOrderByCreatedAtDesc();
    List<ImportBatch> findBySupplierIdOrderByCreatedAtDesc(UUID supplierId);
}
