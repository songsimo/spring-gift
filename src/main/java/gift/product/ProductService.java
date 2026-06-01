package gift.product;

import gift.category.CategoryRepository;
import gift.exception.BadRequestException;
import gift.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found.")));
    }

    public ProductResponse create(ProductRequest request) {
        validateName(request.name());
        var category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category not found."));
        return ProductResponse.from(productRepository.save(request.toEntity(category)));
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        validateName(request.name());
        var product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        var category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category not found."));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        return ProductResponse.from(productRepository.save(product));
    }

    private void validateName(String name) {
        var errors = ProductNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join(", ", errors));
        }
    }
}
