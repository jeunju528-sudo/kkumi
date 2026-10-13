package com.kkumi.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    // DB없이 사용하는 가짜 Repository
	@Mock
	MemberRepository memberRepository;

    // Mock 가짜를 service에 생성자로 넣어서 객체를 만들어줌
	@InjectMocks
	MemberService memberService;

	@Test
	@DisplayName("가입한 카카오 ID면 회원 ID 반환")
	void findMemberIdByKakaoId_registered() {
		// given

        // 테스트용 회원 생성
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        // 대상 객체의 필드에 reflection으로 값을 넣는 테스트 유틸, private 변수여도 값을 넣을 수 있음
        ReflectionTestUtils.setField(member, "id", 123L);
        // given 가짜 객체한테 이 메서드를 이 인자로 부르면 이 값을 돌려줘 라고 선언하는 것
        // findByKakaoId 이 메서드를 member.getKakaoId() 이 인자로 부르면 willReturn에 작성된 값을 돌려줘
        given(memberRepository.findByKakaoId("abcd")).willReturn(Optional.of(member));
                                                                // 회원 한명을 담고있는 상자

		// when
        Optional<Long> result = memberService.findMemberIdByKakaoId("abcd");

		// then
		assertThat(result).contains(123L);
	}

	@Test
	@DisplayName("가입하지 않은 카카오 ID면 빈 값 반환")
	void findMemberIdByKakaoId_notRegistered() {
		// given
        given(memberRepository.findByKakaoId("efgh")).willReturn(Optional.empty());

        // when
        Optional<Long> result = memberService.findMemberIdByKakaoId("efgh");

		// then
		assertThat(result).isEmpty();
	}

    @Test
    @DisplayName("이미 가입한 카카오 ID면 ALREADY_JOINED 예외")
    void join_alreadyJoined() {
        // given
        given(memberRepository.existsByKakaoId("abcd")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.join("abcd", "꿈이", SeedMoney.SMALL))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")              // 예외 객체의 errorCode 필드 꺼내기
                .isEqualTo(ErrorCode.ALREADY_JOINED);
    }

    @Test
    @DisplayName("가입하지 않은 카카오 ID면 가입 후 회원 ID 반환")
    void join_success() {
        // given
        given(memberRepository.existsByKakaoId("abcd")).willReturn(false);

        Member saved = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(saved, "id", 1L);              // DB가 id를 채운 척
        given(memberRepository.save(any(Member.class))).willReturn(saved);

        // when
        long memberId = memberService.join("abcd", "꿈이", SeedMoney.SMALL);

        // then
        assertThat(memberId).isEqualTo(1L);
    }
}
