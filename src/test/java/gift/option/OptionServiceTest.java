package gift.option;

import gift.category.Category;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    @Mock
    OptionRepository optionRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OptionService optionService;

    @Test
    @DisplayName("존재하는 상품의 옵션 목록 조회 시 해당 옵션들을 반환한다")
    void getOptions_existingProductId_returnsOptions() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductId(1L)).willReturn(List.of(
            new Option(product, "실버 256GB", 10),
            new Option(product, "스페이스그레이 512GB", 5)
        ));

        List<OptionResponse> result = optionService.getOptions(1L);

        assertThat(result).hasSize(2)
            .extracting("name")
            .containsExactly("실버 256GB", "스페이스그레이 512GB");
    }

    @Test
    @DisplayName("존재하지 않는 상품의 옵션 조회 시 예외가 발생한다")
    void getOptions_nonExistingProductId_throwsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.getOptions(999L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}