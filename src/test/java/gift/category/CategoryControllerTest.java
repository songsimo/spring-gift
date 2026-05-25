package gift.category;

import gift.category.service.CategoryRequest;
import gift.category.service.CategoryResponse;
import gift.category.service.CategoryService;
import gift.category.web.CategoryController;
import gift.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    private static final String VALID_BODY =
        "{\"name\":\"전자기기\",\"color\":\"#1E90FF\",\"imageUrl\":\"https://example.com/img.png\",\"description\":\"전자제품\"}";

    @Test
    @DisplayName("카테고리 목록 조회 시 200을 반환한다")
    void getCategories_returns200() throws Exception {
        given(categoryService.getAll()).willReturn(List.of(
            new CategoryResponse(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품")
        ));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("전자기기"));
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 201을 반환한다")
    void createCategory_validRequest_returns201() throws Exception {
        given(categoryService.create(any(CategoryRequest.class)))
            .willReturn(new CategoryResponse(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("전자기기"));
    }

    @Test
    @DisplayName("카테고리명이 빈 문자열이면 400을 반환한다")
    void createCategory_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"color\":\"#1E90FF\",\"imageUrl\":\"https://example.com/img.png\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 수정 시 200을 반환한다")
    void updateCategory_validRequest_returns200() throws Exception {
        given(categoryService.update(anyLong(), any(CategoryRequest.class)))
            .willReturn(new CategoryResponse(1L, "가전제품", "#1E90FF", "https://example.com/img.png", ""));

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("가전제품"));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 404를 반환한다")
    void updateCategory_notFound_returns404() throws Exception {
        given(categoryService.update(anyLong(), any(CategoryRequest.class)))
            .willThrow(new NotFoundException("카테고리를 찾을 수 없습니다."));

        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("카테고리 삭제 시 204를 반환한다")
    void deleteCategory_returns204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("상품이 있는 카테고리 삭제 시 409를 반환한다")
    void deleteCategory_hasProducts_returns409() throws Exception {
        willThrow(new gift.exception.ConflictException("카테고리에 속한 상품이 있어 삭제할 수 없습니다."))
            .given(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isConflict());
    }
}
