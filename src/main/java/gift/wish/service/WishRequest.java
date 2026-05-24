package gift.wish.service;

import jakarta.validation.constraints.NotNull;

public record WishRequest(@NotNull Long productId) {
}
