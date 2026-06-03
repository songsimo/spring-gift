package gift.product;

import gift.category.CategoryRepository;
import gift.exception.BadRequestException;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.order.OrderRepository;
import gift.wish.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final WishRepository wishRepository;

    public ProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        OrderRepository orderRepository,
        WishRepository wishRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.wishRepository = wishRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return ProductResponse.from(productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found.")));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateName(request.name());
        var category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category not found."));
        return ProductResponse.from(productRepository.save(request.toEntity(category)));
    }

    @Transactional
    public void delete(Long id) {
        if (orderRepository.existsByOptionProductId(id)) {
            throw new ConflictException("Product has existing orders.");
        }
        var product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        wishRepository.deleteAllByProductId(id);
        productRepository.delete(product);
    }

    @Transactional
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
