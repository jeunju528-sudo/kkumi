import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatNumber, formatRate, formatSignedKRW } from '../utils/format'
import type { TradeHistoryResponse, TradeType } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './TradeHistoryPage.css'

type HistoryTab = 'LIST' | 'BY_STOCK'
type Period = 'WEEK' | 'MONTH' | 'QUARTER' | 'ALL'
type KindFilter = 'ALL' | TradeType

type MockTrade = TradeHistoryResponse & {
  costBasis: number // 매도 수량 × 평균 매입가 (수익률 계산용)
}

type StockProfit = {
  code: string
  name: string
  profit: number
  costBasis: number
  sellCount: number
}

// 목업 데이터. 실제 연결 시 API 응답으로 교체
const CASH = 4_210_000
const TODAY = new Date('2026-10-07T23:59:59')
const TOTAL_PROFIT = 230_000 // 홈 평가액 카드의 시드 대비 손익
const TRADES: MockTrade[] = [
  { id: 8, code: 'KK0003', name: '쿠르텍', tradeType: 'SELL', price: 40200, quantity: 2, realizedProfit: 2400, tradedAt: '2026-10-07T14:12:00', costBasis: 78000 },
  { id: 7, code: 'KK0002', name: '마르덴전자', tradeType: 'BUY', price: 248000, quantity: 1, realizedProfit: null, tradedAt: '2026-10-07T10:03:00', costBasis: 0 },
  { id: 6, code: 'KK0004', name: '브론델배터리', tradeType: 'SELL', price: 181500, quantity: 3, realizedProfit: -25500, tradedAt: '2026-10-06T15:40:00', costBasis: 570000 },
  { id: 5, code: 'KK0003', name: '쿠르텍', tradeType: 'BUY', price: 39000, quantity: 6, realizedProfit: null, tradedAt: '2026-10-06T09:21:00', costBasis: 0 },
  { id: 4, code: 'KK0013', name: '라미로보틱스', tradeType: 'SELL', price: 131000, quantity: 2, realizedProfit: 38000, tradedAt: '2026-10-05T13:05:00', costBasis: 224000 },
  { id: 3, code: 'KK0004', name: '브론델배터리', tradeType: 'BUY', price: 190000, quantity: 3, realizedProfit: null, tradedAt: '2026-10-04T11:30:00', costBasis: 0 },
  { id: 2, code: 'KK0013', name: '라미로보틱스', tradeType: 'BUY', price: 112000, quantity: 2, realizedProfit: null, tradedAt: '2026-10-04T10:10:00', costBasis: 0 },
  { id: 1, code: 'KK0001', name: '세르반반도체', tradeType: 'BUY', price: 50500, quantity: 12, realizedProfit: null, tradedAt: '2026-09-28T09:00:00', costBasis: 0 },
]

const PERIODS: { value: Period; label: string; title: string; days: number }[] = [
  { value: 'WEEK', label: '1주', title: '최근 1주', days: 7 },
  { value: 'MONTH', label: '1달', title: '최근 1달', days: 31 },
  { value: 'QUARTER', label: '3달', title: '최근 3달', days: 92 },
  { value: 'ALL', label: '전체', title: '전체 기간', days: Number.POSITIVE_INFINITY },
]
const KINDS: { value: KindFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'BUY', label: '매수' },
  { value: 'SELL', label: '매도' },
]
const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
const DAY_MS = 86_400_000

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

  // 목업 계산. 실제는 기간을 API 파라미터로 넘기고 합계는 응답으로 받음
  const periodInfo = PERIODS.find((p) => p.value === period) ?? PERIODS[0]
  const inPeriod = TRADES.filter((t) => (TODAY.getTime() - new Date(t.tradedAt).getTime()) / DAY_MS < periodInfo.days)
  const sells = inPeriod.filter((t) => t.tradeType === 'SELL')
  const realized = sells.reduce((sum, t) => sum + (t.realizedProfit ?? 0), 0)
  const soldCost = sells.reduce((sum, t) => sum + t.costBasis, 0)
  const realizedRate = soldCost > 0 ? realized / soldCost : 0
  const buyTotal = inPeriod.filter((t) => t.tradeType === 'BUY').reduce((sum, t) => sum + t.price * t.quantity, 0)
  const sellTotal = sells.reduce((sum, t) => sum + t.price * t.quantity, 0)

  const allRealized = TRADES.reduce((sum, t) => sum + (t.realizedProfit ?? 0), 0)
  const unrealized = TOTAL_PROFIT - allRealized

  const shown = inPeriod.filter((t) => kind === 'ALL' || t.tradeType === kind)
  const dayGroups: { label: string; trades: MockTrade[] }[] = []
  shown.forEach((t) => {
    const label = toDayLabel(t.tradedAt)
    const last = dayGroups[dayGroups.length - 1]
    if (last && last.label === label) {
      last.trades.push(t)
    } else {
      dayGroups.push({ label, trades: [t] })
    }
  })

  const profitMap = new Map<string, StockProfit>()
  sells.forEach((t) => {
    const prev = profitMap.get(t.code) ?? { code: t.code, name: t.name, profit: 0, costBasis: 0, sellCount: 0 }
    profitMap.set(t.code, {
      ...prev,
      profit: prev.profit + (t.realizedProfit ?? 0),
      costBasis: prev.costBasis + t.costBasis,
      sellCount: prev.sellCount + 1,
    })
  })
  const stockProfits = [...profitMap.values()].sort((a, b) => b.profit - a.profit)

  const isListTab = tab === 'LIST'
  const isEmpty = isListTab ? dayGroups.length === 0 : stockProfits.length === 0

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
          <Link to="/homes" className="k-link">
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
            <div className="k-num k-num--cash">{formatNumber(CASH)}</div>
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
              <div className="k-stat-value">{inPeriod.length}건</div>
            </div>
          </div>
        </section>

        <section className="k-card k-sum" aria-label="총손익 구성">
          <span className="k-sum-item">
            <span>총손익(시드 대비)</span>
            <b className={toProfitClass(TOTAL_PROFIT)}>{toProfitText(TOTAL_PROFIT)}</b>
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

        <p className="k-note">※ 거래와 금액은 샘플이에요. 전부 가짜 돈이에요.</p>
      </main>
    </div>
  )
}
