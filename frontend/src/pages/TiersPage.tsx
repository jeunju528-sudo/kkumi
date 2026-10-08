import { Link } from 'react-router-dom'
import type { Tier } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import tier2 from '../assets/sprites/kkumi-tier2.svg'
import tier3 from '../assets/sprites/kkumi-tier3.svg'
import tier4 from '../assets/sprites/kkumi-tier4.svg'
import './TiersPage.css'

type TierStage = {
  tier: Tier
  stage: number
  name: string
  condition: string
  image: string
}

const TIER_STAGES: TierStage[] = [
  { tier: 'GEOLBAENGI', stage: 1, name: '걸뱅이', condition: '집 없음', image: tier1 },
  { tier: 'SEOMIN', stage: 2, name: '서민', condition: '집 1채 보유', image: tier2 },
  { tier: 'JOKBU', stage: 3, name: '졸부', condition: '집 2~4채 보유', image: tier3 },
  { tier: 'GAPBU', stage: 4, name: '갑부', condition: '집 5채 이상', image: tier4 },
]

// 목업: 현재 단계
const CURRENT_TIER: Tier = 'GEOLBAENGI'

// 변신 4단계 안내
export function TiersPage() {
  return (
    <div className="page-tiers">
      <header className="k-top">
        <Link to="/home" className="k-logo">
          <span className="k-mark">꿈</span>
          <span>꿈이</span>
        </Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
              <path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z"></path>
            </svg>
            <span>홈</span>
          </Link>
          <Link to="/trade" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
              <path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z"></path>
            </svg>
            <span>투자</span>
          </Link>
          <Link to="/apartments" className="k-link">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
              <path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z"></path>
            </svg>
            <span>집 찾기</span>
          </Link>
          <Link to="/village" className="k-link k-on" aria-current="page">
            <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
              <path d="M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z"></path>
            </svg>
            <span>내 마을</span>
          </Link>
        </nav>
        <div className="k-grow"></div>
      </header>

      <main className="k-wrap">
        <h1 className="k-h1">꿈이 변신 4단계</h1>
        <p className="k-desc">걸뱅이 → 서민 → 졸부 → 갑부. 가진 집 개수에 따라 꿈이의 모습이 바뀌어요.</p>

        <div className="k-grid">
          {TIER_STAGES.map((stage) => {
            const isCurrent = stage.tier === CURRENT_TIER
            return (
              <article key={stage.tier} className={isCurrent ? 'k-card is-current' : 'k-card'}>
                {isCurrent && <div className="k-here">지금 여기</div>}
                <div className="k-stage">
                  <div className="k-ground"></div>
                  <img className="k-char" src={stage.image} alt={`${stage.name} 꿈이`} />
                </div>
                <div className="k-lab">STAGE {stage.stage}</div>
                <div className="k-name">{stage.name}</div>
                <div className="k-cond">{stage.condition}</div>
              </article>
            )
          })}
        </div>
        <p className="k-note">※ 보유한 집 수에 따라 꿈이가 변신해요. 전부 가짜 돈입니다.</p>
      </main>
    </div>
  )
}
