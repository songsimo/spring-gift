package gift.option;

import gift.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    public List<OptionResponse> getOptions(Long productId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }
}