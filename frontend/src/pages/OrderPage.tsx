import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { StockResponse } from '../types/api'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatNumber } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './OrderPage.css'

type OrderMode = 'buy' | 'sell'

// 화면 전용 목업 종목 (open: 오늘 시가, held: 보유 수량)
type OrderStock = Pick<StockResponse, 'code' | 'name' | 'price'> & {
  open: number
  held: number
}

type Ticker = {
  price: number
  hist: number[]
  flash: number
}

type OrderPanelProps = {
  stock: OrderStock
}

// 목업 데이터
const MOCK_CASH = 4210000
const MOCK_STOCKS: OrderStock[] = [
  { code: 'KK0002', name: '마르덴전자', price: 248000, open: 251270, held: 1 },
  { code: 'KK0001', name: '세르반반도체', price: 71200, open: 70500, held: 3 },
  { code: 'KK0003', name: '쿠르텍', price: 42350, open: 43100, held: 0 },
  { code: 'KK0004', name: '브론델배터리', price: 182500, open: 179800, held: 2 },
]
const DEFAULT_STOCK = MOCK_STOCKS[0]

const createTicker = (price: number): Ticker => ({
  price,
  hist: Array.from({ length: 24 }, (_, j) =>
    Math.round(price * (1 + 0.012 * Math.sin(j / 3 + 2) - 0.0006 * (23 - j))),
  ),
  flash: 0,
})

// 시안 헤더 (투자 탭 활성)
function OrderHeader() {
  return (
    <header className="k-top">
      <Link to="/home" className="k-logo">
        <span className="k-mark">꿈</span>
        <span>꿈이</span>
      </Link>
      <nav className="k-links" aria-label="메인 메뉴">
        <Link to="/home" className="k-link">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" />
          </svg>
          <span>홈</span>
        </Link>
        <Link to="/trade" className="k-link k-on" aria-current="page">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" />
          </svg>
          <span>투자</span>
        </Link>
        <Link to="/apartments" className="k-link">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" />
          </svg>
          <span>집 찾기</span>
        </Link>
        <Link to="/village" className="k-link">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z" />
          </svg>
          <span>내 마을</span>
        </Link>
      </nav>
      <div className="k-grow" />
      <div className="k-chip">
        <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
        <div>
          <div className="k-lab">CASH</div>
          <div className="k-num k-cash">{formatNumber(MOCK_CASH)}</div>
        </div>
      </div>
    </header>
  )
}

