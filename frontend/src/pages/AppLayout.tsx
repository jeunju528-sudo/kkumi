import { useState, useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { AppHeader } from '../components/AppHeader'
import type { MenuKey } from '../components/AppHeader'
import { AlertToast } from '../components/AlertToast'
import { useMemberQuery } from '../hooks/useMemberQuery'
import { useNotificationsQuery } from '../hooks/useNotificationsQuery'
import { useUnreadCountQuery } from '../hooks/useUnreadCountQuery'
import { useReadNotificationMutation } from '../hooks/useReadNotificationMutation'
import { useReadAllNotificationsMutation } from '../hooks/useReadAllNotificationsMutation'
import { useDeleteNotificationMutation } from '../hooks/useDeleteNotificationMutation'
import { useMidnightCountdown } from '../hooks/useMidnightCountdown'
import { useMemberSocket } from '../hooks/useMemberSocket'
import './AppLayout.css'

// 장면형 화면: 모바일에서 띠를 접고 칩만 장면 위에 얹음
const HERO_PATHS = ['/home', '/village']

// 주소 → 켜진 메뉴. 하위 화면도 부모 메뉴 (계약서 → 집 찾기, 변신 단계 → 내 마을)
function toActiveMenu(pathname: string): MenuKey | null {
  if (pathname === '/home') return 'home'
  if (pathname.startsWith('/trade')) return 'trade'
  if (pathname.startsWith('/apartments') || pathname.startsWith('/contract')) return 'apartments'
  if (pathname.startsWith('/village') || pathname.startsWith('/tiers')) return 'village'
  return null
}

// 로그인 화면 공통 틀: 헤더 1번 + 화면 자리(Outlet)
// 1초 시계로 이 컴포넌트만 다시 그려짐. Outlet은 라우터가 준 같은 element라 페이지는 다시 안 그려짐
export function AppLayout() {
  // 브라우저 주소창의 주소 정보를 꺼내주는 react-router hook
  // {pathname, search, hash, ...} 등을 반환해줌
  // 주소가 바뀌면 이 훅을 부른 컴포넌트가 다시 그려짐
  // 화면이 바뀌면 AppLayout이 새 주소로 다시 계산함
  // useParams 과의 차이 : useParams 는 라우트에 정의된 :code 같은 칸의 값만 받음
  const { pathname } = useLocation()
  const { data: member } = useMemberQuery()
  // 0시에 오늘 시작가·상하한·일별 기록이 바뀜 → 장 시간 대신 하루 마감 (ADR 050)
  const dayCloseLeft = useMidnightCountdown()

  const { latestAlert, clearLatestAlert } = useMemberSocket()

  // 알림 목록을 연 화면 주소 (null = 닫힘)
  const [notificationOpenPath, setNotificationOpenPath] = useState<string | null>(null)

  // 알람 연 주소 = 현재 URL  일 때만 열림, 화면을 옮기면 주소가 달라져 저절로 닫힘
  const isNotificationOpen = notificationOpenPath === pathname

  const { data: notifications, isPending: isNotificationLoading } = useNotificationsQuery(isNotificationOpen)
  const { data: unreadCount } = useUnreadCountQuery()
  const { mutate: readNotification } = useReadNotificationMutation()
  const { mutate: readAllNotifications } = useReadAllNotificationsMutation()
  const { mutate: deleteNotification } = useDeleteNotificationMutation()

  // 종 클릭 : 열려 있으면 닫기(null), 닫혀 있으면 지금 주소 저장(열기)
  const handleBellClick = () => setNotificationOpenPath(isNotificationOpen ? null : pathname)
  // 배경 클릭 : 닫기
  const handleNotificationClose = () => setNotificationOpenPath(null)

  // 열려있을 때만 esc 키 듣기
  useEffect(() => {
    if (!isNotificationOpen) return // 닫혀 있으면 리스너 안 붙임
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setNotificationOpenPath(null)
    }
    window.addEventListener('keydown', handleKeyDown) // 브라우저 전체 키 입력에 리스너 등록
    return () => window.removeEventListener('keydown', handleKeyDown) // cleanup : 닫히거나 화면 떠나면 해제 (안 하면 열 때마다 쌓임)
  }, [isNotificationOpen]) // 열림여부가 바뀔 때마다 다시 실행

  return (
    <div className="app-layout">
      <AppHeader
        activeMenu={toActiveMenu(pathname)} // 켜진 메뉴 (골드 테두리)
        cash={member?.cash ?? 0} // 조회 전엔 0
        dayCloseLeft={dayCloseLeft} // 시계 문자열
        unreadCount={unreadCount ?? 0} // 종 배지 숫자
        isHero={HERO_PATHS.includes(pathname)} // 홈·내 마을이면 장면형 헤더
        isNotificationOpen={isNotificationOpen} // 열림 여부
        onBellClick={handleBellClick} // 종 클릭 시 부를 함수
        onNotificationClose={handleNotificationClose} // 바깥 클릭 시 부를 함수
        notifications={notifications ?? []} // 드롭다운에 보여줄 목록 
        isNotificationLoading={isNotificationLoading}
        onNotificationRead={readNotification}
        onNotificationReadAll={() => readAllNotifications()}
        onNotificationDelete={deleteNotification}
      />
      {latestAlert !== null && !isNotificationOpen && (
        <AlertToast notification={latestAlert.notification} onClose={clearLatestAlert} />
      )}
      <Outlet />
    </div>
  )
}
