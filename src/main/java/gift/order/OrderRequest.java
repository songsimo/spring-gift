package gift.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(
    @NotNull Long optionId,
    @Min(1) int quantity,
    @Size(max = 255) String message
) {
}
