import type { CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import type { SpriteId } from '../components/PixelDefs'
import { formatKRW, formatManwon, formatNumber } from '../utils/format'
import tier3 from '../assets/sprites/kkumi-tier3.svg'
import './VillagePage.css'

type OwnedHouse = {
  dealId: number
  complexName: string
  region: string
  area: number
  priceManwon: number
  sprite: { id: SpriteId; viewBox: string; width: number; height: number; roof?: string }
}

// 목업 데이터 (API 연동 전)
const MOCK_HOUSES: OwnedHouse[] = [
  {
    dealId: 1,
    complexName: '태백 [단지명]',
    region: '강원 태백시',
    area: 39,
    priceManwon: 1800,
    sprite: { id: 'px-apt-low', viewBox: '0 0 16 20', width: 48, height: 60, roof: '#3B82F6' },
  },
  {
    dealId: 2,
    complexName: '군산 [단지명]',
    region: '전북 군산시',
    area: 45,
    priceManwon: 2400,
    sprite: { id: 'px-apt-mid', viewBox: '0 0 14 26', width: 28, height: 52 },
  },
]
const MOCK_CLOCK = '02:14:30'
const NEXT_TIER_GOAL = 5
const ORDINALS = ['첫', '두', '세', '네', '다섯', '여섯']

const roofStyle = (roof: string): CSSProperties => ({ '--roof': roof }) as CSSProperties

export function VillagePage() {
  const houseCount = MOCK_HOUSES.length
  const totalManwon = MOCK_HOUSES.reduce((sum, h) => sum + h.priceManwon, 0)
  const totalWon = totalManwon * 10_000
  const avgManwon = houseCount > 0 ? totalManwon / houseCount : 0
  const progressPct = Math.min(100, (houseCount / NEXT_TIER_GOAL) * 100)
  const nextOrdinal = ORDINALS[houseCount] ?? `${houseCount + 1}번째`

  const hudChips = (
    <>
      <div className="k-chip">
        <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
        <div><div className="k-lab">TOTAL</div><div className="k-num" style={{ color: '#FFC93C' }}>{formatNumber(totalWon)}</div></div>
      </div>
      <div className="k-chip">
        <Sprite id="px-clock" viewBox="0 0 8 8" width={16} height={16} />
        <div><div className="village-clock-lab">장 마감까지</div><div className="k-num" style={{ color: '#FFFFFF' }}>{MOCK_CLOCK}</div></div>
      </div>
    </>
  )

  return (
    <div className="page-village k-page">
      <header className="k-top k-top--hero">
        <Link to="/home" className="k-logo"><span className="k-mark">꿈</span><span>꿈이</span></Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" /></svg><span>홈</span></Link>
          <Link to="/trade" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" /></svg><span>투자</span></Link>
          <Link to="/homes" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" /></svg><span>집 찾기</span></Link>
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
        <Sprite id="px-apt-low" viewBox="0 0 16 20" width={80} height={100} className="sp k-low" style={{ left: '7%', bottom: 34, ...roofStyle('#3B82F6') }} />
        <Sprite id="px-apt-mid" viewBox="0 0 14 26" width={70} height={130} className="sp k-mid" style={{ left: '33%', bottom: 34 }} />
        <Sprite id="px-tree" viewBox="0 0 10 12" width={50} height={60} className="sp k-tree" style={{ left: '56%', bottom: 38 }} />
        <div className="k-slot k-next" role="img" aria-label="다음 집 자리">
          <svg width="16" height="16" viewBox="0 0 8 8" className="village-plus" aria-hidden="true"><path d="M3 0h2v8h-2zM0 3h8v2h-8z" /></svg>
          <div className="k-lab village-lab-white">NEXT</div>
        </div>
        <div className="k-slot k-wide" style={{ left: '66%', opacity: 0.55 }} role="img" aria-label="빈 집 자리"><div className="k-lab village-lab-white">?</div></div>
        <div className="k-slot k-wide" style={{ left: '82%', opacity: 0.55 }} role="img" aria-label="빈 집 자리"><div className="k-lab village-lab-white">?</div></div>
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
              <div className="village-total-sub">보유 {houseCount}채 · 평균 {formatManwon(avgManwon)}</div>
            </section>

            <section className="k-card village-stage" aria-label="꿈이 변신 단계">
              <div className="village-stage-img"><img src={tier3} alt="졸부 꿈이" /></div>
              <div className="village-stage-text">
                <div className="k-lab" style={{ color: '#6B6F80' }}>STAGE 3 / 4</div>
                <div className="village-stage-name">졸부 꿈이</div>
                <div className="village-stage-desc">집 {NEXT_TIER_GOAL}채 모으면 갑부 꿈이 해금!</div>
                <div className="village-progress-row">
                  <div className="village-progress">
                    <div className="village-progress-fill" style={{ width: `${progressPct}%` }} />
                    <div className="k-stripe village-progress-stripe" />
                  </div>
                  <div className="village-progress-text">{houseCount}/{NEXT_TIER_GOAL}채</div>
                </div>
                <Link to="/tiers" className="village-tiers-link">변신 4단계 보기</Link>
              </div>
            </section>

            <button type="button" className="village-share">친구한테 자랑하기</button>
          </div>

          <div className="k-col">
            <div className="village-dex-head">
              <h2 className="village-dex-title">내 도감</h2>
              <div className="village-dex-sub">모은 집이 곧 인생역전 기록이에요</div>
            </div>

            {MOCK_HOUSES.map((house) => (
              <article key={house.dealId} className="k-card village-house">
                <div className="village-thumb">
                  <div className="village-thumb-grass" />
                  <Sprite
                    id={house.sprite.id}
                    viewBox={house.sprite.viewBox}
                    width={house.sprite.width}
                    height={house.sprite.height}
                    className="village-thumb-sprite"
                    style={house.sprite.roof ? roofStyle(house.sprite.roof) : undefined}
                  />
                </div>
                <div className="village-house-info">
                  <div className="village-house-name">{house.complexName}</div>
                  <div className="village-house-meta">{house.region} · 전용 {house.area}㎡</div>
                  <div className="village-house-price">
                    매수가 <b>{formatManwon(house.priceManwon)}</b> · 대출 {formatManwon(house.priceManwon * 0.6)}
                  </div>
                </div>
                <Link to={`/contract/${house.dealId}`} className="village-contract-link">계약서</Link>
              </article>
            ))}

            <Link to="/homes" className="k-card village-empty">
              <div className="village-empty-box">?</div>
              <div className="village-empty-text">
                <div className="village-house-name">다음 집은?</div>
                <div className="village-empty-sub">{nextOrdinal} 번째 칸이 비어 있어요</div>
              </div>
              <div className="village-empty-cta">집 찾기 &gt;</div>
            </Link>
          </div>
        </div>
        <p className="k-note village-footnote">※ 집을 2채 산 뒤를 가정한 샘플이에요. 전부 가짜 돈이에요.</p>
      </main>
    </div>
  )
}
