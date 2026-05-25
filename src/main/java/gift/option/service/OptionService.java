package gift.option.service;

import gift.exception.ConflictException;
import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import gift.option.model.Option;
import gift.option.model.OptionNameValidator;
import gift.option.repository.OptionRepository;
import gift.order.service.OrderService;
import gift.product.model.Product;
import gift.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public OptionService(OptionRepository optionRepository, ProductService productService, OrderService orderService) {
        this.optionRepository = optionRepository;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptions(Long productId) {
        productService.getProductEntity(productId);
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }

    @Transactional
    public OptionResponse createOption(Long productId, OptionRequest request) {
        List<String> errors = OptionNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        Product product = productService.getProductEntity(productId);
        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new DuplicateException("이미 존재하는 옵션명입니다.");
        }
        Option saved = optionRepository.save(new Option(product, request.name(), request.quantity()));
        return OptionResponse.from(saved);
    }

    @Transactional
    public OptionResponse updateOption(Long productId, Long optionId, OptionRequest request) {
        List<String> errors = OptionNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        Option option = optionRepository.findById(optionId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new NotFoundException("옵션을 찾을 수 없습니다. id=" + optionId));
        if (optionRepository.existsByProductIdAndNameAndIdNot(productId, request.name(), optionId)) {
            throw new DuplicateException("이미 존재하는 옵션명입니다.");
        }
        option.update(request.name(), request.quantity());
        return OptionResponse.from(optionRepository.save(option));
    }

    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        productService.getProductEntity(productId);
        List<Option> options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new ConflictException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }
        Option option = optionRepository.findById(optionId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new NotFoundException("옵션을 찾을 수 없습니다. id=" + optionId));
        if (orderService.existsByOptionId(optionId)) {
            throw new ConflictException("주문이 있는 옵션은 삭제할 수 없습니다.");
        }
        optionRepository.delete(option);
    }
}