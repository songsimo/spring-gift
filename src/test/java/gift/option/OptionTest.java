package gift.option;

import gift.category.Category;
import gift.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    private Option option;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        option = new Option(product, "실버 256GB", 10);
    }

    @Test
    @DisplayName("유효한 수량 차감 시 재고가 줄어든다")
    void subtractQuantity_validAmount_reducesQuantity() {
        option.subtractQuantity(3);

        assertThat(option.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고보다 많은 수량 차감 시 예외가 발생한다")
    void subtractQuantity_exceedsStock_throwsException() {
        assertThatThrownBy(() -> option.subtractQuantity(11))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("0 수량 차감 시 예외가 발생한다")
    void subtractQuantity_zeroAmount_throwsException() {
        assertThatThrownBy(() -> option.subtractQuantity(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("음수 수량 차감 시 예외가 발생한다")
    void subtractQuantity_negativeAmount_throwsException() {
        assertThatThrownBy(() -> option.subtractQuantity(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
