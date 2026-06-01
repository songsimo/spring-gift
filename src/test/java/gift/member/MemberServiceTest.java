package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import gift.exception.DuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gift.exception.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private MemberService memberService;

    @Test
    void register_신규_이메일은_토큰을_반환한다() {
        given(memberRepository.existsByEmail("new@test.com")).willReturn(false);
        given(memberRepository.save(any())).willReturn(new Member("new@test.com", "pass"));
        given(jwtProvider.createToken("new@test.com")).willReturn("test-token");

        TokenResponse response = memberService.register(new MemberRequest("new@test.com", "pass"));

        assertThat(response.token()).isEqualTo("test-token");
    }

    @Test
    void register_중복_이메일은_DuplicateException을_던진다() {
        given(memberRepository.existsByEmail("exists@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.register(new MemberRequest("exists@test.com", "pass")))
            .isInstanceOf(DuplicateException.class);
    }

    @Test
    void login_올바른_자격증명은_토큰을_반환한다() {
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "pass")));
        given(jwtProvider.createToken("user@test.com")).willReturn("test-token");

        TokenResponse response = memberService.login(new MemberRequest("user@test.com", "pass"));

        assertThat(response.token()).isEqualTo("test-token");
    }

    @Test
    void login_존재하지_않는_이메일은_NotFoundException을_던진다() {
        given(memberRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.login(new MemberRequest("unknown@test.com", "pass")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void login_비밀번호_불일치는_NotFoundException을_던진다() {
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "correct")));

        assertThatThrownBy(() -> memberService.login(new MemberRequest("user@test.com", "wrong")))
            .isInstanceOf(NotFoundException.class);
    }
}
