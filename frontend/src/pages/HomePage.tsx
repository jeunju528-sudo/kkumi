import { useState } from 'react'
import type { CSSProperties } from 'react'
import { usePortfolioQuery } from '../hooks/usePortfolioQuery'
import { usePortfolioHistoryQuery } from '../hooks/usePortfolioHistoryQuery'
import { useMidnightCountdown } from '../hooks/useMidnightCountdown'
import { useMyHousesQuery } from '../hooks/useMyHousesQuery'
import { useAptGoalQuery } from '../hooks/useAptGoalQuery'
import { ValuationChart } from '../components/ValuationChart'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatManwon, formatNumber } from '../utils/format'
import { NetWorthSummary } from '../components/NetWorthSummary'
import { addDays, daysBetween, toAxisLabels, toDateKey } from '../utils/chart'
import type { ChartPoint } from '../utils/chart'
import type { ChartPeriod } from '../types/api'
import { TIER_INFO } from '../utils/tier'
import './HomePage.css'

// 손익 차트 기간: 1일 탭 없음, 일별 기록 + 오늘 현재값 (ADR 049)
const CHART_PERIODS: { value: ChartPeriod; label: string }[] = [
  { value: 'WEEK', label: '1주' },
  { value: 'MONTH', label: '1달' },
]
// 오늘 앞 지난 날 수 (백엔드 ChartPeriod와 같은 값)
const CHART_PAST_DAYS: Record<ChartPeriod, number> = { WEEK: 6, MONTH: 29 }

// 소수 1자리 등락률: 0.046 → "+4.6%"
function toRateText(rate: number): string {
  return `${rate > 0 ? '+' : ''}${(rate * 100).toFixed(1)}%`
}

// 손익 부호 → 색 구분 (design.md --up / --down, 0은 회색)
function toProfitTone(profit: number): 'up' | 'down' | 'flat' {
  if (profit > 0) return 'up'
  if (profit < 0) return 'down'
  return 'flat'
}

function cssVars(vars: Record<string, string>): CSSProperties {
  return vars as CSSProperties
}

