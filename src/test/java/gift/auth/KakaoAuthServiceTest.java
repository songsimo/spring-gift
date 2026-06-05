package gift.auth;

import gift.member.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    private KakaoLoginClient kakaoLoginClient;

    @Mock
    private KakaoMemberService kakaoMemberService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Test
    void callback_신규_카카오_회원은_자동_가입_후_인증_토큰이_발급된다() {
        var kakaoToken = new KakaoLoginClient.KakaoTokenResponse("kakao-access-token");
        var kakaoUser = new KakaoLoginClient.KakaoUserResponse(
            new KakaoLoginClient.KakaoUserResponse.KakaoAccount("test@test.com")
        );
        given(kakaoLoginClient.requestAccessToken("code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("kakao-access-token")).willReturn(kakaoUser);
        given(kakaoMemberService.saveOrUpdate("test@test.com", "kakao-access-token")).willReturn(new Member("test@test.com"));
        given(jwtProvider.createToken("test@test.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.callback("code");

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void callback_기존_회원의_카카오_액세스_토큰이_갱신된다() {
        var kakaoToken = new KakaoLoginClient.KakaoTokenResponse("new-kakao-token");
        var kakaoUser = new KakaoLoginClient.KakaoUserResponse(
            new KakaoLoginClient.KakaoUserResponse.KakaoAccount("existing@test.com")
        );
        var existingMember = new Member("existing@test.com");
        existingMember.updateKakaoAccessToken("new-kakao-token");
        given(kakaoLoginClient.requestAccessToken("code")).willReturn(kakaoToken);
        given(kakaoLoginClient.requestUserInfo("new-kakao-token")).willReturn(kakaoUser);
        given(kakaoMemberService.saveOrUpdate("existing@test.com", "new-kakao-token")).willReturn(existingMember);
        given(jwtProvider.createToken("existing@test.com")).willReturn("jwt-token");

        TokenResponse result = kakaoAuthService.callback("code");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(existingMember.getKakaoAccessToken()).isEqualTo("new-kakao-token");
    }
}
