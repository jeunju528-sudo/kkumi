import type { Tier } from '../types/api'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import tier2 from '../assets/sprites/kkumi-tier2.svg'
import tier3 from '../assets/sprites/kkumi-tier3.svg'
import tier4 from '../assets/sprites/kkumi-tier4.svg'

export type TierInfo = {
  stage: number // 1~4
  name: string
  image: string
  nextText: string // 다음 변신 안내 (갑부는 최종 단계 문구)
}

// 변신 4단계 그림·문구 (집 0 / 1 / 2~4 / 5채 이상, 단계 판정은 서버 Tier)
export const TIER_INFO: Record<Tier, TierInfo> = {
  GEOLBAENGI: { stage: 1, name: '걸뱅이', image: tier1, nextText: '집 1채만 사도 서민으로 변신해요.' },
  SEOMIN: { stage: 2, name: '서민', image: tier2, nextText: '집 2채면 졸부로 변신해요.' },
  JOKBU: { stage: 3, name: '졸부', image: tier3, nextText: '집 5채 모으면 갑부 꿈이 해금!' },
  GAPBU: { stage: 4, name: '갑부', image: tier4, nextText: '최종 단계 달성! 인생역전 완료.' },
}
