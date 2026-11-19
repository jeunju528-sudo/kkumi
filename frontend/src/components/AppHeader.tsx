import { Link } from 'react-router-dom'
import type { NotificationResponse } from '../types/api'
import { BellButton } from './BellButton'
import { NotificationPanel } from './NotificationPanel'
import { Sprite } from './Sprite' // 픽셀 그림 하나를 그려주는 컴포넌트
import { formatNumber } from '../utils/format'
import './AppHeader.css'

export type MenuKey = 'home' | 'trade' | 'apartments' | 'village'

type MenuItem = {
  key: MenuKey
  to: string
  label: string
  iconPath: string
}

const MENU_ITEMS: MenuItem[] = [
  { key: 'home', to: '/home', label: '홈', iconPath: 'M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z' },
  { key: 'trade', to: '/trade', label: '투자', iconPath: 'M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z' },
  { key: 'apartments', to: '/apartments', label: '집 찾기', iconPath: 'M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z' },
  { key: 'village', to: '/village', label: '내 마을', iconPath: 'M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z' },
]

type AppHeaderProps = {
  activeMenu: MenuKey | null
  cash: number
  dayCloseLeft: string
  unreadCount: number
  isHero: boolean
  notifications: NotificationResponse[]
  isNotificationLoading: boolean
  isNotificationOpen: boolean
  onBellClick: () => void
  onNotificationClose: () => void
  onNotificationRead: (id: number) => void
  onNotificationReadAll: () => void
  onNotificationDelete: (id: number) => void
}

// 공통 상단 띠. 데스크톱: 로고 + 메뉴 + CASH + 시계 + 종
// 모바일: 메뉴는 하단 탭 바, 로고 숨김. 장면형 화면(isHero)은 칩만 장면 위에 얹음 (design.md §4)
// 종을 누르면 알림 목록이 종 아래로 펼쳐짐 (데스크톱 오른쪽 360px, 모바일 전체 폭)
export function AppHeader({
  activeMenu,
  cash,
  dayCloseLeft,
  unreadCount,
  isHero,
  notifications,
  isNotificationLoading,
  isNotificationOpen,
  onBellClick,
  onNotificationClose,
  onNotificationRead,
  onNotificationReadAll,
  onNotificationDelete,
}: AppHeaderProps) {
  return (
    <header className={isHero ? 'hdr hdr--hero' : 'hdr'}>
      <Link to="/home" className="hdr-logo">
        <span className="hdr-mark">꿈</span>
        <span>꿈이</span>
      </Link>
      <nav className="hdr-links" aria-label="메인 메뉴">
        {MENU_ITEMS.map((item) => {
          const isOn = item.key === activeMenu
          return (
            <Link
              key={item.key}
              to={item.to}
              className={isOn ? 'hdr-link hdr-link--on' : 'hdr-link'}
              aria-current={isOn ? 'page' : undefined}
            >
              <svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true">
                <path d={item.iconPath} />
              </svg>
              <span>{item.label}</span>
            </Link>
          )
        })}
      </nav>
      <div className="hdr-grow" />
      <div className="hdr-chip hdr-chip--cash">
        <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
        <div>
          <div className="hdr-lab">CASH</div>
          <div className="hdr-num hdr-num--cash">{formatNumber(cash)}</div>
        </div>
      </div>
      <div className="hdr-chip">
        <Sprite id="px-clock" viewBox="0 0 8 8" width={16} height={16} />
        <div>
          <div className="hdr-clock-lab">오늘 마감까지</div>
          <div className="hdr-num">{dayCloseLeft}</div>
        </div>
      </div>
      <BellButton unreadCount={unreadCount} isOpen={isNotificationOpen} onClick={onBellClick} />
      {isNotificationOpen && (
        <>
          {/* 바깥 클릭 → 닫기 (모바일은 뒤를 어둡게) */}
          <div className="hdr-backdrop" onClick={onNotificationClose} aria-hidden="true" />
          <NotificationPanel
            notifications={notifications}
            isLoading={isNotificationLoading}
            onRead={onNotificationRead}
            onReadAll={onNotificationReadAll}
            onDelete={onNotificationDelete}
          />
        </>
      )}
    </header>
  )
}
