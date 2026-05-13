package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("신규 이메일로 회원가입 시 토큰을 반환한다")
    void register_newEmail_returnsToken() {
        given(memberRepository.existsByEmail("test@test.com")).willReturn(false);
        given(memberRepository.save(any())).willReturn(new Member("test@test.com", "password"));
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = memberService.register("test@test.com", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("이미 등록된 이메일로 회원가입 시 예외가 발생한다")
    void register_duplicateEmail_throwsException() {
        given(memberRepository.existsByEmail("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.register("test@test.com", "password"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}