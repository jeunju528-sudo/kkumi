import type { ErrorResponse } from '../types/api'

/**
             JAVA                            TypeScript
상속	    extends Exception	            extends Error
필드	    private final String code;	    readonly code: string :: private x, public o, 재대입이 불가
생성자	    ApiError(...) { super(msg); }	constructor(...) { super(message) }
생성	    new ApiError(...)	            new ApiError(...)
타입 확인	e instanceof ApiError	        e instanceof ApiError
 */
// 데이터 묶음, 타입확인이 필요한 곳에 class 사용, 클래스는 런타임에도 남아있어서 instanceof 사용 가능
export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

// 파일 하나가 모듈이고 export가 붙은 class나 function은 다른 파일에서 import해서 쓸 수 있음
// export가 없는 함수는 이 파일에서만 사용하는 전용 함수

// 타입 가드 : 런타임에 서버 바디가 정말 그 모양인지 확인하기 위함, 실행하면 true나 false를 돌려주는 함수
// 서버 바디가 항상 ErrorResponse 모양은 아님 (예: 쿠키 없음 400은 Spring 기본 JSON이라 code 없음)
// 그래서 실행 중에 code·message가 문자열인지 직접 확인 → 통과하면 body.code 사용 가능
// 반환 타입 자리에 boolean 대신 body is ErrorResponse 를 작성하면
// 이 함수가 true를 주면 그 뒤로 body를 ErrorResponse로 믿어도 된다고 컴파일러와 약속
function isErrorResponse(body: unknown): body is ErrorResponse {
  return (
    typeof body === 'object' &&
    body !== null &&
    'code' in body &&
    typeof body.code === 'string' &&
    'message' in body &&
    typeof body.message === 'string'
  )
}

// Promise : 지금은 없지만 나중에 도착할 결과를 담는 객체
// async 함수는 항상 Promise를 반환함
// await는 그 결과가 올 때 까지 이 함수 안에서 기다림, 브라우저 전체를 멈추지 않음
export async function toApiError(res: Response): Promise<ApiError> {
  // .json: 응답 바디(글자)를 읽어서 js객체로 바꿔주는 함수, body json  파싱
  // 서버는 {"code":"ALREADY_JOINED","message":"이미 가입한 회원"} 이렇게 텍스트로 전송
  // 브라우저가 이해할 수 있도록 json 객체로 변환 -> .json : { code: 'ALREADY_JOINED', message: '이미 가입한 회원' }
  // .catch: Promise에 붙이는 실패하면 이걸로 대신해 라고 설정하는 장치
  const body: unknown = await res.json().catch(() => null)
  if (isErrorResponse(body)) {
    return new ApiError(res.status, body.code, body.message)
  }
  return new ApiError(res.status, 'UNKNOWN', `요청 실패(${res.status})`)
}