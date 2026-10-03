# 코드 작성 예시

올바른 패턴을 따르고 금지 패턴은 쓰지 않음.

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

## 프론트: 데이터 흐름 (api → hooks → page → component)
```tsx
// src/api/houseApi.ts — fetch는 api 레이어에서만
export async function getMyHouses(): Promise<HouseResponse[]> {
  const res = await fetch('/api/houses')
  if (!res.ok) throw new Error('집 목록 조회 실패')
  return res.json() as Promise<HouseResponse[]>
}

// src/hooks/queryKeys.ts — 쿼리 키는 여기서만
export const queryKeys = {
  myHouses: () => ['houses', 'me'] as const,
  member: () => ['member', 'me'] as const,
  portfolio: () => ['portfolio', 'me'] as const,
}

// src/hooks/useMyHousesQuery.ts — useQuery는 hooks에서만
export function useMyHousesQuery() {
  return useQuery({ queryKey: queryKeys.myHouses(), queryFn: getMyHouses })
}

// src/hooks/useBuyHouseMutation.ts — 쓰기 후 관련 쿼리 무효화
export function useBuyHouseMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: buyHouse,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.myHouses() })
      queryClient.invalidateQueries({ queryKey: queryKeys.member() })
    },
  })
}

// src/pages/MyTownPage.tsx — page는 훅 호출 → component에 props 전달
export function MyTownPage() {
  const { data: houses, isLoading } = useMyHousesQuery()
  if (isLoading || !houses) return <Spinner />
  return houses.map((house) => <HouseCard key={house.id} house={house} onSelect={handleSelect} />)
}

// src/components/HouseCard.tsx — props만 받아서 그림
type HouseCardProps = {
  house: HouseResponse
  onSelect: (houseId: number) => void
}

export function HouseCard({ house, onSelect }: HouseCardProps) {
  return <button onClick={() => onSelect(house.id)}>{formatKRW(house.purchasePrice)}</button>
}

// 금지 패턴: component에서 fetch·useQuery, 쿼리 키 직접 입력, any, default export, 직접 포맷
export default function HouseCard() {
  const { data } = useQuery<any>({ queryKey: ['houses'], queryFn: () => fetch('/api/houses').then((r) => r.json()) })
  return <div>{data[0].price.toLocaleString()}원</div>
}
```

## 프론트: WebSocket 구독
```tsx
// 올바른 패턴: src/ws 함수로 구독 → 쿼리 캐시에 반영, cleanup에서 해제 (hooks 안에서)
useEffect(() => {
  const unsubscribe = subscribePortfolio(memberId, (valuation) => {
    queryClient.setQueryData(queryKeys.portfolio(), valuation)
  })
  return () => unsubscribe()
}, [memberId, queryClient])

// 금지 패턴: 컴포넌트에서 직접 연결, cleanup 없음
useEffect(() => {
  const socket = new WebSocket('ws://localhost:8080/ws')
  socket.onmessage = (e) => setValue(JSON.parse(e.data))
}, [])
```

## 테스트: 도메인 규칙 → 테스트
```java
// house/TransformStageTest.java — 변신 단계: 0채 걸뱅이 / 1채 서민 / 2~4채 졸부 / 5채 이상 갑부
class TransformStageTest {

	@Test
	@DisplayName("집이 없으면 걸뱅이")
	void zeroHouse() {
		// given
		int houseCount = 0;

		// when
		TransformStage stage = TransformStage.of(houseCount);

		// then
		assertThat(stage).isEqualTo(TransformStage.BEGGAR);
	}

	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4})
	@DisplayName("2~4채면 졸부")
	void nouveauRiche(int houseCount) {
		assertThat(TransformStage.of(houseCount)).isEqualTo(TransformStage.NOUVEAU_RICHE);
	}

	@Test
	@DisplayName("집 수가 음수면 예외")
	void negativeCount() {
		assertThatThrownBy(() -> TransformStage.of(-1))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
```
