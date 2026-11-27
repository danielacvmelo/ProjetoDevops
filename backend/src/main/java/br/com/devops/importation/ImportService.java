package br.com.devops.importation;

import br.com.devops.product.ProductRepository;
import br.com.devops.shared.Text;
import br.com.devops.supplier.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ImportService {
    private static final Logger log = LoggerFactory.getLogger(ImportService.class);
    private final ImportRepository batches;
    private final ImportItemRepository items;
    private final SupplierLinkRepository links;
    private final PriceHistoryRepository history;
    private final ProductRepository products;
    private final PdfExtractor extractor;
    private final EntityManager entityManager;

    public ImportService(ImportRepository batches, ImportItemRepository items, SupplierLinkRepository links,
                         PriceHistoryRepository history, ProductRepository products, PdfExtractor extractor, EntityManager entityManager) {
        this.batches = batches;
        this.items = items;
        this.links = links;
        this.history = history;
        this.products = products;
        this.extractor = extractor;
        this.entityManager = entityManager;
    }

    public List<ImportBatch> list(UUID supplierId) {
        return supplierId == null ? batches.findAllByOrderByCreatedAtDesc() : batches.findBySupplierIdOrderByCreatedAtDesc(supplierId);
    }

    public ImportView view(UUID id) {
        ImportBatch batch = find(id);
        return new ImportView(batch, items.findByImportIdOrderByLineNumberAsc(id));
    }

    public ImportBatch find(UUID id) {
        return batches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Importação não encontrada."));
    }

    @Transactional
    public ImportView upload(UUID supplierId, String filename, byte[] bytes) {
        lockSupplier(supplierId);
        ImportBatch batch = new ImportBatch();
        batch.supplierId = supplierId;
        String name = Text.clean(filename).replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        batch.fileName = (name.isBlank() ? "arquivo.pdf" : name).substring(0, Math.min(255, name.isBlank() ? 11 : name.length()));
        batch.fileData = bytes;
        batch.hash = hash(bytes);
        batch = batches.saveAndFlush(batch);
        log.info("Importação {} iniciada para fornecedor {}", batch.id, supplierId);
        var previous = batches.findFirstBySupplierIdAndHashAndDuplicateOfIsNullOrderByCreatedAtAsc(supplierId, batch.hash);
        if (previous.isPresent() && !previous.get().id.equals(batch.id)) {
            batch.duplicateOf = previous.get().id;
            batch.status = "DUPLICADA";
            batch.error = "Este arquivo já foi recebido para este fornecedor.";
            log.warn("Importação {} duplicada de {}", batch.id, batch.duplicateOf);
            return new ImportView(batch, List.of());
        }
        List<ImportItem> extracted;
        try {
            extracted = extractor.extract(bytes);
            if (extracted.isEmpty()) throw new IOException("Nenhum item detectado.");
        } catch (IOException | IllegalArgumentException exception) {
            batch.status = "FALHA";
            batch.error = "Não foi possível extrair itens: PDF inválido, protegido, sem texto ou fora dos limites. Envie um PDF compatível.";
            log.warn("Importação {} falhou: {}", batch.id, exception.getMessage());
            return new ImportView(batch, List.of());
        }
        batch.itemCount = extracted.size();
        for (ImportItem item : extracted) {
            item.importId = batch.id;
            links.findBySupplierIdAndItemKey(supplierId, key(item)).ifPresent(link -> item.productId = link.productId);
        }
        validateItems(extracted);
        items.saveAll(extracted);
        batch.status = extracted.stream().anyMatch(item -> !item.error.isEmpty()) ? "PENDENTE" : "PRONTA";
        log.info("Importação {} extraída: {} itens, status {}", batch.id, batch.itemCount, batch.status);
        return new ImportView(batch, extracted);
    }

    @Transactional
    public ImportView update(UUID batchId, UUID itemId, ItemRequest request) {
        ImportBatch batch = lockBatch(batchId);
        editable(batch);
        List<ImportItem> rows = items.findByImportIdOrderByLineNumberAsc(batchId);
        ImportItem item = rows.stream().filter(row -> row.id.equals(itemId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado nesta importação."));
        if (item.confirmed) throw new ResponseStatusException(HttpStatus.CONFLICT, "O histórico deste item já foi confirmado e é imutável.");
        if (request.productId() != null && !products.existsById(request.productId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        item.reference = Text.clean(request.reference());
        item.description = Text.clean(request.description());
        item.unit = Text.clean(request.unit());
        item.packaging = Text.clean(request.packaging());
        item.quantity = request.quantity();
        item.price = request.price();
        item.productId = request.productId();
        validateItems(rows);
        if (item.productId != null && !item.description.isBlank() && !item.unit.isBlank() && !item.packaging.isBlank()) {
            SupplierLink link = links.findBySupplierIdAndItemKey(batch.supplierId, key(item)).orElseGet(SupplierLink::new);
            link.supplierId = batch.supplierId;
            link.itemKey = key(item);
            link.productId = item.productId;
            links.save(link);
        }
        status(batch, rows);
        return new ImportView(batch, rows);
    }

    @Transactional
    public ImportView confirm(UUID id) {
        ImportBatch batch = lockBatch(id);
        if ("DUPLICADA".equals(batch.status) || "FALHA".equals(batch.status))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta tentativa não pode ser confirmada.");
        List<ImportItem> rows = items.findByImportIdOrderByLineNumberAsc(id);
        validateItems(rows);
        int count = 0;
        for (ImportItem item : rows) {
            if (item.confirmed || !item.error.isEmpty()) continue;
            PriceHistory record = new PriceHistory();
            record.importId = id;
            record.itemId = item.id;
            record.supplierId = batch.supplierId;
            record.productId = item.productId;
            record.itemKey = key(item);
            record.price = item.price;
            record.previousPrice = history.findFirstBySupplierIdAndItemKeyOrderByCreatedAtDescIdDesc(batch.supplierId, record.itemKey)
                    .map(value -> value.price).orElse(null);
            PriceComparison comparison = PriceComparison.compare(record.previousPrice, record.price);
            record.classification = comparison.classification();
            record.difference = comparison.difference();
            record.percentage = comparison.percentage();
            record.createdAt = Instant.now();
            history.saveAndFlush(record);
            item.confirmed = true;
            item.classification = record.classification;
            count++;
        }
        status(batch, rows);
        log.info("Importação {} confirmada: {} novos preços, status {}", batch.id, count, batch.status);
        return new ImportView(batch, rows);
    }

    public List<PriceHistory> history(UUID productId, UUID supplierId, UUID importId) {
        Specification<PriceHistory> filter = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (productId != null) predicates.add(builder.equal(root.get("productId"), productId));
            if (supplierId != null) predicates.add(builder.equal(root.get("supplierId"), supplierId));
            if (importId != null) predicates.add(builder.equal(root.get("importId"), importId));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return history.findAll(filter, Sort.by(Sort.Direction.ASC, "createdAt", "id"));
    }

    private void validateItems(List<ImportItem> rows) {
        Map<String, Long> counts = new HashMap<>();
        for (ImportItem item : rows) counts.merge(key(item), 1L, Long::sum);
        for (ImportItem item : rows) {
            if (item.confirmed) continue;
            List<String> errors = new ArrayList<>();
            if (item.description.isBlank()) errors.add("Informe a descrição.");
            if (item.unit.isBlank()) errors.add("Informe a unidade.");
            if (item.packaging.isBlank()) errors.add("Informe a embalagem.");
            if (item.price == null || item.price.signum() <= 0) errors.add("Preço ausente, zero ou inválido.");
            if (item.quantity != null && item.quantity.signum() <= 0) errors.add("Quantidade inválida.");
            if (item.productId == null) errors.add("Associe um produto interno.");
            if (counts.get(key(item)) > 1) errors.add("Apresentação repetida no documento; corrija a identificação.");
            item.error = String.join(" ", errors);
            item.classification = errors.isEmpty() ? "PRONTO" : "PENDENTE";
        }
    }

    private void status(ImportBatch batch, List<ImportItem> rows) {
        boolean pending = rows.stream().anyMatch(item -> !item.confirmed);
        boolean confirmed = rows.stream().anyMatch(item -> item.confirmed);
        batch.status = pending ? (confirmed ? "PARCIAL" : rows.stream().allMatch(item -> item.error.isEmpty()) ? "PRONTA" : "PENDENTE") : "CONCLUIDA";
    }

    private void editable(ImportBatch batch) {
        if (Set.of("DUPLICADA", "FALHA", "CONCLUIDA").contains(batch.status))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta importação não permite alterações.");
    }

    private ImportBatch lockBatch(UUID id) {
        ImportBatch batch = entityManager.find(ImportBatch.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (batch == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Importação não encontrada.");
        lockSupplier(batch.supplierId);
        return batch;
    }

    private void lockSupplier(UUID id) {
        if (entityManager.find(Supplier.class, id, LockModeType.PESSIMISTIC_WRITE) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado.");
    }

    static String key(ImportItem item) {
        String identity = java.util.stream.Stream.of(item.reference, item.description, item.unit, item.packaging)
                .map(Text::normalize).map(value -> value.length() + ":" + value)
                .collect(java.util.stream.Collectors.joining());
        return hash(identity.getBytes(StandardCharsets.UTF_8));
    }

    static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
