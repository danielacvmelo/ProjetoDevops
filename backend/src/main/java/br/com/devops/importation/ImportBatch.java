package br.com.devops.importation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "imports")
public class ImportBatch {
    @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id;
    @Column(name = "supplier_id", nullable = false) public UUID supplierId;
    @Column(name = "file_name", nullable = false) public String fileName;
    @Column(nullable = false, length = 64) public String hash;
    @JsonIgnore @Column(name = "file_data", nullable = false) public byte[] fileData;
    @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
    @Column(nullable = false, length = 20) public String status = "PENDENTE";
    @Column(name = "duplicate_of") public UUID duplicateOf;
    @Column(name = "item_count", nullable = false) public int itemCount;
    @Column(length = 1000) public String error = "";
}
