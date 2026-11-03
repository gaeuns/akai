import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// /api 요청은 Spring Boot 서버(8080)로 넘긴다
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
