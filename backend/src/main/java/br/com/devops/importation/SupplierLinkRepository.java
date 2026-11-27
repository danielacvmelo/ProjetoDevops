package br.com.devops.importation;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierLinkRepository extends JpaRepository<SupplierLink, UUID> {
    Optional<SupplierLink> findBySupplierIdAndItemKey(UUID supplierId, String itemKey);
}
