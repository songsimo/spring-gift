package gift.member;

import gift.auth.JwtProvider;
import gift.auth.TokenResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

    @Transactional
    public void updateMember(Long id, String email, String password) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + id));
        member.update(email, password);
        memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Transactional
    public void chargePoint(Long id, int amount) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + id));
        member.chargePoint(amount);
        memberRepository.save(member);
    }

    public void adminCreate(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        memberRepository.save(new Member(email, encoder.encode(password)));
    }

    public Member findByEmailOrNull(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public Member findOrCreateKakaoMember(String email, String kakaoAccessToken) {
        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> new Member(email));
        member.updateKakaoAccessToken(kakaoAccessToken);
        return memberRepository.save(member);
    }

    public TokenResponse register(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        Member member = memberRepository.save(new Member(email, encoder.encode(password)));
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }

    public TokenResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (member.getPassword() == null || !encoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}