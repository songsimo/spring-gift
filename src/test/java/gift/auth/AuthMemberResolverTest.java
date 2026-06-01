package gift.auth;

import gift.exception.UnauthorizedException;
import gift.member.Member;
import gift.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthMemberResolverTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AuthMemberResolver resolver;

    @Test
    void resolveArgument_유효한_토큰이면_Member를_반환한다() throws Exception {
        var request = mock(NativeWebRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer valid-token");
        given(jwtProvider.getEmail("valid-token")).willReturn("user@test.com");
        given(memberRepository.findByEmail("user@test.com"))
            .willReturn(Optional.of(new Member("user@test.com", "pass")));

        Object result = resolver.resolveArgument(mock(MethodParameter.class), null, request, null);

        assertThat(result).isInstanceOf(Member.class);
        assertThat(((Member) result).getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void resolveArgument_Authorization_헤더가_없으면_UnauthorizedException을_던진다() {
        var request = mock(NativeWebRequest.class);
        given(request.getHeader("Authorization")).willReturn(null);

        assertThatThrownBy(() -> resolver.resolveArgument(mock(MethodParameter.class), null, request, null))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void resolveArgument_유효하지_않은_토큰이면_UnauthorizedException을_던진다() {
        var request = mock(NativeWebRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer bad-token");
        given(jwtProvider.getEmail("bad-token")).willThrow(new RuntimeException("invalid"));

        assertThatThrownBy(() -> resolver.resolveArgument(mock(MethodParameter.class), null, request, null))
            .isInstanceOf(UnauthorizedException.class);
    }
}
