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

const MINUTE_MS = 60_000
const HOUR_MS = 60 * MINUTE_MS

// 알림 시각 → "방금" / "5분 전" / "3시간 전" / "어제" / "4일 전" (달력 날짜 기준)
export function toRelativeTime(iso: string, now: Date): string {
  const at = new Date(iso)
  const diffMs = now.getTime() - at.getTime()
  if (diffMs < MINUTE_MS) return '방금'
  if (diffMs < HOUR_MS) return `${Math.floor(diffMs / MINUTE_MS)}분 전`
  const atDay = new Date(at.getFullYear(), at.getMonth(), at.getDate())
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const days = Math.round((today.getTime() - atDay.getTime()) / (24 * HOUR_MS))
  if (days === 0) return `${Math.floor(diffMs / HOUR_MS)}시간 전`
  if (days === 1) return '어제'
  return `${days}일 전`
}
