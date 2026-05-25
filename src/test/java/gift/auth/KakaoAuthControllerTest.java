package gift.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KakaoAuthController.class)
class KakaoAuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KakaoAuthService kakaoAuthService;

    @Test
    @DisplayName("카카오 로그인 요청 시 카카오 인증 URL로 302 리다이렉트한다")
    void login_returns302WithKakaoAuthUrl() throws Exception {
        given(kakaoAuthService.getAuthorizationUrl())
            .willReturn("https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=test");

        mockMvc.perform(get("/api/auth/kakao/login"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location",
                "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=test"));
    }

    @Test
    @DisplayName("유효한 인가 코드로 콜백 시 200과 JWT 토큰을 반환한다")
    void callback_validCode_returns200WithToken() throws Exception {
        given(kakaoAuthService.processCallback("valid-code"))
            .willReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(get("/api/auth/kakao/callback")
                .param("code", "valid-code"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
