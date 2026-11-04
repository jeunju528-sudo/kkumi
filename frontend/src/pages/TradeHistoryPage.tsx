import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { useMemberQuery } from '../hooks/useMemberQuery'
import { useStockProfitsQuery } from '../hooks/useStockProfitsQuery'
import { useTradeHistoryQuery } from '../hooks/useTradeHistoryQuery'
import { usePortfolioQuery } from '../hooks/usePortfolioQuery'
import { useTradeHistorySummaryQuery } from '../hooks/useTradeHistorySummaryQuery'
import { formatKRW, formatNumber, formatRate, formatSignedKRW } from '../utils/format'
import type { Period, TradeHistoryResponse, TradeType } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './TradeHistoryPage.css'

type HistoryTab = 'LIST' | 'BY_STOCK'
type KindFilter = 'ALL' | TradeType

const PERIODS: { value: Period; label: string; title: string }[] = [
  { value: 'WEEK', label: '1주', title: '최근 1주' },
  { value: 'MONTH', label: '1달', title: '최근 1달' },
  { value: 'QUARTER', label: '3달', title: '최근 3달' },
  { value: 'ALL', label: '전체', title: '전체 기간' },
]

const KINDS: { value: KindFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'BUY', label: '매수' },
  { value: 'SELL', label: '매도' },
]
const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// "▲ +14,900원" / "▼ -25,500원" / "0원"
function toProfitText(won: number): string {
  const arrow = won > 0 ? '▲ ' : won < 0 ? '▼ ' : ''
  return `${arrow}${formatSignedKRW(won)}`
}

function toProfitClass(won: number): string {
  return won > 0 ? 'k-up' : won < 0 ? 'k-down' : ''
}

