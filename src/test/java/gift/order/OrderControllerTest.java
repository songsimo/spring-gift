package gift.order;

import gift.auth.JwtProvider;
import gift.member.Member;
import gift.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private MemberRepository memberRepository;

    @Test
    void getOrders_Authorization_헤더가_없으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrders_유효한_토큰이면_200을_반환한다() throws Exception {
        given(jwtProvider.getEmail("valid-token")).willReturn("user@test.com");
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "pass")));
        given(orderService.getOrders(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .willReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk());
    }
}
