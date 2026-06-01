package gift.order;

import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    Long optionId,
    int quantity,
    LocalDateTime orderDateTime,
    String message,
    boolean notificationSent
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOption().getId(),
            order.getQuantity(),
            order.getOrderDateTime(),
            order.getMessage(),
            false
        );
    }

    public OrderResponse withNotificationSent(boolean sent) {
        return new OrderResponse(id, optionId, quantity, orderDateTime, message, sent);
    }
}