// 종목별 시세·주문 패널 (종목 바뀌면 key로 리셋)
function OrderPanel({ stock }: OrderPanelProps) {
  const [mode, setMode] = useState<OrderMode>('buy')
  const [qty, setQty] = useState(3)
  const [ticker, setTicker] = useState<Ticker>(() => createTicker(stock.price))

  // 1.5초마다 가짜 시세 변동
  useEffect(() => {
    const timer = setInterval(() => {
      setTicker((prev) => {
        const next = Math.max(10000, prev.price + Math.round((Math.random() - 0.47) * 4) * 500)
        const hist = [...prev.hist, next]
        if (hist.length > 30) hist.shift()
        return { price: next, hist, flash: next >= prev.price ? 1 : -1 }
      })
    }, 1500)
    return () => clearInterval(timer)
  }, [])

  const { price, hist, flash } = ticker
  const isBuy = mode === 'buy'
  const delta = price - stock.open
  const isUp = delta >= 0
  const pct = Math.abs((delta / stock.open) * 100).toFixed(1)
  const maxBuy = Math.max(1, Math.floor(MOCK_CASH / price))
  const max = isBuy ? maxBuy : stock.held
  const q = Math.min(Math.max(1, qty), max)

  const lo = Math.min(...hist)
  const hi = Math.max(...hist)
  const span = Math.max(1, hi - lo)
  const chartPoints = hist
    .map(
      (v, i) =>
        `${(i * (300 / (hist.length - 1))).toFixed(1)},${(92 - ((v - lo) / span) * 84).toFixed(1)}`,
    )
    .join(' ')

  const deltaText = `${isUp ? '▲ ' : '▼ '}${formatNumber(Math.abs(delta))} (${isUp ? '+' : '-'}${pct}%) 오늘`
  const deltaColor = isUp ? '#D32F36' : '#2563EB'
  const flashBg = flash === 1 ? '#FFE3E6' : flash === -1 ? '#DCE9FF' : 'transparent'

  const handleBuyMode = () => setMode('buy')
  const handleSellMode = () => setMode('sell')
  const handleDecrease = () => setQty(Math.max(1, q - 1))
  const handleIncrease = () => setQty(Math.min(max, q + 1))

  return (
    <div className="k-cols">
      <section className="k-card" aria-label="종목 시세">
        <div className="k-head">
          <div className="k-tick">{stock.name.charAt(0)}</div>
          <div className="k-head-info">
            <h1 className="k-name">{stock.name}</h1>
            <div className="k-sub">
              {stock.code} · 보유 {stock.held}주
            </div>
          </div>
          <div className="k-live">
            <span className="k-live-dot" />
            <span className="k-lab">LIVE</span>
          </div>
        </div>
        <div className="k-price" style={{ background: flashBg }}>
          {formatNumber(price)}
          <span className="k-price-unit">원</span>
        </div>
        <div className="k-delta" style={{ color: deltaColor }}>
          {deltaText}
        </div>
        <svg
          viewBox="0 0 300 100"
          width="100%"
          role="img"
          aria-label="실시간 가격 추이 그래프"
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
          <polyline
            points={chartPoints}
            fill="none"
            stroke={deltaColor}
            strokeWidth="2.5"
            strokeLinejoin="round"
            strokeLinecap="round"
            vectorEffect="non-scaling-stroke"
          />
        </svg>
      </section>

      <section className="k-card k-order" aria-label="주문">
        <div className="k-modes">
          <button
            type="button"
            className="k-mode"
            onClick={handleBuyMode}
            style={{ background: isBuy ? '#D32F36' : '#FFFFFF', color: isBuy ? '#FFFFFF' : '#231B3A' }}
          >
            담기 (매수)
          </button>
          <button
            type="button"
            className="k-mode"
            onClick={handleSellMode}
            style={{ background: isBuy ? '#FFFFFF' : '#2563EB', color: isBuy ? '#231B3A' : '#FFFFFF' }}
          >
            팔기 (매도)
          </button>
        </div>

        <div className="k-talk">
          <div className="k-avatar">
            <img src={tier1} alt="걸뱅이 꿈이" />
          </div>
          <div className="k-bubble">{isBuy ? `${q}주 담아볼까?` : `${q}주 팔고 치킨 먹자!`}</div>
        </div>

        <div className="k-qty-row">
          <div className="k-qty-label">수량</div>
          <div className="k-stepper">
            <button type="button" className="k-step" aria-label="수량 줄이기" onClick={handleDecrease}>
              −
            </button>
            <div className="k-qty">
              {q}
              <span className="k-qty-unit">주</span>
            </div>
            <button type="button" className="k-step" aria-label="수량 늘리기" onClick={handleIncrease}>
              +
            </button>
          </div>
        </div>

        <div className="k-sum">
          <div className="k-sum-row">
            <span className="k-sum-label">{isBuy ? '살 수 있는 돈' : '팔 수 있는 수량'}</span>
            <span className="k-sum-val">
              {isBuy ? `${formatKRW(MOCK_CASH)} (최대 ${maxBuy}주)` : `${stock.held}주`}
            </span>
          </div>
          <div className="k-sum-row">
            <span className="k-sum-label">주문 금액 (지금 시세)</span>
            <span className="k-sum-total">{formatKRW(price * q)}</span>
          </div>
        </div>

        <button type="button" className="k-cta" style={{ background: isBuy ? '#D32F36' : '#2563EB' }}>
          {isBuy ? '담기' : '팔기'}
        </button>
        <p className="k-note">실제 돈은 1원도 안 나가요. 누르는 순간의 시세로 체결돼요.</p>
      </section>
    </div>
  )
}

export function OrderPage() {
  const { code } = useParams()
  const stock = MOCK_STOCKS.find((s) => s.code === code) ?? DEFAULT_STOCK

  return (
    <div className="page-order">
      <OrderHeader />
      <main className="k-wrap">
        <Link to="/trade" className="k-back">
          &lt; 종목 목록
        </Link>
        <OrderPanel key={stock.code} stock={stock} />
      </main>
    </div>
  )
}
