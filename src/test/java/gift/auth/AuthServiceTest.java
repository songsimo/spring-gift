package gift.auth;

import gift.member.Member;
import gift.member.MemberRequest;
import gift.member.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_회원가입_후_인증_토큰이_발급된다() {
        var member = new Member("new@test.com", "pass");
        given(memberService.register(any())).willReturn(member);
        given(jwtProvider.createToken("new@test.com")).willReturn("test-token");

        TokenResponse result = authService.register(new MemberRequest("new@test.com", "pass"));

        assertThat(result.token()).isEqualTo("test-token");
    }

    @Test
    void login_로그인_후_인증_토큰이_발급된다() {
        var member = new Member("user@test.com", "pass");
        given(memberService.login(any())).willReturn(member);
        given(jwtProvider.createToken("user@test.com")).willReturn("test-token");

        TokenResponse result = authService.login(new MemberRequest("user@test.com", "pass"));

        assertThat(result.token()).isEqualTo("test-token");
    }
}
