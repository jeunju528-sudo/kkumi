import { useState } from 'react'
import type { ChangeEvent, CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import type { AptRecommendResponse } from '../types/api'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatManwon, formatNumber } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './ApartmentsPage.css'

// 화면 전용 목업 집 (가격은 만원 단위, roof: 썸네일 지붕색)
type HomeMock = Pick<AptRecommendResponse, 'dealId' | 'complexName' | 'region' | 'area'> & {
  priceManwon: number
  roof: string
}

// 목업 데이터 (W: 평가액, C: 현금)
const MOCK_TOTAL_ASSET = 5230000
const MOCK_CASH = 4210000
const MOCK_APARTMENTS: HomeMock[] = [
  { dealId: 101, complexName: '태백 [단지명]', region: '강원 태백시', area: 39, priceManwon: 1800, roof: '#D64545' },
  { dealId: 102, complexName: '군산 [단지명]', region: '전북 군산시', area: 45, priceManwon: 2400, roof: '#3E9B3E' },
  { dealId: 103, complexName: '영주 [단지명]', region: '경북 영주시', area: 49, priceManwon: 3200, roof: '#E58A2B' },
  { dealId: 104, complexName: '청주 [단지명]', region: '충북 청주시', area: 59, priceManwon: 6500, roof: '#3B82F6' },
  { dealId: 105, complexName: '천안 [단지명]', region: '충남 천안시', area: 59, priceManwon: 12000, roof: '#8E5BD6' },
  { dealId: 106, complexName: '창원 [단지명]', region: '경남 창원시', area: 84, priceManwon: 18000, roof: '#D64545' },
]

// 평가액이 집값 40% 이상인 최대 집값 (만원)
const MAX_PRICE_MANWON = Math.floor(MOCK_TOTAL_ASSET / 0.4 / 10000)

