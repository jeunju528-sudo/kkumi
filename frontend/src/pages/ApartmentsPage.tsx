import { useState } from 'react'
import type { ChangeEvent, CSSProperties, FormEvent } from 'react'
import { useAptRecommendationsQuery } from '../hooks/useAptRecommendationsQuery'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatManwon } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './ApartmentsPage.css'

// 시안 헤더 (집 찾기 탭 활성)
const ROOF_COLORS = ['#D64545', '#3E9B3E', '#E58A2B', '#3B82F6', '#8E5BD6']
const ONLY_OK_STORAGE_KEY = 'apartments.isOnlyOk'
const LOW_RISE_MAX_PRICE = 50_000_000

export function ApartmentsPage() {
  const [query, setQuery] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [isOnlyOk, setIsOnlyOk] = useState(() => sessionStorage.getItem(ONLY_OK_STORAGE_KEY) !== 'false')

  const { data, isPending, isError } = useAptRecommendationsQuery(searchQuery, isOnlyOk)
  const items = data?.items ?? []
  const maxPriceManwon = Math.floor((data?.maxPrice ?? 0) / 10000)

  const handleQueryChange = (e: ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)
  const handleSearchSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setSearchQuery(query.trim())
  }
  const handleOnlyOkToggle = () => {
    const next = !isOnlyOk
    setIsOnlyOk(next)
    sessionStorage.setItem(ONLY_OK_STORAGE_KEY, String(next))
  }

  return (
    <div className="page-apartments">
      <main className="k-wrap">
        <h1 className="k-title">동네 부동산</h1>
        <p className="k-desc">전국 진짜 아파트를 검색해서 계약해요. 국토부 실거래가 기준이에요.</p>

        <div className="k-top2">
          <section className="k-px" aria-label="대출 안내">
            <div className="k-px-head">
              <div className="k-px-label">주택담보대출 60% 자동 적용</div>
              <div className="k-lab">LOAN</div>
            </div>
            <div className="k-px-body">
              집값의 40%만 현금에서 나가요. 금리는 연 3.7%, 30년 원리금균등 기준이에요.
              추천은 평가액으로, 계약은 현금으로 해요.
            </div>
          </section>
          <section className="k-px" aria-label="살 수 있는 최대 집값">
            <div className="k-px-head">
              <div className="k-px-label">내 평가액으로 살 수 있는 집</div>
              <div className="k-lab">MAX</div>
            </div>
            <div className="k-max">약 {formatManwon(maxPriceManwon)}까지</div>
          </section>
        </div>

        <section className="k-card k-search" aria-label="집 검색">
          <form onSubmit={handleSearchSubmit}>
            <label htmlFor="q" className="k-search-label">
              집 찾기
            </label>
            <div className="k-search-row">
              <input
                id="q"
                type="text"
                className="k-input"
                placeholder="서울, 강남구, 단지 이름 입력 후 Enter"
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
          </form>
          <div className="k-count">
            검색 결과 {items.length}채{data?.hasNext ? ' 이상' : ''}
          </div>
        </section>

        {isPending && <p className="k-note">집 목록 불러오는 중...</p>}
        {isError && <p className="k-note">집 목록을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</p>}

        <div className="k-list">
          {items.map((apt, index) => {
            const roofStyle = { '--roof': ROOF_COLORS[apt.dealId % ROOF_COLORS.length] } as CSSProperties
            const isLow = apt.price < LOW_RISE_MAX_PRICE
            return (
              <article key={apt.dealId} className="k-card k-home">
                <div className="k-home-top">
                  <div className="k-thumb">
                    <div className="k-thumb-grass" />
                    {isLow ? (
                      <Sprite id="px-apt-low" viewBox="0 0 16 20" width={48} height={60} className="k-thumb-apt" style={roofStyle} />
                    ) : (
                      <Sprite id="px-apt-mid" viewBox="0 0 14 26" width={28} height={52} className="k-thumb-apt" style={roofStyle} />
                    )}
                  </div>
                  <div className="k-home-info">
                    <div className="k-home-name-row">
                      <div className="k-home-name">{apt.complexName}</div>
                      {isOnlyOk && index === 0 && <div className="k-badge">영끌</div>}
                    </div>
                    <div className="k-home-meta">
                      {apt.region} · 전용 {apt.area}㎡ · {apt.floor}층
                    </div>
                    <div className="k-home-price">{formatManwon(apt.price / 10000)}</div>
                  </div>
                </div>
                <div className="k-split">
                  <div className="k-split-row">
                    <span className="k-split-label">내 돈 40%</span>
                    <span className="k-split-own">{formatManwon(apt.ownAmount / 10000)}</span>
                  </div>
                  <div className="k-split-row">
                    <span className="k-split-label">대출 60% (연 3.7%, 원리금균등)</span>
                    <span className="k-split-loan">{formatManwon(apt.loanAmount / 10000)}</span>
                  </div>
                </div>
                {apt.affordability === 'OK' && (
                  <Link to={`/contract/${apt.dealId}`} className="k-btn">
                    부동산 방문
                  </Link>
                )}
                {apt.affordability === 'NEED_SELL' && (
                  <Link to="/trade" className="k-btn k-btn--sell">
                    현금 {formatKRW(apt.shortage)} 부족 · 주식 팔러 가기
                  </Link>
                )}
                {apt.affordability === 'SHORT' && (
                  <button type="button" className="k-btn k-btn--off" disabled>
                    {formatKRW(apt.shortage)} 모자라요
                  </button>
                )}
              </article>
            )
          })}
        </div>

        {!isPending && !isError && items.length === 0 && (
          <section className="k-card k-empty">
            <div className="k-avatar">
              <img src={tier1} alt="걸뱅이 꿈이" />
            </div>
            <div>
              <div className="k-empty-title">{isOnlyOk ? '살 수 있는 집이 아직 없어요.' : '검색 결과가 없어요.'}</div>
              <div className="k-empty-sub">종목을 불려서 다시 오거나 다른 동네를 찾아봐요!</div>
            </div>
          </section>
        )}
      </main>
    </div>
  )
}
