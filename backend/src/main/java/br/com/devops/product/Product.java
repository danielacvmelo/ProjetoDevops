package br.com.devops.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, length = 80)
    public String code;

    @JsonIgnore
    @Column(name = "code_key", unique = true, length = 80)
    public String codeKey;

    @Column(nullable = false, length = 160)
    public String name;

    @Column(nullable = false, length = 100)
    public String category;

    @Column(nullable = false, length = 30)
    public String unit;

    @Column(name = "purchase_price", nullable = false, precision = 14, scale = 2)
    public BigDecimal purchasePrice;

    @Column(name = "sale_price", nullable = false, precision = 14, scale = 2)
    public BigDecimal salePrice;

    @Column(nullable = false, precision = 15, scale = 3)
    public BigDecimal stock;

    @Column(name = "minimum_stock", nullable = false, precision = 15, scale = 3)
    public BigDecimal minimumStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public ProductStatus status;

    @JsonIgnore
    @Column(name = "search_key", nullable = false, length = 400)
    public String searchKey;
}
