package br.com.devops.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @Size(max = 80) String code,
        @NotBlank(message = "Informe o nome.") @Size(max = 160) String name,
        @Size(max = 100) String category,
        @NotBlank(message = "Informe a unidade.") @Size(max = 30) String unit,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal purchasePrice,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal salePrice,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 3) BigDecimal stock,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 3) BigDecimal minimumStock,
        @NotNull ProductStatus status) {
}
