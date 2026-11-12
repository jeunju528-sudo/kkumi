// 손익 차트 계산 (순수 함수, 화면 모양만)

export type ChartPoint = {
  slot: number // 날짜 칸 위치 (0 = 기간 첫날)
  value: number // 평가액 (원)
}

export type ChartCoord = {
  x: number
  y: number
}

const DAY_MS = 24 * 60 * 60 * 1000
const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// Date → 'YYYY-MM-DD' (브라우저 로컬 날짜, 서버 LocalDate와 같은 모양)
export function toDateKey(date: Date): string {
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${m}-${d}`
}

// 'YYYY-MM-DD' → UTC 자정 시각 (시간대 영향 없이 날짜 차이만 계산)
function toUtcMs(key: string): number {
  const [y, m, d] = key.split('-').map(Number)
  return Date.UTC(y, m - 1, d)
}

// 날짜 더하기
export function addDays(key: string, days: number): string {
  const date = new Date(toUtcMs(key) + days * DAY_MS)
  const m = String(date.getUTCMonth() + 1).padStart(2, '0')
  const d = String(date.getUTCDate()).padStart(2, '0')
  return `${date.getUTCFullYear()}-${m}-${d}`
}

// 두 날짜 사이 일수 (to − from)
export function daysBetween(from: string, to: string): number {
  return Math.round((toUtcMs(to) - toUtcMs(from)) / DAY_MS)
}

// 축 라벨: 1주는 요일 7개, 1달은 4개(M/D), 마지막은 '오늘'
export function toAxisLabels(from: string, slotCount: number): string[] {
  const lastSlot = slotCount - 1
  const slots = slotCount <= 7
    ? Array.from({ length: slotCount }, (_, i) => i)
    : [0, Math.round(lastSlot / 3), Math.round((lastSlot * 2) / 3), lastSlot]
  return slots.map((slot) => {
    if (slot === lastSlot) return '오늘'
    const key = addDays(from, slot)
    if (slotCount <= 7) return WEEKDAYS[new Date(toUtcMs(key)).getUTCDay()]
    const [, m, d] = key.split('-').map(Number)
    return `${m}/${d}`
  })
}

// 값 → 좌표. x는 칸 위치(빈 날은 점 없이 앞뒤 점을 직선 연결), y는 기준선(시드머니) 포함 범위
export function toChartCoords(
  points: ChartPoint[],
  slotCount: number,
  baseline: number,
  width: number,
  height: number,
  padding: number,
): { coords: ChartCoord[]; baselineY: number } {
  const values = [baseline, ...points.map((p) => p.value)]
  let min = Math.min(...values)
  let max = Math.max(...values)
  // 값이 모두 같으면 위아래 1% 여유 (0으로 나누기 방지)
  if (max === min) {
    const gap = Math.max(1, Math.abs(max) * 0.01)
    min -= gap
    max += gap
  }
  const innerWidth = width - padding * 2
  const innerHeight = height - padding * 2
  const step = slotCount > 1 ? innerWidth / (slotCount - 1) : 0
  const toY = (value: number) => padding + ((max - value) / (max - min)) * innerHeight

  return {
    coords: points.map((p) => ({ x: padding + p.slot * step, y: toY(p.value) })),
    baselineY: toY(baseline),
  }
}

// 차트 칸 수: 데이터 수를 [최소, 최대]로 자름 (모자라면 최소 폭에 오른쪽 정렬, 많으면 꽉 채움)
export function toSlotCount(dataCount: number, minSlots: number, maxSlots: number): number {
  return Math.min(maxSlots, Math.max(minSlots, dataCount))
}

// 축 3칸(왼쪽·가운데·오른쪽) 문구: 데이터 있는 칸은 그 데이터 문구, 빈 칸은 "N분 전" 같은 상대 문구
// dataLabels는 오른쪽 정렬로 놓인다고 보고 계산
export function toThreeAxisLabels(
  slotCount: number,
  dataLabels: string[],
  toEmptyLabel: (slotsAgo: number) => string,
): string[] {
  const offset = slotCount - dataLabels.length
  const labelAt = (slot: number) => {
    const index = slot - offset
    return index >= 0 ? dataLabels[index] : toEmptyLabel(slotCount - 1 - slot)
  }
  return [labelAt(0), labelAt(Math.floor((slotCount - 1) / 2)), labelAt(slotCount - 1)]
}

// '2026-11-12T22:10:00' → "22:10"
export function toClockLabel(time: string): string {
  return time.slice(11, 16)
}

// '2026-11-12' → "11/12"
export function toMonthDayLabel(date: string): string {
  return `${Number(date.slice(5, 7))}/${Number(date.slice(8, 10))}`
}
