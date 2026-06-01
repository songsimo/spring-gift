package gift.exception;

import gift.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new NotFoundException("not found"));
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void handleDuplicate_returns409() {
        var response = handler.handleDuplicate(new DuplicateException("duplicate"));
        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleConflict_returns409() {
        var response = handler.handleConflict(new ConflictException("conflict"));
        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleForbidden_returns403() {
        var response = handler.handleForbidden(new ForbiddenException("forbidden"));
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void handleBadRequest_returns400_withMessage() {
        var response = handler.handleBadRequest(new BadRequestException("bad input"));
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("bad input");
    }

    @Test
    void handleUnauthorized_returns401() {
        var response = handler.handleUnauthorized(new UnauthorizedException("unauthorized"));
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void handleIllegalArgument_returns400_withMessage() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("invalid"));
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("invalid");
    }
}
