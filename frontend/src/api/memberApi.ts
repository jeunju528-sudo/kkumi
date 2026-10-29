import type { MemberResponse, SelectSeedRequest } from '../types/api' // type을 가져올 때는 import type 으로 작성
import { toApiError } from './apiError'

// export default 는 파일 하나당 하나만 있어야됨
// export 는 개수 제한 없음
// .tsx 화면 파일에서는 컴포넌트가 하나만 있어야함

// 성공 응답은 body가 없기 때문에 반환타입이 void
// Promise<void> 는 끝나긴 하는데 돌려줄 결과는 없다는 뜻
// 만약 함수가 실패하면 throw가 Promise를 실패 상태로 만들고 useMutation이 이걸 받아서 error로 넘겨줌
export async function selectSeed(request: SelectSeedRequest): Promise<void> {
  // fetch에 쿠키 옵션을 따로 안줘도 같은 주소(5173, 프록시 때문에 가능)으로 보내는 요청이라
  // same-origin으로 signupToken 쿠키가 자동으로 실려서 감
  const res = await fetch('/api/members/seed', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) {
    // 실패응답 : ApiError로 변환해서 돌려줌
    throw await toApiError(res)
  }
}

// 로그인 한 회원 정보, 실패하면 ApiError로 던짐 
export async function getMyInfo(): Promise<MemberResponse> {
  const res = await fetch('/api/members/me')
  if(!res.ok){
    throw await toApiError(res)
  }
  return res.json() as Promise<MemberResponse>
}