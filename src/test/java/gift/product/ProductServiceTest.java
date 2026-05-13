package gift.product;

import gift.category.Category;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

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
        assertThat(result.getContent().get(0).name()).isEqualTo("MacBook");
    }
}