import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatNumber } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './TradePage.css'

type StockTab = 'MINE' | 'ALL'

type MockStock = {
  name: string
  code: string
  price: number
  prevClose: number
  quantity: number
  tickSize: number
}

// 목업 데이터
const CASH = 4_210_000
const TICK_INTERVAL_MS = 2000
const STOCKS: MockStock[] = [
  { name: '대박전자', code: 'KK0001', price: 51000, prevClose: 49950, quantity: 12, tickSize: 100 },
  { name: '쌩쌩칩스', code: 'KK0002', price: 248000, prevClose: 251270, quantity: 1, tickSize: 500 },
  { name: '수다메신저', code: 'KK0003', price: 40000, prevClose: 39760, quantity: 4, tickSize: 50 },
  { name: '탱탱메모리', code: 'KK0004', price: 187000, prevClose: 184400, quantity: 0, tickSize: 500 },
  { name: '두리번검색', code: 'KK0005', price: 192000, prevClose: 192800, quantity: 0, tickSize: 500 },
  { name: '달려라모터스', code: 'KK0006', price: 241000, prevClose: 238800, quantity: 0, tickSize: 500 },
  { name: '찌릿전기차', code: 'KK0007', price: 331000, prevClose: 338500, quantity: 0, tickSize: 500 },
  { name: '동글이폰', code: 'KK0008', price: 301000, prevClose: 300100, quantity: 0, tickSize: 500 },
  { name: '야근소프트', code: 'KK0009', price: 689000, prevClose: 683500, quantity: 0, tickSize: 1000 },
  { name: '만물연구소', code: 'KK0010', price: 247000, prevClose: 248500, quantity: 0, tickSize: 500 },
]

// 전일 대비 등락: "▲ +2.1%" / "▼ -1.3%"
function toChangeText(price: number, prevClose: number): string {
  const diff = price - prevClose
  const pct = ((diff / prevClose) * 100).toFixed(1)
  return diff >= 0 ? `▲ +${pct}%` : `▼ ${pct}%`
}

export function TradePage() {
  const [tab, setTab] = useState<StockTab>('MINE')
  const [prices, setPrices] = useState<number[]>(() => STOCKS.map((s) => s.price))

  // 샘플 시세 흔들기
  useEffect(() => {
    const timer = setInterval(() => {
      setPrices((prev) =>
        prev.map((p, i) => {
          const tick = STOCKS[i].tickSize
          return Math.max(tick * 20, p + Math.round((Math.random() - 0.47) * 4) * tick)
        }),
      )
    }, TICK_INTERVAL_MS)
    return () => clearInterval(timer)
  }, [])

  const isMineTab = tab === 'MINE'
  const stockValue = STOCKS.reduce((sum, s, i) => sum + prices[i] * s.quantity, 0)
  const rows = STOCKS.map((s, i) => ({ ...s, price: prices[i] })).filter((s) => !isMineTab || s.quantity > 0)

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
              {rows.map((s) => {
                const isUp = s.price - s.prevClose >= 0
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
                      <div className={`k-row-rate ${isUp ? 'k-up' : 'k-down'}`}>{toChangeText(s.price, s.prevClose)}</div>
                    </div>
                  </Link>
                )
              })}
            </section>
          </div>

          <div className="k-col k-side">
            <section className="k-px" aria-label="내 투자">
              <div className="k-px-head">
                <div className="k-px-label">내 종목 평가액</div>
                <div className="k-lab k-lab--gold">STOCKS</div>
              </div>
              <div className="k-stock-value">{formatKRW(stockValue)}</div>
              <div className="k-px-sub">살 수 있는 돈 {formatKRW(CASH)}</div>
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
        <p className="k-note">※ 종목과 시세는 샘플이에요. 전부 가짜 돈이에요.</p>
      </main>
    </div>
  )
}
