package br.com.devops.importation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_history")
public class PriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id;
    @Column(name = "import_id", nullable = false) public UUID importId;
    @Column(name = "item_id", nullable = false, unique = true) public UUID itemId;
    @Column(name = "supplier_id", nullable = false) public UUID supplierId;
    @Column(name = "product_id", nullable = false) public UUID productId;
    @Column(name = "item_key", nullable = false, length = 64) public String itemKey;
    @Column(nullable = false, precision = 14, scale = 2) public BigDecimal price;
    @Column(name = "previous_price", precision = 14, scale = 2) public BigDecimal previousPrice;
    @Column(precision = 14, scale = 2) public BigDecimal difference;
    @Column(precision = 24, scale = 2) public BigDecimal percentage;
    @Column(nullable = false, length = 20) public String classification;
    @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
}
