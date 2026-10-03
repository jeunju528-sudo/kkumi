# 백엔드 코딩 규칙 예시

올바른 패턴을 따르고 금지 패턴은 쓰지 않음.

규칙 목록은 CLAUDE.md "백엔드 코드 규칙", 자동 검사는 `ArchitectureTest` 참고.

## 백엔드: Controller
```java
// 올바른 패턴: 검증 → Service 호출 → Response DTO 반환
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/houses")
public class HouseController {

	private final HouseService houseService;

	@PostMapping
	public HouseResponse buyHouse(@AuthenticationPrincipal LoginMember loginMember,
	                              @Valid @RequestBody BuyHouseRequest request) {
		return houseService.buyHouse(loginMember.id(), request.dealId());
	}
}

// 금지 패턴: Repository 직접 호출, 비즈니스 분기, 엔티티 반환
@PostMapping
public House buyHouse(@RequestBody BuyHouseRequest request) {
	Member member = memberRepository.findById(request.memberId()).get();
	if (member.getCash() < request.price()) {
		throw new RuntimeException("돈 부족");
	}
	return houseRepository.save(...);
}
```

## 백엔드: Service
```java
// 올바른 패턴: 클래스 readOnly, 쓰기 메서드만 @Transactional, 없으면 BusinessException
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public MemberResponse join(String kakaoId, String nickname, SeedMoney seedMoney) {
		if (memberRepository.findByKakaoId(kakaoId).isPresent()) {
			throw new BusinessException(ErrorCode.ALREADY_JOINED);
		}
		Member member = memberRepository.save(Member.join(kakaoId, nickname, seedMoney));
		return MemberResponse.from(member);
	}

	public Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}
}

// 금지 패턴: 필드 주입, Optional.get(), RuntimeException 직접 throw
@Autowired
private MemberRepository memberRepository;

public Member getMember(Long memberId) {
	return memberRepository.findById(memberId).get();
}
```

## 백엔드: 엔티티 상태 변경
```java
// 올바른 패턴: 의미 있는 메서드 + 엔티티 안에서 검증
public void withdraw(long amount) {
	if (this.cash < amount) {
		throw new BusinessException(ErrorCode.INSUFFICIENT_CASH);
	}
	this.cash -= amount;
}

// 금지 패턴: setter로 밖에서 값 덮어쓰기
member.setCash(member.getCash() - amount);
```
