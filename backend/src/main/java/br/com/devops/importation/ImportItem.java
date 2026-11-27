package br.com.devops.importation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "import_items")
public class ImportItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id;
    @Column(name = "import_id", nullable = false) public UUID importId;
    @Column(name = "line_number", nullable = false) public int lineNumber;
    @Column(name = "raw_line", nullable = false, length = 2000) public String rawLine;
    @Column(nullable = false, length = 80) public String reference = "";
    @Column(nullable = false, length = 300) public String description = "";
    @Column(nullable = false, length = 30) public String unit = "";
    @Column(nullable = false, length = 100) public String packaging = "";
    @Column(precision = 15, scale = 3) public BigDecimal quantity;
    @Column(precision = 14, scale = 2) public BigDecimal price;
    @Column(name = "product_id") public UUID productId;
    @Column(nullable = false) public boolean confirmed;
    @Column(nullable = false, length = 20) public String classification = "PENDENTE";
    @Column(length = 1000) public String error = "";
}
