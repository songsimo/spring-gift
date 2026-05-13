package gift.option;

import gift.product.Product;
import gift.product.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Each product must have at least one option at all times.
 * Option names are validated against allowed characters and length constraints.
 */
@RestController
@RequestMapping(path = "/api/products/{productId}/options")
public class OptionController {
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;
    private final OptionService optionService;

    public OptionController(OptionRepository optionRepository, ProductRepository productRepository, OptionService optionService) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
        this.optionService = optionService;
    }

    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(optionService.getOptions(productId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<OptionResponse> createOption(
        @PathVariable Long productId,
        @Valid @RequestBody OptionRequest request
    ) {
        OptionResponse response = optionService.createOption(productId, request);
        URI location = URI.create("/api/products/" + productId + "/options/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping(path = "/{optionId}")
    public ResponseEntity<Void> deleteOption(
        @PathVariable Long productId,
        @PathVariable Long optionId
    ) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        List<Option> options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }

        Option option = optionRepository.findById(optionId).orElse(null);
        if (option == null || !option.getProduct().getId().equals(productId)) {
            return ResponseEntity.notFound().build();
        }

        optionRepository.delete(option);
        return ResponseEntity.noContent().build();
    }

    private void validateName(String name) {
        List<String> errors = OptionNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
