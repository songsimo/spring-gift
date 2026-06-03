package gift.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    class BusinessException_처리 {

        @Test
        void NotFoundException은_404를_반환한다() {
            var response = handler.handleBusiness(new NotFoundException("not found"));
            assertThat(response.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        void DuplicateException은_409를_반환한다() {
            var response = handler.handleBusiness(new DuplicateException("duplicate"));
            assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        }

        @Test
        void ConflictException은_409를_반환한다() {
            var response = handler.handleBusiness(new ConflictException("conflict"));
            assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        }

        @Test
        void ForbiddenException은_403을_반환한다() {
            var response = handler.handleBusiness(new ForbiddenException("forbidden"));
            assertThat(response.getStatusCode().value()).isEqualTo(403);
        }

        @Test
        void UnauthorizedException은_401을_반환한다() {
            var response = handler.handleBusiness(new UnauthorizedException("unauthorized"));
            assertThat(response.getStatusCode().value()).isEqualTo(401);
        }
    }

    @Nested
    class BadRequest_처리 {

        @Test
        void BadRequestException은_400과_메시지를_반환한다() {
            var response = handler.handleBadRequest(new BadRequestException("bad input"));
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isEqualTo("bad input");
        }

        @Test
        void IllegalArgumentException은_400과_메시지를_반환한다() {
            var response = handler.handleIllegalArgument(new IllegalArgumentException("invalid"));
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isEqualTo("invalid");
        }
    }
}
