package gift.member.service;

import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public void updateMember(Long id, String email, String password) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + id));
        member.update(email, encoder.encode(password));
        memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Transactional
    public void chargePoint(Long id, int amount) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + id));
        member.chargePoint(amount);
        memberRepository.save(member);
    }

    @Transactional
    public void adminCreate(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 가입된 이메일입니다.");
        }
        memberRepository.save(new Member(email, encoder.encode(password)));
    }

    @Transactional(readOnly = true)
    public MemberResponse getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. email=" + email));
        return MemberResponse.from(member);
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

    @Transactional
    public Member registerMember(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 가입된 이메일입니다.");
        }
        return memberRepository.save(new Member(email, encoder.encode(password)));
    }

    public Member authenticate(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (member.getPassword() == null || !encoder.matches(password, member.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

}