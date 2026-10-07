import { useState } from 'react'
import type { CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatManwon, formatNumber, formatSignedKRW } from '../utils/format'
import type { HoldingResponse } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './HomePage.css'

type ChartPeriod = 'DAY' | 'WEEK' | 'MONTH'

// 목업 데이터
const SEED_MONEY = 5_000_000
const CASH = 4_210_000
const NET_WORTH = 5_230_000
const MARKET_CLOSE_LEFT = '02:14:30'
const GOAL = { region: '태백', complexName: '[단지명]', area: 39, priceManwon: 1800 }
const HOLDINGS: HoldingResponse[] = [
  { code: 'KK0001', name: '세르반반도체', quantity: 12, avgPrice: 49950, price: 51000 },
  { code: 'KK0002', name: '마르덴전자', quantity: 1, avgPrice: 251270, price: 248000 },
  { code: 'KK0003', name: '쿠르텍', quantity: 4, avgPrice: 39760, price: 40000 },
]
const CHART_PERIODS: { value: ChartPeriod; label: string }[] = [
  { value: 'DAY', label: '1일' },
  { value: 'WEEK', label: '1주' },
  { value: 'MONTH', label: '1달' },
]

// 소수 1자리 등락률: 0.046 → "+4.6%"
function toRateText(rate: number): string {
  return `${rate > 0 ? '+' : ''}${(rate * 100).toFixed(1)}%`
}

function cssVars(vars: Record<string, string>): CSSProperties {
  return vars as CSSProperties
}

