import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(process.cwd(), "src"),
    },
  },
  server: {
    port: 5173,
    host: true,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: "dist",
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom"],
          "mui-core": ["@mui/material/styles", "@mui/material/colors"],
          "mui-components": [
            "@mui/material",
            "@mui/icons-material",
            "@mui/lab",
          ],
          "mui-data": ["@mui/x-data-grid"],
          router: ["react-router-dom"],
          query: ["@tanstack/react-query", "@tanstack/react-query-devtools"],
          charts: ["recharts"],
          utils: [
            "axios",
            "zod",
            "zustand",
            "@hookform/resolvers",
            "react-hook-form",
          ],
        },
      },
    },
    target: "esnext",
    minify: "esbuild",
    chunkSizeWarningLimit: 600,
  },
});
