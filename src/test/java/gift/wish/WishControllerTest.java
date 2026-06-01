package gift.wish;

import gift.auth.JwtProvider;
import gift.member.Member;
import gift.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishController.class)
class WishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishService wishService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private MemberRepository memberRepository;

    @Test
    void getWishes_Authorization_헤더가_없으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/wishes"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getWishes_유효한_토큰이면_200을_반환한다() throws Exception {
        given(jwtProvider.getEmail("valid-token")).willReturn("user@test.com");
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "pass")));
        given(wishService.getWishes(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .willReturn(Page.empty());

        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk());
    }
}
