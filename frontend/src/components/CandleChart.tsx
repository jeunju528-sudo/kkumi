import type { StockCandleResponse } from '../types/api'

type CandleChartProps = {
  candles: StockCandleResponse[]
  livePrice: number
  slots: number
}

// 오르면 빨강, 내리면 파랑 (국내 증권사 관례, design.md --up / --down)
const UP_COLOR = '#D32F36'
const DOWN_COLOR = '#2563EB'

// 마지막(오늘) 일봉에 5초 실시간 가격 반영 (서버 일봉은 1분마다 갱신)
function withLivePrice(candles: StockCandleResponse[], livePrice: number): StockCandleResponse[] {
  if (candles.length === 0) return candles
  const today = candles[candles.length - 1]
  const live = {
    ...today,
    high: Math.max(today.high, livePrice),
    low: Math.min(today.low, livePrice),
    close: livePrice,
  }
  return [...candles.slice(0, -1), live]
}

// 일봉 차트: 칸마다 심지(고가~저가) + 몸통(시가~종가), 오른쪽 끝이 오늘
export function CandleChart({ candles, livePrice, slots }: CandleChartProps) {
  const shown = withLivePrice(candles.slice(-slots), livePrice)
  const lo = shown.length > 0 ? Math.min(...shown.map((c) => c.low)) : livePrice
  const hi = shown.length > 0 ? Math.max(...shown.map((c) => c.high)) : livePrice
  const span = Math.max(livePrice * 0.02, hi - lo) // 세로 폭 최소 2% (분봉 차트와 같은 기준)
  const slotWidth = 300 / slots
  const bodyWidth = Math.min(12, Math.max(2, slotWidth * 0.6)) // 일봉이 적어도 몸통이 너무 두껍지 않게
  const offset = slots - shown.length // 일봉이 모자라면 오른쪽 정렬 (오늘은 항상 오른쪽 끝)
  const toY = (v: number) => 92 - ((v - lo) / span) * 84

  return (
    <svg
      viewBox="0 0 300 100"
      width="100%"
      role="img"
      aria-label="최근 30일 일봉 그래프"
      preserveAspectRatio="none"
      className="k-chart"
    >
      <line
        x1="0"
        y1="99"
        x2="300"
        y2="99"
        stroke="#E6E8F0"
        strokeWidth="1.5"
        strokeDasharray="4 4"
        vectorEffect="non-scaling-stroke"
      />
      {shown.map((c, i) => {
        const centerX = (offset + i + 0.5) * slotWidth
        const color = c.close >= c.open ? UP_COLOR : DOWN_COLOR
        const bodyTop = toY(Math.max(c.open, c.close))
        const bodyBottom = toY(Math.min(c.open, c.close))
        return (
          <g key={c.date}>
            <line
              x1={centerX}
              y1={toY(c.high)}
              x2={centerX}
              y2={toY(c.low)}
              stroke={color}
              strokeWidth="1.5"
              vectorEffect="non-scaling-stroke"
            />
            <rect
              x={centerX - bodyWidth / 2}
              y={bodyTop}
              width={bodyWidth}
              height={Math.max(1, bodyBottom - bodyTop)}
              fill={color}
            />
          </g>
        )
      })}
    </svg>
  )
}
