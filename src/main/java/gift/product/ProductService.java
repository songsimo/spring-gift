package gift.product;

import gift.category.Category;
import gift.category.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public ProductResponse createProduct(ProductRequest request) {
        List<String> errors = ProductNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
        Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        List<String> errors = ProductNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        productRepository.save(product);
        return ProductResponse.from(product);
    }
}