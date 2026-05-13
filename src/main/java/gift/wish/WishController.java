package gift.wish;

import gift.auth.AuthenticationResolver;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final WishRepository wishRepository;
    private final AuthenticationResolver authenticationResolver;
    private final WishService wishService;

    public WishController(
        WishRepository wishRepository,
        AuthenticationResolver authenticationResolver,
        WishService wishService
    ) {
        this.wishRepository = wishRepository;
        this.authenticationResolver = authenticationResolver;
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<Page<WishResponse>> getWishes(
        @RequestHeader("Authorization") String authorization,
        Pageable pageable
    ) {
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishService.getWishes(member.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody WishRequest request
    ) {
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        WishResponse response = wishService.addWish(member.getId(), request.productId());
        return ResponseEntity.created(URI.create("/api/wishes/" + response.id()))
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(
        @RequestHeader("Authorization") String authorization,
        @PathVariable Long id
    ) {
        // check auth
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }

        var wish = wishRepository.findById(id).orElse(null);
        if (wish == null) {
            return ResponseEntity.notFound().build();
        }

        if (!wish.getMemberId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        wishRepository.delete(wish);
        return ResponseEntity.noContent().build();
    }
}
