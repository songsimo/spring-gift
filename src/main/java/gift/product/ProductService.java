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
        if (orderRepository.existsByOptionProductId(id)) {
            throw new ConflictException("주문이 있는 상품은 삭제할 수 없습니다.");
        }
        var product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        wishRepository.deleteAllByProductId(id);
        productRepository.delete(product);
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
