// { } 는 클래스 없이 바로 만든 객체 하나(객체 리터럴)
// new도 없고 클래스도 없음
// 키: member
// 값: 화살표 함수
export const queryKeys = {
    // member: 객체 속성
    // as const : 이 배열은 바뀌지 않는 딱 이 값이라고 TypeScript에 알려줌
    // TypeScript는 타입을 지정해야하니까 readonly ['member', 'me']
    // as const안쓰면 string[] 타입이고 문자열이 몇 개 든 배열, 나중에 바뀔 수도 있음 이라는 뜻이 됨
    member: () => ['member', 'me'] as const,
    // () => [배열] : 이 객체 속성을 부르면 배열을 돌려준다는 뜻이 됨
}

/**
 * hook : 컴포넌트에서 react의 기능을 쓸 수 있게 만들어주는 함수
 * 이름은 항상 use로 시작함, useMutation, useQuery...
 * react 컴포넌트는 그냥 함수이고, 화면이 바뀔때마다(렌더) 처음부터 다시 실행됨
 * 자바 메서드의 지역변수처럼 함수 안에 든 변수는 실행이 끝나면 사라지고 다음 실행 때 새로 만들어져서
 * 컴포넌트 혼자서는 아무것도 기억할 수 없음
 * 그래서 훅은 react 한테 이 값 좀 대신 맡아줘, 이 작업 좀 대신 해줘 하고 부탁하는 창구가 됨
 * useState : 값 기억
 * useQuery : 서버 요청, 로딩 실패, 상태, 캐시 
 * useEffect : 렌더 후 작업
 * useContext : 여러 화면이 같이 쓰는 값 꺼내기
 * useNavigate(react-router) : 화면 이동
 * 
 * 커스텀 훅도 가능함 , 이름은 use로 시작해야됨
 * 
 * 컴포넌트나 다른 훅의 맨 위에서만 호출할 수 있음
 * if, 반복문, 클릭 핸들러 안에서 부르면 안됨
 * react는 훅을 이름이 아니라 호출 순서로 구분함
 */