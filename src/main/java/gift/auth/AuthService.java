package gift.auth;

import gift.member.MemberRequest;
import gift.member.MemberService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public AuthService(MemberService memberService, JwtProvider jwtProvider) {
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse register(MemberRequest request) {
        var member = memberService.register(request);
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }

    public TokenResponse login(MemberRequest request) {
        var member = memberService.login(request);
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}
