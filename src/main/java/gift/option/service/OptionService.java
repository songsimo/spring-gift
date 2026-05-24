package gift.option.service;

import gift.option.model.Option;
import gift.option.model.OptionNameValidator;
import gift.option.repository.OptionRepository;
import gift.order.OrderRepository;
import gift.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<OptionResponse> getOptions(Long productId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }

    public OptionResponse createOption(Long productId, OptionRequest request) {
        List<String> errors = OptionNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        var product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new IllegalArgumentException("이미 존재하는 옵션명입니다.");
        }
        Option saved = optionRepository.save(new Option(product, request.name(), request.quantity()));
        return OptionResponse.from(saved);
    }

    public OptionResponse updateOption(Long productId, Long optionId, OptionRequest request) {
        List<String> errors = OptionNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        Option option = optionRepository.findById(optionId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));
        if (optionRepository.existsByProductIdAndNameAndIdNot(productId, request.name(), optionId)) {
            throw new IllegalArgumentException("이미 존재하는 옵션명입니다.");
        }
        option.update(request.name(), request.quantity());
        return OptionResponse.from(optionRepository.save(option));
    }

    public void deleteOption(Long productId, Long optionId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        List<Option> options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }
        Option option = optionRepository.findById(optionId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));
        if (orderRepository.existsByOptionId(optionId)) {
            throw new IllegalArgumentException("주문이 있는 옵션은 삭제할 수 없습니다.");
        }
        optionRepository.delete(option);
    }
}