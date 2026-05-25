package gift.order;

import gift.auth.AuthenticationResolver;
import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.order.service.OrderResponse;
import gift.order.service.OrderService;
import gift.order.web.OrderController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @MockitoBean
    AuthenticationResolver authenticationResolver;

    private static final OrderResponse SAMPLE_ORDER = new OrderResponse(
        1L, 10L, "MacBook", "실버 256GB", 2, 2000000,
        LocalDateTime.of(2026, 5, 25, 12, 0), "선물이에요", false
    );

    @Test
    @DisplayName("유효한 토큰으로 주문 목록 조회 시 200을 반환한다")
    void getOrders_validToken_returns200() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        given(orderService.getOrders(anyLong(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(SAMPLE_ORDER)));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].productName").value("MacBook"))
            .andExpect(jsonPath("$.content[0].quantity").value(2));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 주문 목록 조회 시 401을 반환한다")
    void getOrders_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 토큰으로 주문 생성 시 201을 반환한다")
    void createOrder_validToken_returns201() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        given(orderService.createOrder(anyLong(), any())).willReturn(SAMPLE_ORDER);

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionId\": 10, \"quantity\": 2, \"message\": \"선물이에요\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.productName").value("MacBook"))
            .andExpect(jsonPath("$.totalPrice").value(2000000));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 주문 생성 시 401을 반환한다")
    void createOrder_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer bad-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionId\": 10, \"quantity\": 2, \"message\": null}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("주문 수량이 0이면 400을 반환한다")
    void createOrder_zeroQuantity_returns400() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionId\": 10, \"quantity\": 0, \"message\": null}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("optionId가 없으면 400을 반환한다")
    void createOrder_missingOptionId_returns400() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\": 2, \"message\": null}"))
            .andExpect(status().isBadRequest());
    }
}
