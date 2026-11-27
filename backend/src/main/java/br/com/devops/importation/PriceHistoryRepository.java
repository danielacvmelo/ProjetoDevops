package br.com.devops.importation;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID>, JpaSpecificationExecutor<PriceHistory> {
    Optional<PriceHistory> findFirstBySupplierIdAndItemKeyOrderByCreatedAtDescIdDesc(UUID supplierId, String itemKey);
}
