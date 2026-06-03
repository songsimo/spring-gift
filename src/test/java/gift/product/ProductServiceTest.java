package gift.product;

import gift.category.Category;
import gift.category.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import gift.exception.BadRequestException;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.order.OrderRepository;
import gift.wish.WishRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WishRepository wishRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_상품이_등록된다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var request = new ProductRequest("사과", 1000, "apple.png", 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any())).willReturn(new Product("사과", 1000, "apple.png", category));

        ProductResponse result = productService.create(request);

        assertThat(result.name()).isEqualTo("사과");
    }

    @Test
    void create_존재하지_않는_카테고리에는_상품을_등록할_수_없다() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(new ProductRequest("사과", 1000, "apple.png", 99L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_유효하지_않은_상품명은_등록할_수_없다() {
        assertThatThrownBy(() -> productService.create(new ProductRequest("카카오상품", 1000, "img.png", 1L)))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void update_상품_정보가_수정된다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var product = new Product("사과", 1000, "apple.png", category);
        var request = new ProductRequest("배", 2000, "pear.png", 1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any())).willReturn(new Product("배", 2000, "pear.png", category));

        ProductResponse result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("배");
    }

    @Test
    void update_존재하지_않는_상품은_수정할_수_없다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, new ProductRequest("배", 2000, "pear.png", 1L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_존재하지_않는_카테고리로는_변경할_수_없다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        given(productRepository.findById(1L)).willReturn(Optional.of(new Product("사과", 1000, "apple.png", category)));
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, new ProductRequest("배", 2000, "pear.png", 99L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_상품을_조회한다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        given(productRepository.findById(1L))
            .willReturn(Optional.of(new Product("사과", 1000, "apple.png", category)));

        ProductResponse result = productService.getById(1L);

        assertThat(result.name()).isEqualTo("사과");
    }

    @Test
    void getById_존재하지_않는_상품은_조회할_수_없다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_상품이_삭제된다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var product = new Product("사과", 1000, "apple.png", category);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(orderRepository.existsByOptionProductId(1L)).willReturn(false);

        productService.delete(1L);

        org.mockito.Mockito.verify(wishRepository).deleteAllByProductId(1L);
        org.mockito.Mockito.verify(productRepository).delete(product);
    }

    @Test
    void delete_주문_이력이_있는_상품은_삭제할_수_없다() {
        given(orderRepository.existsByOptionProductId(1L)).willReturn(true);

        assertThatThrownBy(() -> productService.delete(1L))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void getAll_상품_목록을_조회한다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var product = new Product("사과", 1000, "apple.png", category);
        given(productRepository.findAll(any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("사과");
    }
}