export function HomePage() {
  const [chartPeriod, setChartPeriod] = useState<ChartPeriod>('DAY')

  const profit = NET_WORTH - SEED_MONEY
  const goalOwnAmount = (GOAL.priceManwon * 10000 * 4) / 10
  const goalLeft = Math.max(0, goalOwnAmount - NET_WORTH)
  const goalPct = `${Math.min(100, (NET_WORTH / goalOwnAmount) * 100).toFixed(1)}%`

  const cashChip = (
    <div className="k-chip">
      <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
      <div>
        <div className="k-lab">CASH</div>
        <div className="k-num k-num--cash">{formatNumber(CASH)}</div>
      </div>
    </div>
  )
  const clockChip = (
    <div className="k-chip">
      <Sprite id="px-clock" viewBox="0 0 8 8" width={16} height={16} />
      <div>
        <div className="k-clock-lab">장 마감까지</div>
        <div className="k-num k-num--clock">{MARKET_CLOSE_LEFT}</div>
      </div>
    </div>
  )

  return (
    <div className="page-home">
      <header className="k-top k-top--hero">
        <Link to="/home" className="k-logo">
          <span className="k-mark">꿈</span>
          <span>꿈이</span>
        </Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link k-on" aria-current="page">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" /></svg>
            <span>홈</span>
          </Link>
          <Link to="/trade" className="k-link">
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
        {cashChip}
        {clockChip}
      </header>

      <div className="k-scene">
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '40%', top: 70 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '5%', top: 96, opacity: 0.9 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '78%', top: 82, opacity: 0.9 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud k-wide" style={{ left: '62%', top: 40, opacity: 0.9 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud k-wide" style={{ left: '24%', top: 34 }} />

        <Sprite id="px-tree" viewBox="0 0 10 12" width={40} height={48} className="sp k-tree" style={{ left: '1%', bottom: 24 }} />
        <Sprite id="px-house" viewBox="0 0 22 18" width={66} height={54} className="sp k-house" style={{ left: '8%', bottom: 24 }} />
        <Sprite id="px-hanok" viewBox="0 0 24 14" width={72} height={42} className="sp k-hanok k-wide" style={{ left: '18%', bottom: 24 }} />
        <Sprite id="px-tower" viewBox="0 0 12 36" width={36} height={108} className="sp k-tower" style={{ left: '33%', bottom: 24 }} />
        <Sprite id="px-tower" viewBox="0 0 12 36" width={36} height={108} className="sp k-tower k-wide" style={{ left: '38%', bottom: 24, ...cssVars({ '--glass': '#3FB6A8' }) }} />
        <Sprite id="px-mansion" viewBox="0 0 30 20" width={90} height={60} className="sp k-mansion" style={{ left: '43%', bottom: 24 }} />
        <Sprite id="px-apt-low" viewBox="0 0 16 20" width={48} height={60} className="sp k-alow k-wide" style={{ left: '54%', bottom: 24, ...cssVars({ '--roof': '#3E9B3E' }) }} />
        <Sprite id="px-villa" viewBox="0 0 18 16" width={54} height={48} className="sp k-villa" style={{ left: '66%', bottom: 24 }} />
        <Sprite id="px-apt-mid" viewBox="0 0 14 26" width={42} height={78} className="sp k-amid k-wide" style={{ left: '74%', bottom: 24, ...cssVars({ '--roof': '#8E5BD6' }) }} />
        <Sprite id="px-house" viewBox="0 0 22 18" width={66} height={54} className="sp k-house k-wide" style={{ left: '79%', bottom: 24, ...cssVars({ '--roof': '#3B82F6', '--wall': '#FFE3E6' }) }} />
        <Sprite id="px-tree" viewBox="0 0 10 12" width={40} height={48} className="sp k-tree" style={{ left: '88%', bottom: 24 }} />
        <div className="k-grass" />
        <div className="k-soil" />

        <div className="k-hero-hud">
          {cashChip}
          {clockChip}
        </div>
      </div>

      <main className="k-wrap">
        <div className="k-cols">
          <div className="k-col">
            <section className="k-px" aria-label="내 평가액">
              <div className="k-px-head">
                <div className="k-px-label">내 평가액</div>
                <div className="k-lab k-lab--gold">NET WORTH</div>
              </div>
              <div className="k-net">{formatKRW(NET_WORTH)}</div>
              <div className="k-profit">
                <svg width="16" height="16" viewBox="0 0 8 8" style={{ shapeRendering: 'crispEdges' }} aria-hidden="true">
                  <path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM3 3h2v5h-2z" style={{ fill: '#FF7A7E' }} />
                </svg>
                <div className="k-profit-num">
                  {formatSignedKRW(profit)} ({toRateText(profit / SEED_MONEY)})
                </div>
                <div className="k-profit-cap">시드머니 대비</div>
              </div>
              <Link to="/trade/history" className="k-px-link">
                <span>실현손익 · 거래내역 보기</span>
                <span className="k-px-link-arrow">&gt;</span>
              </Link>
            </section>

            <section className="k-quest" aria-label="오늘의 목표">
              <div className="k-quest-head">
                <svg width="16" height="16" viewBox="0 0 8 8" style={{ shapeRendering: 'crispEdges' }} aria-hidden="true">
                  <path d="M3 0h2v5h-2zM3 6h2v2h-2z" style={{ fill: '#E0A800' }} />
                </svg>
                <div className="k-quest-title">첫 집 목표</div>
                <div className="k-lab k-lab--sub">QUEST</div>
              </div>
              <div className="k-quest-info">
                {GOAL.region} {GOAL.complexName} {GOAL.area}㎡ · {formatManwon(GOAL.priceManwon)} · 내 돈 40%{' '}
                {formatManwon((GOAL.priceManwon * 4) / 10)}
              </div>
              <div className="k-quest-left">계약금까지 {formatKRW(goalLeft)} 남았습니다. 조금만 더 존버!</div>
              <div className="k-bar-row">
                <div className="k-bar">
                  <div className="k-bar-fill" style={{ width: goalPct }} />
                  <div className="k-stripe k-bar-stripe" />
                </div>
                <div className="k-bar-pct">{goalPct}</div>
              </div>
            </section>

            <section className="k-card" aria-label="내 손익">
              <div className="k-card-head">
                <div className="k-card-title">내 손익</div>
                <div className="k-tabs">
                  {CHART_PERIODS.map((p) => (
                    <button
                      key={p.value}
                      type="button"
                      className={`k-tab${chartPeriod === p.value ? ' k-tab--on' : ''}`}
                      aria-pressed={chartPeriod === p.value}
                      onClick={() => setChartPeriod(p.value)}
                    >
                      <span>{p.label}</span>
                    </button>
                  ))}
                </div>
              </div>
              <svg viewBox="0 0 326 120" width="100%" role="img" aria-label="최근 5일 평가액 추이 그래프, 우상향" className="k-chart">
                <line x1="0" y1="100" x2="326" y2="100" stroke="#E6E8F0" strokeWidth="1.5" strokeDasharray="4 4" />
                <polyline points="0,92 54,84 108,96 162,70 216,60 270,44 322,30" fill="none" stroke="#E5484D" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" />
                <circle cx="322" cy="30" r="5" fill="#E5484D" stroke="#FFFFFF" strokeWidth="2" />
                <text x="2" y="114" fontSize="11" fill="#6B6F80" fontFamily="Noto Sans KR, sans-serif">
                  시드머니 {formatKRW(SEED_MONEY)}
                </text>
              </svg>
              <div className="k-days">
                <span>월</span>
                <span>화</span>
                <span>수</span>
                <span>목</span>
                <span>금</span>
              </div>
            </section>
          </div>

          <div className="k-col">
            <section className="k-card k-holdings" aria-label="보유 종목">
              <div className="k-holdings-head">
                <div className="k-card-title">보유 종목</div>
                <Link to="/trade" className="k-more">종목 거래하기 &gt;</Link>
              </div>
              {HOLDINGS.map((h) => {
                const rate = (h.price - h.avgPrice) / h.avgPrice
                return (
                  <Link key={h.code} to="/trade" className="k-row">
                    <div className="k-tick">{h.name.charAt(0)}</div>
                    <div className="k-row-main">
                      <div className="k-row-name">{h.name}</div>
                      <div className="k-row-sub">
                        {h.quantity}주 · {formatKRW(h.price)}
                      </div>
                    </div>
                    <div className="k-row-right">
                      <div className="k-row-value">{formatKRW(h.price * h.quantity)}</div>
                      <div className={`k-row-rate ${rate >= 0 ? 'k-up' : 'k-down'}`}>{toRateText(rate)}</div>
                    </div>
                  </Link>
                )
              })}
            </section>

            <section className="k-card k-stage" aria-label="꿈이 변신 단계">
              <div className="k-avatar">
                <img src={tier1} alt="걸뱅이 꿈이" />
              </div>
              <div className="k-stage-body">
                <div className="k-lab k-lab--sub">STAGE 1 / 4</div>
                <div className="k-stage-title">지금은 걸뱅이 꿈이</div>
                <div className="k-stage-desc">집 1채만 사도 서민으로 변신해요.</div>
                <Link to="/tiers" className="k-stage-link">변신 4단계 보기</Link>
              </div>
            </section>

            <Link to="/homes" className="k-btn">집 보러 가기</Link>
          </div>
        </div>
        <p className="k-note">※ 샘플 데이터예요. 전부 가짜 돈이라 잃을 게 없어요.</p>
      </main>
    </div>
  )
}
