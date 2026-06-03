package gift.option;

import gift.TestFixture;
import gift.exception.BadRequestException;
import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OptionService optionService;

    @Nested
    class 옵션_조회 {

        @Test
        void 상품의_옵션_목록을_조회한다() {
            var product = TestFixture.sampleProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.findByProductId(1L))
                .willReturn(List.of(new Option(product, "대", 100)));

            List<OptionResponse> result = optionService.getOptions(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("대");
        }

        @Test
        void 존재하지_않는_상품의_옵션은_조회할_수_없다() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> optionService.getOptions(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class 옵션_등록 {

        @Test
        void 옵션이_등록된다() {
            var product = TestFixture.sampleProduct();
            var request = new OptionRequest("대", 100);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.existsByProductIdAndName(1L, "대")).willReturn(false);
            given(optionRepository.save(any())).willReturn(new Option(product, "대", 100));

            OptionResponse result = optionService.create(1L, request);

            assertThat(result.name()).isEqualTo("대");
        }

        @Test
        void 존재하지_않는_상품에는_옵션을_추가할_수_없다() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> optionService.create(99L, new OptionRequest("대", 100)))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 같은_상품에_중복된_옵션명은_등록할_수_없다() {
            var product = TestFixture.sampleProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.existsByProductIdAndName(1L, "대")).willReturn(true);

            assertThatThrownBy(() -> optionService.create(1L, new OptionRequest("대", 100)))
                .isInstanceOf(DuplicateException.class);
        }

        @Test
        void 유효하지_않은_옵션명은_등록할_수_없다() {
            assertThatThrownBy(() -> optionService.create(1L, new OptionRequest("", 100)))
                .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    class 옵션_삭제 {

        @Test
        void 옵션이_삭제된다() {
            var product = mock(Product.class);
            given(product.getId()).willReturn(1L);
            var option = new Option(product, "대", 100);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.findByProductId(1L)).willReturn(List.of(option, new Option(product, "소", 50)));
            given(optionRepository.findById(1L)).willReturn(Optional.of(option));

            optionService.delete(1L, 1L);

            org.mockito.Mockito.verify(optionRepository).delete(option);
        }

        @Test
        void 마지막_옵션은_삭제할_수_없다() {
            var product = TestFixture.sampleProduct();
            var option = new Option(product, "대", 100);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.findByProductId(1L)).willReturn(List.of(option));

            assertThatThrownBy(() -> optionService.delete(1L, 1L))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        void 존재하지_않는_상품의_옵션은_삭제할_수_없다() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> optionService.delete(99L, 1L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 존재하지_않는_옵션은_삭제할_수_없다() {
            var product = TestFixture.sampleProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(optionRepository.findByProductId(1L))
                .willReturn(List.of(new Option(product, "대", 100), new Option(product, "소", 50)));
            given(optionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> optionService.delete(1L, 99L))
                .isInstanceOf(NotFoundException.class);
        }
    }
}
