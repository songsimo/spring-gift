package gift.member;

import gift.auth.AuthService;
import gift.auth.AuthenticationResolver;
import gift.auth.TokenResponse;
import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.member.service.MemberResponse;
import gift.member.service.MemberService;
import gift.member.web.MemberController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberService memberService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    AuthenticationResolver authenticationResolver;

    @Test
    @DisplayName("유효한 토큰으로 내 정보 조회 시 200을 반환한다")
    void getMyInfo_validToken_returns200() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        given(memberService.getMyInfo("test@test.com")).willReturn(new MemberResponse("test@test.com", 0));

        mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 내 정보 조회 시 401을 반환한다")
    void getMyInfo_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 이메일과 비밀번호로 회원가입 시 201과 토큰을 반환한다")
    void register_validRequest_returns201WithToken() throws Exception {
        given(authService.register("test@test.com", "password")).willReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@test.com\", \"password\": \"password\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("이메일 형식이 잘못된 회원가입 요청 시 400을 반환한다")
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"not-an-email\", \"password\": \"password\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 없는 회원가입 요청 시 400을 반환한다")
    void register_missingPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@test.com\", \"password\": \"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 자격증명으로 로그인 시 200과 토큰을 반환한다")
    void login_validCredentials_returns200WithToken() throws Exception {
        given(authService.login("test@test.com", "password")).willReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@test.com\", \"password\": \"password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 400을 반환한다")
    void login_unknownEmail_returns400() throws Exception {
        given(authService.login("none@test.com", "password"))
            .willThrow(new IllegalArgumentException("Invalid email or password."));

        mockMvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"none@test.com\", \"password\": \"password\"}"))
            .andExpect(status().isBadRequest());
    }
}
