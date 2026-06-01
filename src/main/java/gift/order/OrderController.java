package gift.order;

import gift.auth.AuthMember;
import gift.member.Member;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
        @AuthMember Member member,
        Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrders(member.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @AuthMember Member member,
        @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.create(member, request);
        boolean notificationSent = orderService.notifyOrder(member, response.id());
        return ResponseEntity.created(URI.create("/api/orders/" + response.id()))
            .body(response.withNotificationSent(notificationSent));
    }
}
