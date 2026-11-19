import { Link } from 'react-router-dom'
import type { NotificationResponse } from '../types/api'
import { formatAbsRate } from '../utils/format'
import { toRelativeTime } from '../utils/time'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './NotificationPanel.css'

type NotificationPanelProps = {
  notifications: NotificationResponse[]
  isLoading: boolean
  onRead: (id: number) => void
  onReadAll: () => void
  onDelete: (id: number) => void
}

// 종 아래로 펼쳐지는 보유 종목 하락 알림 목록 (ADR 059, 063)
// 줄 → 안 읽음이면 읽음 + 그 종목 주문 화면, X → 삭제, 모두 읽음은 안 읽은 알림 있을 때만
export function NotificationPanel({ notifications, isLoading, onRead, onReadAll, onDelete }: NotificationPanelProps) {
  const now = new Date()
  const hasUnread = notifications.some((n) => !n.isRead)

  return (
    <section id="noti-panel" className="noti-panel" aria-label="알림">
      <div className="noti-head">
        <h2 className="noti-title">알림</h2>
        <div className="noti-head-side">
          <span className="noti-sub">7일 지나면 사라져요</span>
          {hasUnread && (
            <button type="button" className="noti-readall" onClick={onReadAll}>
              모두 읽음
            </button>
          )}
        </div>
      </div>

      {/* 로딩 중 빈 상태 그림 깜빡임 방지 */}
      {isLoading ? (
        <p className="noti-loading">불러오는 중...</p>
      ) : notifications.length === 0 ? (
        <div className="noti-empty">
          <img src={tier1} alt="" />
          <p>읽지 않은 알림이 없어요! 화이팅!</p>
        </div>
      ) : (
        <ul className="noti-list">
          {notifications.map((n) => (
            <li key={n.id} className={n.isRead ? 'noti-row noti-row--read' : 'noti-row'}>
              <Link
                to={`/trade/${n.stockCode}`}
                className="noti-link"
                onClick={n.isRead ? undefined : () => onRead(n.id)}
              >
                <span className="noti-dot" aria-hidden="true" />
                <span className="noti-body">
                  <span className="noti-text">
                    <b className="noti-name">{n.stockName}</b> 평단보다{' '}
                    <span className="noti-rate">▼ {formatAbsRate(n.dropRate)}</span> 내려갔어요
                  </span>
                  <span className="noti-time">
                    {!n.isRead && <span className="noti-sr">안 읽음, </span>}
                    {toRelativeTime(n.createdAt, now)}
                  </span>
                </span>
              </Link>
              <button
                type="button"
                className="noti-delete"
                aria-label={`${n.stockName} 알림 삭제`}
                onClick={() => onDelete(n.id)}
              >
                <svg width="16" height="16" viewBox="0 0 8 8" aria-hidden="true">
                  <path d="M1 1h1v1h-1zM2 2h1v1h-1zM3 3h2v2h-2zM5 2h1v1h-1zM6 1h1v1h-1zM2 5h1v1h-1zM1 6h1v1h-1zM5 5h1v1h-1zM6 6h1v1h-1z" />
                </svg>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
