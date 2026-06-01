package gift.option;

import gift.category.Category;
import gift.exception.NotFoundException;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import gift.product.Product;

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

    private Product sampleProduct() {
        return new Product("사과", 1000, "apple.png", new Category("식품", "#fff", "img.png", "desc"));
    }

    @Test
    void getOptions_상품의_옵션_목록을_반환한다() {
        var product = sampleProduct();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductId(1L))
            .willReturn(List.of(new Option(product, "대", 100)));

        List<OptionResponse> result = optionService.getOptions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("대");
    }

    @Test
    void getOptions_존재하지_않는_상품은_NotFoundException을_던진다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.getOptions(99L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_옵션을_저장하고_반환한다() {
        var product = sampleProduct();
        var request = new OptionRequest("대", 100);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.existsByProductIdAndName(1L, "대")).willReturn(false);
        given(optionRepository.save(any())).willReturn(new Option(product, "대", 100));

        OptionResponse result = optionService.create(1L, request);

        assertThat(result.name()).isEqualTo("대");
    }

    @Test
    void create_존재하지_않는_상품은_NotFoundException을_던진다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.create(99L, new OptionRequest("대", 100)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_중복_옵션명은_IllegalArgumentException을_던진다() {
        var product = sampleProduct();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.existsByProductIdAndName(1L, "대")).willReturn(true);

        assertThatThrownBy(() -> optionService.create(1L, new OptionRequest("대", 100)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_유효하지_않은_옵션명은_IllegalArgumentException을_던진다() {
        assertThatThrownBy(() -> optionService.create(1L, new OptionRequest("", 100)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_옵션을_삭제한다() {
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
    void delete_상품에_옵션이_1개면_IllegalArgumentException을_던진다() {
        var product = sampleProduct();
        var option = new Option(product, "대", 100);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductId(1L)).willReturn(List.of(option));

        assertThatThrownBy(() -> optionService.delete(1L, 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_존재하지_않는_상품은_NotFoundException을_던진다() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.delete(99L, 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_존재하지_않는_옵션은_NotFoundException을_던진다() {
        var product = sampleProduct();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductId(1L))
            .willReturn(List.of(new Option(product, "대", 100), new Option(product, "소", 50)));
        given(optionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.delete(1L, 99L))
            .isInstanceOf(NotFoundException.class);
    }
}
