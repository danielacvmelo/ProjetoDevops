package br.com.devops.importation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PriceComparison(String classification, BigDecimal difference, BigDecimal percentage) {
    public static PriceComparison compare(BigDecimal previous, BigDecimal current) {
        if (current == null || current.signum() <= 0) throw new IllegalArgumentException("Preço atual inválido.");
        if (previous == null || previous.signum() <= 0) return new PriceComparison("NOVO", null, null);
        BigDecimal difference = current.subtract(previous);
        String status = difference.signum() > 0 ? "AUMENTOU" : difference.signum() < 0 ? "DIMINUIU" : "SEM_ALTERACAO";
        return new PriceComparison(status, difference, difference.multiply(BigDecimal.valueOf(100)).divide(previous, 2, RoundingMode.HALF_UP));
    }
}
