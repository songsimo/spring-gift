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

    @Test
    @DisplayName("유효한 요청으로 옵션 생성 시 저장된 옵션을 반환한다")
    void createOption_validRequest_returnsCreatedOption() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        OptionRequest request = new OptionRequest("실버 256GB", 10);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.existsByProductIdAndName(1L, "실버 256GB")).willReturn(false);
        given(optionRepository.save(any())).willReturn(new Option(product, "실버 256GB", 10));

        OptionResponse result = optionService.createOption(1L, request);

        assertThat(result.name()).isEqualTo("실버 256GB");
        assertThat(result.quantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("유효하지 않은 옵션명으로 생성 시 예외가 발생한다")
    void createOption_invalidName_throwsException() {
        OptionRequest request = new OptionRequest("잘못된!@#옵션", 10);

        assertThatThrownBy(() -> optionService.createOption(1L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품에 옵션 생성 시 예외가 발생한다")
    void createOption_productNotFound_throwsException() {
        OptionRequest request = new OptionRequest("실버 256GB", 10);
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.createOption(999L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("중복된 옵션명으로 생성 시 예외가 발생한다")
    void createOption_duplicateName_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        OptionRequest request = new OptionRequest("실버 256GB", 10);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.existsByProductIdAndName(1L, "실버 256GB")).willReturn(true);

        assertThatThrownBy(() -> optionService.createOption(1L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}