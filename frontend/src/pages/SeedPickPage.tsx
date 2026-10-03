import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { SeedMoney } from '../types/api'
import { formatManwon } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './SeedPickPage.css'

type SeedOption = {
  seedMoney: SeedMoney
  label: string
  manwon: number
  desc: string
}

const SEED_OPTIONS: SeedOption[] = [
  { seedMoney: 'SMALL', label: 'SEED 1', manwon: 500, desc: '걸뱅이로 시작하는 정석 코스' },
  { seedMoney: 'LARGE', label: 'SEED 2', manwon: 5000, desc: '처음부터 집이 보이는 쉬운 코스' },
]

// 1-1 시드 선택. 가입 직후 1회
export function SeedPickPage() {
  const navigate = useNavigate()
  const [seed, setSeed] = useState<SeedMoney>('SMALL')

  const selected = SEED_OPTIONS.find((o) => o.seedMoney === seed) ?? SEED_OPTIONS[0]
  const ctaLabel = `${formatManwon(selected.manwon)}으로 시작하기`

  const handleSeedClick = (seedMoney: SeedMoney) => {
    setSeed(seedMoney)
  }

  const handleStartClick = () => {
    navigate('/home')
  }

  return (
    <div className="page-seed">
      <header className="k-top">
        <Link to="/" className="k-logo">
          <span className="k-mark">꿈</span>
          <span>꿈이</span>
        </Link>
      </header>

      <main className="k-main">
        <div className="k-hello">
          <img className="k-char" src={tier1} alt="걸뱅이 꿈이" />
          <div className="k-bubble">가입 완료! 얼마로 시작할래요?</div>
        </div>

        <div className="k-opts">
          {SEED_OPTIONS.map((option) => {
            const isSelected = option.seedMoney === seed
            return (
              <button
                key={option.seedMoney}
                type="button"
                className={isSelected ? 'k-opt is-on' : 'k-opt'}
                aria-pressed={isSelected}
                onClick={() => handleSeedClick(option.seedMoney)}
              >
                <span className="k-lab k-opt-lab">{option.label}</span>
                <span className="k-opt-amount">{formatManwon(option.manwon)}</span>
                <span className="k-opt-desc">{option.desc}</span>
              </button>
            )
          })}
        </div>

        <button type="button" className="k-cta" onClick={handleStartClick}>
          {ctaLabel}
        </button>
        <p className="k-note">전부 가짜 돈이에요. 잃어도 괜찮아요.</p>
      </main>
    </div>
  )
}
