package gift.auth;

import gift.member.Member;
import gift.member.MemberRepository;
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
    private KakaoLoginClient kakaoLoginClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Test
    void callback_신규_회원이면_저장하고_토큰을_반환한다() {
        var kakaoToken = new KakaoLoginClient.KakaoTokenResponse("kakao-access-token");
        var kakaoUser = new KakaoLoginClient.KakaoUserResponse(
            new KakaoLoginClient.KakaoUserResponse.KakaoAccount("test@test.com")
        );
        given(kakaoLoginClient.requestAccessToken("code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("kakao-access-token")).willReturn(kakaoUser);
        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.callback("code");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void callback_기존_회원이면_카카오_토큰을_갱신하고_반환한다() {
        var kakaoToken = new KakaoLoginClient.KakaoTokenResponse("new-kakao-token");
        var kakaoUser = new KakaoLoginClient.KakaoUserResponse(
            new KakaoLoginClient.KakaoUserResponse.KakaoAccount("existing@test.com")
        );
        var existingMember = new Member("existing@test.com");
        given(kakaoLoginClient.requestAccessToken("code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("new-kakao-token")).willReturn(kakaoUser);
        given(memberRepository.findByEmail("existing@test.com")).willReturn(Optional.of(existingMember));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(jwtProvider.createToken("existing@test.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.callback("code");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(existingMember.getKakaoAccessToken()).isEqualTo("new-kakao-token");
    }
}
