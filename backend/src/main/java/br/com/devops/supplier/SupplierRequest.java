package br.com.devops.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 160) String name,
        @Email(message = "Informe um e-mail válido.") @Size(max = 255) String email,
        @Size(max = 40) String phone) {
}
