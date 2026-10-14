import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import './index.css'
import { App } from './App.tsx'

// 앱 전체에서 사용하는 캐시 관리자
// 컴포넌트 밖에서 1번만 생성 → 렌더마다 새로 만들면 캐시가 매번 비워짐
const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {/* 안쪽 모든 화면이 같은 queryClient 사용. 없으면 useQuery·useMutation 호출 시 에러 */}
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
