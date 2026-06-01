package gift.wish;

import gift.category.Category;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishService wishService;

    private Product sampleProduct() {
        return new Product("사과", 1000, "apple.png",
            new Category("식품", "#fff", "img.png", "desc"));
    }

    @Test
    void getWishes_위시_목록을_반환한다() {
        var product = sampleProduct();
        given(wishRepository.findByMemberId(any(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(new Wish(1L, product))));

        var result = wishService.getWishes(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }
}
