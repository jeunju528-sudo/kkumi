// 1234567 → "1,234,567원"
export function formatKRW(won: number): string {
  return `${Math.round(won).toLocaleString('ko-KR')}원`
}

// 1234567 → "1,234,567" (단위 없이)
export function formatNumber(n: number): string {
  return Math.round(n).toLocaleString('ko-KR')
}

// 만원 단위 금액 → "13억 2,000만원"
export function formatManwon(manwon: number): string {
  const eok = Math.floor(manwon / 10000)
  const rest = Math.round(manwon % 10000)
  if (eok === 0) return `${rest.toLocaleString('ko-KR')}만원`
  if (rest === 0) return `${eok}억원`
  return `${eok}억 ${rest.toLocaleString('ko-KR')}만원`
}

// 0.0123 → "+1.23%"
export function formatRate(rate: number): string {
  const pct = (rate * 100).toFixed(2)
  return `${rate > 0 ? '+' : ''}${pct}%`
}

// 부호 포함 금액: 12000 → "+12,000원"
export function formatSignedKRW(won: number): string {
  return `${won > 0 ? '+' : won < 0 ? '-' : ''}${formatKRW(Math.abs(won))}`
}
