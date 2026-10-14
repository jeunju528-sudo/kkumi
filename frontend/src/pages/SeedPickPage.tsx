import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { SeedMoney } from '../types/api'
import { ApiError } from '../api/apiError'
import { useSelectSeedMutation } from '../hooks/useSelectSeedMutation'
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
  { seedMoney: 'SMALL', label: 'SEED 1', manwon: 500, desc: '현실감 있는 정석 코스' },
  { seedMoney: 'LARGE', label: 'SEED 2', manwon: 5000, desc: '가상감 있는 쉬운 코스' },
]

// 1-1 시드 선택. 가입 직후 1회
export function SeedPickPage() {
  const navigate = useNavigate()
  const [seed, setSeed] = useState<SeedMoney>('SMALL')

  // 훅 호출 추가
  const { mutate, isPending, isError } = useSelectSeedMutation()

  const selected = SEED_OPTIONS.find((o) => o.seedMoney === seed) ?? SEED_OPTIONS[0]
  const ctaLabel = `${formatManwon(selected.manwon)}으로 시작하기`

  const handleSeedClick = (seedMoney: SeedMoney) => {
    setSeed(seedMoney)
  }

  const handleStartClick = () => {
    // mutate(값, {onSuccess, onError}) : 성공이나 실패 때 할 일을 호출하는 자리에서 넘김
    mutate(
      { seedMoney: seed },
      {
        // navigate : react-router가 주는 화면 이동 함수, hook(useNavigate)이 돌려주는 값
        onSuccess: () => navigate('/home'),
        onError: (error) => {
          // ApiError를 확인하는 이유 : 네트워크가 끊기면 ApiError가 아닌 일반 에러가 와서 code가 없음
          if (error instanceof ApiError && error.code === 'ALREADY_JOINED') {
            navigate('/home')
            return
          }
          if (error instanceof ApiError && (error.status === 400 || error.status === 401)) {
            navigate('/')
          }
        },
      },
    )
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

        {/** disabled={isPending} 요청 중에 버튼을 다시 누르는 걸 막아줌 */}
        <button type="button" className="k-cta" onClick={handleStartClick} disabled={isPending}>
          {ctaLabel}
        </button>
        <p className="k-note">
          {isError ? '잠깐 문제가 생겼어요. 다시 눌러 주세요.' : '전부 가짜 돈이에요. 잃어도 괜찮아요.'}
        </p>
      </main>
    </div>
  )
}
