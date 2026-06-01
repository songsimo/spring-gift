package gift.product;

import gift.category.CategoryRepository;
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
}
