package gift.wish;

import gift.category.Category;
import gift.product.Product;
import gift.product.ProductRepository;
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

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @Mock
    WishRepository wishRepository;

    @Mock
    ProductRepository productRepository;

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

    @Test
    @DisplayName("찜 추가 시 저장된 찜 항목을 반환한다")
    void addWish_newProduct_returnsSavedWish() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.empty());
        given(wishRepository.save(any())).willReturn(new Wish(1L, product));

        WishResponse result = wishService.addWish(1L, 1L);

        assertThat(result.name()).isEqualTo("MacBook");
        assertThat(result.productId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 찜한 상품은 기존 항목을 반환한다")
    void addWish_duplicate_returnsExisting() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Wish existing = new Wish(1L, product);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.of(existing));

        WishResponse result = wishService.addWish(1L, 1L);

        assertThat(result.name()).isEqualTo("MacBook");
        then(wishRepository).should().findByMemberIdAndProductId(1L, 1L);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 찜 추가 시 예외가 발생한다")
    void addWish_productNotFound_throwsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> wishService.addWish(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}