package gift.wish;

import gift.TestFixture;
import gift.exception.ForbiddenException;
import gift.exception.NotFoundException;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Nested
    class 위시리스트_추가 {

        @Test
        void 위시리스트에_상품이_추가된다() {
            var product = TestFixture.sampleProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(wishRepository.findByMemberIdAndProductId(1L, product.getId())).willReturn(Optional.empty());
            given(wishRepository.save(any())).willReturn(new Wish(1L, product));

            WishResponse result = wishService.addWish(1L, 1L);

            assertThat(result.name()).isEqualTo("사과");
        }

        @Test
        void 이미_추가된_상품은_중복_추가되지_않는다() {
            var product = TestFixture.sampleProduct();
            var existing = new Wish(1L, product);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(wishRepository.findByMemberIdAndProductId(1L, product.getId())).willReturn(Optional.of(existing));

            WishResponse result = wishService.addWish(1L, 1L);

            assertThat(result.name()).isEqualTo("사과");
        }

        @Test
        void 존재하지_않는_상품은_위시리스트에_추가할_수_없다() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> wishService.addWish(1L, 99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class 위시리스트_삭제 {

        @Test
        void 위시리스트에서_상품이_제거된다() {
            var product = TestFixture.sampleProduct();
            var wish = new Wish(1L, product);
            given(wishRepository.findById(1L)).willReturn(Optional.of(wish));

            wishService.removeWish(1L, 1L);

            org.mockito.Mockito.verify(wishRepository).delete(wish);
        }

        @Test
        void 존재하지_않는_항목은_삭제할_수_없다() {
            given(wishRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> wishService.removeWish(1L, 99L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 다른_회원의_위시리스트는_수정할_수_없다() {
            var product = TestFixture.sampleProduct();
            var wish = new Wish(2L, product);
            given(wishRepository.findById(1L)).willReturn(Optional.of(wish));

            assertThatThrownBy(() -> wishService.removeWish(1L, 1L))
                .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    class 위시리스트_조회 {

        @Test
        void 위시_목록을_조회한다() {
            var product = TestFixture.sampleProduct();
            given(wishRepository.findByMemberId(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(new Wish(1L, product))));

            var result = wishService.getWishes(1L, Pageable.unpaged());

            assertThat(result.getContent()).hasSize(1);
        }
    }
}
