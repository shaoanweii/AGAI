import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import pluginJsxA11y from 'eslint-plugin-jsx-a11y'
import { defineConfig } from 'eslint/config'
import eslintConfigPrettier from 'eslint-config-prettier'

import { readFile } from 'node:fs/promises'

// 添加 auto-import 插件生成的配置
const autoImportFile = new URL('./.eslintrc-auto-import.json', import.meta.url)
let autoImportGlobals = {}
try {
  autoImportGlobals = JSON.parse(await readFile(autoImportFile, 'utf8'))
} catch (error) {
  console.warn('Auto-import file not found, continuing without it')
}

export default defineConfig([
  {
    ignores: ['docs/exampleCode/**']
  },
  {
    files: ['**/*.{js,mjs,cjs,ts,tsx,vue}'],
    plugins: {
      js,
      'jsx-a11y': pluginJsxA11y
    },
    extends: ['js/recommended']
  },
  {
    files: ['**/*.{js,mjs,cjs,ts,tsx,vue}'],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...(autoImportGlobals.globals || {})
      }
    }
  },
  js.configs.recommended,
  tseslint.configs.recommended,
  pluginVue.configs['flat/essential'],
  eslintConfigPrettier,
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
        ecmaFeatures: {
          jsx: true
        }
      }
    }
  },
  {
    // 专门处理 Vue 文件中的 JSX/TSX
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: {
          jsx: true
        }
      }
    }
  },
  {
    // 处理 .tsx 文件
    files: ['**/*.tsx'],
    languageOptions: {
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: {
          jsx: true
        }
      }
    }
  },
  {
    rules: {
      // 常用规则自定义
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-empty-object-type': 'off',
      '@typescript-eslint/no-unused-vars': 'warn',
      'vue/multi-word-component-names': 'off',
      'vue/no-unused-vars': 'warn',
      '@typescript-eslint/ban-ts-comment': 'warn',
    }
  },
  {
    files: ['**/*.vue', '**/*.ts', '**/*.tsx'],
    rules: {
      'no-undef': 'off'
    }
  },
  {
    // 忽略全局类型声明文件中的未使用变量警告
    files: ['src/types/**/*.d.ts'],
    rules: {
      '@typescript-eslint/no-unused-vars': 'off'
    }
  }
])
