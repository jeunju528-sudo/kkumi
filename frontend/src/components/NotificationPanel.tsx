import { Link } from 'react-router-dom'
import type { NotificationResponse } from '../types/api'
import { formatAbsRate } from '../utils/format'
import { toRelativeTime } from '../utils/time'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './NotificationPanel.css'

type NotificationPanelProps = {
  notifications: NotificationResponse[]
}

// 종 아래로 펼쳐지는 보유 종목 하락 알림 목록 (ADR 059). 줄 → 그 종목 주문 화면, X → 삭제(동작은 데이터 연결 때)
export function NotificationPanel({ notifications }: NotificationPanelProps) {
  const now = new Date()

  return (
    <section id="noti-panel" className="noti-panel" aria-label="알림">
      <div className="noti-head">
        <h2 className="noti-title">알림</h2>
        <span className="noti-sub">7일 지나면 사라져요</span>
      </div>

      {notifications.length === 0 ? (
        <div className="noti-empty">
          <img src={tier1} alt="" />
          <p>아직 물린 종목이 없어요. 존버 중!</p>
        </div>
      ) : (
        <ul className="noti-list">
          {notifications.map((n) => (
            <li key={n.id} className={n.isRead ? 'noti-row noti-row--read' : 'noti-row'}>
              <Link to={`/trade/${n.stockCode}`} className="noti-link">
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
              <button type="button" className="noti-delete" aria-label={`${n.stockName} 알림 삭제`}>
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
