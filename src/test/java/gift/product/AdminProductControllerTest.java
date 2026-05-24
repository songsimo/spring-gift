package gift.product;

import gift.category.service.CategoryResponse;
import gift.category.service.CategoryService;
import gift.product.model.Product;
import gift.product.service.ProductService;
import gift.product.web.AdminProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    CategoryService categoryService;

    @Test
    @DisplayName("상품 목록 페이지를 반환한다")
    void list_returnsProductListView() throws Exception {
        given(productService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/admin/products"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/list"))
            .andExpect(model().attributeExists("products"));
    }

    @Test
    @DisplayName("상품 추가 폼을 반환한다")
    void newForm_returnsNewFormView() throws Exception {
        given(categoryService.getAll()).willReturn(List.of());

        mockMvc.perform(get("/admin/products/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/new"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("유효한 상품 생성 요청 시 목록 페이지로 리다이렉트한다")
    void create_validRequest_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/products")
                .param("name", "MacBook")
                .param("price", "1000000")
                .param("imageUrl", "https://example.com/img.png")
                .param("categoryId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("유효하지 않은 상품명으로 생성 시 에러와 함께 new 폼을 반환한다")
    void create_invalidName_returnsNewFormWithErrors() throws Exception {
        given(categoryService.getAll()).willReturn(List.of(
            new CategoryResponse(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품")
        ));
        willThrow(new IllegalArgumentException("허용되지 않는 특수 문자"))
            .given(productService).adminCreateProduct(anyString(), anyInt(), anyString(), anyLong());

        mockMvc.perform(post("/admin/products")
                .param("name", "!invalid!")
                .param("price", "1000")
                .param("imageUrl", "https://example.com/img.png")
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/new"))
            .andExpect(model().attributeExists("errors"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("상품 수정 폼을 반환한다")
    void editForm_returnsEditFormView() throws Exception {
        given(productService.getProductEntity(1L)).willReturn(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png",
                new gift.category.model.Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품"))
        );
        given(categoryService.getAll()).willReturn(List.of());

        mockMvc.perform(get("/admin/products/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/edit"))
            .andExpect(model().attributeExists("product"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("유효한 상품 수정 요청 시 목록 페이지로 리다이렉트한다")
    void update_validRequest_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/products/1/edit")
                .param("name", "MacBook Pro")
                .param("price", "2000000")
                .param("imageUrl", "https://example.com/new.png")
                .param("categoryId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("유효하지 않은 상품명으로 수정 시 에러와 함께 edit 폼을 반환한다")
    void update_invalidName_returnsEditFormWithErrors() throws Exception {
        given(productService.getProductEntity(1L)).willReturn(
            new Product(1L, "MacBook", 1000000, "https://example.com/mac.png",
                new gift.category.model.Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품"))
        );
        given(categoryService.getAll()).willReturn(List.of());
        willThrow(new IllegalArgumentException("허용되지 않는 특수 문자"))
            .given(productService).adminUpdateProduct(anyLong(), anyString(), anyInt(), anyString(), anyLong());

        mockMvc.perform(post("/admin/products/1/edit")
                .param("name", "!invalid!")
                .param("price", "1000")
                .param("imageUrl", "https://example.com/img.png")
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/edit"))
            .andExpect(model().attributeExists("errors"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("상품 삭제 요청 시 목록 페이지로 리다이렉트한다")
    void delete_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/products/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }
}
