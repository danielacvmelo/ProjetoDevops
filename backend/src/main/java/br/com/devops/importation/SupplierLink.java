package br.com.devops.importation;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "supplier_links")
public class SupplierLink {
    @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id;
    @Column(name = "supplier_id", nullable = false) public UUID supplierId;
    @Column(name = "item_key", nullable = false, length = 64) public String itemKey;
    @Column(name = "product_id", nullable = false) public UUID productId;
}
