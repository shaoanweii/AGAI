module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true,
    'vue/setup-compiler-macros': true
  },
  extends: [
    'prettier',
    'plugin:vue/vue3-essential',
    'plugin:@typescript-eslint/recommended',
    'plugin:prettier/recommended' // 解决ESlint和Prettier冲突
  ],
  overrides: [],
  // 配置解析vue文件
  parser: 'vue-eslint-parser',
  parserOptions: {
    ecmaVersion: 'latest',
    parser: '@typescript-eslint/parser',
    sourceType: 'module',
    jsxPragma: 'React',
    ecmaFeatures: {
      jsx: true
    }
  },
  plugins: ['vue', '@typescript-eslint'],
  /**
   * "off" 或 0 - 关闭该规则
   * "warn" 或 1 - 启用并警告（不影响现有代码）
   * "error" 或 2 - 启用并报错（错误代码 1）
   */
  rules: {
    '@typescript-eslint/ban-types': [
      'error',
      {
        extendDefaults: true,
        types: {
          '{}': false
        }
      }
    ],
    'vue/multi-word-component-names': 0,
    '@typescript-eslint/no-explicit-any': ['off'],
    'vue/no-setup-props-destructure': ['off'],
    // 'no-unused-vars': 1,
    '@typescript-eslint/no-unused-vars': [1]
  }
}
