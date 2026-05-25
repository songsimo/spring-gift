package gift.option;

import gift.exception.NotFoundException;
import gift.option.service.OptionRequest;
import gift.option.service.OptionResponse;
import gift.option.service.OptionService;
import gift.option.web.OptionController;
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

@WebMvcTest(OptionController.class)
class OptionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OptionService optionService;

    @Test
    @DisplayName("상품의 옵션 목록을 조회하면 200을 반환한다")
    void getOptions_existingProduct_returns200() throws Exception {
        given(optionService.getOptions(1L)).willReturn(List.of(
            new OptionResponse(1L, "실버 256GB", 10),
            new OptionResponse(2L, "스페이스그레이 512GB", 5)
        ));

        mockMvc.perform(get("/api/products/1/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("실버 256GB"))
            .andExpect(jsonPath("$[1].name").value("스페이스그레이 512GB"));
    }

    @Test
    @DisplayName("존재하지 않는 상품의 옵션 조회 시 404를 반환한다")
    void getOptions_nonExistingProduct_returns404() throws Exception {
        given(optionService.getOptions(999L)).willThrow(new NotFoundException("상품을 찾을 수 없습니다. id=999"));

        mockMvc.perform(get("/api/products/999/options"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유효한 요청으로 옵션 생성 시 201을 반환한다")
    void createOption_validRequest_returns201() throws Exception {
        given(optionService.createOption(anyLong(), any(OptionRequest.class)))
            .willReturn(new OptionResponse(1L, "실버 256GB", 10));

        mockMvc.perform(post("/api/products/1/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"실버 256GB\", \"quantity\": 10}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("실버 256GB"))
            .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @DisplayName("옵션명이 빈 문자열이면 400을 반환한다")
    void createOption_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/products/1/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"quantity\": 10}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수량이 0이면 400을 반환한다")
    void createOption_zeroQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/products/1/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"실버 256GB\", \"quantity\": 0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 요청으로 옵션 수정 시 200을 반환한다")
    void updateOption_validRequest_returns200() throws Exception {
        given(optionService.updateOption(anyLong(), anyLong(), any(OptionRequest.class)))
            .willReturn(new OptionResponse(1L, "골드 512GB", 20));

        mockMvc.perform(put("/api/products/1/options/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"골드 512GB\", \"quantity\": 20}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("골드 512GB"));
    }

    @Test
    @DisplayName("옵션 삭제 시 204를 반환한다")
    void deleteOption_validRequest_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1/options/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("마지막 옵션 삭제 시 409를 반환한다")
    void deleteOption_lastOption_returns409() throws Exception {
        willThrow(new gift.exception.ConflictException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다."))
            .given(optionService).deleteOption(1L, 1L);

        mockMvc.perform(delete("/api/products/1/options/1"))
            .andExpect(status().isConflict());
    }
}
