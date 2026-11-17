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
        <p className="k-note">※ 보유한 집 수에 따라 꿈이가 변신해요.</p>
      </main>
    </div>
  )
}
