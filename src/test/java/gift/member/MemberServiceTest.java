package gift.member;

import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.member.repository.MemberRepository;
import gift.member.service.MemberResponse;
import gift.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Mock
    MemberRepository memberRepository;

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
    @DisplayName("회원 정보 수정 시 비밀번호가 BCrypt로 인코딩되어 저장된다")
    void updateMember_encodesPasswordBeforeSaving() {
        Member member = new Member(1L, "old@test.com", encoder.encode("oldpw"));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);

        memberService.updateMember(1L, "new@test.com", "newpw");

        assertThat(encoder.matches("newpw", member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 회원 수정 시 예외가 발생한다")
    void nonExistingMember_throwsException() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMember(99L, "x@test.com", "pw"))
            .isInstanceOf(NotFoundException.class);
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
            .isInstanceOf(NotFoundException.class);
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
            .isInstanceOf(DuplicateException.class);
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
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("회원을 삭제한다")
    void deleteMember_callsRepositoryDeleteById() {
        memberService.deleteMember(1L);

        org.mockito.Mockito.verify(memberRepository).deleteById(1L);
    }

    @Test
    @DisplayName("신규 이메일로 회원가입 시 회원을 저장하고 반환한다")
    void registerMember_newEmail_returnsMember() {
        given(memberRepository.existsByEmail("test@test.com")).willReturn(false);
        given(memberRepository.save(any())).willReturn(new Member("test@test.com", "hashed"));

        Member result = memberService.registerMember("test@test.com", "password");

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("이미 등록된 이메일로 회원가입 시 예외가 발생한다")
    void registerMember_duplicateEmail_throwsException() {
        given(memberRepository.existsByEmail("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.registerMember("test@test.com", "password"))
            .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 인증 시 회원을 반환한다")
    void authenticate_validCredentials_returnsMember() {
        Member member = new Member(1L, "test@test.com", encoder.encode("password"));
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));

        Member result = memberService.authenticate("test@test.com", "password");

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 인증 시 예외가 발생한다")
    void authenticate_emailNotFound_throwsException() {
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.authenticate("none@test.com", "password"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 인증 시 예외가 발생한다")
    void authenticate_wrongPassword_throwsException() {
        Member member = new Member(1L, "test@test.com", encoder.encode("password"));
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.authenticate("test@test.com", "wrong"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 BCrypt로 인코딩되어 저장된다")
    void registerMember_encodesPasswordBeforeSaving() {
        given(memberRepository.existsByEmail("test@test.com")).willReturn(false);
        given(memberRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        memberService.registerMember("test@test.com", "password");

        org.mockito.Mockito.verify(memberRepository).save(captor.capture());
        assertThat(encoder.matches("password", captor.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("관리자 회원 생성 시 비밀번호가 BCrypt로 인코딩되어 저장된다")
    void adminCreate_encodesPasswordBeforeSaving() {
        given(memberRepository.existsByEmail("admin@test.com")).willReturn(false);
        given(memberRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        memberService.adminCreate("admin@test.com", "adminpw");

        org.mockito.Mockito.verify(memberRepository).save(captor.capture());
        assertThat(encoder.matches("adminpw", captor.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("존재하는 이메일로 조회 시 회원을 반환한다")
    void findByEmailOrNull_existingEmail_returnsMember() {
        Member member = new Member(1L, "test@test.com", "pw");
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));

        Member result = memberService.findByEmailOrNull("test@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 null을 반환한다")
    void findByEmailOrNull_nonExistingEmail_returnsNull() {
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        Member result = memberService.findByEmailOrNull("none@test.com");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("카카오 신규 회원 처리 시 회원을 생성하고 카카오 토큰을 저장한다")
    void findOrCreateKakaoMember_newMember_createsAndSaves() {
        given(memberRepository.findByEmail("new@kakao.com")).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.findOrCreateKakaoMember("new@kakao.com", "kakao-token");

        assertThat(result.getEmail()).isEqualTo("new@kakao.com");
        assertThat(result.getKakaoAccessToken()).isEqualTo("kakao-token");
    }

    @Test
    @DisplayName("카카오 기존 회원 처리 시 카카오 토큰을 갱신한다")
    void findOrCreateKakaoMember_existingMember_updatesToken() {
        Member member = new Member("existing@kakao.com");
        given(memberRepository.findByEmail("existing@kakao.com")).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);

        Member result = memberService.findOrCreateKakaoMember("existing@kakao.com", "new-token");

        assertThat(result.getKakaoAccessToken()).isEqualTo("new-token");
    }

    @Test
    @DisplayName("이메일로 내 정보를 조회하면 이메일과 포인트를 반환한다")
    void getMyInfo_existingEmail_returnsEmailAndPoint() {
        Member member = new Member(1L, "test@test.com", "pw");
        member.chargePoint(3000);
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));

        MemberResponse result = memberService.getMyInfo("test@test.com");

        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.point()).isEqualTo(3000);
    }

    @Test
    @DisplayName("registerMember는 @Transactional이 선언되어 있다")
    void registerMember_hasTransactionalAnnotation() throws NoSuchMethodException {
        Method method = MemberService.class.getDeclaredMethod("registerMember", String.class, String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("adminCreate는 @Transactional이 선언되어 있다")
    void adminCreate_hasTransactionalAnnotation() throws NoSuchMethodException {
        Method method = MemberService.class.getDeclaredMethod("adminCreate", String.class, String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("deleteMember는 @Transactional이 선언되어 있다")
    void deleteMember_hasTransactionalAnnotation() throws NoSuchMethodException {
        Method method = MemberService.class.getDeclaredMethod("deleteMember", Long.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("findById는 @Transactional(readOnly = true)이 선언되어 있다")
    void findById_hasReadOnlyTransactionalAnnotation() throws NoSuchMethodException {
        Method method = MemberService.class.getDeclaredMethod("findById", Long.class);
        Transactional annotation = method.getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    @Test
    @DisplayName("getMyInfo는 @Transactional(readOnly = true)이 선언되어 있다")
    void getMyInfo_hasReadOnlyTransactionalAnnotation() throws NoSuchMethodException {
        Method method = MemberService.class.getDeclaredMethod("getMyInfo", String.class);
        Transactional annotation = method.getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }
}