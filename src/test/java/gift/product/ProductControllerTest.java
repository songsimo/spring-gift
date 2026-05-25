package gift.product;

import gift.exception.NotFoundException;
import gift.product.service.ProductRequest;
import gift.product.service.ProductResponse;
import gift.product.service.ProductService;
import gift.product.web.ProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    private static final ProductResponse SAMPLE =
        new ProductResponse(1L, "MacBook", 1000000, "https://example.com/mac.png", 1L, "전자기기");

    private static final String VALID_BODY =
        "{\"name\":\"MacBook\",\"price\":1000000,\"imageUrl\":\"https://example.com/mac.png\",\"categoryId\":1}";

    @Test
    @DisplayName("상품 목록 조회 시 200을 반환한다")
    void getProducts_returns200() throws Exception {
        given(productService.getProducts(any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(SAMPLE)));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("MacBook"));
    }

    @Test
    @DisplayName("존재하는 상품 단건 조회 시 200을 반환한다")
    void getProduct_existingId_returns200() throws Exception {
        given(productService.getProduct(1L)).willReturn(SAMPLE);

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("MacBook"))
            .andExpect(jsonPath("$.price").value(1000000));
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 404를 반환한다")
    void getProduct_notFound_returns404() throws Exception {
        given(productService.getProduct(999L))
            .willThrow(new NotFoundException("상품을 찾을 수 없습니다. id=999"));

        mockMvc.perform(get("/api/products/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유효한 요청으로 상품 생성 시 201을 반환한다")
    void createProduct_validRequest_returns201() throws Exception {
        given(productService.createProduct(any(ProductRequest.class))).willReturn(SAMPLE);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("MacBook"));
    }

    @Test
    @DisplayName("상품명이 빈 문자열이면 400을 반환한다")
    void createProduct_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"price\":1000,\"imageUrl\":\"https://example.com/img.png\",\"categoryId\":1}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("가격이 0 이하면 400을 반환한다")
    void createProduct_nonPositivePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"MacBook\",\"price\":0,\"imageUrl\":\"https://example.com/img.png\",\"categoryId\":1}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 요청으로 상품 수정 시 200을 반환한다")
    void updateProduct_validRequest_returns200() throws Exception {
        given(productService.updateProduct(anyLong(), any(ProductRequest.class))).willReturn(SAMPLE);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 삭제 시 204를 반환한다")
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("주문이 있는 상품 삭제 시 409를 반환한다")
    void deleteProduct_hasOrders_returns409() throws Exception {
        willThrow(new gift.exception.ConflictException("주문이 있는 상품은 삭제할 수 없습니다."))
            .given(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isConflict());
    }
}
