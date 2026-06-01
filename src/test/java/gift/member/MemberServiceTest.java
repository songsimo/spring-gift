package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import gift.exception.DuplicateException;
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
}
