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
    },
  },
})
