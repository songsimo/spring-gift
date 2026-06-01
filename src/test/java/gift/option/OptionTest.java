package gift.option;

import gift.category.Category;
import gift.exception.BadRequestException;
import gift.product.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    private Option sampleOption(int quantity) {
        var product = new Product("사과", 1000, "apple.png",
            new Category("식품", "#fff", "img.png", "desc"));
        return new Option(product, "대", quantity);
    }

    @Test
    void subtractQuantity_재고보다_많이_차감하면_BadRequestException을_던진다() {
        var option = sampleOption(10);

        assertThatThrownBy(() -> option.subtractQuantity(11))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void subtractQuantity_0이하_수량은_BadRequestException을_던진다() {
        var option = sampleOption(10);

        assertThatThrownBy(() -> option.subtractQuantity(0))
            .isInstanceOf(BadRequestException.class);
    }
}
