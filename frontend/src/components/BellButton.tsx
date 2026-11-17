import { Sprite } from './Sprite'

type BellButtonProps = {
  unreadCount: number
  isOpen: boolean
  onClick: () => void
}

const MAX_BADGE_COUNT = 9

// 알림 종 + 안 읽은 수 배지 (0이면 배지 없음, 10개 이상은 9+). 누르면 알림 목록 열고 닫기
export function BellButton({ unreadCount, isOpen, onClick }: BellButtonProps) {
  const hasUnread = unreadCount > 0
  const badgeText = unreadCount > MAX_BADGE_COUNT ? `${MAX_BADGE_COUNT}+` : String(unreadCount)
  const label = hasUnread ? `알림, 안 읽은 알림 ${unreadCount}개` : '알림'

  return (
    <button
      type="button"
      className={isOpen ? 'hdr-bell hdr-bell--open' : 'hdr-bell'}
      aria-label={label}
      aria-expanded={isOpen}
      aria-controls="noti-panel"
      onClick={onClick}
    >
      <Sprite id="px-bell" viewBox="0 0 8 8" width={16} height={16} />
      {hasUnread && <span className="hdr-bell-badge" aria-hidden="true">{badgeText}</span>}
    </button>
  )
}
