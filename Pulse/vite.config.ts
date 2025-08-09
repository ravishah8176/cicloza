import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite configuration
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000, // Port for the development server
  },
  css: {
    modules: {
      // Use a more descriptive name format that includes the filename
      // [local] is the class name defined in the CSS file
      // [name] is the name of the file (without extension)
      generateScopedName: '[name]_[local]'
    }
  }
})
