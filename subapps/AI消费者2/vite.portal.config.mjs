import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// AGAI 门户本地联调只需要消费者智调前端，避免 Cloudflare 开发运行时与中文路径冲突。
export default defineConfig({
  plugins: [react()],
  server: {
    strictPort: true
  }
});
