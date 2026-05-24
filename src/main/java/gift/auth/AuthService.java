package gift.auth;

import gift.member.model.Member;
import gift.member.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public AuthService(MemberService memberService, JwtProvider jwtProvider) {
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse register(String email, String password) {
        Member member = memberService.registerMember(email, password);
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }

    public TokenResponse login(String email, String password) {
        Member member = memberService.authenticate(email, password);
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}
