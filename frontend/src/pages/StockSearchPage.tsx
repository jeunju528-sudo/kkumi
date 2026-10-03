import { useState } from 'react'
import type { ChangeEvent } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatNumber } from '../utils/format'
import type { StockResponse } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './StockSearchPage.css'

// 목업 데이터
const CASH = 4_210_000
const INITIAL_RECENT = ['엔비디아', '삼성전자', '테슬라']
const STOCKS: StockResponse[] = [
  { name: '삼성전자', code: '005930', price: 51000, changeRate: (51000 - 49950) / 49950 },
  { name: '엔비디아', code: 'NVDA', price: 248000, changeRate: (248000 - 251270) / 251270 },
  { name: '카카오', code: '035720', price: 40000, changeRate: (40000 - 39760) / 39760 },
  { name: 'SK하이닉스', code: '000660', price: 187000, changeRate: (187000 - 184400) / 184400 },
  { name: '네이버', code: '035420', price: 192000, changeRate: (192000 - 192800) / 192800 },
  { name: '현대차', code: '005380', price: 241000, changeRate: (241000 - 238800) / 238800 },
  { name: '테슬라', code: 'TSLA', price: 331000, changeRate: (331000 - 338500) / 338500 },
  { name: '애플', code: 'AAPL', price: 301000, changeRate: (301000 - 300100) / 300100 },
  { name: '마이크로소프트', code: 'MSFT', price: 689000, changeRate: (689000 - 683500) / 683500 },
  { name: '알파벳', code: 'GOOGL', price: 247000, changeRate: (247000 - 248500) / 248500 },
]
const POPULAR_CODES = ['NVDA', '005930', '000660', 'TSLA', 'AAPL']
const POPULAR = POPULAR_CODES.map((code) => STOCKS.find((s) => s.code === code)).filter(
  (s): s is StockResponse => s !== undefined,
)

// 등락률: "▲ +2.1%" / "▼ -1.3%"
function toChangeText(changeRate: number): string {
  const pct = (changeRate * 100).toFixed(1)
  return changeRate >= 0 ? `▲ +${pct}%` : `▼ ${pct}%`
}

export function StockSearchPage() {
  const [query, setQuery] = useState('')
  const [recent, setRecent] = useState<string[]>(INITIAL_RECENT)

  const keyword = query.trim().toLowerCase()
  const hasQuery = keyword.length > 0
  const results = STOCKS.filter((s) => s.name.toLowerCase().includes(keyword) || s.code.toLowerCase().includes(keyword))

  const handleQueryChange = (e: ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)
  const handleClearRecent = () => setRecent([])

  const renderStockRow = (s: StockResponse, rank?: number) => (
    <Link key={s.code} to={`/trade/${s.code}`} className="k-row">
      {rank !== undefined && <div className="k-rank">{rank}</div>}
      <div className="k-tick">{s.name.charAt(0)}</div>
      <div className="k-row-main">
        <div className="k-row-name">{s.name}</div>
        <div className="k-row-sub">{s.code}</div>
      </div>
      <div className="k-row-right">
        <div className="k-row-price">{formatKRW(s.price)}</div>
        <div className={`k-row-rate ${s.changeRate >= 0 ? 'k-up' : 'k-down'}`}>{toChangeText(s.changeRate)}</div>
      </div>
    </Link>
  )

  return (
    <div className="page-stock-search">
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
        <Link to="/trade" className="k-back">&lt; 종목 목록</Link>
        <h1 className="k-title">종목 검색</h1>

        <div className="k-search">
          <svg width="20" height="20" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" />
          </svg>
          <label htmlFor="q" className="k-sr-only">종목 검색</label>
          <input id="q" type="text" placeholder="종목 이름이나 코드" value={query} onChange={handleQueryChange} className="k-input" />
        </div>

        {!hasQuery && (
          <>
            <section className="k-recent" aria-label="최근 검색">
              <div className="k-sec-head">
                <div className="k-sec-title">최근 검색</div>
                <button type="button" onClick={handleClearRecent} className="k-clear">지우기</button>
              </div>
              <div className="k-chips">
                {recent.map((name) => (
                  <button key={name} type="button" onClick={() => setQuery(name)} className="k-recent-chip">
                    {name}
                  </button>
                ))}
              </div>
              {recent.length === 0 && <div className="k-recent-empty">최근 검색한 종목이 없어요.</div>}
            </section>

            <section className="k-card k-list" aria-label="지금 인기">
              <div className="k-sec-head k-sec-head--base">
                <div className="k-sec-title">지금 인기</div>
                <div className="k-lab k-lab--sub">HOT</div>
              </div>
              {POPULAR.map((s, i) => renderStockRow(s, i + 1))}
            </section>
          </>
        )}

        {hasQuery && results.length > 0 && (
          <section className="k-card k-list" aria-label="검색 결과">
            <div className="k-count">검색 결과 {results.length}개</div>
            {results.map((s) => renderStockRow(s))}
          </section>
        )}

        {hasQuery && results.length === 0 && (
          <section className="k-card k-empty">
            <div className="k-avatar">
              <img src={tier1} alt="걸뱅이 꿈이" />
            </div>
            <div>
              <div className="k-empty-title">그런 종목은 없어요.</div>
              <div className="k-empty-desc">다른 이름이나 코드로 찾아봐요!</div>
            </div>
          </section>
        )}

        <p className="k-note">※ 종목과 시세는 샘플이에요.</p>
      </main>
    </div>
  )
}
