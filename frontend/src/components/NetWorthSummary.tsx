import { formatKRW, formatShortRate, formatSignedKRW } from '../utils/format'
import { toAssetShares } from '../utils/portfolio'

type NetWorthSummaryProps = {
  netWorth: number
  dailyChange: number | null
  dailyChangeRate: number | null
  houseEquity: number
  stockValue: number
  cash: number
}

// 픽셀 화살표 (8x8)
const ARROW_UP = 'M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM3 3h2v5h-2z'
const ARROW_DOWN = 'M3 0h2v5h-2zM1 5h6v1h-6zM2 6h4v1h-4zM3 7h2v1h-2z'

// 증감 부호 → 색 구분 (design.md --up / --down, 0·첫날은 회색)
function toTone(change: number | null): 'up' | 'down' | 'flat' {
  if (change === null || change === 0) return 'flat'
  return change > 0 ? 'up' : 'down'
}

// 홈 순자산 카드 본문: 순자산 + 오늘 증감 + 자산 구성 막대 (ADR 054)
export function NetWorthSummary({ netWorth, dailyChange, dailyChangeRate, houseEquity, stockValue, cash }: NetWorthSummaryProps) {
  const tone = toTone(dailyChange)
  const shares = toAssetShares(houseEquity, stockValue, cash)
  const mixLabel = shares.map((s) => `${s.label} ${s.percent}%`).join(', ')

  return (
    <>
      <div className="k-net">{formatKRW(netWorth)}</div>
      <div className={`k-profit k-profit--${tone}`}>
        {tone !== 'flat' && dailyChange !== null && (
          <svg width="16" height="16" viewBox="0 0 8 8" style={{ shapeRendering: 'crispEdges' }} aria-hidden="true">
            <path d={dailyChange > 0 ? ARROW_UP : ARROW_DOWN} fill="currentColor" />
          </svg>
        )}
        {dailyChange === null ? (
          <div className="k-profit-num">오늘이 첫날이에요</div>
        ) : (
          <>
            <div className="k-profit-num">
              오늘 {formatSignedKRW(dailyChange)}
              {dailyChangeRate !== null && ` (${formatShortRate(dailyChangeRate)})`}
            </div>
            <div className="k-profit-cap">어제 마감 대비</div>
          </>
        )}
      </div>

      <div className="k-mix">
        <div className="k-mix-bar" role="img" aria-label={`자산 구성 ${mixLabel}`}>
          {shares.map((s) => (
            <div key={s.kind} className={`k-mix-seg k-mix--${s.kind}`} style={{ width: `${s.percent}%` }} />
          ))}
        </div>
        <ul className="k-mix-legend">
          {shares.map((s) => (
            <li key={s.kind} className="k-mix-item">
              <div className="k-mix-name">
                <span className={`k-mix-dot k-mix--${s.kind}`} aria-hidden="true" />
                {s.label} {s.percent}%
              </div>
              <div className="k-mix-amt">{formatKRW(s.amount)}</div>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
