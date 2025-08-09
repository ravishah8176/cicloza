import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import importPlugin from 'eslint-plugin-import'
import tseslint from 'typescript-eslint'

export default tseslint.config([
  {
    ignores: ['dist/**/*', 'vite.config.ts'],
  },
  {
    files: ['**/*.{ts,tsx}'],
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
      'import': importPlugin,
    },
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parser: tseslint.parser,
      parserOptions: {
        project: './tsconfig.app.json',
        tsconfigRootDir: import.meta.dirname,
      },
    },
    rules: {
      // React Hooks rules
      ...reactHooks.configs.recommended.rules,
      
      // React Refresh rules
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],

      // Import ordering rules
      'import/order': [
        'error',
        {
          'groups': [
            'builtin',        // Node.js built-in modules
            'external',       // External packages
            'internal',       // Internal modules (your project)
            'parent',         // Parent directory imports
            'sibling',        // Same directory imports
            'index',          // Index file imports
            'type'            // TypeScript type imports
          ],
          'newlines-between': 'always',
          'alphabetize': {
            'order': 'asc',
            'caseInsensitive': true
          },
          'pathGroups': [
            {
              'pattern': 'react',
              'group': 'external',
              'position': 'before'
            },
            {
              'pattern': '@/**',
              'group': 'internal',
              'position': 'before'
            },
            {
              'pattern': '../**',
              'group': 'parent',
              'position': 'before'
            },
            {
              'pattern': './**',
              'group': 'sibling',
              'position': 'before'
            }
          ],
          'pathGroupsExcludedImportTypes': ['react']
        }
      ],
      
      // Additional import rules
      'import/newline-after-import': 'error',
      'import/no-duplicates': 'error',
      'import/no-unresolved': 'off', // TypeScript handles this
      'import/first': 'error',
    },
    settings: {
      'import/resolver': {
        'typescript': {
          'project': './tsconfig.app.json',
        },
      },
    },
  },
])
