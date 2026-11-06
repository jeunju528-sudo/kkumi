import { Client } from "@stomp/stompjs";
import type { PortfolioResponse } from "../types/api";

const PORTFOLIO_DESTINATION = '/user/queue/portfolio'
const RECONNECT_DELAY_MS = 5000

// onMessage: 평가액 올 때마다 불러줄 함수 (콜백), (portfolio: PortfolioResponse) => void : 매개변수 타입 :: 매개변수가 함수임
// : () => void :: 함수 반환타입 :: 인자 없이 부르면 아무것도 돌려주지 않는 함수
// 함수를 돌려주는 이유 : 연결객체는 subscribePortfolio 안에서 만들어져서
// 밖에서는 client.deactivate() 를 직접 부를 수 없음, 그래서 대신 끝날 때 이걸 부르라고 함수를 돌려줌
// 돌려준 함수는 자기가 만들어진 곳의 client를 기억하고 있음(클로저)

// 연결·구독 시작하고, 끊을 때 부를 함수를 돌려줌
export function subscribePortfolio(onMessage: (portfolio: PortfolioResponse) => void): () => void {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const client = new Client({
        brokerURL: `${protocol}://${window.location.host}/ws`,
        reconnectDelay: RECONNECT_DELAY_MS,
        onConnect: () => {
            // 연결 완료되면 구독 (재연결 때도 다시 구독)
            client.subscribe(PORTFOLIO_DESTINATION, (message) => {
                onMessage(JSON.parse(message.body) as PortfolioResponse)
            })
        },
    })
    client.activate()

    // 이 돌려주는 함수는 return할 때 실행되지 않고 버튼을 건네주기만 함
    // 실제로 버튼을 누르는건 화면이 사라질 때 react가 cleanup을 부르는 순간임
    return () => {
        // 연결을 닫으면 구독도 같이 사라져서 deactivate
        // deactivate는 연결을 닫는데 시간이 좀 걸려서 Promise를 돌려줌
        // void는 결과 Promise를 일부러 버린다는 표시를 한 것 
        void client.deactivate()
    }
}