package gift.auth;

import gift.member.Member;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthService {
    private final KakaoLoginClient kakaoLoginClient;
    private final KakaoMemberService kakaoMemberService;
    private final JwtProvider jwtProvider;

    public KakaoAuthService(
        KakaoLoginClient kakaoLoginClient,
        KakaoMemberService kakaoMemberService,
        JwtProvider jwtProvider
    ) {
        this.kakaoLoginClient = kakaoLoginClient;
        this.kakaoMemberService = kakaoMemberService;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse callback(String code) {
        KakaoLoginClient.KakaoTokenResponse kakaoToken = kakaoLoginClient.requestAccessToken(code);
        KakaoLoginClient.KakaoUserResponse kakaoUser = kakaoLoginClient.requestUserInfo(kakaoToken.accessToken());
        Member member = kakaoMemberService.saveOrUpdate(kakaoUser.email(), kakaoToken.accessToken());
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}
