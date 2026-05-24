package gift.product.service;

import gift.category.model.Category;
import gift.category.repository.CategoryRepository;
import gift.order.repository.OrderRepository;
import gift.product.model.Product;
import gift.product.model.ProductNameValidator;
import gift.product.repository.ProductRepository;
import gift.wish.repository.WishRepository;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final WishRepository wishRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, OrderRepository orderRepository, WishRepository wishRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.wishRepository = wishRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
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
        validateNotKakao(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
        Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateNotKakao(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    public void deleteProduct(Long id) {
        if (orderRepository.existsByOptionProductId(id)) {
            throw new IllegalArgumentException("주문이 있는 상품은 삭제할 수 없습니다.");
        }
        wishRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public void adminCreateProduct(String name, int price, String imageUrl, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        productRepository.save(new Product(name, price, imageUrl, category));
    }

    public void adminUpdateProduct(Long id, String name, int price, String imageUrl, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.update(name, price, imageUrl, category);
        productRepository.save(product);
    }

    private static void validateNotKakao(String name) {
        if (ProductNameValidator.containsKakao(name)) {
            throw new IllegalArgumentException("\"카카오\"가 포함된 상품명은 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
        }
    }
}