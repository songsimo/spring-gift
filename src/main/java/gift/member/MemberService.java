package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + id));
    }

    public void adminCreate(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        memberRepository.save(new Member(email, password));
    }

    public TokenResponse register(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        Member member = memberRepository.save(new Member(email, password));
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }

    public TokenResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (member.getPassword() == null || !member.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}