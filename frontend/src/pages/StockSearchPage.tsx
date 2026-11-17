import { useState } from 'react'
import type { ChangeEvent } from 'react'
import { Link } from 'react-router-dom'
import { formatKRW } from '../utils/format'
import type { StockResponse } from '../types/api'
import { useStocksQuery } from '../hooks/useStocksQuery'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './StockSearchPage.css'

const POPULAR_CODES = ['KK0002', 'KK0001', 'KK0004', 'KK0007', 'KK0008']

// 등락률: "▲ +2.1%" / "▼ -1.3%"
function toChangeText(changeRate: number): string {
  const pct = (changeRate * 100).toFixed(1)
  return changeRate >= 0 ? `▲ +${pct}%` : `▼ ${pct}%`
}

export function StockSearchPage() {
  const [query, setQuery] = useState('')
  const [recent, setRecent] = useState<string[]>([])
  const { data: stocks = [], isPending, isError } = useStocksQuery()

  // find는 못 찾으면 undefined를 돌려줘서 타입이 (StockResponse | undefined)[] 가 됨
  // filter 로 undefined를 걸러내서 타입 StockResponse[] 로만 맞추는 것
  // 실제 하는 일 filter((s) => s !== undefined )
  // s is StockResponse : 이 함수가 true를 주면 s 는 StockResponse 라는 뜻이 됨 
  // typeScript는 filter를 해도 타입을 저절로 좁혀주지 않ㅇㅁ
  const popular = POPULAR_CODES.map((code) => stocks.find((s) => s.code === code)).filter(
    (s): s is StockResponse => s !== undefined,
  )

  const keyword = query.trim().toLowerCase()
  const hasQuery = keyword.length > 0
  const results = stocks.filter((s) => s.name.toLowerCase().includes(keyword) || s.code.toLowerCase().includes(keyword))

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
        {isPending && <div className="k-row-sub">불러오는 중...</div>}
        {isError && <div className="k-row-sub">종목을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</div>}
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
              {popular.map((s, i) => renderStockRow(s, i + 1))}
            </section>
          </>
        )}

        {hasQuery && results.length > 0 && (
          <section className="k-card k-list" aria-label="검색 결과">
            <div className="k-count">검색 결과 {results.length}개</div>
            {results.map((s) => renderStockRow(s))}
          </section>
        )}

        {hasQuery && !isPending && results.length === 0 && (
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

        <p className="k-note">※ 종목과 시세는 가상이에요.</p>
      </main>
    </div>
  )
}
