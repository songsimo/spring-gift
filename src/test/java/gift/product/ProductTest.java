package gift.product;

import gift.category.model.Category;
import gift.product.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProductTest {

    private final Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");

    @Test
    @DisplayName("정상 이름으로 상품을 생성한다")
    void create_validName_succeeds() {
        assertThatCode(() -> new Product("MacBook", 1000000, "https://example.com/img.png", category))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void create_nullName_throwsException() {
        assertThatThrownBy(() -> new Product(null, 1000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이름이 공백이면 예외가 발생한다")
    void create_blankName_throwsException() {
        assertThatThrownBy(() -> new Product("   ", 1000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이름이 15자를 초과하면 예외가 발생한다")
    void create_nameTooLong_throwsException() {
        assertThatThrownBy(() -> new Product("가나다라마바사아자차카타파하가나", 1000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("허용되지 않는 특수문자가 포함되면 예외가 발생한다")
    void create_invalidCharacter_throwsException() {
        assertThatThrownBy(() -> new Product("Product@Name", 1000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("15자 이름으로 상품을 생성한다")
    void create_exactly15chars_succeeds() {
        assertThatCode(() -> new Product("가나다라마바사아자차카타파하가", 1000000, "https://example.com/img.png", category))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 시 이름이 15자를 초과하면 예외가 발생한다")
    void update_nameTooLong_throwsException() {
        Product product = new Product("MacBook", 1000000, "https://example.com/img.png", category);
        assertThatThrownBy(() -> product.update("가나다라마바사아자차카타파하가나", 2000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수정 시 허용되지 않는 특수문자가 포함되면 예외가 발생한다")
    void update_invalidCharacter_throwsException() {
        Product product = new Product("MacBook", 1000000, "https://example.com/img.png", category);
        assertThatThrownBy(() -> product.update("Mac@Book", 2000000, "https://example.com/img.png", category))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
