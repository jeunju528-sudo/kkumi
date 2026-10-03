# 프론트 코딩 규칙 예시

올바른 패턴을 따르고 금지 패턴은 쓰지 않음.

규칙 목록은 CLAUDE.md "프론트 코드 규칙", 자동 검사는 `frontend/eslint.config.js` 참고.

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
