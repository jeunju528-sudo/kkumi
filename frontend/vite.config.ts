import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// 개발 서버(5173)가 백엔드(8080)로 넘겨줌. 운영은 nginx가 같은 역할
const toBackend = {
  target: 'http://localhost:8080',  // 어디로 넘길지
  changeOrigin: false,              // Host 칸을 고쳐 쓰지 말 것
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
