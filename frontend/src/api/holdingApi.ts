import type { HoldingResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getHoldings(): Promise<HoldingResponse[]> {
    // 서버에서 온 응답에서 바로 볼 수 있는건 status(200, 401..), ok(200번대면 true), headers
    // 바로 못보는건 body(바이트 흐름(stream)상태) 이기 때문에 나중에 body보려면 .json해줘야함
    const res = await fetch('/api/holdings');
    if(!res.ok){
        throw await toApiError(res);
    }
    // JSON : 서버가 보낸 글자 형식의 이름
    // .json() : json 글자를 js 객체로 바꿔주는 메서드
    // 서버 응답은 text로 옴 : '[{"code":"KK0001","price":55500}]'
    // .json 을 하면 : [{ code: 'KK0001', price: 55500 }] js 배열(객체)이 됨
    // .json 도 body를 읽는데 시간이 걸려서 Promise를 돌려줌
    // 그래서 await나 return 으로 넘겨야 함
    // const data = await res.json() 
    // return data 와 같은 뜻, 이거는 우리가 기다렸다가 넘겨주는것
    // async 함수는 무엇을 리턴하든 Promise로 감싸서 돌려주는데
    // return 하는게 이미 Promise라면 한 번 더 감싸지 않고 그 Promise가 끝날 때까지 기다렸다가
    // 결과를 이어받음 그래서 그냥 return 해도 이중으로 감싸지지않음
    // 이렇게 리턴하면 이제 이 함수를 가져다 쓰는 useQuery가 Promise를 기다렸다가
    // 값 다 읽으면 그 때 화면을 다시 그려주는 거임
    return res.json() as Promise<HoldingResponse[]>
}