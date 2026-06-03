package gift.member;

import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void register_신규_이메일은_Member를_반환한다() {
        given(memberRepository.existsByEmail("new@test.com")).willReturn(false);
        given(memberRepository.save(any())).willReturn(new Member("new@test.com", "pass"));

        Member result = memberService.register(new MemberRequest("new@test.com", "pass"));

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void register_중복_이메일은_DuplicateException을_던진다() {
        given(memberRepository.existsByEmail("exists@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.register(new MemberRequest("exists@test.com", "pass")))
            .isInstanceOf(DuplicateException.class);
    }

    @Test
    void login_올바른_자격증명은_Member를_반환한다() {
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "pass")));

        Member result = memberService.login(new MemberRequest("user@test.com", "pass"));

        assertThat(result.getEmail()).isEqualTo("user@test.com");
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
