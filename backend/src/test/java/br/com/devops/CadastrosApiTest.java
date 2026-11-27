package br.com.devops;

import br.com.devops.product.ProductRepository;
import br.com.devops.supplier.SupplierRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CadastrosApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ProductRepository products;
    @Autowired SupplierRepository suppliers;

    @BeforeEach
    void clean() {
        products.deleteAll();
        suppliers.deleteAll();
    }

    private ObjectNode product() {
        ObjectNode body = mapper.createObjectNode();
        body.put("code", "PRD-001");
        body.put("name", "Hibisco");
        body.put("category", "Chás");
        body.put("unit", "kg");
        body.put("purchasePrice", 33);
        body.put("salePrice", 50);
        body.put("stock", 1.5);
        body.put("minimumStock", 0.5);
        body.put("status", "Ativo");
        return body;
    }

    private String createProduct(ObjectNode body) throws Exception {
        MvcResult result = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codeKey").doesNotExist()).andExpect(jsonPath("$.searchKey").doesNotExist()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void createsReadsAndUpdatesProduct() throws Exception {
        String id = createProduct(product());
        mvc.perform(get("/api/products/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hibisco")).andExpect(jsonPath("$.stock").value(1.5));
        ObjectNode body = product().put("salePrice", 55).put("status", "Inativo");
        mvc.perform(put("/api/products/" + id).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/products/" + id)).andExpect(jsonPath("$.salePrice").value(55))
                .andExpect(jsonPath("$.status").value("Inativo"));
        assertThat(products.count()).isEqualTo(1);
    }

    @Test
    void searchesProductsByNameCodeAndCategoryIgnoringAccents() throws Exception {
        createProduct(product());
        for (String query : new String[]{"hibisco", "prd-001", "chas"}) {
            mvc.perform(get("/api/products").param("q", query)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
        mvc.perform(get("/api/products").param("q", "camomila")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsDuplicateProductCodeOnCreationAndUpdate() throws Exception {
        createProduct(product());
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().put("code", " prd-001 ").toString()))
                .andExpect(status().isConflict());
        String id = createProduct(product().put("code", "PRD-002"));
        mvc.perform(put("/api/products/" + id).contentType(MediaType.APPLICATION_JSON).content(product().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void allowsEmptyCodesAndZeroPrices() throws Exception {
        createProduct(product().put("code", "").put("purchasePrice", 0).put("salePrice", 0));
        createProduct(product().put("code", ""));
        assertThat(products.count()).isEqualTo(2);
    }

    @Test
    void rejectsNegativeAndMissingNumbers() throws Exception {
        for (String field : new String[]{"purchasePrice", "salePrice", "stock", "minimumStock"}) {
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().put(field, -1).toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors." + field).exists());
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().putNull(field).toString()))
                    .andExpect(status().isBadRequest());
        }
        assertThat(products.count()).isZero();
    }

    @Test
    void rejectsBlankRequiredFieldsAndInvalidStatus() throws Exception {
        for (String field : new String[]{"name", "unit"}) {
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().put(field, "  ").toString()))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().put("status", "Outro").toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(product().put("salePrice", 1.123).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundAndRejectsMalformedId() throws Exception {
        for (String endpoint : new String[]{"/api/products/", "/api/suppliers/"}) {
            mvc.perform(get(endpoint + UUID.randomUUID())).andExpect(status().isNotFound());
            mvc.perform(get(endpoint + "invalido")).andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/products/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(product().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsFindsAndSearchesSupplier() throws Exception {
        ObjectNode body = mapper.createObjectNode().put("name", " Raízes Naturais ").put("email", "contato@example.com").put("phone", "(83) 3333-0100");
        MvcResult result = mvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Raízes Naturais"))
                .andExpect(jsonPath("$.nameKey").doesNotExist()).andReturn();
        String id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/api/suppliers/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("contato@example.com"));
        mvc.perform(get("/api/suppliers").param("q", "RAIZES")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/suppliers").param("q", "outra loja")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsDuplicateSupplierAndInvalidEmail() throws Exception {
        mvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Raízes Naturais\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" raizes naturais \"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Outro\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        assertThat(suppliers.count()).isEqualTo(1);
    }

    @Test
    void allowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/products").header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/products").header("Origin", "http://other.example").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
