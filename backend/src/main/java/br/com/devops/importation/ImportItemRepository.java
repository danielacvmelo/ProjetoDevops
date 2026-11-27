package br.com.devops.importation;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportItemRepository extends JpaRepository<ImportItem, UUID> {
    List<ImportItem> findByImportIdOrderByLineNumberAsc(UUID importId);
}
