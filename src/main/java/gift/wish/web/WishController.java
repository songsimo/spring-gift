package gift.wish.web;

import gift.auth.AuthenticationResolver;
import gift.wish.service.WishRequest;
import gift.wish.service.WishResponse;
import gift.wish.service.WishService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final AuthenticationResolver authenticationResolver;
    private final WishService wishService;

    public WishController(
        AuthenticationResolver authenticationResolver,
        WishService wishService
    ) {
        this.authenticationResolver = authenticationResolver;
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<Page<WishResponse>> getWishes(
        @RequestHeader("Authorization") String authorization,
        Pageable pageable
    ) {
        var member = authenticationResolver.extractMember(authorization);
        return ResponseEntity.ok(wishService.getWishes(member.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody WishRequest request
    ) {
        var member = authenticationResolver.extractMember(authorization);
        WishResponse response = wishService.addWish(member.getId(), request.productId());
        return ResponseEntity.created(URI.create("/api/wishes/" + response.id()))
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(
        @RequestHeader("Authorization") String authorization,
        @PathVariable Long id
    ) {
        var member = authenticationResolver.extractMember(authorization);
        wishService.removeWish(member.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
