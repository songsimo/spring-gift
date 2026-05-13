package gift.auth;

import gift.member.Member;
import gift.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    KakaoLoginClient kakaoLoginClient;

    @Mock
    MemberRepository memberRepository;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    KakaoAuthService kakaoAuthService;

    @Test
    @DisplayName("신규 회원의 카카오 인가 코드로 자동 가입 후 JWT를 반환한다")
    void processCallback_newMember_returnsToken() {
        KakaoLoginClient.KakaoTokenResponse kakaoToken =
            new KakaoLoginClient.KakaoTokenResponse("kakao-access-token");
        KakaoLoginClient.KakaoUserResponse kakaoUser =
            new KakaoLoginClient.KakaoUserResponse(new KakaoLoginClient.KakaoUserResponse.KakaoAccount("test@kakao.com"));

        given(kakaoLoginClient.requestAccessToken("auth-code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("kakao-access-token")).willReturn(kakaoUser);
        given(memberRepository.findByEmail("test@kakao.com")).willReturn(Optional.empty());
        given(memberRepository.save(any())).willReturn(new Member("test@kakao.com"));
        given(jwtProvider.createToken("test@kakao.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.processCallback("auth-code");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("기존 회원의 카카오 인가 코드로 카카오 토큰을 갱신하고 JWT를 반환한다")
    void processCallback_existingMember_returnsToken() {
        KakaoLoginClient.KakaoTokenResponse kakaoToken =
            new KakaoLoginClient.KakaoTokenResponse("new-kakao-token");
        KakaoLoginClient.KakaoUserResponse kakaoUser =
            new KakaoLoginClient.KakaoUserResponse(new KakaoLoginClient.KakaoUserResponse.KakaoAccount("existing@kakao.com"));
        Member member = new Member("existing@kakao.com");

        given(kakaoLoginClient.requestAccessToken("auth-code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("new-kakao-token")).willReturn(kakaoUser);
        given(memberRepository.findByEmail("existing@kakao.com")).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);
        given(jwtProvider.createToken("existing@kakao.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.processCallback("auth-code");

        assertThat(result.token()).isEqualTo("jwt-token");
    }
}