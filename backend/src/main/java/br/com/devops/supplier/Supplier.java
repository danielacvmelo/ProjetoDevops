package br.com.devops.supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, length = 160)
    public String name;

    @JsonIgnore
    @Column(name = "name_key", nullable = false, unique = true, length = 160)
    public String nameKey;

    @Column(nullable = false)
    public String email;

    @Column(nullable = false, length = 40)
    public String phone;
}
