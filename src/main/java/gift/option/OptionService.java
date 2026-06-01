package gift.option;

import gift.exception.BadRequestException;
import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import gift.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
            .orElseThrow(() -> new NotFoundException("Product not found."));
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }

    public OptionResponse create(Long productId, OptionRequest request) {
        validateName(request.name());
        var product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new DuplicateException("이미 존재하는 옵션명입니다.");
        }
        return OptionResponse.from(optionRepository.save(new Option(product, request.name(), request.quantity())));
    }

    public void delete(Long productId, Long optionId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        var options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new BadRequestException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }
        var option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NotFoundException("Option not found."));
        if (!Objects.equals(option.getProduct().getId(), productId)) {
            throw new NotFoundException("Option not found.");
        }
        optionRepository.delete(option);
    }

    private void validateName(String name) {
        var errors = OptionNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join(", ", errors));
        }
    }
}