// 시안 헤더 (집 찾기 탭 활성)
function ApartmentsHeader() {
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
        <Link to="/trade" className="k-link">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" />
          </svg>
          <span>투자</span>
        </Link>
        <Link to="/apartments" className="k-link k-on" aria-current="page">
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

export function ApartmentsPage() {
  const [query, setQuery] = useState('')
  const [isOnlyOk, setIsOnlyOk] = useState(false)

  const q = query.trim()
  const rows = MOCK_APARTMENTS.map((home, index) => {
    const ownWon = home.priceManwon * 10000 * 0.4
    const hasEnoughCash = ownWon <= MOCK_CASH
    const isNeedSell = !hasEnoughCash && ownWon <= MOCK_TOTAL_ASSET
    const isRecommended = hasEnoughCash || isNeedSell
    return { home, index, ownWon, hasEnoughCash, isNeedSell, isRecommended }
  }).filter(
    (row) =>
      (!q || row.home.complexName.includes(q) || row.home.region.includes(q)) &&
      (!isOnlyOk || row.isRecommended),
  )

  const handleQueryChange = (e: ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)
  const handleOnlyOkToggle = () => setIsOnlyOk((prev) => !prev)

  return (
    <div className="page-apartments">
      <ApartmentsHeader />
      <main className="k-wrap">
        <h1 className="k-title">동네 부동산</h1>
        <p className="k-desc">전국 진짜 아파트를 검색해서 가짜로 계약해요. 국토부 실거래가 기준이에요.</p>

        <div className="k-top2">
          <section className="k-px" aria-label="대출 안내">
            <div className="k-px-head">
              <div className="k-px-label">가짜 대출 60% 자동 적용</div>
              <div className="k-lab">LOAN</div>
            </div>
            <div className="k-px-body">
              집값의 40%만 현금에서 나가요. 추천은 평가액으로, 계약은 현금으로 해요. 현금이 모자라면 주식을
              팔아요. 이자는 없어요.
            </div>
          </section>
          <section className="k-px" aria-label="살 수 있는 최대 집값">
            <div className="k-px-head">
              <div className="k-px-label">내 평가액으로 살 수 있는 집</div>
              <div className="k-lab">MAX</div>
            </div>
            <div className="k-max">약 {formatManwon(MAX_PRICE_MANWON)}까지</div>
          </section>
        </div>

        <section className="k-card k-search" aria-label="집 검색">
          <label htmlFor="q" className="k-search-label">
            집 찾기
          </label>
          <div className="k-search-row">
            <input
              id="q"
              type="text"
              className="k-input"
              placeholder="동네나 단지 이름"
              value={query}
              onChange={handleQueryChange}
            />
            <button
              type="button"
              className="k-filter"
              onClick={handleOnlyOkToggle}
              aria-pressed={isOnlyOk}
              style={{ background: isOnlyOk ? '#FFC93C' : '#FFFFFF' }}
            >
              살 수 있는 집만
            </button>
          </div>
          <div className="k-count">검색 결과 {rows.length}채</div>
        </section>

        <div className="k-list">
          {rows.map(({ home, index, ownWon, hasEnoughCash, isNeedSell, isRecommended }) => {
            const roofStyle = { '--roof': home.roof } as CSSProperties
            const isLow = home.priceManwon < 5000
            return (
              <article key={home.dealId} className="k-card k-home">
                <div className="k-home-top">
                  <div className="k-thumb">
                    <div className="k-thumb-grass" />
                    {isLow ? (
                      <Sprite
                        id="px-apt-low"
                        viewBox="0 0 16 20"
                        width={48}
                        height={60}
                        className="k-thumb-apt"
                        style={roofStyle}
                      />
                    ) : (
                      <Sprite
                        id="px-apt-mid"
                        viewBox="0 0 14 26"
                        width={28}
                        height={52}
                        className="k-thumb-apt"
                        style={roofStyle}
                      />
                    )}
                  </div>
                  <div className="k-home-info">
                    <div className="k-home-name-row">
                      <div className="k-home-name">{home.complexName}</div>
                      {index === 0 && <div className="k-badge">가장 가까워요</div>}
                    </div>
                    <div className="k-home-meta">
                      {home.region} · 전용 {home.area}㎡
                    </div>
                    <div className="k-home-price">{formatManwon(home.priceManwon)}</div>
                  </div>
                </div>
                <div className="k-split">
                  <div className="k-split-row">
                    <span className="k-split-label">내 돈 40%</span>
                    <span className="k-split-own">{formatManwon(Math.round(home.priceManwon * 0.4))}</span>
                  </div>
                  <div className="k-split-row">
                    <span className="k-split-label">가짜 대출 60%</span>
                    <span className="k-split-loan">{formatManwon(Math.round(home.priceManwon * 0.6))}</span>
                  </div>
                </div>
                {hasEnoughCash && (
                  <Link to={`/contract/${home.dealId}`} className="k-btn">
                    계약하기
                  </Link>
                )}
                {isNeedSell && (
                  <Link to="/trade" className="k-btn k-btn--sell">
                    현금 {formatKRW(ownWon - MOCK_CASH)} 부족 · 주식 팔러 가기
                  </Link>
                )}
                {!isRecommended && (
                  <button type="button" className="k-btn k-btn--off" disabled>
                    {formatKRW(ownWon - MOCK_TOTAL_ASSET)} 모자라요
                  </button>
                )}
              </article>
            )
          })}
        </div>

        {rows.length === 0 && (
          <section className="k-card k-empty">
            <div className="k-avatar">
              <img src={tier1} alt="걸뱅이 꿈이" />
            </div>
            <div>
              <div className="k-empty-title">살 수 있는 집이 아직 없어요.</div>
              <div className="k-empty-sub">종목을 불려서 다시 오거나 다른 동네를 찾아봐요!</div>
            </div>
          </section>
        )}

        <p className="k-note k-foot">※ 단지명과 가격은 샘플이에요. 실제 서비스는 국토부 실거래가를 보여줘요.</p>
      </main>
    </div>
  )
}
