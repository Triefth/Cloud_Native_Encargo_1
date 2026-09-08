import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on('error', (err, _req, res) => {
            if (err.code === 'ECONNREFUSED') {
              if (!res.headersSent) {
                res.writeHead(503, {
                  'Content-Type': 'application/json',
                });
                res.end(JSON.stringify({
                  error: true,
                  status: 503,
                  message: 'No se pudo conectar con el BFF Gateway (http://localhost:8080 offline)',
                }));
              }
            }
          });
        },
      },
    },
  },
});