export function HomePage() {
  const [chartPeriod, setChartPeriod] = useState<ChartPeriod>('WEEK')
  const { data: portfolio, isError } = usePortfolioQuery()
  const { data: myHouses } = useMyHousesQuery()
  const { data: goal, isError: isGoalError } = useAptGoalQuery()
  const { data: history = [] } = usePortfolioHistoryQuery(chartPeriod)
  // 0시에 오늘 시작가·상하한·일별 기록이 바뀜 → 장 시간 대신 하루 마감 (ADR 050)
  const dayCloseLeft = useMidnightCountdown()

  const cash = portfolio?.cash ?? 0
  const totalAsset = portfolio?.totalAsset ?? 0
  const holdings = portfolio?.holdings ?? []

  const tier = myHouses?.tier ?? 'GEOLBAENGI'
  const tierInfo = TIER_INFO[tier]

  // 목표 패널: 0채면 첫 집, 1채 이상이면 다음 집. 진행 바 = 평가액 / 목표 집 계약금(40%)
  const isGoalLoaded = goal !== undefined
  const goalTitle = (myHouses?.houseCount ?? 0) === 0 ? '첫 집 목표' : '다음 집 목표'
  const goalLeft = goal ? Math.max(0, goal.ownAmount - totalAsset) : 0
  const isGoalReached = goalLeft === 0
  const goalPct = goal ? `${Math.min(100, (totalAsset / goal.ownAmount) * 100).toFixed(1)}%` : '100.0%'

  // 차트 칸: 기간 첫날 ~ 오늘. 지난 날은 일별 기록, 마지막 칸은 WebSocket 평가액(5초마다 갱신)
  const pastDays = CHART_PAST_DAYS[chartPeriod]
  const today = toDateKey(new Date())
  const chartFrom = addDays(today, -pastDays)
  const chartPoints: ChartPoint[] = history
    .filter((h) => h.date >= chartFrom && h.date < today)
    .map((h) => ({ slot: daysBetween(chartFrom, h.date), value: h.netWorth }))
  if (portfolio) chartPoints.push({ slot: pastDays, value: portfolio.netWorth })
  const axisLabels = toAxisLabels(chartFrom, pastDays + 1)
  const chartPeriodLabel = chartPeriod === 'WEEK' ? '1주' : '1달'

  const cashChip = (
    <div className="k-chip">
      <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
      <div>
        <div className="k-lab">CASH</div>
        <div className="k-num k-num--cash">{formatNumber(cash)}</div>
      </div>
    </div>
  )
  const clockChip = (
    <div className="k-chip">
      <Sprite id="px-clock" viewBox="0 0 8 8" width={16} height={16} />
      <div>
        <div className="k-clock-lab">오늘 마감까지</div>
        <div className="k-num k-num--clock">{dayCloseLeft}</div>
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
            <section className="k-px" aria-label="내 순자산">
              <div className="k-px-head">
                <div className="k-px-label">내 순자산</div>
                <div className="k-lab k-lab--gold">NET WORTH</div>
              </div>
              {portfolio ? (
                <NetWorthSummary
                  netWorth={portfolio.netWorth}
                  dailyChange={portfolio.dailyChange}
                  dailyChangeRate={portfolio.dailyChangeRate}
                  houseEquity={portfolio.houseEquity}
                  stockValue={portfolio.stockValue}
                  cash={portfolio.cash}
                />
              ) : (
                <div className="k-profit-cap">
                  {isError ? '순자산을 못 불러왔어요. 잠시 후 다시 시도해 주세요.' : '불러오는 중...'}
                </div>
              )}
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
                <div className="k-quest-title">{goalTitle}</div>
                <div className="k-lab k-lab--sub">QUEST</div>
              </div>
              {isGoalLoaded ? (
                <>
                  {goal && (
                    <div className="k-quest-info">
                      {goal.region} {goal.complexName} {goal.area}㎡ · {formatManwon(goal.price / 10000)} · 내 돈 40%{' '}
                      {formatKRW(goal.ownAmount)}
                    </div>
                  )}
                  {isGoalReached ? (
                    <>
                      <div className="k-quest-left">계약금 모았어요! 집 사러 가요.</div>
                      <Link to="/apartments" className="k-quest-link">
                        <span>부동산 가기</span>
                        <span className="k-quest-link-arrow">&gt;</span>
                      </Link>
                    </>
                  ) : (
                    <>
                      <div className="k-quest-left">계약금까지 {formatKRW(goalLeft)} 남았습니다. 조금만 더 존버!</div>
                      <div className="k-bar-row">
                        <div className="k-bar">
                          <div className="k-bar-fill" style={{ width: goalPct }} />
                          <div className="k-stripe k-bar-stripe" />
                        </div>
                        <div className="k-bar-pct">{goalPct}</div>
                      </div>
                    </>
                  )}
                </>
              ) : (
                <div className="k-quest-info">{isGoalError ? '목표를 못 불러왔어요.' : '불러오는 중...'}</div>
              )}
            </section>

            <section className="k-card" aria-label="순자산 추이">
              <div className="k-card-head">
                <div className="k-card-title">순자산 추이</div>
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
              {/* 기록 0개면 점 하나뿐이라 빈 차트 대신 안내 (첫 기록은 다음 0시) */}
              {portfolio && history.length === 0 && (
                <div className="k-chart-empty">
                  <div className="k-chart-empty-title">평가액이 쌓이면 그래프로 한눈에 볼 수 있어요</div>
                  <div>매일 0시에 그날 평가액이 기록돼요</div>
                </div>
              )}
              {portfolio && history.length > 0 && (
                <ValuationChart
                  points={chartPoints}
                  slotCount={pastDays + 1}
                  baseline={portfolio.initialCash}
                  tone={toProfitTone(portfolio.profit)}
                  label={`최근 ${chartPeriodLabel} 순자산 추이 그래프, 점선은 시드머니`}
                />
              )}
              {portfolio && history.length > 0 && (
                <>
                  <div className="k-chart-base">점선 시드머니 {formatKRW(portfolio.initialCash)}</div>
                  <div className="k-days">
                    {axisLabels.map((day, i) => (
                      <span key={i}>{day}</span>
                    ))}
                  </div>
                </>
              )}
            </section>
          </div>

          <div className="k-col">
            <section className="k-card k-holdings" aria-label="보유 종목">
              <div className="k-holdings-head">
                <div className="k-card-title">보유 종목</div>
                <Link to="/trade" className="k-more">종목 거래하기 &gt;</Link>
              </div>
              {portfolio && holdings.length === 0 && <div className="k-row-sub">아직 산 종목이 없어요.</div>}
              {holdings.map((h) => {
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
                <img src={tierInfo.image} alt={`${tierInfo.name} 꿈이`} />
              </div>
              <div className="k-stage-body">
                <div className="k-lab k-lab--sub">STAGE {tierInfo.stage} / 4</div>
                <div className="k-stage-title">지금은 {tierInfo.name} 꿈이</div>
                <div className="k-stage-desc">{tierInfo.nextText}</div>
                <Link to="/tiers" className="k-stage-link">변신 4단계 보기</Link>
              </div>
            </section>

            {/* 보유 종목 0개(신규)면 첫 행동은 종목 사기, 집 화면은 "모자라요"만 보임 */}
            {portfolio && holdings.length === 0 ? (
              <Link to="/trade" className="k-btn">첫 종목 사러 가기</Link>
            ) : (
              <Link to="/apartments" className="k-btn">집 보러 가기</Link>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
