package gift.product.service;

import gift.category.model.Category;
import gift.category.repository.CategoryRepository;
import gift.exception.NotFoundException;
import gift.order.service.OrderService;
import gift.product.model.Product;
import gift.product.model.ProductNameValidator;
import gift.product.repository.ProductRepository;
import gift.wish.service.WishService;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final WishService wishService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, OrderService orderService, WishService wishService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderService = orderService;
        this.wishService = wishService;
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
            .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateNotKakao(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + request.categoryId()));
        Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateNotKakao(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + request.categoryId()));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (orderService.existsByProductId(id)) {
            throw new IllegalArgumentException("주문이 있는 상품은 삭제할 수 없습니다.");
        }
        wishService.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
    }

    public void adminCreateProduct(String name, int price, String imageUrl, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId));
        productRepository.save(new Product(name, price, imageUrl, category));
    }

    public void adminUpdateProduct(Long id, String name, int price, String imageUrl, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
        product.update(name, price, imageUrl, category);
        productRepository.save(product);
    }

    private static void validateNotKakao(String name) {
        if (ProductNameValidator.containsKakao(name)) {
            throw new IllegalArgumentException("\"카카오\"가 포함된 상품명은 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
        }
    }
}