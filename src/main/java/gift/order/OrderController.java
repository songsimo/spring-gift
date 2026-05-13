package gift.order;

import gift.auth.AuthenticationResolver;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final AuthenticationResolver authenticationResolver;
    private final OrderService orderService;

    public OrderController(
        AuthenticationResolver authenticationResolver,
        OrderService orderService
    ) {
        this.authenticationResolver = authenticationResolver;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> getOrders(
        @RequestHeader("Authorization") String authorization,
        Pageable pageable
    ) {
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(orderService.getOrders(member.getId(), pageable));
    }

    // order flow:
    // 1. auth check
    // 2. validate option
    // 3. subtract stock
    // 4. deduct points
    // 5. save order
    // 6. cleanup wish
    // 7. send kakao notification
    @PostMapping
    public ResponseEntity<?> createOrder(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody OrderRequest request
    ) {
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        OrderResponse response = orderService.createOrder(member.getId(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id()))
            .body(response);
    }
}
