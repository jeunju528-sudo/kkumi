import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

// 상태관리 라이브러리 금지 (useState, useContext만)
const bannedLibs = ['redux', 'react-redux', '@reduxjs/toolkit', 'zustand', 'recoil', 'jotai', 'mobx'].map(
  (name) => ({ name, message: '상태관리 라이브러리 금지 → useState / useContext 사용' }),
)

// no-restricted-imports는 파일별 설정이 덮어써지므로 공통 금지 목록을 항상 같이 넣음
const restrictImports = (patterns = [], paths = []) => [
  'error',
  { paths: [...bannedLibs, ...paths], patterns },
]

const uiLayers = ['**/pages', '**/pages/*', '**/components', '**/components/*', '**/hooks', '**/hooks/*']

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
  },

  // 공통 규칙
  {
    files: ['src/**/*.{ts,tsx}'],
    rules: {
      'no-console': 'error',
      'prefer-const': 'error',
      eqeqeq: ['error', 'always'],
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      '@typescript-eslint/consistent-type-definitions': ['error', 'type'],
      'no-restricted-exports': [
        'error',
        { restrictDefaultExports: { direct: true, named: true, defaultFrom: true, namedFrom: true, namespaceFrom: true } },
      ],
      'no-restricted-imports': restrictImports(),
    },
  },

  // pages, components, hooks: fetch / WebSocket 직접 사용 금지
  {
    files: ['src/pages/**', 'src/components/**', 'src/hooks/**'],
    rules: {
      'no-restricted-globals': [
        'error',
        { name: 'fetch', message: 'fetch 직접 호출 금지 → src/api 함수 사용' },
        { name: 'WebSocket', message: 'WebSocket 직접 연결 금지 → src/ws 사용' },
      ],
    },
  },

  // components: api, ws 참조 금지 (데이터는 props로)
  {
    files: ['src/components/**'],
    rules: {
      'no-restricted-imports': restrictImports([
        {
          group: ['**/api', '**/api/*', '**/ws', '**/ws/*', '**/pages', '**/pages/*'],
          message: 'components에서 api / ws / pages 참조 금지 → 데이터는 props로 받기',
        },
      ]),
    },
  },

  // api, ws, types: UI 레이어 참조 금지
  {
    files: ['src/api/**', 'src/ws/**', 'src/types/**'],
    rules: {
      'no-restricted-imports': restrictImports([
        { group: uiLayers, message: 'api / ws / types에서 UI 레이어(pages, components, hooks) 참조 금지' },
      ]),
    },
  },

  // utils: 순수 함수만 (React, api, ws, UI 참조 금지)
  {
    files: ['src/utils/**'],
    rules: {
      'no-restricted-imports': restrictImports(
        [
          {
            group: [...uiLayers, '**/api', '**/api/*', '**/ws', '**/ws/*'],
            message: 'utils는 순수 함수만 → 다른 레이어 참조 금지',
          },
        ],
        [{ name: 'react', message: 'utils는 순수 함수만 → React 사용 금지' }],
      ),
    },
  },
])
