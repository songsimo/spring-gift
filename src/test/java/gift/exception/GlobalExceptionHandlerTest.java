package gift.exception;

import gift.category.web.CategoryController;
import gift.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    @Test
    @DisplayName("서비스에서 IllegalArgumentException 발생 시 400을 반환한다")
    void illegalArgumentException_returns400WithMessage() throws Exception {
        given(categoryService.update(anyLong(), any()))
            .willThrow(new IllegalArgumentException("잘못된 요청입니다."));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("잘못된 요청입니다."));
    }

    @Test
    @DisplayName("서비스에서 NotFoundException 발생 시 404를 반환한다")
    void notFoundException_returns404WithMessage() throws Exception {
        given(categoryService.update(anyLong(), any()))
            .willThrow(new NotFoundException("카테고리를 찾을 수 없습니다."));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("카테고리를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("서비스에서 DuplicateException 발생 시 409를 반환한다")
    void duplicateException_returns409WithMessage() throws Exception {
        given(categoryService.update(anyLong(), any()))
            .willThrow(new DuplicateException("이미 존재하는 카테고리입니다."));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isConflict())
            .andExpect(content().string("이미 존재하는 카테고리입니다."));
    }

    @Test
    @DisplayName("서비스에서 UnauthorizedException 발생 시 401을 반환한다")
    void unauthorizedException_returns401WithMessage() throws Exception {
        given(categoryService.update(anyLong(), any()))
            .willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("인증이 필요합니다."));
    }

    @Test
    @DisplayName("@Valid 검증 실패 시 400과 필드 오류 메시지를 반환한다")
    void validationFailure_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }

    @Test
    @DisplayName("정상 요청은 기존대로 처리된다")
    void normalRequest_processesNormally() throws Exception {
        given(categoryService.getAll()).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk());
    }
}
