package gift.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gift.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.product.Product;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoMessageClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KakaoMessageClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public void sendToMe(String accessToken, Order order, Product product) {
        var templateObject = buildTemplate(order, product);

        var params = new LinkedMultiValueMap<String, String>();
        params.add("template_object", templateObject);

        restClient.post()
            .uri("https://kapi.kakao.com/v2/api/talk/memo/default/send")
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(params)
            .retrieve()
            .toBodilessEntity();
    }

    public String buildTemplate(Order order, Product product) {
        var totalPrice = String.format("%,d", product.getPrice() * order.getQuantity());
        var message = order.getMessage() != null && !order.getMessage().isBlank()
            ? "\\n\\n💌 " + escapeJson(order.getMessage())
            : "";
        return """
            {
                "object_type": "text",
                "text": "🎁 선물이 도착했어요!\\n\\n%s (%s)\\n수량: %d개\\n금액: %s원%s",
                "link": {},
                "button_title": "선물 확인하기"
            }
            """.formatted(
            escapeJson(product.getName()),
            escapeJson(order.getOption().getName()),
            order.getQuantity(),
            totalPrice,
            message
        );
    }

    private String escapeJson(String value) {
        try {
            String quoted = objectMapper.writeValueAsString(value);
            return quoted.substring(1, quoted.length() - 1);
        } catch (JsonProcessingException e) {
            return value;
        }
    }
}
