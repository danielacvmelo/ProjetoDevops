package br.com.devops.importation;

import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.List;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.junit.jupiter.api.Test;

public class DemoPdfsTest {
    public static final List<String> FIRST = List.of(
            "REF001;Hibisco;kg;Pacote 1 kg;1;33,00",
            "REF002;Alecrim;kg;Pacote 1 kg;1;15,00",
            "REF003;Camomila;kg;Pacote 1 kg;1;38,00",
            "REF001;Hibisco;kg;Pacote 5 kg;1;165,00");
    public static final List<String> SECOND = List.of(
            "REF001;Hibisco;kg;Pacote 1 kg;1;35,00",
            "REF002;Alecrim;kg;Pacote 1 kg;1;14,00",
            "REF003;Camomila;kg;Pacote 1 kg;1;38,00",
            "REF001;Hibisco;kg;Pacote 5 kg;1;175,00",
            "REF004;Boldo;kg;Pacote 1 kg;1;20,00",
            "REF005;Semente exemplo;un;Pacote 100 g;1;0,00");

    public static byte[] pdf(List<String> lines) throws Exception {
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (var content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 9);
                content.setLeading(17);
                content.newLineAtOffset(30, 750);
                content.showText("Tabela de precos - demonstracao ficticia");
                content.newLine();
                content.showText("Fornecedor: Fornecedor Exemplo");
                content.newLine();
                content.showText("Referencia;Descricao;Unidade;Embalagem;Quantidade;Preco");
                for (String line : lines) {
                    content.newLine();
                    content.showText(line);
                }
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    @Test
    void createsDemoFiles() throws Exception {
        Path directory = Path.of("../docs/exemplos").toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Files.write(directory.resolve("fornecedor-exemplo-01.pdf"), pdf(FIRST));
        Files.write(directory.resolve("fornecedor-exemplo-02.pdf"), pdf(SECOND));
    }
}
