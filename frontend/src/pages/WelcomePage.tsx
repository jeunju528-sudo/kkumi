import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './WelcomePage.css'

// 1 시작 화면. 카카오 OAuth 연동 전까지 시드 선택으로 바로 이동
export function WelcomePage() {
  return (
    <div className="page-welcome">
      <header className="k-top">
        <Link to="/" className="k-logo">
          <span className="k-mark">꿈</span>
          <span>꿈이</span>
        </Link>
        <div className="k-grow"></div>
      </header>

      <main className="k-main">
        <div className="k-copy">
          <div className="k-badge">가짜 돈으로 진짜 집 사기</div>
          <h1 className="k-title k-dot" aria-label="인생역전 VS 인생여전">
            <span className="k-title-gold">인생역전</span>
            <span className="k-lab k-title-vs">VS</span>
            <span className="k-title-white">인생여전</span>
          </h1>
          <div className="k-slogan">
            현실에선 못 사는 그 집,
            <br />
            여기선 살 수 있다.
          </div>
          <div className="k-sub">가짜 돈으로 시작하는 나의 부동산 인생역전</div>
          <div className="k-gift">
            <span className="k-gift-label">가입 선물</span>
            <span className="k-gift-value">가짜 시드머니</span>
          </div>
          <div className="k-actions">
            <Link to="/seed" className="k-btn k-kakao">
              카카오로 시작하기
            </Link>
          </div>
        </div>

        <div className="k-scene" aria-label="집 구경 중인 걸뱅이 꿈이">
          <Sprite id="px-cloud" viewBox="0 0 16 6" width={80} height={30} className="sp" style={{ right: 40, top: 140 }} />
          <Sprite id="px-cloud" viewBox="0 0 16 6" width={64} height={24} className="sp" style={{ left: 30, top: 210, opacity: 0.9 }} />
          <Sprite id="px-tower" viewBox="0 0 12 36" width={36} height={108} className="sp" style={{ left: '3%', bottom: 90 }} />
          <Sprite id="px-house" viewBox="0 0 22 18" width={66} height={54} className="sp" style={{ left: '14%', bottom: 90 }} />
          <Sprite id="px-hanok" viewBox="0 0 24 14" width={72} height={42} className="sp k-wide" style={{ left: '30%', bottom: 90 }} />
          <Sprite id="px-mansion" viewBox="0 0 30 20" width={90} height={60} className="sp" style={{ left: '66%', bottom: 90 }} />
          <Sprite id="px-villa" viewBox="0 0 18 16" width={54} height={48} className="sp k-wide" style={{ left: '84%', bottom: 90 }} />
          <div className="k-grass"></div>
          <div className="k-soil"></div>

          <div className="k-bubble">
            저 많은 집 중에
            <br />
            내 집이 하나는 있겠지!
          </div>

          <div className="k-char">
            <img className="k-sprite" src={tier1} alt="걸뱅이 꿈이" />
          </div>
        </div>
      </main>
    </div>
  )
}
