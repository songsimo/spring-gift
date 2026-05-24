package gift.order;

import gift.order.service.OrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("message가 null이면 검증을 통과한다")
    void message_null_passesValidation() {
        OrderRequest request = new OrderRequest(1L, 1, null);

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("message가 255자 이하이면 검증을 통과한다")
    void message_maxLength_passesValidation() {
        OrderRequest request = new OrderRequest(1L, 1, "a".repeat(255));

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("message가 255자를 초과하면 검증에 실패한다")
    void message_tooLong_failsValidation() {
        OrderRequest request = new OrderRequest(1L, 1, "a".repeat(256));

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
