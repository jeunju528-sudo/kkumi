import { Link } from 'react-router-dom'
import { Sprite } from './Sprite'
import { formatNumber } from '../utils/format'

type OrderHeaderProps = {
  cash: number
}

// 화면 상단 띠(로고, 메뉴, CASH칩)
export function OrderHeader({cash}: OrderHeaderProps) {
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
        <Link to="/trade" className="k-link k-on" aria-current="page">
          <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
            <path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" />
          </svg>
          <span>투자</span>
        </Link>
        <Link to="/apartments" className="k-link">
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
          <div className="k-num k-cash">{formatNumber(cash)}</div>
        </div>
      </div>
    </header>
  )
}
