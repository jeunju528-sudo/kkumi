import { useMutation } from '@tanstack/react-query'
import { selectSeed } from '../api/memberApi'

/**
 * 렌더란? react가 컴포넌트 함수를 실행해서 화면이 어떻게 생겨야하는지 계산하고
 * 이전과 달라진 부분만 실제 브라우저 화면에 반영하는 과정, 리렌더는 그 함수가 다시 실행되는 것
 * 
 * Hook : 함수 컴포넌트가 react 기능을 쓸 수 있게 해주는 use로 시작하는 함수
 * react 기능 : 값 기억, 화면 다시 그리기, 랜더 후 작업, 공유 데이터 같은 것
 * hook이 필요한 이유
 *  : 리액트 컴포넌트는 그냥 함수이기 때문에 화면이 바뀔 때(렌더될 때) 마다 처음부터 다시 실행됨
 *  : 자바에서의 메서드 안의 지역 변수같은 것, 메서드가 끝나면 사라지고 다시 호출하면 새로 만들어짐
 *  :: 그래서 값을 기억하려면 리액트가 값을 대신 보관해주고 그 보관함을 여는 열쇠가 hook 임!!
 * react 가 맡아두었던 값을 돌려준다고 생각하면 됨
 * react는 hook을 이름이 아니라 호출 순서로 구분함 그래서 컴포넌트나 다른 hook의 맨 위에서만 호출해야함
 *  if, 반복문, 클릭 핸들러 안에서는 호출하면 안 됨 그러면 어떤 랜더에서는 순서가 밀려서 다른 칸의 값을 꺼내게 됨
 * !! hook의 종류
 * useState	    값 기억, 값이 바뀌면 화면 다시 그림
 * useEffect	렌더 후 작업 (WebSocket 구독, cleanup)
 * useContext	여러 화면이 공유하는 값
 * useMutation, useQuery	서버 요청과 상태 관리 (TanStack Query)
 * useNavigate	화면 이동 (react-router)
 * useSelectSeedMutation(예시) 커스텀 훅도 가능, 위 hook들을 묶어 이름 붙인 함수
 */

// useMutation : 쓰기, 캐시 X, 재시도 0번, mutate() 부를 때만 실행
// useQuery : 읽기, 캐시 O, 재시도 3번(기본), 화면 뜨면 자동 실행

// 시드 선택(가입) 요청 훅. 화면에서는 mutate({ seedMoney }) 로 호출
export function useSelectSeedMutation() {
  return useMutation({
    mutationFn: selectSeed, // 여기서 selectSeed() 함수를 바로 호출하지않고 함수 자체를 넘김
  })
}