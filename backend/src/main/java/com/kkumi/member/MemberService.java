package com.kkumi.member;

import java.util.Optional;
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
}
