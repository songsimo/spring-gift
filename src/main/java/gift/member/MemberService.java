package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import gift.exception.DuplicateException;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse register(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateException("Email is already registered.");
        }
        Member member = memberRepository.save(new Member(request.email(), request.password()));
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}
