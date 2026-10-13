package com.kkumi.member;

import java.util.Optional;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	// 가입한 회원이면 memberId, 아니면 빈 값
	public Optional<Long> findMemberIdByKakaoId(String kakaoId) {
        /* map(Member::getId) ::
           Optional<Member> 에서 값이 있는 경우에는 getId를 반환하고 없는경우에는 에러로 빠지지않고 빈 값을 반환하게 함
           map(m -> m.getId()) == map(Member::getId)
        */
        return memberRepository.findByKakaoId(kakaoId).map(Member::getId);
	}

	// 회원 가입 후 memberId 반환. 이미 가입한 kakaoId면 ALREADY_JOINED
    @Transactional
    public long join(String kakaoId, String nickname, SeedMoney seedMoney) {
        if (memberRepository.existsByKakaoId(kakaoId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        // Member.join() : Member 클래스의 static 메서드로 객체 없이 호출하여 내부에서 Member 객체를 만들어서 반환함
        Member member = memberRepository.save(Member.join(kakaoId, nickname, seedMoney));
        return member.getId();
    }
}
