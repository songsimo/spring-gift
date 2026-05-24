package gift.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.category.model.Category;
import gift.option.Option;
import gift.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KakaoMessageClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KakaoMessageClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(mock(RestClient.class));
        client = new KakaoMessageClient(builder, objectMapper);
    }

    @Test
    @DisplayName("일반 메시지는 유효한 JSON을 생성한다")
    void buildTemplate_normalMessage_producesValidJson() throws Exception {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(product, "실버 256GB", 10);
        Order order = new Order(option, 1L, 2, "생일 축하해");

        String json = client.buildTemplate(order, product);

        assertThatCode(() -> objectMapper.readTree(json)).doesNotThrowAnyException();
        JsonNode node = objectMapper.readTree(json);
        assertThat(node.get("text").asText()).contains("MacBook");
    }

    @Test
    @DisplayName("메시지에 큰따옴표가 포함되어도 유효한 JSON을 생성한다")
    void buildTemplate_messageWithQuotes_producesValidJson() throws Exception {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(product, "실버 256GB", 10);
        Order order = new Order(option, 1L, 1, "He said \"hello\" and \\escaped\\");

        String json = client.buildTemplate(order, product);

        assertThatCode(() -> objectMapper.readTree(json)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("메시지가 null이어도 유효한 JSON을 생성한다")
    void buildTemplate_nullMessage_producesValidJson() throws Exception {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(product, "실버 256GB", 10);
        Order order = new Order(option, 1L, 1, null);

        String json = client.buildTemplate(order, product);

        assertThatCode(() -> objectMapper.readTree(json)).doesNotThrowAnyException();
    }
}
