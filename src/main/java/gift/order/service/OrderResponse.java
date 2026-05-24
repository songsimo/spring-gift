package gift.order.service;

import gift.order.model.Order;
import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    Long optionId,
    String productName,
    String optionName,
    int quantity,
    int totalPrice,
    LocalDateTime orderDateTime,
    String message
) {
    public static OrderResponse from(Order order) {
        var option = order.getOption();
        var product = option.getProduct();
        return new OrderResponse(
            order.getId(),
            option.getId(),
            product.getName(),
            option.getName(),
            order.getQuantity(),
            product.getPrice() * order.getQuantity(),
            order.getOrderDateTime(),
            order.getMessage()
        );
    }
}
