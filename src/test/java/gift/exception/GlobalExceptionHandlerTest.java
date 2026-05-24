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
            .willThrow(new IllegalArgumentException("Category not found: 999"));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\",\"color\":\"#FF0000\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Category not found: 999"));
    }

    @Test
    @DisplayName("정상 요청은 기존대로 처리된다")
    void normalRequest_processesNormally() throws Exception {
        given(categoryService.getAll()).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk());
    }
}
