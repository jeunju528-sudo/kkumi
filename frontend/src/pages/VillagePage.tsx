import type { CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { useMidnightCountdown } from '../hooks/useMidnightCountdown'
import { useMemberQuery } from '../hooks/useMemberQuery'
import { useMyHousesQuery } from '../hooks/useMyHousesQuery'
import { usePortfolioQuery } from '../hooks/usePortfolioQuery'
import type { SpriteId } from '../components/PixelDefs'
import { formatKRW, formatManwon, formatNumber } from '../utils/format'
import { toLoanAmount } from '../utils/loan'
import { toSeedMultiple } from '../utils/portfolio'
import { TIER_INFO } from '../utils/tier'
import './VillagePage.css'

type HouseSprite = {
  id: SpriteId
  viewBox: string
  sceneClass: string // 마을 그림 크기 (CSS k-low / k-mid)
  sceneWidth: number
  sceneHeight: number
  thumbWidth: number
  thumbHeight: number
  roof: string
}

// 집 순서별 그림 (응답에 그림 정보 없음, 순서대로 돌려 씀)
const HOUSE_SPRITES: HouseSprite[] = [
  { id: 'px-apt-low', viewBox: '0 0 16 20', sceneClass: 'k-low', sceneWidth: 80, sceneHeight: 100, thumbWidth: 48, thumbHeight: 60, roof: '#3B82F6' },
  { id: 'px-apt-mid', viewBox: '0 0 14 26', sceneClass: 'k-mid', sceneWidth: 70, sceneHeight: 130, thumbWidth: 28, thumbHeight: 52, roof: '#D64545' },
  { id: 'px-apt-low', viewBox: '0 0 16 20', sceneClass: 'k-low', sceneWidth: 80, sceneHeight: 100, thumbWidth: 48, thumbHeight: 60, roof: '#FFC93C' },
  { id: 'px-apt-mid', viewBox: '0 0 14 26', sceneClass: 'k-mid', sceneWidth: 70, sceneHeight: 130, thumbWidth: 28, thumbHeight: 52, roof: '#7A4B3A' },
]
const toHouseSprite = (index: number): HouseSprite => HOUSE_SPRITES[index % HOUSE_SPRITES.length]

// 마을 그림 집터 칸 수 (모바일은 앞 3칸만, 위치는 CSS village-lot-N)
const LOT_COUNT = 5
const MOBILE_LOT_COUNT = 3

const ORDINALS = ['첫', '두', '세', '네', '다섯', '여섯']

const roofStyle = (roof: string): CSSProperties => ({ '--roof': roof }) as CSSProperties

export function VillagePage() {
  const dayCloseLeft = useMidnightCountdown()
  // HUD는 다른 화면과 같은 현금 (마을 총 가치는 아래 카드)
  const { data: member } = useMemberQuery()
  // 캐시에 값 있으면 바로 표시, 뒤에서 재조회 (staleTime 0)
  const { data: myHouses, isError: isHousesError } = useMyHousesQuery()
  // 순자산은 WebSocket이 5초마다 캐시 갱신
  const { data: portfolio } = usePortfolioQuery()
  const houses = myHouses?.houses ?? []
  const houseCount = myHouses?.houseCount ?? 0
  const tier = myHouses?.tier ?? 'GEOLBAENGI'
  const tierInfo = TIER_INFO[tier]
  const totalWon = houses.reduce((sum, h) => sum + h.purchasePrice, 0)
  const avgWon = houseCount > 0 ? totalWon / houseCount : 0
  // 갑부는 다음 단계 없음 → 진행바 가득
  const progressPct = tierInfo.nextGoal === null ? 100 : Math.min(100, (houseCount / tierInfo.nextGoal) * 100)
  const progressText = tierInfo.nextGoal === null ? `${houseCount}채` : `${houseCount}/${tierInfo.nextGoal}채`
  const nextOrdinal = ORDINALS[houseCount] ?? `${houseCount + 1}`
  const seedMultiple = portfolio ? toSeedMultiple(portfolio.netWorth, portfolio.initialCash) : null

  const hudChips = (
    <>
      <div className="k-chip">
        <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
        <div><div className="k-lab">CASH</div><div className="k-num" style={{ color: '#FFC93C' }}>{formatNumber(member?.cash ?? 0)}</div></div>
      </div>
      <div className="k-chip">
        <Sprite id="px-clock" viewBox="0 0 8 8" width={16} height={16} />
        <div><div className="village-clock-lab">오늘 마감까지</div><div className="k-num" style={{ color: '#FFFFFF' }}>{dayCloseLeft}</div></div>
      </div>
    </>
  )

  // 집터: 산 집 → 집 그림, 바로 다음 칸 → NEXT, 나머지 → 빈 칸
  const lots = Array.from({ length: LOT_COUNT }, (_, i) => {
    const lotClass = `village-lot-${i}${i >= MOBILE_LOT_COUNT ? ' k-wide' : ''}`
    if (i < houseCount) {
      const sprite = toHouseSprite(i)
      return (
        <Sprite
          key={i}
          id={sprite.id}
          viewBox={sprite.viewBox}
          width={sprite.sceneWidth}
          height={sprite.sceneHeight}
          className={`sp ${sprite.sceneClass} ${lotClass}`}
          style={{ bottom: 34, ...roofStyle(sprite.roof) }}
        />
      )
    }
    if (i === houseCount) {
      return (
        <div key={i} className={`k-slot ${lotClass}`} role="img" aria-label="다음 집 자리">
          <svg width="16" height="16" viewBox="0 0 8 8" className="village-plus" aria-hidden="true"><path d="M3 0h2v8h-2zM0 3h8v2h-8z" /></svg>
          <div className="k-lab village-lab-white">NEXT</div>
        </div>
      )
    }
    return (
      <div key={i} className={`k-slot ${lotClass}`} style={{ opacity: 0.55 }} role="img" aria-label="빈 집 자리">
        <div className="k-lab village-lab-white">?</div>
      </div>
    )
  })

  return (
    <div className="page-village k-page">
      <header className="k-top k-top--hero">
        <Link to="/home" className="k-logo"><span className="k-mark">꿈</span><span>꿈이</span></Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" /></svg><span>홈</span></Link>
          <Link to="/trade" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" /></svg><span>투자</span></Link>
          <Link to="/apartments" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" /></svg><span>집 찾기</span></Link>
          <Link to="/village" className="k-link k-on" aria-current="page"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z" /></svg><span>내 마을</span></Link>
        </nav>
        <div className="k-grow" />
        {hudChips}
      </header>

      <div className="k-scene">
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '8%', top: 130 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '66%', top: 112, opacity: 0.9 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud" style={{ left: '44%', top: 160, opacity: 0.9 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud k-wide" style={{ left: '84%', top: 70 }} />
        <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp k-cloud k-wide" style={{ left: '26%', top: 80, opacity: 0.9 }} />

        <Sprite id="px-tree" viewBox="0 0 10 12" width={50} height={60} className="sp k-tree" style={{ left: 0, bottom: 38 }} />
        {lots}
        <div className="k-grass" />
        <div className="k-soil" />

        <div className="village-title-wrap">
          <div className="k-lab village-title-lab">MY TOWN</div>
          <h1 className="village-title">내 마을</h1>
        </div>

        <div className="k-hero-hud village-hero-hud">{hudChips}</div>
      </div>

      <main className="k-wrap village-main">
        <div className="k-cols">
          <div className="k-col">
            <section className="k-px" aria-label="마을 총 가치">
              <div className="village-row-between">
                <div className="village-total-lab">마을 총 가치</div>
                <div className="k-lab" style={{ color: '#FFC93C' }}>HOUSES {houseCount}</div>
              </div>
              <div className="village-total">{formatKRW(totalWon)}</div>
              <div className="village-total-sub">보유 {houseCount}채 · 평균 {formatManwon(avgWon / 10_000)}</div>
            </section>

            <section className="k-card village-stage" aria-label="꿈이 변신 단계">
              <div className="village-stage-img"><img src={tierInfo.image} alt={`${tierInfo.name} 꿈이`} /></div>
              <div className="village-stage-text">
                <div className="k-lab" style={{ color: '#6B6F80' }}>STAGE {tierInfo.stage} / 4</div>
                <div className="village-stage-name">{tierInfo.name} 꿈이</div>
                <div className="village-stage-desc">{tierInfo.nextText}</div>
                <div className="village-progress-row">
                  <div className="village-progress">
                    <div className="village-progress-fill" style={{ width: `${progressPct}%` }} />
                    <div className="k-stripe village-progress-stripe" />
                  </div>
                  <div className="village-progress-text">{progressText}</div>
                </div>
                <Link to="/tiers" className="village-tiers-link">변신 4단계 보기</Link>
              </div>
            </section>

            <section className="k-px village-result" aria-label="시드머니 대비 결과">
              {portfolio && seedMultiple ? (
                <>
                  <div className="village-row-between">
                    <div className="village-total-lab">
                      시드 {formatManwon(portfolio.initialCash / 10_000)} → 순자산 {formatManwon(portfolio.netWorth / 10_000)}
                    </div>
                    <div className="k-lab" style={{ color: '#FFC93C' }}>RESULT</div>
                  </div>
                  <div className={`village-result-num${seedMultiple.isUp ? ' village-result-num--up' : ''}`}>
                    {seedMultiple.text} {seedMultiple.isUp ? '인생역전' : '인생여전'}
                  </div>
                </>
              ) : (
                <div className="village-total-lab">불러오는 중...</div>
              )}
            </section>
          </div>

          <div className="k-col">
            <div className="village-dex-head">
              <h2 className="village-dex-title">내 도감</h2>
              <div className="village-dex-sub">모은 집이 곧 인생역전 기록이에요</div>
            </div>
            {!myHouses ? (
              <div className="village-dex-sub">
                {isHousesError ? '내 마을을 못 불러왔어요. 잠시 후 다시 시도해 주세요.' : '불러오는 중...'}
              </div>
            ) : (
              <>
                {houses.map((house, i) => {
                  const sprite = toHouseSprite(i)
                  return (
                    <article key={house.houseId} className="k-card village-house">
                      <div className="village-thumb">
                        <div className="village-thumb-grass" />
                        <Sprite
                          id={sprite.id}
                          viewBox={sprite.viewBox}
                          width={sprite.thumbWidth}
                          height={sprite.thumbHeight}
                          className="village-thumb-sprite"
                          style={roofStyle(sprite.roof)}
                        />
                      </div>
                      <div className="village-house-info">
                        <div className="village-house-name">{house.complexName}</div>
                        <div className="village-house-meta">{house.region} · 전용 {house.area}㎡ · {house.floor}층</div>
                        <div className="village-house-price">
                          매수가 <b>{formatManwon(house.purchasePrice / 10_000)}</b> · 대출 {formatManwon(toLoanAmount(house.purchasePrice) / 10_000)}
                        </div>
                      </div>
                      <Link to={`/contract/${house.dealId}`} className="village-contract-link">계약서</Link>
                    </article>
                  )
                })}

                <Link to="/apartments" className="k-card village-empty">
                  <div className="village-empty-box">?</div>
                  <div className="village-empty-text">
                    <div className="village-house-name">다음 집은?</div>
                    <div className="village-empty-sub">{nextOrdinal} 번째 칸이 비어 있어요</div>
                  </div>
                  <div className="village-empty-cta">집 찾기 &gt;</div>
                </Link>
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
