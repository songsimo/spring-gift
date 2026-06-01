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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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
}
