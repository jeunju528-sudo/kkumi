export type AssetKind = 'house' | 'stock' | 'cash'

export type AssetShare = {
  kind: AssetKind
  label: string
  amount: number
  percent: number // 정수 %, 세 개 합 100 (순자산 0이면 모두 0)
}

// 순자산 구성: 부동산(내 집 지분) / 주식 / 현금, 막대·범례 순서
// 반올림만 하면 합이 99·101이 될 수 있음 → 내림 후 나머지 큰 순으로 1%씩 (최대 나머지 방식)
export function toAssetShares(houseEquity: number, stockValue: number, cash: number): AssetShare[] {
  const items: { kind: AssetKind; label: string; amount: number }[] = [
    { kind: 'house', label: '부동산', amount: houseEquity },
    { kind: 'stock', label: '주식', amount: stockValue },
    { kind: 'cash', label: '현금', amount: cash },
  ]
  const total = items.reduce((sum, item) => sum + item.amount, 0)
  if (total <= 0) return items.map((item) => ({ ...item, percent: 0 }))

  const raw = items.map((item) => (item.amount / total) * 100)
  const percents = raw.map((value) => Math.floor(value))
  let left = 100 - percents.reduce((sum, value) => sum + value, 0)
  const byRemainder = raw
    .map((value, index) => ({ index, remainder: value - Math.floor(value) }))
    .sort((a, b) => b.remainder - a.remainder)
  for (const { index } of byRemainder) {
    if (left <= 0) break
    percents[index] += 1
    left -= 1
  }
  return items.map((item, index) => ({ ...item, percent: percents[index] }))
}

export type SeedMultiple = {
  text: string // "9.1배"
  isUp: boolean // 시드 이상이면 인생역전, 아래면 인생여전
}

// 시드머니 대비 배수 (내 마을 자랑 화면)
export function toSeedMultiple(netWorth: number, initialCash: number): SeedMultiple {
  if (initialCash <= 0) return { text: '-', isUp: false }
  const multiple = netWorth / initialCash
  return { text: `${multiple.toFixed(1)}배`, isUp: multiple >= 1 }
}
