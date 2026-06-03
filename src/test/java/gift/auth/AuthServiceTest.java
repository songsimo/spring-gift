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
    void register_토큰을_반환한다() {
        var member = new Member("new@test.com", "pass");
        given(memberService.register(any())).willReturn(member);
        given(jwtProvider.createToken("new@test.com")).willReturn("test-token");

        TokenResponse result = authService.register(new MemberRequest("new@test.com", "pass"));

        assertThat(result.token()).isEqualTo("test-token");
    }

    @Test
    void login_토큰을_반환한다() {
        var member = new Member("user@test.com", "pass");
        given(memberService.login(any())).willReturn(member);
        given(jwtProvider.createToken("user@test.com")).willReturn("test-token");

        TokenResponse result = authService.login(new MemberRequest("user@test.com", "pass"));

        assertThat(result.token()).isEqualTo("test-token");
    }
}
