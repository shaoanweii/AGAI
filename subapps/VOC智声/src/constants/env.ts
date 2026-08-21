/**
 * 本地演示运行时配置。
 */
const getValueByEnv = <T>(map: Record<string, T>, fallback: T): T => {
  return map[import.meta.env.MODE] || fallback
}

const localAgentId = 1000001

export const qxAgentId = getValueByEnv(
  {
    development: localAgentId,
    'local-demo': localAgentId,
    test: localAgentId,
    production: localAgentId
  },
  localAgentId
)

export const qxMode = getValueByEnv(
  {
    development: 'local',
    'local-demo': 'local',
    test: 'local',
    production: 'local'
  },
  'local'
)

export const eacSso = '/#/login'
export const insUrl = '/#/rootCause'
