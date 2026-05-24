package gift.auth;

import gift.member.Member;
import gift.member.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoAuthService {
    private final KakaoLoginProperties properties;
    private final KakaoLoginClient kakaoLoginClient;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public KakaoAuthService(KakaoLoginProperties properties, KakaoLoginClient kakaoLoginClient, MemberService memberService, JwtProvider jwtProvider) {
        this.properties = properties;
        this.kakaoLoginClient = kakaoLoginClient;
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId())
            .queryParam("redirect_uri", properties.redirectUri())
            .queryParam("scope", "account_email,talk_message")
            .build()
            .toUriString();
    }

    public TokenResponse processCallback(String code) {
        KakaoLoginClient.KakaoTokenResponse kakaoToken = kakaoLoginClient.requestAccessToken(code);
        KakaoLoginClient.KakaoUserResponse kakaoUser = kakaoLoginClient.requestUserInfo(kakaoToken.accessToken());

        Member member = memberService.findOrCreateKakaoMember(kakaoUser.email(), kakaoToken.accessToken());
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}
