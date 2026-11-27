package br.com.devops.importation;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequest(
        @NotNull @Size(max = 80) String reference,
        @NotNull @Size(max = 300) String description,
        @NotNull @Size(max = 30) String unit,
        @NotNull @Size(max = 100) String packaging,
        @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @Digits(integer = 12, fraction = 2) BigDecimal price,
        UUID productId) {
}
