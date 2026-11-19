import { Link } from 'react-router-dom'
import type { NotificationResponse } from '../types/api'
import { formatAbsRate } from '../utils/format'
import { Sprite } from './Sprite'
import './AlertToast.css'

type AlertToastProps = {
  notification: NotificationResponse
  onClose: () => void
}

// 하락 알림 토스트 (ADR 059). 누르면 그 종목 주문 화면, X는 닫기만. 4초 자동 닫힘·교체는 useMemberSocket
export function AlertToast({ notification, onClose }: AlertToastProps) {
  return (
    <div className="alert-toast" role="status">
      <Link to={`/trade/${notification.stockCode}`} className="alert-toast-link" onClick={onClose}>
        <span className="alert-toast-bell" aria-hidden="true">
          <Sprite id="px-bell" viewBox="0 0 8 8" width={20} height={20} />
        </span>
        <span className="alert-toast-text">
          <b className="alert-toast-name">{notification.stockName}</b> 평단보다{' '}
          <span className="alert-toast-rate">▼ {formatAbsRate(notification.dropRate)}</span> 내려갔어요
        </span>
      </Link>
      <button type="button" className="alert-toast-close" aria-label="알림 닫기" onClick={onClose}>
        <svg width="16" height="16" viewBox="0 0 8 8" aria-hidden="true">
          <path d="M1 1h1v1h-1zM2 2h1v1h-1zM3 3h2v2h-2zM5 2h1v1h-1zM6 1h1v1h-1zM2 5h1v1h-1zM1 6h1v1h-1zM5 5h1v1h-1zM6 6h1v1h-1z" />
        </svg>
      </button>
    </div>
  )
}
