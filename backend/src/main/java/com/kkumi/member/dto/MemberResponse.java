package com.kkumi.member.dto;

import com.kkumi.member.Member;

// 로그인한 회원 정보 (화면 가드, 상단 현금 칩). 변신 단계·집 수는 GET /api/houses (member ↔ house 순환 참조 방지)
public record MemberResponse(Long id, String nickname, long cash) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getNickname(), member.getCash());
	}
}
