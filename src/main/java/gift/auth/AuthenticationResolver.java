package gift.auth;

import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.member.service.MemberService;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationResolver {
    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    public AuthenticationResolver(JwtProvider jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    public Member extractMember(String authorization) {
        try {
            final String token = authorization.replace("Bearer ", "");
            final String email = jwtProvider.getEmail(token);
            Member member = memberService.findByEmailOrNull(email);
            if (member == null) {
                throw new UnauthorizedException("인증이 필요합니다.");
            }
            return member;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }
    }
}