// "2026-10-07T..." → "10.07 (수)"
function toDayLabel(iso: string): string {
  const d = new Date(iso)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}.${dd} (${WEEKDAYS[d.getDay()]})`
}

export function TradeHistoryPage() {
  const [tab, setTab] = useState<HistoryTab>('LIST')
  const [period, setPeriod] = useState<Period>('WEEK')
  const [kind, setKind] = useState<KindFilter>('ALL')
  // 'ALL'은 화면 전용 값, api에는 null로 (type 생략 = 전체)
  const tradeType = kind === 'ALL' ? null : kind
  const { data: member } = useMemberQuery()
  const { data: summary } = useTradeHistorySummaryQuery(period)
  const { data: portfolio } = usePortfolioQuery()
  // 총손익 구성은 기간 필터와 무관하게 전체 기간 실현손익
  const { data: allSummary } = useTradeHistorySummaryQuery('ALL')
  const historyQuery = useTradeHistoryQuery(period, tradeType)
  const stockProfitsQuery = useStockProfitsQuery(period)

  const cash = member?.cash ?? 0
  const periodInfo = PERIODS.find((p) => p.value === period) ?? PERIODS[0]
  // 첫 응답 전엔 data가 undefined라 0으로 시작
  const realized = summary?.realizedProfit ?? 0
  const soldCost = summary?.soldCostBasis ?? 0
  const realizedRate = soldCost > 0 ? realized / soldCost : 0
  const buyTotal = summary?.buyTotal ?? 0
  const sellTotal = summary?.sellTotal ?? 0
  const tradeCount = summary?.tradeCount ?? 0

  const totalProfit = portfolio?.profit ?? 0
  const allRealized = allSummary?.realizedProfit ?? 0
  // 평가손익은 역산 (총손익 − 실현손익)
  const unrealized = totalProfit - allRealized

  const trades = historyQuery.data?.items ?? []
  const dayGroups: { label: string; trades: TradeHistoryResponse[] }[] = []
  trades.forEach((t) => {
    const label = toDayLabel(t.tradedAt)
    const last = dayGroups[dayGroups.length - 1]
    if (last && last.label === label) {
      last.trades.push(t)
    } else {
      dayGroups.push({ label, trades: [t] })
    }
  })

  const stockProfits = stockProfitsQuery.data ?? []

  const isListTab = tab === 'LIST'
  const isLoading = isListTab ? historyQuery.isPending : stockProfitsQuery.isPending
  const isLoadError = isListTab ? historyQuery.isError : stockProfitsQuery.isError
  // 로딩 중 빈 배열일 때 "사고판 게 없어요"가 깜빡이지 않게
  const isEmpty = !isLoading && !isLoadError && (isListTab ? dayGroups.length === 0 : stockProfits.length === 0)

  const handleListTabClick = () => setTab('LIST')
  const handleStockTabClick = () => setTab('BY_STOCK')

  return (
    <div className="page-history">
      <header className="k-top">
        <Link to="/home" className="k-logo">
          <span className="k-mark">꿈</span>
          <span>꿈이</span>
        </Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" /></svg>
            <span>홈</span>
          </Link>
          <Link to="/trade" className="k-link k-on" aria-current="page">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" /></svg>
            <span>투자</span>
          </Link>
          <Link to="/apartments" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" /></svg>
            <span>집 찾기</span>
          </Link>
          <Link to="/village" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z" /></svg>
            <span>내 마을</span>
          </Link>
        </nav>
        <div className="k-grow" />
        <div className="k-chip">
          <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
          <div>
            <div className="k-lab">CASH</div>
            <div className="k-num k-num--cash">{formatNumber(cash)}</div>
          </div>
        </div>
      </header>

      <main className="k-wrap">
        <div className="k-head">
          <Link to="/trade" className="k-back">&lt; 종목 목록</Link>
          <h1 className="k-title">내 거래</h1>
        </div>

        <section className="k-px" aria-label="실현손익 요약">
          <div className="k-px-head">
            <div className="k-px-label">{periodInfo.title} 실현손익 (판 것만)</div>
            <div className="k-lab k-lab--gold">PROFIT</div>
          </div>
          <div className={`k-realized ${toProfitClass(realized)}`}>
            <span className="k-realized-won">{toProfitText(realized)}</span>
            <span className="k-realized-rate">{formatRate(realizedRate)}</span>
          </div>
          <div className="k-stats">
            <div>
              <div className="k-stat-label">매수 금액</div>
              <div className="k-stat-value">{formatKRW(buyTotal)}</div>
            </div>
            <div>
              <div className="k-stat-label">매도 금액</div>
              <div className="k-stat-value">{formatKRW(sellTotal)}</div>
            </div>
            <div>
              <div className="k-stat-label">거래</div>
              <div className="k-stat-value">{tradeCount}건</div>
            </div>
          </div>
        </section>

        <section className="k-card k-sum" aria-label="총손익 구성">
          <span className="k-sum-item">
            <span>총손익(시드 대비)</span>
            <b className={toProfitClass(totalProfit)}>{toProfitText(totalProfit)}</b>
          </span>
          <span className="k-sum-item">
            <span>= 실현</span>
            <b>{formatSignedKRW(allRealized)}</b>
          </span>
          <span className="k-sum-item">
            <span>+ 보유 종목 평가</span>
            <b>{formatSignedKRW(unrealized)}</b>
          </span>
        </section>

        <section className="k-card k-list" aria-label="거래 목록">
          <div className="k-tabs" role="tablist">
            <button type="button" role="tab" aria-selected={isListTab} onClick={handleListTabClick} className={`k-tab${isListTab ? ' k-tab--on' : ''}`}>
              거래내역
            </button>
            <button type="button" role="tab" aria-selected={!isListTab} onClick={handleStockTabClick} className={`k-tab${isListTab ? '' : ' k-tab--on'}`}>
              종목별 손익
            </button>
          </div>

          <div className="k-filters">
            <div className="k-seg" aria-label="기간">
              {PERIODS.map((p) => (
                <button
                  key={p.value}
                  type="button"
                  aria-pressed={period === p.value}
                  onClick={() => setPeriod(p.value)}
                  className={`k-pill${period === p.value ? ' k-pill--on' : ''}`}
                >
                  {p.label}
                </button>
              ))}
            </div>
            {isListTab && (
              <div className="k-seg" aria-label="구분">
                {KINDS.map((k) => (
                  <button
                    key={k.value}
                    type="button"
                    aria-pressed={kind === k.value}
                    onClick={() => setKind(k.value)}
                    className={`k-pill${kind === k.value ? ' k-pill--on' : ''}`}
                  >
                    {k.label}
                  </button>
                ))}
              </div>
            )}
          </div>
          {isLoading && <div className="k-row-sub">불러오는 중...</div>}
          {isLoadError && <div className="k-row-sub">거래내역을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</div>}
          {isListTab &&
            dayGroups.map((g) => (
              <div key={g.label}>
                <div className="k-day">{g.label}</div>
                {g.trades.map((t) => {
                  const isSell = t.tradeType === 'SELL'
                  return (
                    <div key={t.id} className="k-row">
                      <div className="k-tick">{t.name.charAt(0)}</div>
                      <div className="k-row-main">
                        <div className="k-row-name">
                          <span className={`k-badge${isSell ? ' k-badge--sell' : ''}`}>{isSell ? '매도' : '매수'}</span>
                          {t.name}
                        </div>
                        <div className="k-row-sub">
                          {t.quantity}주 × {formatKRW(t.price)}
                        </div>
                      </div>
                      <div className="k-row-right">
                        <div className="k-row-amount">{formatKRW(t.price * t.quantity)}</div>
                        {isSell && t.realizedProfit !== null && (
                          <div className={`k-row-profit ${toProfitClass(t.realizedProfit)}`}>실현 {toProfitText(t.realizedProfit)}</div>
                        )}
                      </div>
                    </div>
                  )
                })}
              </div>
            ))}

          {!isListTab &&
            stockProfits.map((s) => (
              <div key={s.code} className="k-row">
                <div className="k-tick">{s.name.charAt(0)}</div>
                <div className="k-row-main">
                  <div className="k-row-name">{s.name}</div>
                  <div className="k-row-sub">
                    매도 {s.sellCount}회 · {s.code}
                  </div>
                </div>
                <div className={`k-row-right ${toProfitClass(s.profit)}`}>
                  <div className="k-row-amount">{toProfitText(s.profit)}</div>
                  <div className="k-row-profit">{formatRate(s.profit / s.costBasis)}</div>
                </div>
              </div>
            ))}

          {isEmpty && (
            <div className="k-empty">
              <img src={tier1} alt="걸뱅이 꿈이" />
              <div>이 기간엔 사고판 게 없어요.</div>
              <Link to="/trade" className="k-empty-cta">종목 보러 가기</Link>
            </div>
          )}
        </section>

      </main>
    </div>
  )
}
