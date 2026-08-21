import { fileURLToPath, URL } from 'node:url'
import type { IncomingMessage, ServerResponse } from 'node:http'

import { defineConfig, loadEnv } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
// import vueDevTools from 'vite-plugin-vue-devtools'
// @ts-expect-error 消除错误提示
import eslintPlugin from 'vite-plugin-eslint'
import path from 'path'
import { createLocalSvgIconsPlugin } from './build/svg-icons-plugin'

const timestamp = new Date().getTime()
// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_PROXY_API_TARGET || 'http://172.16.80.16:32215/'
  const newWordsTarget = env.VITE_PROXY_NEW_WORDS_TARGET || 'http://172.16.76.178:8210'
  const aiTarget = env.VITE_PROXY_AI_TARGET || 'http://172.16.76.178:8250'

  return {
    // base: '/ins/',
    base: './',
    build: {
      // 打包文件超过1M 警告提示
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          // 入口文件名
          entryFileNames: `assets/[name].${timestamp}.js`,
          // 块文件名
          chunkFileNames: `assets/[name]-[hash].${timestamp}.js`,
          // 资源文件名 css 图片等等
          assetFileNames: `assets/[name]-[hash].${timestamp}.[ext]`,
          // 超过 chunkSizeWarningLimit值 分包
          manualChunks(id) {
            if (id.includes('node_modules')) {
              return id.toString().split('node_modules/')[1].split('/')[0].toString()
            }
          }
        }
      }
    },
    plugins: [
      {
        name: 'local-standalone-api-mock',
        configureServer(server) {
          const sendJson = (res: ServerResponse, data: unknown) => {
            res.statusCode = 200
            res.setHeader('Content-Type', 'application/json; charset=utf-8')
            res.end(JSON.stringify(data))
          }

          const ok = (result: unknown, message = '操作成功！') => ({
            success: true,
            code: '200',
            message,
            result
          })

          const readJsonBody = (req: IncomingMessage) => {
            return new Promise<Record<string, any>>(resolve => {
              let raw = ''
              req.on('data', chunk => {
                raw += chunk
              })
              req.on('end', () => {
                if (!raw) {
                  resolve({})
                  return
                }
                try {
                  resolve(JSON.parse(raw))
                } catch {
                  resolve({})
                }
              })
              req.on('error', () => resolve({}))
            })
          }

          const getPageParams = (payload: Record<string, any> = {}, requestUrl = '') => {
            let queryParams: Record<string, string> = {}
            try {
              queryParams = Object.fromEntries(
                new URL(requestUrl, 'http://localhost').searchParams.entries()
              )
            } catch {
              queryParams = {}
            }
            const source = { ...queryParams, ...payload }
            const pageNum = Math.max(Number(source.pageNum || source.current || 1) || 1, 1)
            const pageSize = Math.max(Number(source.pageSize || source.size || 10) || 10, 1)
            return { pageNum, pageSize }
          }

          const page = (records: Record<string, any>[] = [], total = records.length) => ({
            records,
            total,
            size: 10,
            current: 1
          })

          const tablePage = (records: Record<string, any>[] = []) => ({
            records,
            list: records,
            total: records.length,
            size: 10,
            current: 1
          })

          const tablePageByPayload = (
            records: Record<string, any>[] = [],
            payload: Record<string, any> = {},
            requestUrl = ''
          ) => {
            const { pageNum, pageSize } = getPageParams(payload, requestUrl)
            const start = (pageNum - 1) * pageSize
            const currentRecords = records.slice(start, start + pageSize)
            return {
              records: currentRecords,
              list: currentRecords,
              total: records.length,
              size: pageSize,
              current: pageNum,
              pageNum,
              pageSize
            }
          }

          const getStandaloneInsightsPath = (requestUrl: string) => {
            let pathname = requestUrl
            try {
              pathname = new URL(requestUrl, 'http://localhost').pathname
            } catch {
              pathname = requestUrl.split('?')[0]
            }
            const shortPrefixes = [
              'accountInfo',
              'accountLexicon',
              'addLabel',
              'attributeLabel',
              'automark',
              'basicInfo',
              'brandInfo',
              'carScene',
              'carSceneCategory',
              'carSeriesInfo',
              'channel',
              'customer',
              'downLoad',
              'insBatchRule',
              'insBatchRuleCategory',
              'insClientInfo',
              'insClosedRule',
              'insCqCaDataSource',
              'insDataExpect',
              'insDataResource',
              'insDataResourceDesc',
              'insDataSource',
              'insKnowledgeBase',
              'insKnowledgeBaseDetails',
              'insProvinceArea',
              'insProjectInfo',
              'insTagLib',
              'insTagLibClient',
              'insTagInfo',
              'keywords',
              'label',
              'regulation',
              'region',
              'role',
              'ruleTest'
            ]
            const localServicePrefixes = [
              '/api/ai/',
              '/ai/',
              '/api/model/',
              '/model/',
              '/api/new-words/',
              '/new-words/'
            ]
            if (pathname.startsWith('/api/insights/')) {
              return pathname.replace('/api/insights', '')
            }
            if (pathname.startsWith('/insights/')) {
              return pathname.replace('/insights', '')
            }
            if (shortPrefixes.some(prefix => pathname.startsWith(`/${prefix}/`))) {
              return pathname
            }
            if (localServicePrefixes.some(prefix => pathname.startsWith(prefix))) {
              return pathname
            }
            return ''
          }

          const standaloneUserPermissions = {
            userId: 'admin',
            username: 'admin',
            name: 'admin',
            employeeId: 'admin',
            clientIds: {
              key: 'clientId',
              details: [
                { key: '0', value: '标准', code: 'system' },
                { key: '764547797eb2e192763f5334028d49c9', value: '东风日产', code: 'dndc' }
              ]
            },
            isAdmin: false,
            defaultClientId: '764547797eb2e192763f5334028d49c9',
            defaultClientIdText: '东风日产',
            menus: [
              {
                id: 'm_data_center',
                icon: 'menus-database-2-fill',
                name: '数据中心',
                path: '/dataCenter',
                permissionKey: 'dataCenter',
                children: [
                  {
                    id: 'm_data_query',
                    icon: null,
                    name: '数据查询',
                    path: '/dataCenter/dataQuery',
                    permissionKey: 'dataCenter-dataQuery'
                  },
                  {
                    id: 'm_data_source',
                    icon: null,
                    name: '数据处理',
                    path: '/dataCenter/insDataSource',
                    permissionKey: 'dataCenter-insDataSource'
                  }
                ]
              },
              {
                id: 'm_operation_management',
                icon: 'menus-operation-management-fill',
                name: '运营管理',
                path: '/operationManagement',
                permissionKey: 'operationManagement',
                children: [
                  {
                    id: 'm_operation_discovery',
                    icon: null,
                    name: '新词发现',
                    path: '/operationManagement/discovery',
                    permissionKey: 'operationManagement-discovery'
                  }
                ]
              },
              {
                id: 'm_knowledge_center',
                icon: 'menus-knowledge-center-fill',
                name: '知识中心',
                path: '/knowledgeCenter',
                permissionKey: 'knowledgeCenter',
                children: [
                  {
                    id: 'm_standard_point',
                    icon: null,
                    name: '标准观点',
                    path: '/knowledgeCenter/standardPoint',
                    permissionKey: 'knowledgeCenter-standardPoint'
                  },
                  {
                    id: 'm_experience_code',
                    icon: null,
                    name: '标签体系',
                    path: '/knowledgeCenter/experienceCode',
                    permissionKey: 'knowledgeCenter-experienceCode'
                  },
                  {
                    id: 'm_user_journey',
                    icon: null,
                    name: '用户旅程',
                    path: '/knowledgeCenter/userJourney',
                    permissionKey: 'knowledgeCenter-userJourney'
                  },
                  {
                    id: 'm_car_usage_scenarios',
                    icon: null,
                    name: '用车场景',
                    path: '/knowledgeCenter/carUsageScenarios',
                    permissionKey: 'knowledgeCenter-carUsageScenarios'
                  },
                  {
                    id: 'm_brand_series',
                    icon: null,
                    name: '品牌车系',
                    path: '/knowledgeCenter/brandSeries',
                    permissionKey: 'knowledgeCenter-brandSeries'
                  },
                  {
                    id: 'm_keyword_library',
                    icon: null,
                    name: '关键词库',
                    path: '/knowledgeCenter/keywordLibrary',
                    permissionKey: 'knowledgeCenter-keywordLibrary'
                  },
                  {
                    id: 'm_corpus_mapping',
                    icon: null,
                    name: '语料映射',
                    path: '/knowledgeCenter/corpusMapping',
                    permissionKey: 'knowledgeCenter-corpusMapping'
                  }
                ]
              },
              {
                id: 'm_rules',
                icon: 'menus-ruler-fill',
                name: '规则引擎',
                path: '/rules',
                permissionKey: 'rules',
                children: [
                  {
                    id: 'm_rules_test',
                    icon: null,
                    name: '规则测试',
                    path: '/rules/rulesTest',
                    permissionKey: 'rules-rulesTest'
                  },
                  {
                    id: 'm_cleaning_rules',
                    icon: null,
                    name: '清洗规则',
                    path: '/rules/cleaningRules',
                    permissionKey: 'rules-cleaningRules'
                  }
                ]
              },
              {
                id: 'm_settings',
                icon: 'menus-settings-6-fill',
                name: '系统设置',
                path: '/settings',
                permissionKey: 'settings',
                children: [
                  {
                    id: 'm_account_management',
                    icon: null,
                    name: '账号管理',
                    path: '/settings/accountManagement',
                    permissionKey: 'settings-accountManagement'
                  },
                  {
                    id: 'm_role',
                    icon: null,
                    name: '角色管理',
                    path: '/settings/role',
                    permissionKey: 'settings-role'
                  },
                  {
                    id: 'm_download',
                    icon: null,
                    name: '下载管理',
                    path: '/settings/download',
                    permissionKey: 'settings-download'
                  }
                ]
              }
            ],
            button: [
              'settings-accountManagement-select',
              'settings-accountManagement-add',
              'settings-accountManagement-edit',
              'settings-role-select',
              'settings-role-add',
              'settings-role-edit',
              'rules-cleaningRules-select',
              'dataCenter-dataSource-select',
              'dataCenter-dataSource-import',
              'dataCenter-dataSource-add',
              'dataCenter-dataSource-edit',
              'dataCenter-dataSource-delete',
              'local_upload'
            ]
          }

          const createRoleAuthTree = (checked = false) => {
            const toPermissionNode = (menu: any, parentId = '0'): any => ({
              id: menu.id,
              pid: parentId,
              code: menu.permissionKey || menu.path || null,
              name: menu.name,
              checked,
              checkButton: false,
              indeterminate: false,
              expand: false,
              icon: menu.icon || '',
              path: menu.path || '',
              children: Array.isArray(menu.children)
                ? menu.children.map((child: any) => toPermissionNode(child, menu.id))
                : null
            })

            return standaloneUserPermissions.menus.map(menu => toPermissionNode(menu))
          }

          const departAccountTree = [
            {
              id: 'dept_1',
              code: 'dept_1',
              name: '客户创新及数据处',
              account: [
                {
                  id: 'admin',
                  userId: 'admin',
                  employeeId: 'admin',
                  username: 'admin',
                  name: 'admin'
                },
                {
                  id: 'u_jiqiu',
                  userId: 'u_jiqiu',
                  employeeId: '6356329',
                  username: '*建秋',
                  name: '*建秋'
                }
              ],
              child: [
                {
                  id: 'dept_1_1',
                  code: 'dept_1_1',
                  name: 'NPS运营管理室',
                  account: [
                    {
                      id: 'u_xuchen',
                      userId: 'u_xuchen',
                      employeeId: '6315436',
                      username: '*旭东',
                      name: '*旭东'
                    }
                  ],
                  child: []
                }
              ]
            },
            {
              id: 'dept_2',
              code: 'dept_2',
              name: '数字化推进室',
              account: [
                {
                  id: 'u_zhangquan',
                  userId: 'u_zhangquan',
                  employeeId: '6315909',
                  username: '*寿权',
                  name: '*寿权'
                }
              ],
              child: []
            }
          ]

          const expandRows = (
            records: Record<string, any>[],
            prefix: string,
            total = 100,
            labelKeys: string[] = []
          ) => {
            if (!records.length) return []

            return Array.from({ length: total }, (_, index) => {
              const source = records[index % records.length]
              const pageNo = Math.floor(index / records.length) + 1
              const next: Record<string, any> = {
                ...source,
                id: `${prefix}_${index + 1}`
              }

              if ('dataId' in next) {
                next.dataId = `${prefix.toUpperCase()}_${String(index + 1).padStart(4, '0')}`
              }
              if ('roleId' in next) {
                next.roleId = `${prefix}_${index + 1}`
              }
              if ('tagCode' in next) {
                next.tagCode = `${prefix.toUpperCase()}_${String(index + 1).padStart(4, '0')}`
              }
              if ('topicCode' in next) {
                next.topicCode = `TOPIC_${String(index + 1).padStart(4, '0')}`
              }
              if ('employeeId' in next) {
                next.employeeId = `${next.employeeId || 'EMP'}${String(pageNo).padStart(2, '0')}`
              }

              labelKeys.forEach(key => {
                const value = next[key]
                if (typeof value === 'string' && value.trim()) {
                  next[key] = value.replace(/-\d{3}$/, '')
                }
              })

              return next
            })
          }

          const enableConditions = [
            {
              key: 'stopOrEnable',
              details: [
                { key: '1', value: '启用' },
                { key: '0', value: '禁用' }
              ]
            }
          ]

          const commonConditions = [
            ...enableConditions,
            {
              key: 'enableType',
              details: [
                { key: '1', value: '启用' },
                { key: '0', value: '禁用' }
              ]
            },
            {
              key: 'completionRate',
              details: [
                { key: '100', value: '100%' },
                { key: '0', value: '0%' }
              ]
            },
            {
              key: 'auditStatus',
              details: [
                { key: '0', value: '待审核' },
                { key: '1', value: '已通过' },
                { key: '2', value: '已拒绝' }
              ]
            },
            {
              key: 'ruleType',
              details: [{ key: '2', value: '闭环规则' }]
            },
            {
              key: 'ruleTest',
              details: [
                { key: '0', value: '未开始' },
                { key: '1', value: '已完成' }
              ]
            },
            {
              key: 'isCore',
              details: [
                { key: '1', value: '是' },
                { key: '0', value: '否' }
              ]
            },
            {
              key: 'isNewCar',
              details: [
                { key: '1', value: '是' },
                { key: '0', value: '否' }
              ]
            },
            {
              key: 'competitiveType',
              details: [
                { key: '0', value: '本品' },
                { key: '1', value: '竞品' }
              ]
            },
            {
              key: 'brand',
              details: [
                { key: 'BRAND_ALPHA', value: '领航品牌' },
                { key: 'BRAND_BETA', value: '启程品牌' }
              ]
            },
            {
              key: 'tagLibeType',
              details: [
                { key: 'CA', value: '全领域业务' },
                { key: 'JOUR', value: '用户全旅程' },
                { key: 'PRO', value: '商品化属性' },
                { key: 'NPS', value: 'NPS' },
                { key: 'VRT', value: 'VRT' },
                { key: 'CPT', value: 'CPT' }
              ]
            },
            {
              key: 'vocSentiment',
              details: [
                { key: '负面', value: '负面' },
                { key: '中性', value: '中性' }
              ]
            },
            {
              key: 'vocIntention',
              details: [
                { key: '抱怨', value: '抱怨' },
                { key: '建议', value: '建议' }
              ]
            },
            {
              key: 'closedRuleLevel',
              details: [
                { key: 'B', value: 'B' },
                { key: 'C', value: 'C' }
              ]
            },
            {
              key: 'issueSeverit',
              details: [
                { key: '负面', value: '负面' },
                { key: '中性', value: '中性' }
              ]
            },
            {
              key: 'eventClarity',
              details: [{ key: '事实', value: '事实' }]
            },
            {
              key: 'businessDomain',
              details: [
                { key: '产品质量', value: '产品质量' },
                { key: '产品体验', value: '产品体验' }
              ]
            },
            {
              key: 'complaintFlagNeedingReply',
              details: [
                { key: '是', value: '是' },
                { key: '否', value: '否' }
              ]
            },
            {
              key: 'needForvclosedLoop',
              details: [
                { key: '是', value: '是' },
                { key: '否', value: '否' }
              ]
            },
            {
              key: 'accuracy',
              details: [{ key: '准确', value: '准确' }]
            },
            {
              key: 'susceptiveType',
              details: [{ key: '一般', value: '一般' }]
            }
          ]

          const corpusMappings = [
            ['语音操控', '特别顺手', '语音助手好用', '2026-05-25 16:36:50', '冉江雪 6304963'],
            ['续航扎实', '', '续航里程长', '2026-05-25 16:35:37', '冉江雪 6304963'],
            [
              '加满油不跳枪，油直接往外喷',
              '',
              '油箱加注不畅/倒灌',
              '2026-05-25 16:33:56',
              '赵玉兰 Y00522'
            ],
            ['星海S09雨天漏水', '', '车辆漏水', '2026-05-25 16:32:28', '冉江雪 6304963'],
            ['前支柱', '在渗油', '底盘支柱问题', '2026-05-25 16:32:03', '赵玉兰 Y00522'],
            ['车辆有一股烧焦味道', '', '车内有异味', '2026-05-25 16:29:01', '赵玉兰 Y00522'],
            ['四轮定位参数', '失准', '四轮定位数据异常', '2026-05-25 16:26:21', '赵玉兰 Y00522'],
            ['车内', '不识别人脸', '人脸识别无法识别', '2026-05-25 16:24:41', '冉江雪 6304963']
          ].map(([entity, description, standardOpinion, createdTime, operator], index) => ({
            id: `corpus_${index + 1}`,
            entity,
            description,
            standard_opinion: standardOpinion,
            created_time: createdTime,
            updated_time: createdTime,
            operator,
            enable_status: '1'
          }))

          const sceneCategories = [
            ['操作情景', 23],
            ['路况', 14],
            ['天气', 6],
            ['行车速度', 5],
            ['驾驶者身份', 6],
            ['使用场景', 20],
            ['里程数', 8],
            ['使用时间', 6],
            ['车机操作', 8],
            ['驾驶辅助与模式', 6],
            ['车辆管理', 14],
            ['舆情管理', 1],
            ['权益兑现', 5],
            ['测试', 1]
          ].map(([categoryName, leafCount], index) => ({
            id: `scene_category_${index + 1}`,
            patentId: '0',
            categoryName,
            categoryDescription: '',
            synonyms: '',
            typeName: '',
            level: 1,
            leafCount,
            status: '1',
            children: []
          }))

          const sceneRows = [
            ['使用导航', '*雯婷-6320824', '2026-04-14 14:12:59', '2026-03-25 14:11:57'],
            ['开启空调', '*雯婷-6320824', '2026-04-14 14:13:30', '2026-03-25 14:11:57'],
            ['播放音乐', '*雯婷-6320824', '2026-04-14 14:14:25', '2026-03-25 14:11:57'],
            ['播放视频', '', '2026-03-25 14:11:57', '2026-03-25 14:11:57'],
            ['开锁/解锁', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53'],
            ['掉头', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53'],
            ['充电', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53'],
            ['开闭车外门把手', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53'],
            ['开关车门', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53'],
            ['减速', '', '2026-03-25 14:11:53', '2026-03-25 14:11:53']
          ].map(([sceneName, operator, updateTime, createTime], index) => ({
            id: `scene_${index + 1}`,
            sceneName,
            operator,
            updateTime,
            createTime,
            status: '1',
            statusName: '已启用'
          }))

          const experienceCategoryTree = [
            ['CA', '全领域业务', 2035],
            ['JOUR', '用户全旅程', 62],
            ['PRO', '商品化属性', 199],
            ['NPS', 'NPS', 164],
            ['VRT', 'VRT', 478],
            ['CPT', 'CPT', 495]
          ].map(([tagType, tagName, leafCount], index) => ({
            id: `exp_root_${tagType}`,
            tagName,
            tagType,
            tagCode: String(tagType),
            level: 0,
            tagStatus: '1',
            leafCount,
            child:
              tagType === 'CA'
                ? [
                    {
                      id: 'exp_ca_product',
                      tagName: '产品',
                      tagType,
                      tagCode: 'CA_PRODUCT',
                      level: 1,
                      tagStatus: '1',
                      leafCount: 1621,
                      child: [
                        {
                          id: 'exp_ca_power',
                          tagName: '动力系统',
                          tagType,
                          tagCode: 'CA_POWER',
                          level: 2,
                          tagStatus: '1',
                          leafCount: 120,
                          child: [
                            {
                              id: 'exp_ca_fuel',
                              tagName: '燃油及进排气系统',
                              tagType,
                              tagCode: 'CA_FUEL',
                              level: 3,
                              tagStatus: '1',
                              leafCount: 10,
                              child: []
                            }
                          ]
                        }
                      ]
                    }
                  ]
                : [],
            order: index + 1
          }))

          const experienceCodeRows = [
            '排气歧管材质',
            '排气歧管综合表征',
            '油箱容积',
            '油箱燃油加注',
            '油箱综合表征',
            '消声器漆面表征',
            '消声器综合表征',
            '脱附管路综合表征',
            '碳罐系统流通性',
            '碳罐系统综合表征'
          ].map((tagName, index) => ({
            id: `exp_code_${index + 1}`,
            tagParentId: 'exp_ca_fuel',
            tagName,
            tagCode: `CA_FUEL_${index + 1}`,
            tagType: 'CA',
            tagTypeName: '全领域业务',
            tagStatus: '1',
            tagStatusText: '启用',
            tagLibNameHierarchical: '产品#动力系统#燃油及进排气系统',
            createTime: `2025-09-27 01:46:${String(21 + index * 2).padStart(2, '0')}`,
            updateTime: '2026-04-15 20:47:49',
            operateUser: '',
            hasFinalTopic: true
          }))

          const attributeRows = [
            ['舒适性', '*雯婷-6320824', '2026-04-27 13:51:44'],
            ['实用性', '', '2026-04-14 14:50:21'],
            ['动力性', '王集福-6322321', '2026-05-08 14:37:54'],
            ['电流电压', '', '2026-04-14 14:50:21'],
            ['人机交互', '', '2026-04-14 14:50:21'],
            ['车辆运行状态', '', '2026-04-14 14:50:21'],
            ['车辆风格', '', '2026-04-14 14:50:21'],
            ['新车上市', '', '2026-04-14 14:50:21'],
            ['车机系统运行', '', '2026-04-14 14:50:21'],
            ['电子模块/传感器', '', '2026-04-14 14:50:21']
          ].map(([name, updateUser, updateTime], index) => ({
            id: `attr_${index + 1}`,
            name,
            updateUser,
            updateTime,
            createTime: '2026-04-14 14:50:21',
            status: '1',
            statusName: '已启用'
          }))

          const brandRows = [
            {
              id: 'brand_1',
              code: 'BRAND_LH',
              brandCode: 'BRAND_LH',
              brandName: '领航品牌',
              name: '领航品牌',
              status: '1',
              statusName: '已启用'
            },
            {
              id: 'brand_2',
              code: 'BRAND_QC',
              brandCode: 'BRAND_QC',
              brandName: '启程品牌',
              name: '启程品牌',
              status: '1',
              statusName: '已启用'
            },
            {
              id: 'brand_3',
              code: 'BRAND_XH',
              brandCode: 'BRAND_XH',
              brandName: '星海汽车',
              name: '星海汽车',
              status: '1',
              statusName: '已启用'
            }
          ]

          const brandSeriesRows = [
            [
              'CS35 PLUS',
              '领航品牌',
              '是',
              '否',
              '本品',
              '王集福(6322321)',
              '2026-05-15 11:34:07',
              '2025-10-13 18:56:44'
            ],
            [
              'CS75 PLUS',
              '领航品牌',
              '是',
              '是',
              '本品',
              '*雯婷(6320824)',
              '2026-05-21 14:19:27',
              '2025-10-13 18:56:45'
            ],
            [
              'UNI-T',
              '领航品牌',
              '是',
              '否',
              '本品',
              '王集福(6322321)',
              '2026-05-18 10:50:16',
              '2025-10-13 18:56:46'
            ],
            [
              'UNI-K',
              '领航品牌',
              '是',
              '否',
              '本品',
              '王集福(6322321)',
              '2026-05-18 10:50:25',
              '2025-10-13 18:56:46'
            ],
            [
              'UNI-V',
              '领航品牌',
              '是',
              '否',
              '本品',
              '杨浪(6305166)',
              '2026-04-28 17:23:45',
              '2025-10-13 18:56:47'
            ],
            [
              '糯玉米',
              '启程品牌',
              '是',
              '否',
              '本品',
              '王棚(56460)',
              '2026-05-07 16:58:58',
              '2025-10-13 18:56:47'
            ],
            [
              '第二代CS55PLUS',
              '领航品牌',
              '是',
              '',
              '本品',
              '',
              '2026-03-11 20:54:25',
              '2025-10-13 18:56:49'
            ],
            [
              '逸动PLUS',
              '领航品牌',
              '是',
              '否',
              '本品',
              '黄雯婷(6320824)',
              '2026-04-21 15:53:51',
              '2025-10-13 18:56:51'
            ]
          ].map(
            (
              [
                name,
                brandName,
                isCoreName,
                isNewCarName,
                competitiveTypeName,
                operator,
                updateTime,
                createTime
              ],
              index
            ) => ({
              code: `SERIES_${String(index + 1).padStart(3, '0')}`,
              id: `series_${index + 1}`,
              name,
              brandName,
              brandId: brandRows.find(brand => brand.name === brandName)?.id || brandRows[0].id,
              brandCode:
                brandRows.find(brand => brand.name === brandName)?.code || brandRows[0].code,
              img: 'mock-series-cover.png',
              alias: `${name} ${name.replace(/\s/g, '')}`,
              exclusionWords: '',
              isCore: isCoreName === '是' ? '1' : '0',
              isCoreName,
              isNewCar: isNewCarName === '是' ? '1' : '0',
              isNewCarName,
              competitiveType: competitiveTypeName === '竞品' ? 2 : 1,
              competitiveTypeName,
              competitiveProduct: [],
              preheatStartTime: '2026-05-01',
              preheatEndTime: '2026-05-10',
              launchStartTime: '2026-05-11',
              launchEndTime: '2026-05-20',
              stableStartTime: '2026-05-21',
              stableEndTime: '2026-06-30',
              operator,
              updateTime,
              createTime,
              status: '1',
              statusName: '已启用'
            })
          )

          const standardPointRows = [
            {
              id: 'topic_1',
              topicCode: 'TOPIC_001',
              topicName: '智驾综合表征',
              emotion: '负面',
              intention: '抱怨',
              tagCustomerIssueClassification: 'B',
              eventClarity: '事实',
              tagBusinessDomain: '产品质量',
              tagComplaintFlagNeedingReply: '是',
              tagNeedForvclosedLoop: '是',
              ca: {
                firstName: '产品',
                secondName: '智能化',
                thirdName: '驾驶辅助',
                fourthName: '智驾综合表征'
              },
              operateUser: '杨浪',
              updateTime: '2026-05-12 15:29:07',
              createUser: '杨浪',
              createTime: '2026-05-12 15:29:07',
              tagStatus: '1'
            },
            {
              id: 'topic_2',
              topicCode: 'TOPIC_002',
              topicName: '智驾避障能力',
              emotion: '中性',
              intention: '建议',
              tagCustomerIssueClassification: 'C',
              eventClarity: '事实',
              tagBusinessDomain: '产品体验',
              tagComplaintFlagNeedingReply: '是',
              tagNeedForvclosedLoop: '是',
              ca: {
                firstName: '产品',
                secondName: '智能化',
                thirdName: '驾驶辅助',
                fourthName: '智驾避障能力'
              },
              pro: { firstName: '安全性', secondName: '主动安全体验', thirdName: '主动绕障体验' },
              operateUser: '杨浪',
              updateTime: '2026-05-12 09:29:21',
              createUser: '杨浪',
              createTime: '2026-05-12 09:29:21',
              tagStatus: '1'
            },
            {
              id: 'topic_3',
              topicCode: 'TOPIC_003',
              topicName: '发动机轴承异响',
              emotion: '负面',
              intention: '抱怨',
              tagCustomerIssueClassification: 'B',
              eventClarity: '事实',
              tagBusinessDomain: '产品体验',
              tagComplaintFlagNeedingReply: '否',
              tagNeedForvclosedLoop: '是',
              ca: {
                firstName: '产品',
                secondName: '动力系统',
                thirdName: '发动机',
                fourthName: '发动机综合表征'
              },
              operateUser: '赵玉兰',
              updateTime: '2026-05-11 15:35:36',
              createUser: '赵玉兰',
              createTime: '2026-05-11 15:35:36',
              tagStatus: '1'
            }
          ]

          const correctionRows = [
            [
              '数据纠错20260401-20260525',
              '1',
              '*雯婷',
              '2026-05-25 10:39:30',
              '2026-05-25 10:39:30',
              '',
              '0',
              '待审核'
            ],
            [
              '数据纠错20260512-20260518',
              '1',
              '*建秋',
              '2026-05-19 11:05:14',
              '2026-05-19 11:05:49',
              '*建秋',
              '2',
              '已拒绝'
            ],
            [
              '数据纠错20260416-20260515',
              '1',
              '王集福',
              '2026-05-15 17:35:57',
              '2026-05-15 17:36:32',
              '王集福',
              '1',
              '已通过'
            ],
            [
              '数据纠错20260416-20260515',
              '1',
              '王集福',
              '2026-05-15 17:32:21',
              '2026-05-15 17:33:25',
              '王集福',
              '1',
              '已通过'
            ],
            [
              '数据纠错20260501-20260507',
              '1',
              '*棚',
              '2026-05-07 16:01:52',
              '2026-05-07 16:01:51',
              '',
              '0',
              '待审核'
            ],
            [
              '数据纠错20260501-20260507',
              '1',
              '*雯婷',
              '2026-05-07 10:16:01',
              '2026-05-07 10:16:08',
              '*雯婷',
              '1',
              '已通过'
            ],
            [
              '数据纠错20260101-20260506',
              '1',
              '王集福',
              '2026-05-06 15:39:17',
              '2026-05-06 15:39:16',
              '',
              '0',
              '待审核'
            ]
          ].map(
            (
              [
                correctionInfo,
                correctionCount,
                createUser,
                createTime,
                auditTime,
                auditUser,
                auditStatusCode,
                auditStatus
              ],
              index
            ) => ({
              id: `correction_${index + 1}`,
              correctionInfo,
              correctionCount,
              createUser,
              createTime,
              auditTime,
              auditUser,
              auditStatusCode,
              auditStatus
            })
          )

          const ruleTestRows = [
            [
              '闭环规则2026-04-15 09:54:28',
              '2026-05-07 14:26:49',
              '王集福',
              '2026-04-28 16:15:04',
              '0',
              '未开始'
            ],
            [
              '闭环规则2026-04-15 09:54:28',
              '2026-04-28 16:14:55',
              '王集福',
              '2026-04-28 16:14:55',
              '1',
              '已完成'
            ],
            [
              '闭环规则2026-04-15 09:54:28',
              '2026-04-28 16:14:50',
              '王集福',
              '2026-04-15 10:25:34',
              '0',
              '未开始'
            ],
            [
              '闭环规则2026-04-15 09:54:28',
              '2026-04-15 10:25:27',
              '王集福',
              '2026-04-15 10:25:27',
              '1',
              '已完成'
            ],
            [
              '闭环规则2026-04-15 09:54:28',
              '2026-04-15 09:54:29',
              '王集福',
              '2026-04-15 09:54:36',
              '1',
              '已完成'
            ],
            [
              '闭环规则2026-04-15 09:46:28',
              '2026-04-15 09:46:28',
              '王集福',
              '2026-04-15 09:46:36',
              '1',
              '已完成'
            ],
            [
              '闭环规则2026-04-15 09:46:05',
              '2026-04-15 09:46:06',
              '王集福',
              '2026-04-15 09:46:13',
              '1',
              '已完成'
            ]
          ].map(
            (
              [ruleTestInfo, createTime, createUser, finishTime, testStatus, testStatusStr],
              index
            ) => ({
              id: `rule_test_${index + 1}`,
              ruleTestInfo,
              ruleType: '2',
              ruleTypeText: '闭环规则',
              ruleCount: 2,
              sampleCount: 99,
              createTime,
              createUser,
              finishTime,
              testStatus,
              testStatusStr
            })
          )

          const accountRows = [
            ['*瑞鸿', '6319203', '', '超管', 21, '2026-05-21 09:26:40', 100, '1', '已启用'],
            ['*家利', '6321506', '', '超管', 27, '2026-06-04 16:51:34', 100, '1', '已启用'],
            [
              '*延',
              '202517877',
              '数字化推进室',
              '超管',
              2,
              '2026-05-15 13:53:41',
              100,
              '1',
              '已启用'
            ],
            ['*浚梅', '202318418', 'NPS运营管理室', '业务测试', 0, '', 0, '1', '已启用'],
            ['*厚涛', '56221', '客户创新及数据处', '超管', 0, '', 0, '1', '已启用'],
            ['*夏莲', '202313774', '网络技术室', '', 0, '', 0, '0', '已禁用'],
            ['*锡琛', '202224146', '客户管家室', '', 0, '', 0, '1', '已启用'],
            ['*浪', '6305166', '', '超管', 34, '2026-05-21 13:41:58', 100, '1', '已启用'],
            ['*旭东', '6315436', '', '', 4, '2026-05-06 16:31:58', 100, '1', '已启用'],
            ['*玉兰', 'Y00522', '', '超管', 2, '2026-01-13 14:34:48', 100, '1', '已启用']
          ].map(
            (
              [
                userName,
                employeeId,
                deptName,
                roleName,
                loginCounts,
                lastLoginTime,
                completeRate,
                status,
                statusText
              ],
              index
            ) => ({
              id: `account_${index + 1}`,
              userName,
              employeeId,
              deptName,
              roleName,
              loginCounts,
              lastLoginTime,
              completeRate,
              status,
              statusText
            })
          )

          const roleRows = [
            ['role_1', '业务-闭环规则', '', 1, '启用中'],
            ['role_2', '业务测试', '', 2, '启用中'],
            ['role_3', '测试角色1029', '测试角色1029', 5, '启用中'],
            ['role_4', '超管', '', 28, '启用中']
          ].map(([roleId, roleName, remark, userCount, roleStatusName]) => ({
            id: roleId,
            roleId,
            roleName,
            remark,
            userCount,
            roleStatusName
          }))

          const downloadRows = [
            {
              id: 'download_1',
              fileName: '原始数据-20260414101506',
              downloadTime: '2026-04-14 10:15:07',
              operator: '*建秋',
              status: '1',
              filePath: '/mock-download/original-data-20260414101506.xlsx'
            }
          ]

          const dataProcessingTaskRows = [
            [
              'TASK_001',
              'local',
              '本地舆情原声导入',
              '本地上传',
              1280,
              1280,
              '*建秋-6356329',
              '2026-07-07 11:34:15',
              '2'
            ],
            [
              'TASK_002',
              'system',
              '客服工单系统同步',
              '小红书',
              856,
              1200,
              '*旭东-6315436',
              '2026-07-07 10:42:36',
              '1'
            ],
            [
              'TASK_003',
              'local',
              '新车上市专项分析',
              '汽车之家',
              0,
              2048,
              '*寿权-6315909',
              '2026-07-06 18:12:20',
              '0'
            ],
            [
              'TASK_004',
              'system',
              '经销商回访数据同步',
              '抖音',
              960,
              960,
              '*建秋-6356329',
              '2026-07-06 15:06:48',
              '2'
            ],
            [
              'TASK_005',
              'local',
              '售后热线投诉导入',
              '车质网',
              86,
              100,
              '*雯婷-6320824',
              '2026-07-05 17:20:10',
              '-1'
            ],
            [
              'TASK_006',
              'system',
              '汽车之家口碑同步',
              '汽车之家',
              720,
              720,
              '*旭东-6315436',
              '2026-07-05 14:26:42',
              '2'
            ],
            [
              'TASK_007',
              'local',
              '抖音评论本地导入',
              '本地上传',
              360,
              500,
              '*雯婷-6320824',
              '2026-07-04 16:08:55',
              '1'
            ],
            [
              'TASK_008',
              'system',
              '小红书笔记同步',
              '小红书',
              1860,
              1860,
              '*建秋-6356329',
              '2026-07-04 10:14:33',
              '2'
            ],
            [
              'TASK_009',
              'system',
              '车质网投诉同步',
              '车质网',
              154,
              320,
              '*寿权-6315909',
              '2026-07-03 18:35:18',
              '1'
            ],
            [
              'TASK_010',
              'local',
              '汽车之家论坛导入',
              '汽车之家',
              640,
              640,
              '*建秋-6356329',
              '2026-07-03 15:21:07',
              '2'
            ],
            [
              'TASK_011',
              'system',
              '抖音短视频同步',
              '抖音',
              0,
              800,
              '*旭东-6315436',
              '2026-07-02 11:42:26',
              '0'
            ],
            [
              'TASK_012',
              'local',
              '小红书评论导入',
              '本地上传',
              50,
              120,
              '*雯婷-6320824',
              '2026-07-02 09:18:41',
              '-1'
            ]
          ].map(
            (
              [
                batchId,
                taskType,
                taskName,
                dataSourceName,
                completedCount,
                totalCount,
                createUser,
                createTime,
                status
              ],
              index
            ) => ({
              batchId,
              dataSourceId: `data_source_${index + 1}`,
              taskType,
              taskName,
              dataSourceName,
              completedCount,
              totalCount,
              createUser,
              createTime,
              status,
              availableDataStages:
                status === '2'
                  ? ['raw', 'clean', 'result']
                  : status === '-1'
                  ? index === 11
                    ? ['raw', 'clean', 'result']
                    : ['raw', 'clean']
                  : []
            })
          )

          const resourceGroupRows = [
            ['resource_closed_1', '智能座舱负面', 'closedLoop', 36],
            ['resource_rule_1', '智驾关键词组', 'rule', 42],
            ['resource_rule_2', '续航关键词组', 'rule', 28],
            ['resource_closed_2', '三电质量闭环', 'closedLoop', 24],
            ['resource_closed_3', '服务体验闭环', 'closedLoop', 18]
          ].map(([id, name, type, resourceCount]) => ({
            id,
            name,
            type,
            resourceCount,
            count: resourceCount,
            createTime: '2026-05-25 10:00:00',
            updateTime: '2026-05-25 10:00:00'
          }))

          const closedLoopRuleRows = [
            ['智能座舱负面闭环', '单点', '智能座舱负面', '王集福-6322321', 'enabled', '启用'],
            ['车机语音异常闭环', '单点', '智能座舱负面', '王集福-6322321', 'enabled', '启用'],
            ['续航衰减投诉闭环', '单点', '三电质量闭环', '王集福-6322321', 'enabled', '启用'],
            ['高端品牌交付服务闭环', '单点', '服务体验闭环', '*雯婷-6320824', 'enabled', '启用'],
            ['异味质量问题闭环', '单点', '智能座舱负面', '*雯婷-6320824', 'enabled', '启用']
          ].map(
            ([ruleName, ruleType, categoryTypeName, creator, isEnabled, isEnabledName], index) => ({
              id: `closed_rule_${index + 1}`,
              ruleId: `closed_rule_${index + 1}`,
              ruleName,
              ruleType,
              categoryTypeName,
              creator,
              isEnabled,
              isEnabledName
            })
          )

          const keywordRows = [
            ['智驾接管', '2026-04-29 11:12:27', 'Enabled'],
            ['续航扎实', '2026-04-14 16:27:55', 'Enabled'],
            ['车内异味', '2026-04-12 10:21:08', 'Disabled'],
            ['二手车交易', '2026-04-10 09:18:35', 'Enabled'],
            ['物流托运', '2026-04-09 15:42:11', 'Enabled']
          ].map(([name, createTime, status], index) => ({
            id: `keyword_${index + 1}`,
            name,
            createTime,
            status
          }))

          const accountLexiconRows = [
            ['领航汽车', 'ca_official', '抖音', '2026-04-29 11:12:27', '1'],
            ['星海汽车', 'deepal_official', '小红书', '2026-04-20 14:28:12', '1'],
            ['高端品牌', 'avatr_official', '微博', '2026-04-18 09:32:44', '1'],
            ['启程品牌', 'qiyuan_official', '懂车帝', '2026-04-15 16:17:39', '0']
          ].map(([accountName, accountId, channel, createTime, status], index) => ({
            id: `account_lexicon_${index + 1}`,
            accountName,
            accountId,
            channel,
            createTime,
            status
          }))

          const dataQueryChannelTree = [
            {
              code: 'public',
              name: '公开渠道',
              child: [
                {
                  code: 'social',
                  name: '社交媒体',
                  child: [
                    { code: 'douyin', name: '抖音' },
                    { code: 'xiaohongshu', name: '小红书' },
                    { code: 'weibo', name: '微博' }
                  ]
                },
                {
                  code: 'auto_media',
                  name: '汽车媒体',
                  child: [
                    { code: 'autohome', name: '汽车之家' },
                    { code: 'dongchedi', name: '懂车帝' },
                    { code: 'yiche', name: '易车' }
                  ]
                }
              ]
            },
            {
              code: 'private',
              name: '私域渠道',
              child: [
                {
                  code: 'service',
                  name: '客服反馈',
                  child: [
                    { code: 'hotline', name: '热线' },
                    { code: 'app_feedback', name: 'APP反馈' }
                  ]
                }
              ]
            }
          ]

          const dataQueryConditions = [
            ...commonConditions,
            {
              key: 'brandCar',
              details: [
                {
                  key: 'BRAND_ALPHA',
                  value: '领航汽车',
                  children: [
                    { key: 'CS75_PLUS', value: 'CS75 PLUS' },
                    { key: 'UNI_V', value: 'UNI-V' },
                    { key: 'CS35_PLUS', value: 'CS35 PLUS' }
                  ]
                },
                {
                  key: 'DEEPAL',
                  value: '星海汽车',
                  children: [
                    { key: 'S09', value: '星海S09' },
                    { key: 'SL03', value: '星海SL03' }
                  ]
                }
              ]
            },
            {
              key: 'contentType',
              details: [
                { key: '文章', value: '文章' },
                { key: '视频', value: '视频' },
                { key: '评论', value: '评论' },
                { key: '投诉', value: '投诉' }
              ]
            },
            {
              key: 'dataStatus',
              details: [
                { key: '已清洗', value: '已清洗' },
                { key: '待清洗', value: '待清洗' },
                { key: '清洗失败', value: '清洗失败' }
              ]
            },
            {
              key: 'sentiment',
              details: [
                { key: '正面', value: '正面' },
                { key: '中性', value: '中性' },
                { key: '负面', value: '负面' }
              ]
            },
            {
              key: 'intention',
              details: [
                { key: '抱怨', value: '抱怨' },
                { key: '建议', value: '建议' },
                { key: '咨询', value: '咨询' },
                { key: '表扬', value: '表扬' }
              ]
            }
          ]

          const dataQueryRawRows = [
            {
              dataId: 'VOC_RAW_202605250001',
              firstContentType: '车主反馈',
              secondContentType: '质量问题',
              title: '星海S09雨天漏水',
              content: '用户反馈星海S09雨天车内出现漏水，影响正常使用体验。',
              firstChannelName: '公开渠道',
              isOuter: '公开渠道',
              secondChannelName: '社交媒体',
              channelName: '抖音',
              contentType: '视频',
              brand: '星海汽车',
              series: '星海S09',
              dataCreateTime: '2026-05-25 16:32:28',
              viewCount: 12800,
              commentCount: 328,
              likeCount: 956,
              shareCount: 84,
              favoriteCount: 126,
              originalLink: 'https://example.com/raw/202605250001',
              authorNick: '示例车主',
              authorId: 'A10001',
              oneId: 'ONE202605250001',
              idCarNo: '500101199001010011',
              mobile: '13800000001',
              email: 'owner001@example.com',
              authorType: '车主',
              model: 'SUV',
              vhlId: 'VHL-S09-0001',
              vhlVin: 'LVS09VIN000000001',
              isWsaterArmy: '否',
              mainPostAuthorNick: '星海车友会',
              mainPostAuthorId: 'MAIN-A10001',
              mainPostId: 'POST-202605250001',
              mainPostContent: '雨天用车时发现车内地毯潮湿，怀疑天窗或门框密封存在问题。',
              weight: 'A'
            },
            {
              dataId: 'VOC_RAW_202605250002',
              firstContentType: '车主评价',
              secondContentType: '体验反馈',
              title: '语音操控特别顺手',
              content: '车主评价语音助手识别准确，常用导航和空调控制都很方便。',
              firstChannelName: '公开渠道',
              isOuter: '公开渠道',
              secondChannelName: '汽车媒体',
              channelName: '懂车帝',
              contentType: '评论',
              brand: '领航汽车',
              series: 'CS75 PLUS',
              dataCreateTime: '2026-05-25 16:36:50',
              viewCount: 8600,
              commentCount: 146,
              likeCount: 512,
              shareCount: 32,
              favoriteCount: 77,
              originalLink: 'https://example.com/raw/202605250002',
              authorNick: 'CS75车友',
              authorId: 'A10002',
              oneId: 'ONE202605250002',
              idCarNo: '500101199202020022',
              mobile: '13800000002',
              email: 'owner002@example.com',
              authorType: '潜客',
              model: 'SUV',
              vhlId: 'VHL-CS75-0002',
              vhlVin: 'LVCS75VIN00000002',
              isWsaterArmy: '否',
              mainPostAuthorNick: '懂车帝用户',
              mainPostAuthorId: 'MAIN-A10002',
              mainPostId: 'POST-202605250002',
              mainPostContent: '这套语音系统日常用起来很顺，导航、空调、音乐都能快速识别。',
              weight: 'B'
            },
            {
              dataId: 'VOC_RAW_202605250003',
              firstContentType: '客服工单',
              secondContentType: '异味投诉',
              title: '车辆有一股烧焦味道',
              content: '用户提到车辆行驶后车内出现烧焦味，希望排查异味来源。',
              firstChannelName: '私域渠道',
              isOuter: '私域渠道',
              secondChannelName: '客服反馈',
              channelName: '热线',
              contentType: '投诉',
              brand: '领航汽车',
              series: 'UNI-V',
              dataCreateTime: '2026-05-25 16:29:01',
              viewCount: 0,
              commentCount: 0,
              likeCount: 0,
              shareCount: 0,
              favoriteCount: 0,
              originalLink: 'https://example.com/raw/202605250003',
              authorNick: '匿名用户',
              authorId: 'A10003',
              oneId: 'ONE202605250003',
              idCarNo: '500101199303030033',
              mobile: '13800000003',
              email: 'owner003@example.com',
              authorType: '车主',
              model: '轿车',
              vhlId: 'VHL-UNIV-0003',
              vhlVin: 'LVUNIVVIN0000003',
              isWsaterArmy: '否',
              mainPostAuthorNick: '客服坐席',
              mainPostAuthorId: 'MAIN-A10003',
              mainPostId: 'POST-202605250003',
              mainPostContent: '客户反馈车辆行驶后出现烧焦味，需安排门店进一步检查。',
              weight: 'A'
            }
          ]

          const dataQueryCleanRows = dataQueryRawRows.map((row, index) => ({
            ...row,
            cleanDataId: `VOC_CLEAN_20260525${String(index + 1).padStart(4, '0')}`,
            cleanTime: `2026-05-25 17:${String(12 + index * 6).padStart(2, '0')}:00`,
            hitRule:
              index % 3 === 0
                ? '去重合并规则'
                : index % 3 === 1
                ? '敏感信息脱敏规则'
                : '噪声文本过滤规则',
            dataStatus: index === 2 ? '清洗失败' : '已清洗'
          }))

          const dataQueryResultRows = [
            {
              id: 'VOC_RESULT_202605250001',
              dataId: 'VOC_RAW_202605250001',
              title: '星海S09雨天漏水',
              originalText: '雨天车内漏水，需要尽快处理。',
              originalTextScene: '雨天漏水',
              brandName: '星海汽车',
              carSeriesName: '星海S09',
              opinion: '星海S09雨天漏水',
              topicText: '车辆漏水',
              sentiment: '负面',
              intention: '抱怨',
              contentType: '视频',
              usageScenarioFirst: '天气',
              usageScenarioSecond: '雨天',
              isOuter: '公开渠道',
              secondChannelName: '社交媒体',
              channelName: '抖音',
              seriesFactory: '领航汽车',
              modelName: '星海S09',
              publishTime: '2026-05-25 16:32:28',
              hotWord: '漏水',
              keywords: '雨天,漏水,S09',
              userJourney1: '使用',
              userJourney2: '行驶',
              userJourney3: '雨天出行',
              topic: 'TOPIC_004',
              domTagFirst: '产品',
              domTagSecond: '车身',
              domTagThree: '密封',
              domTagFour: '车辆漏水',
              tagAccuracy: '准确',
              tagCustomerIssueClassification: 'B',
              tagIssueSeverity: '负面',
              tagBusinessDomain: '产品质量',
              tagEventClarity: '事实',
              tagNeedForvclosedLoop: '是'
            },
            {
              id: 'VOC_RESULT_202605250002',
              dataId: 'VOC_RAW_202605250002',
              title: '语音操控特别顺手',
              originalText: '语音助手好用，导航空调都能直接控制。',
              originalTextScene: '语音操控',
              brandName: '领航汽车',
              carSeriesName: 'CS75 PLUS',
              opinion: '语音操控特别顺手',
              topicText: '语音助手好用',
              sentiment: '正面',
              intention: '表扬',
              contentType: '评论',
              usageScenarioFirst: '操作情景',
              usageScenarioSecond: '使用导航',
              isOuter: '公开渠道',
              secondChannelName: '汽车媒体',
              channelName: '懂车帝',
              seriesFactory: '领航汽车',
              modelName: 'CS75 PLUS',
              publishTime: '2026-05-25 16:36:50',
              hotWord: '语音助手',
              keywords: '语音,导航,空调',
              userJourney1: '使用',
              userJourney2: '车机',
              userJourney3: '语音交互',
              topic: 'TOPIC_005',
              domTagFirst: '产品',
              domTagSecond: '智能化',
              domTagThree: '座舱交互',
              domTagFour: '语音助手好用',
              tagAccuracy: '准确',
              tagCustomerIssueClassification: 'C',
              tagIssueSeverity: '正面',
              tagBusinessDomain: '产品体验',
              tagEventClarity: '事实',
              tagNeedForvclosedLoop: '否'
            },
            {
              id: 'VOC_RESULT_202605250003',
              dataId: 'VOC_RAW_202605250003',
              title: '车辆有一股烧焦味道',
              originalText: '车辆开了一段时间后车内闻到烧焦味。',
              originalTextScene: '车内异味',
              brandName: '领航汽车',
              carSeriesName: 'UNI-V',
              opinion: '车辆有一股烧焦味道',
              topicText: '车内有异味',
              sentiment: '负面',
              intention: '抱怨',
              contentType: '投诉',
              usageScenarioFirst: '使用场景',
              usageScenarioSecond: '日常通勤',
              isOuter: '私域渠道',
              secondChannelName: '客服反馈',
              channelName: '热线',
              seriesFactory: '领航汽车',
              modelName: 'UNI-V',
              publishTime: '2026-05-25 16:29:01',
              hotWord: '异味',
              keywords: '烧焦味,异味',
              userJourney1: '使用',
              userJourney2: '驾驶',
              userJourney3: '日常用车',
              topic: 'TOPIC_006',
              domTagFirst: '产品',
              domTagSecond: '车身内外饰',
              domTagThree: '气味',
              domTagFour: '车内有异味',
              tagAccuracy: '准确',
              tagCustomerIssueClassification: 'B',
              tagIssueSeverity: '负面',
              tagBusinessDomain: '产品质量',
              tagEventClarity: '事实',
              tagNeedForvclosedLoop: '是'
            }
          ]

          dataQueryResultRows.forEach((row, index) => {
            const sequence = index + 1
            Object.assign(row, {
              firstContentType: row.contentType,
              secondContentType:
                row.contentType === '视频'
                  ? '短视频'
                  : row.contentType === '评论'
                  ? '图文评论'
                  : '客服文本',
              firstChannelName: row.isOuter,
              viewCount: 3200 + sequence * 128,
              commentCount: 86 + sequence * 7,
              likeCount: 560 + sequence * 19,
              shareCount: 42 + sequence * 3,
              favoriteCount: 90 + sequence * 5,
              originalLink: `https://example.com/result/${sequence}`,
              authorNick: ['星海车主_雨天', '智能座舱体验官', '通勤车主'][index] || '体验用户',
              authorId: `AUTHOR-R${String(sequence).padStart(5, '0')}`,
              oneId: `ONE-R${String(sequence).padStart(6, '0')}`,
              idCarNo: `50010619900${sequence}0100${sequence}`,
              mobile: `1380000${String(sequence).padStart(4, '0')}`,
              email: `result_user_${sequence}@example.com`,
              authorType: index === 1 ? '潜客' : '车主',
              isWsaterArmy: '否',
              mainPostAuthorNick: ['星海S09车友', 'CS75车友圈', 'UNI-V车主'][index] || '车友',
              mainPostAuthorId: `MAIN-R${String(sequence).padStart(5, '0')}`,
              mainPostId: `POST-R20260525000${sequence}`,
              mainPostContent: row.originalText,
              vhlId: `VHL-R${String(sequence).padStart(5, '0')}`,
              vhlVin: `LS5A3CJCXR${String(sequence).padStart(7, '0')}`,
              weight: ['A', 'B', 'A'][index] || 'B',
              userJourney4:
                row.sentiment === '正面' ? '功能高频使用' : row.topicText || '用户反馈处理',
              dataStatus: index === 1 ? '已纠错' : '待纠错'
            })
          })

          server.middlewares.use(async (req, res, next) => {
            const requestUrl = req.url || ''
            const requestMethod = req.method || ''

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/auth/randomImage/') ||
                requestUrl.startsWith('/auth/randomImage/') ||
                requestUrl.startsWith('/randomImage/'))
            ) {
              const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="96" height="36" viewBox="0 0 96 36"><rect width="96" height="36" rx="4" fill="#f2f6ff"/><text x="48" y="24" text-anchor="middle" font-size="18" font-family="Arial" fill="#1677ff">2587</text></svg>`
              sendJson(res, ok(`data:image/svg+xml;utf8,${encodeURIComponent(svg)}`))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/base/login') ||
                requestUrl.startsWith('/insights/base/login') ||
                requestUrl.startsWith('/base/login'))
            ) {
              sendJson(
                res,
                ok({
                  access_token: 'standalone-token',
                  appId: 'insights',
                  type: 'base',
                  name: 'admin',
                  username: 'admin',
                  employeeId: 'admin',
                  userid: 'admin'
                })
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/userPermissions') ||
                requestUrl.startsWith('/insights/userPermissions') ||
                requestUrl.startsWith('/userPermissions'))
            ) {
              sendJson(res, ok(standaloneUserPermissions))
              return
            }

            if (
              requestMethod.toUpperCase() === 'POST' &&
              (requestUrl.startsWith('/api/insights/userInfo') ||
                requestUrl.startsWith('/insights/userInfo') ||
                requestUrl.startsWith('/userInfo'))
            ) {
              sendJson(
                res,
                ok({ name: 'admin', username: 'admin', employeeId: 'admin', userId: 'admin' })
              )
              return
            }

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/insights/accountInfo/findDepartAccountTree') ||
                requestUrl.startsWith('/insights/accountInfo/findDepartAccountTree') ||
                requestUrl.startsWith('/accountInfo/findDepartAccountTree'))
            ) {
              sendJson(res, ok(departAccountTree))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/logout') ||
                requestUrl.startsWith('/insights/logout') ||
                requestUrl.startsWith('/logout'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/ai/opinion-synonyms/search') ||
                requestUrl.startsWith('/ai/opinion-synonyms/search'))
            ) {
              const items = expandRows(corpusMappings, 'corpus', 100, ['entity'])
              sendJson(res, ok({ items, total: items.length }))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/new-words/search') ||
                requestUrl.startsWith('/new-words/search'))
            ) {
              sendJson(
                res,
                ok({
                  total: 100,
                  page: 1,
                  page_size: 10,
                  items: expandRows(
                    [
                      {
                        id: 'new_word_1',
                        dataId: 'RAW_DATA_0001',
                        title: '星海S09雨天漏水',
                        content: '用户反馈星海S09雨天车内出现漏水，影响正常使用体验。',
                        originalTextScene: '雨天漏水',
                        entity: '智驾体验',
                        opinion: '智驾体验',
                        originalOpinion: '智驾体验稳定',
                        subject: '智驾体验',
                        description: '用户提到智驾体验稳定，NOA接管少，车道保持反馈自然',
                        subject_desc: '用户提到智驾体验稳定，NOA接管少，车道保持反馈自然',
                        full_opinion: '用户提到智驾体验稳定，NOA接管少，车道保持反馈自然',
                        standard_opinion: '智驾综合表征',
                        recommended_topic: '智驾综合表征',
                        standard_opinion_id: 'TOPIC_ZJ_001',
                        frequency: 28,
                        status: -1,
                        process_status: -1,
                        enable_status: -1,
                        publishTime: '2026-07-07 11:34:15',
                        processed_time: '-',
                        created_time: '2026-05-25 10:00:00',
                        update_time: '2026-05-25 10:00:00',
                        operator: '系统'
                      },
                      {
                        id: 'new_word_2',
                        dataId: 'RAW_DATA_0002',
                        title: 'CS75 PLUS续航反馈',
                        content: '用户评价续航表现扎实，高速工况电耗稳定。',
                        originalTextScene: '续航扎实',
                        entity: '续航扎实',
                        opinion: '续航扎实',
                        originalOpinion: '续航表现扎实',
                        subject: '续航扎实',
                        description: '用户评价续航表现扎实，高速工况电耗稳定',
                        subject_desc: '用户评价续航表现扎实，高速工况电耗稳定',
                        full_opinion: '用户评价续航表现扎实，高速工况电耗稳定',
                        standard_opinion: '续航能力好',
                        recommended_topic: '续航能力好',
                        standard_opinion_id: 'TOPIC_XH_001',
                        frequency: 19,
                        status: -1,
                        process_status: -1,
                        enable_status: -1,
                        publishTime: '2026-07-07 10:42:36',
                        processed_time: '-',
                        created_time: '2026-05-25 10:10:00',
                        update_time: '2026-05-25 10:10:00',
                        operator: '系统'
                      }
                    ],
                    'new_word',
                    100,
                    ['entity', 'opinion', 'subject']
                  )
                })
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/new-words/update') ||
                requestUrl.startsWith('/new-words/update'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/new-words/batch-update') ||
                requestUrl.startsWith('/new-words/batch-update'))
            ) {
              sendJson(res, ok({ success_count: 0, failed_count: 0, failed_ids: [] }))
              return
            }

            if (
              requestUrl.startsWith('/api/insights/accountInfo/conditions') ||
              requestUrl.startsWith('/insights/accountInfo/conditions') ||
              requestUrl.startsWith('/api/insights/ruleTest/conditions') ||
              requestUrl.startsWith('/insights/ruleTest/conditions') ||
              requestUrl.startsWith('/api/insights/insTagLibClient/conditions') ||
              requestUrl.startsWith('/insights/insTagLibClient/conditions') ||
              requestUrl.startsWith('/api/insights/carSeriesInfo/conditions') ||
              requestUrl.startsWith('/insights/carSeriesInfo/conditions') ||
              requestUrl.startsWith('/api/insights/insDataResourceDesc/conditions') ||
              requestUrl.startsWith('/insights/insDataResourceDesc/conditions') ||
              requestUrl.startsWith('/api/insights/accountLexicon/conditions') ||
              requestUrl.startsWith('/insights/accountLexicon/conditions') ||
              requestUrl.startsWith('/api/insights/insClosedRule/conditions') ||
              requestUrl.startsWith('/insights/insClosedRule/conditions') ||
              requestUrl.startsWith('/api/insights/insBatchRule/conditions') ||
              requestUrl.startsWith('/insights/insBatchRule/conditions')
            ) {
              sendJson(res, ok(commonConditions))
              return
            }

            if (
              requestUrl.startsWith('/api/insights/insCqCaDataSource/conditions') ||
              requestUrl.startsWith('/insights/insCqCaDataSource/conditions')
            ) {
              sendJson(res, ok(dataQueryConditions))
              return
            }

            if (
              requestUrl.startsWith('/api/insights/carScene/conditions') ||
              requestUrl.startsWith('/insights/carScene/conditions') ||
              requestUrl.startsWith('/api/insights/attributeLabel/conditions') ||
              requestUrl.startsWith('/insights/attributeLabel/conditions') ||
              requestUrl.startsWith('/api/insights/addLabel/conditions') ||
              requestUrl.startsWith('/insights/addLabel/conditions')
            ) {
              sendJson(
                res,
                ok([
                  ...enableConditions,
                  {
                    key: 'intent',
                    details: [
                      { key: '1', value: '抱怨' },
                      { key: '2', value: '表扬' },
                      { key: '3', value: '咨询' },
                      { key: '4', value: '建议' }
                    ]
                  }
                ])
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountInfo/queryRoleALlList') ||
                requestUrl.startsWith('/insights/accountInfo/queryRoleALlList') ||
                requestUrl.startsWith('/api/insights/role/getUserRoleList') ||
                requestUrl.startsWith('/insights/role/getUserRoleList'))
            ) {
              sendJson(res, ok(expandRows(roleRows, 'role_option', 100, ['roleName'])))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountInfo/findDepartList') ||
                requestUrl.startsWith('/insights/accountInfo/findDepartList'))
            ) {
              sendJson(
                res,
                ok([
                  { code: 'dept_1', name: '数字化推进室' },
                  { code: 'dept_2', name: 'NPS运营管理室' },
                  { code: 'dept_3', name: '客户创新及数据处' },
                  { code: 'dept_4', name: '网络技术室' }
                ])
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carSceneCategory/findCarSceneCategoryList') ||
                requestUrl.startsWith('/insights/carSceneCategory/findCarSceneCategoryList') ||
                requestUrl.startsWith('/api/insights/carSceneCategory/findCarSceneCategoryTree') ||
                requestUrl.startsWith('/insights/carSceneCategory/findCarSceneCategoryTree'))
            ) {
              sendJson(res, ok(sceneCategories))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carSeriesInfo/queryBySelect') ||
                requestUrl.startsWith('/insights/carSeriesInfo/queryBySelect') ||
                requestUrl.startsWith('/carSeriesInfo/queryBySelect'))
            ) {
              const payload = await readJsonBody(req)
              sendJson(
                res,
                ok(
                  tablePageByPayload(
                    expandRows(brandSeriesRows, 'series', 100, ['name']),
                    payload,
                    requestUrl
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carSeriesInfo/findCarSeriesInfo') ||
                requestUrl.startsWith('/insights/carSeriesInfo/findCarSeriesInfo') ||
                requestUrl.startsWith('/carSeriesInfo/findCarSeriesInfo'))
            ) {
              const payload = await readJsonBody(req)
              const rows = expandRows(brandSeriesRows, 'series', 100, ['name'])
              sendJson(res, ok(rows.find(item => item.id === payload.id) || rows[0]))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carSeriesInfo/findByParam') ||
                requestUrl.startsWith('/insights/carSeriesInfo/findByParam') ||
                requestUrl.startsWith('/carSeriesInfo/findByParam'))
            ) {
              const payload = await readJsonBody(req)
              const rows = expandRows(brandSeriesRows, 'series', 100, ['name']).filter(item => {
                if (!payload.competitiveType) return true
                return Number(item.competitiveType) === Number(payload.competitiveType)
              })
              sendJson(res, ok(rows))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carSeriesInfo/addCarSeriesInfo') ||
                requestUrl.startsWith('/insights/carSeriesInfo/addCarSeriesInfo') ||
                requestUrl.startsWith('/api/insights/carSeriesInfo/updateCarSeriesInfo') ||
                requestUrl.startsWith('/insights/carSeriesInfo/updateCarSeriesInfo') ||
                requestUrl.startsWith('/api/insights/carSeriesInfo/batchChangeStatus') ||
                requestUrl.startsWith('/insights/carSeriesInfo/batchChangeStatus') ||
                requestUrl.startsWith('/api/insights/carSeriesInfo/deleteCarSeriesInfo') ||
                requestUrl.startsWith('/insights/carSeriesInfo/deleteCarSeriesInfo') ||
                requestUrl.startsWith('/carSeriesInfo/addCarSeriesInfo') ||
                requestUrl.startsWith('/carSeriesInfo/updateCarSeriesInfo') ||
                requestUrl.startsWith('/carSeriesInfo/batchChangeStatus') ||
                requestUrl.startsWith('/carSeriesInfo/deleteCarSeriesInfo'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/uploadCarSeries') ||
                requestUrl.startsWith('/insights/uploadCarSeries') ||
                requestUrl.startsWith('/uploadCarSeries'))
            ) {
              sendJson(
                res,
                ok({
                  key: `mock-series-cover-${Date.now()}.png`,
                  url: '/mock-upload/mock-series-cover.png'
                })
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/brandInfo/queryBySelect') ||
                requestUrl.startsWith('/insights/brandInfo/queryBySelect') ||
                requestUrl.startsWith('/brandInfo/queryBySelect'))
            ) {
              const payload = await readJsonBody(req)
              sendJson(
                res,
                ok(
                  tablePageByPayload(
                    expandRows(brandRows, 'brand', 100, ['brandName', 'name']),
                    payload,
                    requestUrl
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/brandInfo/findByParam') ||
                requestUrl.startsWith('/insights/brandInfo/findByParam') ||
                requestUrl.startsWith('/brandInfo/findByParam'))
            ) {
              sendJson(res, ok(brandRows))
              return
            }

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/insights/brandInfo/findAll') ||
                requestUrl.startsWith('/insights/brandInfo/findAll') ||
                requestUrl.startsWith('/brandInfo/findAll'))
            ) {
              sendJson(res, ok(brandRows))
              return
            }

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/insights/brandInfo/findAllBrandAndCarSeries') ||
                requestUrl.startsWith('/insights/brandInfo/findAllBrandAndCarSeries') ||
                requestUrl.startsWith('/brandInfo/findAllBrandAndCarSeries'))
            ) {
              sendJson(
                res,
                ok(
                  brandRows.map(brand => ({
                    ...brand,
                    carSeriesList: brandSeriesRows.filter(series => series.brandId === brand.id)
                  }))
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/automark/findAutomarkList') ||
                requestUrl.startsWith('/insights/automark/findAutomarkList'))
            ) {
              sendJson(
                res,
                ok(
                  tablePage(
                    expandRows(
                      [
                        { id: 'automark_1', name: '领航汽车', statusName: '已启用' },
                        { id: 'automark_2', name: '高端科技', statusName: '已启用' },
                        { id: 'automark_3', name: '星海汽车', statusName: '已启用' }
                      ],
                      'automark',
                      100,
                      ['name']
                    )
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carScene/findCarSceneList') ||
                requestUrl.startsWith('/insights/carScene/findCarSceneList'))
            ) {
              sendJson(res, ok(page(expandRows(sceneRows, 'scene', 100, ['sceneName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/attributeLabel/findAttributeLabelList') ||
                requestUrl.startsWith('/insights/attributeLabel/findAttributeLabelList'))
            ) {
              sendJson(res, ok(page(expandRows(attributeRows, 'attr', 100, ['name']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insTagLibClient/findExperienceCodeList') ||
                requestUrl.startsWith('/insights/insTagLibClient/findExperienceCodeList'))
            ) {
              sendJson(res, ok(page(expandRows(experienceCodeRows, 'exp_code', 100, ['tagName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith(
                '/api/insights/insTagLibClient/findAllFinalTagLibClientVoList'
              ) ||
                requestUrl.startsWith('/insights/insTagLibClient/findAllFinalTagLibClientVoList') ||
                requestUrl.startsWith('/api/insights/insTagLibClient/findTagTree') ||
                requestUrl.startsWith('/insights/insTagLibClient/findTagTree') ||
                requestUrl.startsWith('/api/insights/insTagLibClient/getTagLibClientTree') ||
                requestUrl.startsWith('/insights/insTagLibClient/getTagLibClientTree') ||
                requestUrl.startsWith(
                  '/api/insights/insTagLibClient/findAllUpTagLibHierarchicalByTagId'
                ) ||
                requestUrl.startsWith(
                  '/insights/insTagLibClient/findAllUpTagLibHierarchicalByTagId'
                ))
            ) {
              sendJson(res, ok([]))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insTagLibClient/findCategoryList') ||
                requestUrl.startsWith('/insights/insTagLibClient/findCategoryList') ||
                requestUrl.startsWith('/api/insights/carSeriesInfo/findTagLibCategoryTree') ||
                requestUrl.startsWith('/insights/carSeriesInfo/findTagLibCategoryTree'))
            ) {
              sendJson(res, ok(experienceCategoryTree))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insTagLibClient/findAllTopicList') ||
                requestUrl.startsWith('/insights/insTagLibClient/findAllTopicList'))
            ) {
              sendJson(res, ok(page(expandRows(standardPointRows, 'topic', 100, ['topicName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insTagLibClient/findTopicList') ||
                requestUrl.startsWith('/insights/insTagLibClient/findTopicList') ||
                requestUrl.startsWith('/api/insights/insTagLibClient/findAllFinalTagLib') ||
                requestUrl.startsWith('/insights/insTagLibClient/findAllFinalTagLib'))
            ) {
              sendJson(
                res,
                ok(
                  expandRows(standardPointRows, 'topic_option', 100, ['topicName']).map(item => ({
                    tagName: item.topicName,
                    tagCode: item.topicCode
                  }))
                )
              )
              return
            }

            if (
              requestUrl.startsWith('/api/insights/insCqCaDataSource/getChannelTree') ||
              requestUrl.startsWith('/insights/insCqCaDataSource/getChannelTree')
            ) {
              sendJson(res, ok(dataQueryChannelTree))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insCqCaDataSource/getRawData') ||
                requestUrl.startsWith('/insights/insCqCaDataSource/getRawData'))
            ) {
              const payload = await readJsonBody(req)
              const task = dataProcessingTaskRows.find(item => item.batchId === payload.batchId)
              const rows =
                task && !task.availableDataStages.includes('raw')
                  ? []
                  : expandRows(dataQueryRawRows, 'raw_data', 100, ['title']).map(row => ({
                      ...row,
                      batchId: payload.batchId || row.batchId,
                      dataSourceId: payload.dataSourceId || row.dataSourceId
                    }))
              sendJson(res, ok(tablePageByPayload(rows, payload, requestUrl)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insCqCaDataSource/getCleanData') ||
                requestUrl.startsWith('/insights/insCqCaDataSource/getCleanData'))
            ) {
              const payload = await readJsonBody(req)
              const task = dataProcessingTaskRows.find(item => item.batchId === payload.batchId)
              const rows =
                task && !task.availableDataStages.includes('clean')
                  ? []
                  : expandRows(dataQueryCleanRows, 'clean_data', 100, ['title']).map(row => ({
                      ...row,
                      batchId: payload.batchId || row.batchId,
                      dataSourceId: payload.dataSourceId || row.dataSourceId
                    }))
              sendJson(res, ok(tablePageByPayload(rows, payload, requestUrl)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insCqCaDataSource/getResultData') ||
                requestUrl.startsWith('/insights/insCqCaDataSource/getResultData'))
            ) {
              const payload = await readJsonBody(req)
              const task = dataProcessingTaskRows.find(item => item.batchId === payload.batchId)
              const rows =
                task && !task.availableDataStages.includes('result')
                  ? []
                  : expandRows(dataQueryResultRows, 'result_data', 100, [
                      'title',
                      'originalTextScene'
                    ]).map(row => ({
                      ...row,
                      batchId: payload.batchId || row.batchId,
                      dataSourceId: payload.dataSourceId || row.dataSourceId
                    }))
              sendJson(res, ok(tablePageByPayload(rows, payload, requestUrl)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataSource/findDataProcessingTasks') ||
                requestUrl.startsWith('/insights/insDataSource/findDataProcessingTasks'))
            ) {
              const payload = await readJsonBody(req)
              const taskRows = dataProcessingTaskRows.filter(
                item =>
                  (!payload.taskName || String(item.taskName).includes(payload.taskName)) &&
                  (!payload.taskType || item.taskType === payload.taskType) &&
                  (!payload.dataSourceName ||
                    String(item.dataSourceName).includes(payload.dataSourceName)) &&
                  (!payload.status || item.status === payload.status) &&
                  (!payload.createUser || String(item.createUser).includes(payload.createUser))
              )
              sendJson(res, ok(tablePageByPayload(taskRows, payload, requestUrl)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataSource/startProcessing') ||
                requestUrl.startsWith('/insights/insDataSource/startProcessing') ||
                requestUrl.startsWith('/api/insights/insDataSource/deleteDataSourceDetail') ||
                requestUrl.startsWith('/insights/insDataSource/deleteDataSourceDetail'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDictItem/insAllDictItems') ||
                requestUrl.startsWith('/insights/insDictItem/insAllDictItems'))
            ) {
              sendJson(
                res,
                ok({
                  insAllDictItems: {
                    voc_sentiment: [
                      { text: '正向', value: '正向' },
                      { text: '中性', value: '中性' },
                      { text: '负向', value: '负向' }
                    ]
                  }
                })
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/carScene/findCarSceneOperatorList') ||
                requestUrl.startsWith('/insights/carScene/findCarSceneOperatorList') ||
                requestUrl.startsWith('/api/insights/insTagLibClient/findTopicOperatorList') ||
                requestUrl.startsWith('/insights/insTagLibClient/findTopicOperatorList'))
            ) {
              sendJson(res, ok([{ id: '1', userName: 'admin' }]))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/attributeLabel/findAllAttributeLabelList') ||
                requestUrl.startsWith('/insights/attributeLabel/findAllAttributeLabelList'))
            ) {
              sendJson(res, ok(expandRows(attributeRows, 'attr_all', 100, ['name'])))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/addLabel/queryCreateUserList') ||
                requestUrl.startsWith('/insights/addLabel/queryCreateUserList') ||
                requestUrl.startsWith('/api/insights/ruleTest/queryCreateUserList') ||
                requestUrl.startsWith('/insights/ruleTest/queryCreateUserList'))
            ) {
              sendJson(res, ok(['*雯婷', '*建秋', '王集福', '*棚']))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/addLabel/queryLabelCorrectionList') ||
                requestUrl.startsWith('/insights/addLabel/queryLabelCorrectionList'))
            ) {
              sendJson(
                res,
                ok(tablePage(expandRows(correctionRows, 'correction', 100, ['correctionInfo'])))
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/ruleTest/ruleTestList') ||
                requestUrl.startsWith('/insights/ruleTest/ruleTestList'))
            ) {
              sendJson(
                res,
                ok(tablePage(expandRows(ruleTestRows, 'rule_test', 100, ['ruleTestInfo'])))
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountInfo/findAccountInfoList') ||
                requestUrl.startsWith('/insights/accountInfo/findAccountInfoList'))
            ) {
              sendJson(res, ok(tablePage(expandRows(accountRows, 'account', 100, ['userName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/role/queryRoleList') ||
                requestUrl.startsWith('/insights/role/queryRoleList'))
            ) {
              sendJson(res, ok(tablePage(expandRows(roleRows, 'role', 100, ['roleName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/role/queryMenuPermissionList') ||
                requestUrl.startsWith('/insights/role/queryMenuPermissionList'))
            ) {
              sendJson(res, ok(createRoleAuthTree(false)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/role/queryRoleInfo') ||
                requestUrl.startsWith('/insights/role/queryRoleInfo'))
            ) {
              const payload = await readJsonBody(req)
              const role = roleRows.find(item => item.id === payload.id) || roleRows[0]
              sendJson(
                res,
                ok({
                  id: role.id,
                  clientId: payload.clientId || standaloneUserPermissions.defaultClientId,
                  roleName: role.roleName,
                  enabled: '1',
                  permissionIdList: createRoleAuthTree(true).map(item => item.id),
                  roleAuthTreeList: createRoleAuthTree(true)
                })
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/role/saveOrUpdateRole') ||
                requestUrl.startsWith('/insights/role/saveOrUpdateRole'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/downLoad/findDownLoadFileList') ||
                requestUrl.startsWith('/insights/downLoad/findDownLoadFileList'))
            ) {
              sendJson(res, ok(tablePage(expandRows(downloadRows, 'download', 100, ['fileName']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataResource/findDataResourceList') ||
                requestUrl.startsWith('/insights/insDataResource/findDataResourceList') ||
                requestUrl.startsWith('/insDataResource/findDataResourceList'))
            ) {
              sendJson(res, ok(tablePage(resourceGroupRows)))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insClosedRule/queryRulePage') ||
                requestUrl.startsWith('/insights/insClosedRule/queryRulePage'))
            ) {
              sendJson(
                res,
                ok(
                  tablePage(
                    expandRows(closedLoopRuleRows, 'closed_rule', 100, ['ruleName']).map(row => ({
                      ...row,
                      ruleId: row.id
                    }))
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataResourceDesc/list') ||
                requestUrl.startsWith('/insights/insDataResourceDesc/list'))
            ) {
              sendJson(res, ok(tablePage(expandRows(keywordRows, 'keyword', 100, ['name']))))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataResource/findDataResourceList') ||
                requestUrl.startsWith('/insights/insDataResource/findDataResourceList'))
            ) {
              const payload = await readJsonBody(req)
              sendJson(
                res,
                ok(
                  tablePageByPayload(
                    expandRows(
                      [
                        { id: 'account_group_1', name: '重点账号词库', ruleType: 'single' },
                        { id: 'account_group_2', name: '汽车媒体账号', ruleType: 'single' },
                        { id: 'account_group_3', name: '社媒官方账号', ruleType: 'single' }
                      ],
                      'account_group',
                      20,
                      ['name']
                    ),
                    payload,
                    requestUrl
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/insDataResource/insert') ||
                requestUrl.startsWith('/insights/insDataResource/insert') ||
                requestUrl.startsWith('/api/insights/insDataResource/update') ||
                requestUrl.startsWith('/insights/insDataResource/update') ||
                requestUrl.startsWith('/api/insights/insDataResource/delete') ||
                requestUrl.startsWith('/insights/insDataResource/delete') ||
                requestUrl.startsWith('/insDataResource/insert') ||
                requestUrl.startsWith('/insDataResource/update') ||
                requestUrl.startsWith('/insDataResource/delete'))
            ) {
              sendJson(res, ok({ id: `account_group_${Date.now()}` }))
              return
            }

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/insights/accountLexicon/getChannelTree') ||
                requestUrl.startsWith('/insights/accountLexicon/getChannelTree') ||
                requestUrl.startsWith('/accountLexicon/getChannelTree'))
            ) {
              sendJson(res, ok(dataQueryChannelTree))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountLexicon/findAccountLexiconList') ||
                requestUrl.startsWith('/insights/accountLexicon/findAccountLexiconList') ||
                requestUrl.startsWith('/accountLexicon/findAccountLexiconList'))
            ) {
              const payload = await readJsonBody(req)
              sendJson(
                res,
                ok(
                  tablePageByPayload(
                    expandRows(accountLexiconRows, 'account_lexicon', 100, ['accountName']),
                    payload,
                    requestUrl
                  )
                )
              )
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountLexicon/findAccountLexiconInfo') ||
                requestUrl.startsWith('/insights/accountLexicon/findAccountLexiconInfo') ||
                requestUrl.startsWith('/accountLexicon/findAccountLexiconInfo'))
            ) {
              const payload = await readJsonBody(req)
              const rows = expandRows(accountLexiconRows, 'account_lexicon', 100, ['accountName'])
              sendJson(res, ok(rows.find(item => item.id === payload.id) || rows[0]))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/accountLexicon/saveAccountLexiconDetails') ||
                requestUrl.startsWith('/insights/accountLexicon/saveAccountLexiconDetails') ||
                requestUrl.startsWith('/api/insights/accountLexicon/updateAccountLexiconDetails') ||
                requestUrl.startsWith('/insights/accountLexicon/updateAccountLexiconDetails') ||
                requestUrl.startsWith('/api/insights/accountLexicon/changeAccountLexiconStatus') ||
                requestUrl.startsWith('/insights/accountLexicon/changeAccountLexiconStatus') ||
                requestUrl.startsWith('/accountLexicon/saveAccountLexiconDetails') ||
                requestUrl.startsWith('/accountLexicon/updateAccountLexiconDetails') ||
                requestUrl.startsWith('/accountLexicon/changeAccountLexiconStatus'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestUrl.startsWith('/api/insights/words/queryWordsInfo') ||
              requestUrl.startsWith('/insights/words/queryWordsInfo') ||
              requestUrl.startsWith('/words/queryWordsInfo')
            ) {
              sendJson(
                res,
                ok({
                  id: 'word_1',
                  subject: '智驾体验',
                  description: '用户提到智能驾驶体验',
                  originalOpinion: '智驾综合表现',
                  standardOpinion: '智驾综合表现',
                  tagType: 'CA',
                  tagName: '智能驾驶'
                })
              )
              return
            }

            if (
              requestUrl.startsWith('/api/insights/opinions/queryOpinionsInfo') ||
              requestUrl.startsWith('/insights/opinions/queryOpinionsInfo') ||
              requestUrl.startsWith('/opinions/queryOpinionsInfo')
            ) {
              sendJson(
                res,
                ok({
                  id: 'opinion_1',
                  originalOpinion: '语音助手好用',
                  standardOpinion: '语音助手好用',
                  subject: '语音助手',
                  description: '用户反馈语音助手识别准确'
                })
              )
              return
            }

            if (
              requestUrl.startsWith('/api/insights/words/getTagType') ||
              requestUrl.startsWith('/insights/words/getTagType') ||
              requestUrl.startsWith('/words/getTagType')
            ) {
              sendJson(
                res,
                ok([
                  { key: 'CA', value: '标签体系' },
                  { key: 'JOUR', value: '用户旅程' },
                  { key: 'SCENE', value: '用车场景' }
                ])
              )
              return
            }

            if (
              requestUrl.startsWith('/api/insights/words/findTagLibCategoryTree') ||
              requestUrl.startsWith('/insights/words/findTagLibCategoryTree') ||
              requestUrl.startsWith('/words/findTagLibCategoryTree')
            ) {
              sendJson(res, ok(experienceCategoryTree))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/words/allocationWords') ||
                requestUrl.startsWith('/insights/words/allocationWords') ||
                requestUrl.startsWith('/words/allocationWords') ||
                requestUrl.startsWith('/api/insights/opinions/allocationOpinions') ||
                requestUrl.startsWith('/insights/opinions/allocationOpinions') ||
                requestUrl.startsWith('/opinions/allocationOpinions'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'POST' &&
              (requestUrl.startsWith('/api/insights/getFile') ||
                requestUrl.startsWith('/insights/getFile') ||
                requestUrl.startsWith('/getFile'))
            ) {
              sendJson(res, ok({}))
              return
            }

            if (
              requestMethod === 'GET' &&
              (requestUrl.startsWith('/api/insights/downLoad/findVisibleUserList') ||
                requestUrl.startsWith('/insights/downLoad/findVisibleUserList'))
            ) {
              sendJson(res, ok([{ id: 'download_user_1', userName: '*建秋' }]))
              return
            }

            const standalonePath = getStandaloneInsightsPath(requestUrl)
            if (standalonePath) {
              if (standalonePath.endsWith('/conditions')) {
                sendJson(res, ok(commonConditions))
                return
              }
              if (/tree|Tree|channelTree|ChannelTree/.test(standalonePath)) {
                sendJson(res, ok([]))
                return
              }
              if (
                /list|List|query|Query|page|Page|findAll|findByParam|find.*List/.test(
                  standalonePath
                )
              ) {
                sendJson(res, ok(tablePageByPayload([], {}, requestUrl)))
                return
              }
              sendJson(res, ok({}))
              return
            }
            next()
          })
        }
      },
      vue(),
      vueJsx(),
      // vuet调试工具
      // vueDevTools(),
      AutoImport({
        imports: ['vue'],
        resolvers: [ElementPlusResolver()],
        dts: 'src/types/auto-import.d.ts'
      }),
      Components({
        // 指定自动导入组件位置，默认是src/components
        // dirs: ['src/components'],
        // 解决命名冲突
        directoryAsNamespace: true,
        resolvers: [ElementPlusResolver()]
      }),
      eslintPlugin({
        cache: false,
        include: [
          'src/**/*.ts',
          'src/**/*.tsx',
          'src/**/*.js',
          'src/**/*.vue',
          'src/*.ts',
          'src/*.tsx',
          'src/*.js',
          'src/*.vue'
        ]
      }),
      createLocalSvgIconsPlugin(path.resolve(__dirname, 'src/assets/svg'))
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5175,
      host: env.VITE_DEV_HOST || '0.0.0.0',
      proxy: {
        '^/api/new-words': {
          target: newWordsTarget,
          changeOrigin: true
        },
        '^/api/ai': {
          target: aiTarget,
          changeOrigin: true
        },
        '^/api/auth': {
          target: env.VITE_PROXY_AUTH_TARGET || 'http://localhost:8081',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api\/auth/, '')
        },
        '^/api/insights': {
          target: apiTarget,
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api\/insights/, '')
        },
        '^/api': {
          target: apiTarget,
          // target: 'https://example.com/ ', // 标准产品环境示例
          changeOrigin: true
        }
      }
    }
  }
})
