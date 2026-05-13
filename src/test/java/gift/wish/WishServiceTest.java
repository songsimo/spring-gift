package gift.wish;

import gift.category.Category;
import gift.product.Product;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @Mock
    WishRepository wishRepository;

    @InjectMocks
    WishService wishService;

    @Test
    @DisplayName("찜 목록 조회 시 해당 회원의 찜 목록을 페이지로 반환한다")
    void getWishes_returnsPaginatedWishes() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Page<Wish> page = new PageImpl<>(List.of(new Wish(1L, product)));
        given(wishRepository.findByMemberId(1L, Pageable.unpaged())).willReturn(page);

        Page<WishResponse> result = wishService.getWishes(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("MacBook");
    }
}