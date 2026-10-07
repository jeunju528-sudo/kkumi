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
const INITIAL_RECENT = ['마르덴전자', '세르반반도체', '도르빈바이오']
const STOCKS: StockResponse[] = [
  { name: '세르반반도체', code: 'KK0001', price: 51000, changeRate: (51000 - 49950) / 49950 },
  { name: '마르덴전자', code: 'KK0002', price: 248000, changeRate: (248000 - 251270) / 251270 },
  { name: '쿠르텍', code: 'KK0003', price: 40000, changeRate: (40000 - 39760) / 39760 },
  { name: '브론델배터리', code: 'KK0004', price: 187000, changeRate: (187000 - 184400) / 184400 },
  { name: '다이온모터스', code: 'KK0005', price: 192000, changeRate: (192000 - 192800) / 192800 },
  { name: '벨라전기차', code: 'KK0006', price: 241000, changeRate: (241000 - 238800) / 238800 },
  { name: '도르빈바이오', code: 'KK0007', price: 331000, changeRate: (331000 - 338500) / 338500 },
  { name: '소티제약', code: 'KK0008', price: 301000, changeRate: (301000 - 300100) / 300100 },
  { name: '아젤뷰티', code: 'KK0009', price: 689000, changeRate: (689000 - 683500) / 683500 },
  { name: '카르넬게임즈', code: 'KK0010', price: 247000, changeRate: (247000 - 248500) / 248500 },
  { name: '트리아엔터', code: 'KK0011', price: 38200, changeRate: (38200 - 37900) / 37900 },
  { name: '제노텔레콤', code: 'KK0012', price: 24500, changeRate: (24500 - 24650) / 24650 },
  { name: '라미로보틱스', code: 'KK0013', price: 126000, changeRate: (126000 - 121500) / 121500 },
  { name: '아르카홀딩스', code: 'KK0014', price: 8730, changeRate: (8730 - 8800) / 8800 },
  { name: '유르파트너스', code: 'KK0015', price: 15400, changeRate: (15400 - 15100) / 15100 },
  { name: '칼리건설', code: 'KK0016', price: 31200, changeRate: (31200 - 31050) / 31050 },
  { name: '모르조선', code: 'KK0017', price: 87300, changeRate: (87300 - 85900) / 85900 },
  { name: '펠로항공', code: 'KK0018', price: 19800, changeRate: (19800 - 20200) / 20200 },
  { name: '시벨푸드', code: 'KK0019', price: 54800, changeRate: (54800 - 54500) / 54500 },
  { name: '데니마트', code: 'KK0020', price: 12950, changeRate: (12950 - 12900) / 12900 },
]
const POPULAR_CODES = ['KK0002', 'KK0001', 'KK0004', 'KK0007', 'KK0008']
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
