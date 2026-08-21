import { cloudflare } from '@cloudflare/vite-plugin';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

process.env.WRANGLER_WRITE_LOGS ??= 'false';
process.env.WRANGLER_LOG_PATH ??= '.wrangler/logs';
process.env.MINIFLARE_REGISTRY_PATH ??= '.wrangler/registry';

export default defineConfig({
  plugins: [react(), cloudflare()],
});
