package br.com.devops.importation;

import br.com.devops.product.ProductRepository;
import br.com.devops.supplier.SupplierRepository;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:imports;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PriceHistoryRepository history;
    @Autowired ImportItemRepository items;
    @Autowired ImportRepository batches;
    @Autowired SupplierLinkRepository links;
    @Autowired ProductRepository products;
    @Autowired SupplierRepository suppliers;
    String supplierId;
    String productId;

    @BeforeEach
    void setup() throws Exception {
        history.deleteAll();
        items.deleteAll();
        var previousBatches = batches.findAll();
        previousBatches.forEach(batch -> batch.duplicateOf = null);
        batches.saveAllAndFlush(previousBatches);
        batches.deleteAll();
        links.deleteAll();
        products.deleteAll();
        suppliers.deleteAll();
        supplierId = create("/api/suppliers", "{\"name\":\"Fornecedor Exemplo\"}");
        productId = create("/api/products", """
                {"code":"INT-01","name":"Produto interno","category":"Chás","unit":"kg",
                 "purchasePrice":10,"salePrice":50,"stock":2,"minimumStock":1,"status":"Ativo"}
                """);
    }

    String create(String path, String body) throws Exception {
        return mapper.readTree(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    JsonNode upload(byte[] bytes) throws Exception {
        var file = new MockMultipartFile("file", "tabela.pdf", "application/pdf", bytes);
        return mapper.readTree(mvc.perform(multipart("/api/imports").file(file).param("supplierId", supplierId))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    JsonNode associate(JsonNode view, JsonNode item, String price) throws Exception {
        ObjectNode request = mapper.createObjectNode();
        for (String field : List.of("reference", "description", "unit", "packaging", "quantity", "price"))
            request.set(field, item.get(field));
        request.put("productId", productId);
        if (price != null) request.put("price", price);
        return mapper.readTree(mvc.perform(put("/api/imports/" + view.at("/batch/id").asText() + "/items/" + item.get("id").asText())
                .contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    JsonNode confirm(String id) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/imports/" + id + "/confirm")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void importsExamplesReusesLinksComparesAndKeepsHistoryImmutable() throws Exception {
        JsonNode first = upload(DemoPdfsTest.pdf(DemoPdfsTest.FIRST));
        assertThat(first.at("/batch/status").asText()).isEqualTo("PENDENTE");
        String firstId = first.at("/batch/id").asText();
        for (JsonNode item : first.get("items")) associate(first, item, null);
        assertThat(confirm(firstId).at("/batch/status").asText()).isEqualTo("CONCLUIDA");
        assertThat(history.count()).isEqualTo(4);

        JsonNode second = upload(DemoPdfsTest.pdf(DemoPdfsTest.SECOND));
        String secondId = second.at("/batch/id").asText();
        assertThat(second.get("items").get(0).get("productId").asText()).isEqualTo(productId);
        for (JsonNode item : second.get("items")) if (item.get("productId").isNull()) associate(second, item, null);
        JsonNode confirmed = confirm(secondId);
        assertThat(confirmed.at("/batch/status").asText()).isEqualTo("PARCIAL");
        List<String> classifications = new ArrayList<>();
        confirmed.get("items").forEach(item -> classifications.add(item.get("classification").asText()));
        assertThat(classifications).containsExactly("AUMENTOU", "DIMINUIU", "SEM_ALTERACAO", "AUMENTOU", "NOVO", "PENDENTE");
        assertThat(history.count()).isEqualTo(9);
        confirm(secondId);
        assertThat(history.count()).isEqualTo(9);
        var record = history.findAll().stream().filter(row -> row.importId.toString().equals(secondId) &&
                row.previousPrice != null && row.previousPrice.intValue() == 33).findFirst().orElseThrow();
        assertThat(record.percentage).isEqualByComparingTo("6.06");
        assertThat(products.findById(UUID.fromString(productId)).orElseThrow().salePrice).isEqualByComparingTo("50");
        associate(confirmed, confirmed.get("items").get(5), "10.00");
        assertThat(confirm(secondId).at("/batch/status").asText()).isEqualTo("CONCLUIDA");
        assertThat(history.count()).isEqualTo(10);
        mvc.perform(put("/api/imports/" + secondId + "/items/" + confirmed.get("items").get(0).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"reference":"x","description":"x","unit":"kg","packaging":"kg","price":10}
                """)).andExpect(status().isConflict());
        mvc.perform(get("/api/imports/history").param("importId", secondId)).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/imports/" + firstId + "/file")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void recordsDuplicateAttemptWithoutCreatingHistory() throws Exception {
        byte[] bytes = DemoPdfsTest.pdf(DemoPdfsTest.FIRST);
        JsonNode original = upload(bytes);
        JsonNode duplicate = upload(bytes);
        assertThat(duplicate.at("/batch/status").asText()).isEqualTo("DUPLICADA");
        assertThat(duplicate.at("/batch/duplicateOf").asText()).isEqualTo(original.at("/batch/id").asText());
        assertThat(batches.count()).isEqualTo(2);
        assertThat(history.count()).isZero();
        mvc.perform(post("/api/imports/" + duplicate.at("/batch/id").asText() + "/confirm")).andExpect(status().isConflict());
    }

    @Test
    void recordsInvalidPdfAndDoesNotConfirmIt() throws Exception {
        JsonNode view = upload("arquivo invalido".getBytes());
        assertThat(view.at("/batch/status").asText()).isEqualTo("FALHA");
        assertThat(batches.count()).isEqualTo(1);
        mvc.perform(post("/api/imports/" + view.at("/batch/id").asText() + "/confirm")).andExpect(status().isConflict());
    }

    @Test
    void keepsMalformedRowsAndZeroPricesPending() throws Exception {
        JsonNode view = upload(DemoPdfsTest.pdf(List.of("linha fora do formato", "X;Produto;kg;Pacote 1 kg;1;0,00")));
        assertThat(view.get("items").size()).isEqualTo(2);
        for (JsonNode item : view.get("items")) assertThat(item.get("error").asText()).isNotBlank();
        confirm(view.at("/batch/id").asText());
        assertThat(history.count()).isZero();
    }

    @Test
    void repeatedPresentationRemainsPending() throws Exception {
        JsonNode view = upload(DemoPdfsTest.pdf(List.of(DemoPdfsTest.FIRST.get(0), DemoPdfsTest.FIRST.get(0))));
        for (JsonNode item : view.get("items")) associate(view, item, null);
        JsonNode confirmed = confirm(view.at("/batch/id").asText());
        assertThat(confirmed.get("items").get(0).get("error").asText()).contains("repetida");
        assertThat(history.count()).isZero();
    }

    @Test
    void usesLastValidPriceEvenWhenItemWasAbsentInIntermediateImport() throws Exception {
        JsonNode first = upload(DemoPdfsTest.pdf(List.of(DemoPdfsTest.FIRST.get(0))));
        associate(first, first.get("items").get(0), null);
        confirm(first.at("/batch/id").asText());
        JsonNode middle = upload(DemoPdfsTest.pdf(List.of(DemoPdfsTest.FIRST.get(1))));
        associate(middle, middle.get("items").get(0), null);
        confirm(middle.at("/batch/id").asText());
        JsonNode last = upload(DemoPdfsTest.pdf(List.of(DemoPdfsTest.SECOND.get(0))));
        JsonNode confirmed = confirm(last.at("/batch/id").asText());
        assertThat(confirmed.get("items").get(0).get("classification").asText()).isEqualTo("AUMENTOU");
    }

    @Test
    void descriptionChangeRequiresNewManualAssociation() throws Exception {
        JsonNode first = upload(DemoPdfsTest.pdf(List.of(DemoPdfsTest.FIRST.get(0))));
        associate(first, first.get("items").get(0), null);
        JsonNode changed = upload(DemoPdfsTest.pdf(List.of("REF001;Outro nome;kg;Pacote 1 kg;1;35,00")));
        assertThat(changed.get("items").get(0).get("productId").isNull()).isTrue();
    }

    @Test
    void rejectsMissingSupplierAndItemFromDifferentImport() throws Exception {
        var file = new MockMultipartFile("file", "tabela.pdf", "application/pdf", DemoPdfsTest.pdf(DemoPdfsTest.FIRST));
        mvc.perform(multipart("/api/imports").file(file).param("supplierId", UUID.randomUUID().toString())).andExpect(status().isNotFound());
        JsonNode first = upload(DemoPdfsTest.pdf(DemoPdfsTest.FIRST));
        mvc.perform(put("/api/imports/" + first.at("/batch/id").asText() + "/items/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"reference":"X","description":"x","unit":"kg","packaging":"kg","price":10}
                """)).andExpect(status().isNotFound());
    }

    @Test
    void comparesPricesWithoutDivisionByZero() {
        assertThat(PriceComparison.compare(null, new java.math.BigDecimal("20")).classification()).isEqualTo("NOVO");
        assertThat(PriceComparison.compare(java.math.BigDecimal.ZERO, java.math.BigDecimal.ONE).percentage()).isNull();
        assertThat(PriceComparison.compare(new java.math.BigDecimal("15"), new java.math.BigDecimal("14")).percentage()).isEqualByComparingTo("-6.67");
        assertThat(PriceComparison.compare(java.math.BigDecimal.ONE, java.math.BigDecimal.ONE).classification()).isEqualTo("SEM_ALTERACAO");
        assertThatThrownBy(() -> PriceComparison.compare(java.math.BigDecimal.ONE, java.math.BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }
}
