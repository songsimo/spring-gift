package gift.auth;

import gift.exception.DuplicateException;
import gift.member.model.Member;
import gift.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberService memberService;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("신규 이메일로 회원가입 시 JWT 토큰을 반환한다")
    void register_newEmail_returnsToken() {
        Member member = new Member("test@test.com", "hashed-password");
        given(memberService.registerMember("test@test.com", "password")).willReturn(member);
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = authService.register("test@test.com", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외가 전파된다")
    void register_duplicateEmail_throwsException() {
        given(memberService.registerMember("dup@test.com", "password"))
            .willThrow(new DuplicateException("이미 가입된 이메일입니다."));

        assertThatThrownBy(() -> authService.register("dup@test.com", "password"))
            .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인 시 JWT 토큰을 반환한다")
    void login_validCredentials_returnsToken() {
        Member member = new Member("test@test.com", "hashed-password");
        given(memberService.authenticate("test@test.com", "password")).willReturn(member);
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = authService.login("test@test.com", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 전파된다")
    void login_emailNotFound_throwsException() {
        given(memberService.authenticate("none@test.com", "password"))
            .willThrow(new IllegalArgumentException("Invalid email or password."));

        assertThatThrownBy(() -> authService.login("none@test.com", "password"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 시 예외가 전파된다")
    void login_wrongPassword_throwsException() {
        given(memberService.authenticate("test@test.com", "wrong"))
            .willThrow(new IllegalArgumentException("Invalid email or password."));

        assertThatThrownBy(() -> authService.login("test@test.com", "wrong"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
