// 시간 계산 (순수 함수)

// 다음 자정까지 남은 시간 → "02:14:30" (브라우저 로컬 시각 기준, 한국 사용자 = KST)
export function toMidnightLeft(now: Date): string {
  const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
  const totalSec = Math.max(0, Math.floor((midnight.getTime() - now.getTime()) / 1000))
  const h = String(Math.floor(totalSec / 3600)).padStart(2, '0')
  const m = String(Math.floor((totalSec % 3600) / 60)).padStart(2, '0')
  const s = String(totalSec % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
}
