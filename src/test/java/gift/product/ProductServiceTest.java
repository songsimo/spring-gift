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

import gift.exception.NotFoundException;

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

    @InjectMocks
    private ProductService productService;

    @Test
    void create_상품을_저장하고_반환한다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var request = new ProductRequest("사과", 1000, "apple.png", 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any())).willReturn(new Product("사과", 1000, "apple.png", category));

        ProductResponse result = productService.create(request);

        assertThat(result.name()).isEqualTo("사과");
    }

    @Test
    void create_존재하지_않는_카테고리는_NotFoundException을_던진다() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(new ProductRequest("사과", 1000, "apple.png", 99L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_유효하지_않은_상품명은_IllegalArgumentException을_던진다() {
        assertThatThrownBy(() -> productService.create(new ProductRequest("카카오상품", 1000, "img.png", 1L)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_상품을_수정하고_반환한다() {
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
    void update_존재하지_않는_상품은_NotFoundException을_던진다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, new ProductRequest("배", 2000, "pear.png", 1L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_존재하지_않는_카테고리는_NotFoundException을_던진다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        given(productRepository.findById(1L)).willReturn(Optional.of(new Product("사과", 1000, "apple.png", category)));
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, new ProductRequest("배", 2000, "pear.png", 99L)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_존재하는_상품을_반환한다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        given(productRepository.findById(1L))
            .willReturn(Optional.of(new Product("사과", 1000, "apple.png", category)));

        ProductResponse result = productService.getById(1L);

        assertThat(result.name()).isEqualTo("사과");
    }

    @Test
    void getById_존재하지_않는_상품은_NotFoundException을_던진다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_상품_목록을_반환한다() {
        var category = new Category("식품", "#fff", "img.png", "desc");
        var product = new Product("사과", 1000, "apple.png", category);
        given(productRepository.findAll(any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("사과");
    }
}
