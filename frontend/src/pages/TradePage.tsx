import { useState } from "react"
import { Link } from "react-router-dom"
import { Sprite } from "../components/Sprite" // 픽셀 그림 하나를 그려주는 컴포넌트
import { formatKRW, formatNumber } from "../utils/format"
import { useMemberQuery } from "../hooks/useMemberQuery"
import { useStocksQuery } from "../hooks/useStocksQuery"
import { useHoldingsQuery } from "../hooks/useHoldingsQuery"
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './TradePage.css'

type StockTab = 'MINE' | 'ALL'

function toChangeText(changeRate: number): string {
  const pct = (changeRate * 100).toFixed(1)
  return changeRate >= 0 ? `▲ +${pct}%` : `▼ ${pct}%`
}

export function TradePage() {
  const [tab, setTab] = useState<StockTab>('MINE')
  const { data: member } = useMemberQuery()
  const { data: stocks = [], isPending, isError } = useStocksQuery()
  const { data: holdings = [] } = useHoldingsQuery()

  const cash = member?.cash ?? 0
  const quantityByCode = new Map(holdings.map((h) => [h.code, h.quantity]))
  const allRows = stocks.map((s) => ({ ...s, quantity: quantityByCode.get(s.code) ?? 0 }))

  const isMineTab = tab === 'MINE'
  const rows = isMineTab ? allRows.filter((s) => s.quantity > 0) : allRows
  const stockValue = allRows.reduce((sum, s) => sum + s.price * s.quantity, 0)

  const handleMineClick = () => setTab('MINE')
  const handleAllClick = () => setTab('ALL')

  return (
    <div className="page-trade">
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
        <div className="k-title-row">
          <h1 className="k-title">종목</h1>
          <div className="k-live">
            <span className="k-live-dot" />
            <span className="k-lab">LIVE</span>
          </div>
        </div>

        <div className="k-cols">
          <div className="k-col">
            <Link to="/trade/search" aria-label="종목 검색하러 가기" className="k-search">
              <svg width="20" height="20" viewBox="0 0 8 8" aria-hidden="true">
                <path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" />
              </svg>
              <span>종목 이름이나 코드로 찾기</span>
            </Link>

            <section className="k-card k-list" aria-label="종목 목록">
              <div className="k-tabs">
                <button type="button" onClick={handleMineClick} aria-pressed={isMineTab} className={`k-tab${isMineTab ? ' k-tab--on' : ''}`}>
                  내 종목
                </button>
                <button type="button" onClick={handleAllClick} aria-pressed={!isMineTab} className={`k-tab${isMineTab ? '' : ' k-tab--on'}`}>
                  전체
                </button>
              </div>
              <div className="k-list-body">
                {isPending && <div className="k-row-sub">불러오는 중...</div>}
                {isError && <div className="k-row-sub">종목을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</div>}
                {rows.map((s) => {
                  const isUp = s.changeRate >= 0
                  return (
                    <Link key={s.code} to={`/trade/${s.code}`} className="k-row">
                      <div className="k-tick">{s.name.charAt(0)}</div>
                      <div className="k-row-main">
                        <div className="k-row-name">{s.name}</div>
                        <div className="k-row-sub">
                          {s.code}
                          {s.quantity > 0 ? ` · 보유 ${s.quantity}주` : ''}
                        </div>
                      </div>
                      <div className="k-row-right">
                        <div className="k-row-price">{formatKRW(s.price)}</div>
                        <div className={`k-row-rate ${isUp ? 'k-up' : 'k-down'}`}>{toChangeText(s.changeRate)}</div>
                      </div>
                    </Link>
                  )
                })}
              </div>
            </section>
          </div>

          <div className="k-col k-side">
            <section className="k-px" aria-label="내 투자">
              <div className="k-px-head">
                <div className="k-px-label">내 종목 평가액</div>
                <div className="k-lab k-lab--gold">STOCKS</div>
              </div>
              <div className="k-stock-value">{formatKRW(stockValue)}</div>
              <div className="k-px-sub">살 수 있는 돈 {formatKRW(cash)}</div>
              <Link to="/trade/history" className="k-px-link">
                <span>내 거래 · 실현손익</span>
                <span className="k-px-link-arrow">&gt;</span>
              </Link>
            </section>
            <section className="k-card k-cheer">
              <div className="k-avatar">
                <img src={tier1} alt="걸뱅이 꿈이" />
              </div>
              <div className="k-cheer-text">
                뭐라도 담아야
                <br />
                집을 사지!
              </div>
            </section>
          </div>
        </div>
        <p className="k-note">※ 종목과 시세는 가상이에요. 전부 가짜 돈이에요.</p>
      </main>
    </div>
  )
}
