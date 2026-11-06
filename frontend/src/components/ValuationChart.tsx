import { toChartCoords } from '../utils/chart'
import type { ChartPoint } from '../utils/chart'

const WIDTH = 326
const HEIGHT = 104
const PADDING = 6

// 손익 부호별 선 색 (design.md --up / --down, 0은 회색)
const TONE_COLORS = {
  up: '#E5484D',
  down: '#3B82F6',
  flat: '#6B6F80',
} as const

type ValuationChartProps = {
  points: ChartPoint[] // 날짜 순, 마지막 점 = 오늘 현재값
  slotCount: number // 기간 칸 수 (1주 7, 1달 30)
  baseline: number // 시드머니 (점선)
  tone: keyof typeof TONE_COLORS
  label: string // 스크린리더용 설명
}

// 일별 평가액 선 그래프. 행 없는 날은 점 없이 앞뒤 직선 연결
export function ValuationChart({ points, slotCount, baseline, tone, label }: ValuationChartProps) {
  const { coords, baselineY } = toChartCoords(points, slotCount, baseline, WIDTH, HEIGHT, PADDING)
  const color = TONE_COLORS[tone]
  const last = coords[coords.length - 1]

  return (
    <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" role="img" aria-label={label} className="k-chart">
      <line x1="0" y1={baselineY} x2={WIDTH} y2={baselineY} stroke="#E6E8F0" strokeWidth="1.5" strokeDasharray="4 4" />
      {coords.length > 1 && (
        <polyline
          points={coords.map((c) => `${c.x},${c.y}`).join(' ')}
          fill="none"
          stroke={color}
          strokeWidth="2.5"
          strokeLinejoin="round"
          strokeLinecap="round"
        />
      )}
      {last && <circle cx={last.x} cy={last.y} r="5" fill={color} stroke="#FFFFFF" strokeWidth="2" />}
    </svg>
  )
}
