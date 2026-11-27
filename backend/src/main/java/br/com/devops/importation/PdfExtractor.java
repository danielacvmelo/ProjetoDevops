package br.com.devops.importation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import br.com.devops.shared.Text;

@Component
public class PdfExtractor {
    public List<ImportItem> extract(byte[] bytes) throws IOException {
        try (var document = Loader.loadPDF(bytes)) {
            if (document.getNumberOfPages() > 100) throw new IOException("O limite é de 100 páginas.");
            if (!document.getCurrentAccessPermission().canExtractContent()) throw new IOException("O PDF não permite extração.");
            var stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text.isBlank()) throw new IOException("O PDF não contém texto extraível. OCR não está disponível.");
            List<ImportItem> items = new ArrayList<>();
            int lineNumber = 0;
            for (String line : text.split("\\R")) {
                lineNumber++;
                String raw = line.strip();
                if (raw.isBlank()) continue;
                String normalized = Text.normalize(raw);
                if (normalized.startsWith("referencia;") || normalized.startsWith("tabela de precos") ||
                        normalized.startsWith("fornecedor:") || normalized.startsWith("data:") ||
                        normalized.startsWith("pagina ")) continue;
                ImportItem item = new ImportItem();
                item.lineNumber = lineNumber;
                item.rawLine = raw.substring(0, Math.min(2000, raw.length()));
                String[] fields = raw.split(";", -1);
                if (fields.length == 6 && fields[0].strip().length() <= 80 && fields[1].strip().length() <= 300 &&
                        fields[2].strip().length() <= 30 && fields[3].strip().length() <= 100) {
                    item.reference = fields[0].strip();
                    item.description = fields[1].strip();
                    item.unit = fields[2].strip();
                    item.packaging = fields[3].strip();
                    try {
                        item.quantity = number(fields[4], 3);
                        item.price = number(fields[5], 2);
                    } catch (IllegalArgumentException exception) {
                        item.error = "Quantidade ou preço não reconhecido. Confira a linha original.";
                    }
                } else {
                    item.error = "Linha não reconhecida. Corrija os campos antes de confirmar.";
                }
                items.add(item);
                if (items.size() > 2000) throw new IOException("O limite é de 2000 linhas por documento.");
            }
            return items;
        }
    }

    private BigDecimal number(String text, int scale) {
        String value = text.strip().replace("R$", "").strip();
        if (value.isEmpty()) return null;
        if (value.contains(",")) value = value.replace(".", "").replace(",", ".");
        BigDecimal result = new BigDecimal(value);
        if (result.scale() > scale || result.precision() - result.scale() > 12) throw new IllegalArgumentException();
        return result;
    }
}
