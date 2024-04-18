import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import viteTsconfigPaths from 'vite-tsconfig-paths'

export default defineConfig({
    // depending on your application, base can also be "/"
    base: '',
    plugins: [react(), viteTsconfigPaths()],
    build: {
        outDir: 'build',
    },
    server: {    
        // this ensures that the browser opens upon server start
        open: true,  
        port: 3001, 
    },
})