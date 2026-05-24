package gift.product;

import gift.category.model.Category;
import gift.category.repository.CategoryRepository;
import gift.exception.NotFoundException;
import gift.order.repository.OrderRepository;
import gift.product.model.Product;
import gift.product.repository.ProductRepository;
import gift.product.service.ProductRequest;
import gift.product.service.ProductResponse;
import gift.product.service.ProductService;
import gift.wish.repository.WishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    WishRepository wishRepository;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("상품 목록 조회 시 페이지 형태로 모든 상품을 반환한다")
    void getProducts_returnsPageOfProducts() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Page<Product> page = new PageImpl<>(List.of(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category)
        ));
        given(productRepository.findAll(any(Pageable.class))).willReturn(page);

        Page<ProductResponse> result = productService.getProducts(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("MacBook");
    }

    @Test
    @DisplayName("존재하는 상품 단건 조회 시 해당 상품을 반환한다")
    void getProduct_existingId_returnsProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(productRepository.findById(1L)).willReturn(Optional.of(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category)
        ));

        ProductResponse result = productService.getProduct(1L);

        assertThat(result.name()).isEqualTo("MacBook");
        assertThat(result.price()).isEqualTo(1000000);
        assertThat(result.categoryName()).isEqualTo("전자기기");
    }

    @Test
    @DisplayName("존재하지 않는 상품 단건 조회 시 예외가 발생한다")
    void getProduct_nonExistingId_throwsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("유효한 요청으로 상품 생성 시 저장된 상품을 반환한다")
    void createProduct_validRequest_returnsCreatedProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        ProductRequest request = new ProductRequest("MacBook", 1000000, "https://example.com/mac.png", 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any())).willReturn(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category)
        );

        ProductResponse result = productService.createProduct(request);

        assertThat(result.name()).isEqualTo("MacBook");
        assertThat(result.categoryId()).isEqualTo(1L);
        assertThat(result.categoryName()).isEqualTo("전자기기");
    }

    @Test
    @DisplayName("유효하지 않은 상품명으로 생성 시 예외가 발생한다")
    void createProduct_invalidName_throwsException() {
        ProductRequest request = new ProductRequest("카카오상품", 1000, "https://example.com/img.png", 1L);

        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 상품 생성 시 예외가 발생한다")
    void createProduct_categoryNotFound_throwsException() {
        ProductRequest request = new ProductRequest("MacBook", 1000000, "https://example.com/mac.png", 999L);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("유효한 요청으로 상품 수정 시 변경된 상품을 반환한다")
    void updateProduct_validRequest_returnsUpdatedProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product existing = new Product(1L, "OldName", 500000, "https://example.com/old.png", category);
        ProductRequest request = new ProductRequest("MacBook", 1000000, "https://example.com/mac.png", 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(1L)).willReturn(Optional.of(existing));
        given(productRepository.save(any())).willReturn(existing);

        ProductResponse result = productService.updateProduct(1L, request);

        assertThat(result.name()).isEqualTo("MacBook");
        assertThat(result.price()).isEqualTo(1000000);
    }

    @Test
    @DisplayName("유효하지 않은 상품명으로 수정 시 예외가 발생한다")
    void updateProduct_invalidName_throwsException() {
        ProductRequest request = new ProductRequest("카카오상품", 1000, "https://example.com/img.png", 1L);

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 상품 수정 시 예외가 발생한다")
    void updateProduct_categoryNotFound_throwsException() {
        ProductRequest request = new ProductRequest("MacBook", 1000000, "https://example.com/mac.png", 999L);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 시 예외가 발생한다")
    void updateProduct_productNotFound_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        ProductRequest request = new ProductRequest("MacBook", 1000000, "https://example.com/mac.png", 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("주문이 없는 상품은 정상 삭제된다")
    void deleteProduct_noOrders_deletesSuccessfully() {
        given(orderRepository.existsByOptionProductId(1L)).willReturn(false);

        productService.deleteProduct(1L);

        then(productRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("주문이 있는 상품 삭제 시 예외가 발생한다")
    void deleteProduct_hasOrders_throwsException() {
        given(orderRepository.existsByOptionProductId(1L)).willReturn(true);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
            .isInstanceOf(IllegalArgumentException.class);
        then(productRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("상품 삭제 시 연관된 찜이 먼저 삭제된다")
    void deleteProduct_withWishes_removesWishesFirst() {
        given(orderRepository.existsByOptionProductId(1L)).willReturn(false);

        productService.deleteProduct(1L);

        then(wishRepository).should().deleteByProductId(1L);
        then(productRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("관리자가 전체 상품 목록을 반환한다")
    void findAll_returnsAllProducts() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        List<Product> products = List.of(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category)
        );
        given(productRepository.findAll()).willReturn(products);

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("MacBook");
    }

    @Test
    @DisplayName("관리자가 ID로 상품 엔티티를 조회한다")
    void getProductEntity_existingId_returnsProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(productRepository.findById(1L)).willReturn(Optional.of(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category)
        ));

        Product result = productService.getProductEntity(1L);

        assertThat(result.getName()).isEqualTo("MacBook");
    }

    @Test
    @DisplayName("관리자가 존재하지 않는 ID로 상품 엔티티 조회 시 예외가 발생한다")
    void getProductEntity_nonExistingId_throwsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductEntity(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("관리자가 유효한 요청으로 상품을 생성한다")
    void adminCreateProduct_validRequest_savesProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        productService.adminCreateProduct("MacBook", 1000000, "https://example.com/img.png", 1L);

        then(productRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자는 카카오가 포함된 상품명으로 생성할 수 있다")
    void adminCreateProduct_kakaoName_savesProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        productService.adminCreateProduct("카카오 선물", 1000, "https://example.com/img.png", 1L);

        then(productRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자가 유효하지 않은 상품명으로 생성 시 예외가 발생한다")
    void adminCreateProduct_invalidName_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        assertThatThrownBy(() -> productService.adminCreateProduct("!@#invalid", 1000, "https://example.com/img.png", 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("관리자가 존재하지 않는 카테고리로 상품 생성 시 예외가 발생한다")
    void adminCreateProduct_categoryNotFound_throwsException() {
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.adminCreateProduct("MacBook", 1000000, "https://example.com/img.png", 999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("관리자가 유효한 요청으로 상품을 수정한다")
    void adminUpdateProduct_validRequest_updatesProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product existing = new Product(1L, "OldName", 500000, "https://example.com/old.png", category);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(1L)).willReturn(Optional.of(existing));

        productService.adminUpdateProduct(1L, "MacBook", 1000000, "https://example.com/mac.png", 1L);

        assertThat(existing.getName()).isEqualTo("MacBook");
        then(productRepository).should().save(existing);
    }

    @Test
    @DisplayName("관리자는 카카오가 포함된 상품명으로 수정할 수 있다")
    void adminUpdateProduct_kakaoName_updatesProduct() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product existing = new Product(1L, "OldName", 500000, "https://example.com/old.png", category);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(1L)).willReturn(Optional.of(existing));

        productService.adminUpdateProduct(1L, "카카오 선물", 1000, "https://example.com/img.png", 1L);

        assertThat(existing.getName()).isEqualTo("카카오 선물");
    }

    @Test
    @DisplayName("관리자가 유효하지 않은 상품명으로 수정 시 예외가 발생한다")
    void adminUpdateProduct_invalidName_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product existing = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(1L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.adminUpdateProduct(1L, "!@#invalid", 1000, "https://example.com/img.png", 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("관리자가 존재하지 않는 상품 수정 시 예외가 발생한다")
    void adminUpdateProduct_productNotFound_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.adminUpdateProduct(999L, "MacBook", 1000000, "https://example.com/img.png", 1L))
            .isInstanceOf(NotFoundException.class);
    }
}