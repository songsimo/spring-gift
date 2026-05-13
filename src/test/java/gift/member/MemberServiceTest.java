package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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
    @DisplayName("회원 정보를 수정한다")
    void existingMember_updatesMember() {
        Member member = new Member(1L, "old@test.com", "oldpw");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);

        memberService.updateMember(1L, "new@test.com", "newpw");

        org.mockito.Mockito.verify(memberRepository).save(member);
        assertThat(member.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 회원 수정 시 예외가 발생한다")
    void nonExistingMember_throwsException() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMember(99L, "x@test.com", "pw"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ID로 회원을 조회한다")
    void findById_existingId_returnsMember() {
        Member member = new Member(1L, "test@test.com", "pw");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Member result = memberService.findById(1L);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원 조회 시 예외가 발생한다")
    void findById_nonExistingId_throwsException() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("관리자가 신규 이메일로 회원을 생성한다")
    void adminCreate_newEmail_savesMember() {
        given(memberRepository.existsByEmail("new@test.com")).willReturn(false);
        given(memberRepository.save(any())).willReturn(new Member("new@test.com", "pw"));

        memberService.adminCreate("new@test.com", "pw");

        org.mockito.Mockito.verify(memberRepository).save(any());
    }

    @Test
    @DisplayName("관리자가 중복 이메일로 회원 생성 시 예외가 발생한다")
    void adminCreate_duplicateEmail_throwsException() {
        given(memberRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.adminCreate("dup@test.com", "pw"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전체 회원 목록을 반환한다")
    void findAll_returnsAllMembers() {
        List<Member> members = List.of(
            new Member("a@test.com", "pw1"),
            new Member("b@test.com", "pw2")
        );
        given(memberRepository.findAll()).willReturn(members);

        List<Member> result = memberService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("회원 포인트를 충전한다")
    void chargePoint_existingMember_chargesPoint() {
        Member member = new Member(1L, "test@test.com", "pw");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);

        memberService.chargePoint(1L, 500);

        assertThat(member.getPoint()).isEqualTo(500);
    }

    @Test
    @DisplayName("존재하지 않는 회원 포인트 충전 시 예외가 발생한다")
    void chargePoint_nonExistingMember_throwsException() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.chargePoint(99L, 500))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원을 삭제한다")
    void deleteMember_callsRepositoryDeleteById() {
        memberService.deleteMember(1L);

        org.mockito.Mockito.verify(memberRepository).deleteById(1L);
    }

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

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인 시 토큰을 반환한다")
    void login_validCredentials_returnsToken() {
        Member member = new Member(1L, "test@test.com", "password");
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = memberService.login("test@test.com", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_emailNotFound_throwsException() {
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.login("none@test.com", "password"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 시 예외가 발생한다")
    void login_wrongPassword_throwsException() {
        Member member = new Member(1L, "test@test.com", "password");
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.login("test@test.com", "wrong"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}