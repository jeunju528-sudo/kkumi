import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// 개발 서버(5173)가 백엔드(8080)로 넘겨줌. 운영은 nginx가 같은 역할
// changeOrigin이 true면 Vite가 백엔드로 보내는 요청의 Host 헤더를 localhost:8080으로 바꾸고,
// Spring은 그 값으로 {baseUrl}을 만들기 때문에 redirect_uri도 8080이 됨
// 그러면 카카오가 로그인 후 8080으로 돌려보내서 화면(5173)에 못 돌아오기 때문에 Host를 5173으로 유지해서 씀
const toBackend = {
  target: 'http://localhost:8080',  // 어디로 넘길지
  changeOrigin: false,              // Host 헤더를 고쳐 쓰지 말 것
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': toBackend,
      '/oauth2': toBackend,
      '/login/oauth2': toBackend,
      '/ws': {...toBackend, ws: true}, // ws:true vite 프록시가 http를 websocket으로 바꾸는 업그레이드 요청도 넘기게하는 요청
    },
  },
})

/** 
 * Websocket 첫 시작은 HTTP로 시작함, 그리고 요청헤더에 websocket하겠다고 알려줌
 *  - Upgrade: websocket
 * 그러면 응답으로 101 Switching Protocols이 뜨고 TCP연결은 이제 HTTP가 아니라 WebSocket 통로가 됨
 * 이 handshake가 끝나면 데이터는 HTTP메세지가 아닌 Frame이라는 작은 단위로 통신이 됨
 * 헤더가 몇 바이트로 아주 가볍기 때문에 실시간에 적합해짐
 * 양쪽 누구도 아무때나 프레임을 보낼 수 있음 -> full duplex
 * ws://a.com/ws
 * wss://a.com/ws < secure
 * */ 