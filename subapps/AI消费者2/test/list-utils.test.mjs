import test from 'node:test';
import assert from 'node:assert/strict';
import { buildConsumerConditionCoverage, buildConsumerSamplePreview, buildConsumerUsage, buildUserDataPresentation, groupUserTagsByCategory, isQaAttachmentFilenameSupported, paginate, summarizeTags } from '../src/list-utils.mjs';

const rows = [
  { category: '个人属性', name: '年龄', values: ['25-35岁', '36-45岁'] },
  { category: '偏好属性', name: '新能源偏好', values: ['新能源车偏好'] },
];

test('groupUserTagsByCategory retains the primary category and tag definition', () => {
  assert.deepEqual(groupUserTagsByCategory(['25-35岁', '新能源车偏好'], rows), {
    '个人属性': ['年龄：25-35岁'],
    '偏好属性': ['新能源偏好：新能源车偏好'],
  });
});

test('paginate returns a twenty-row page and its page count', () => {
  const records = Array.from({ length: 43 }, (_, index) => index + 1);
  assert.deepEqual(paginate(records, 2, 20), {
    items: records.slice(20, 40),
    pageCount: 3,
    page: 2,
  });
});

test('summarizeTags keeps the first three labels and reports remaining labels', () => {
  assert.deepEqual(summarizeTags(['25-35岁', '新能源车偏好', '预算15-25万', '一线/新一线城市', '高频试驾']), {
    visible: ['25-35岁', '新能源车偏好', '预算15-25万'],
    remaining: 2,
  });
});

test('isQaAttachmentFilenameSupported accepts only the document formats supported by simulation Q&A', () => {
  ['research.xlsx', 'brief.doc', 'brief.DOCX', 'notes.md', 'responses.csv', 'summary.txt'].forEach((filename) => {
    assert.equal(isQaAttachmentFilenameSupported(filename), true, filename);
  });

  ['image.png', 'presentation.pptx', 'archive.zip', 'file'].forEach((filename) => {
    assert.equal(isQaAttachmentFilenameSupported(filename), false, filename);
  });
});

test('buildUserDataPresentation uses taxonomy categories for compact fields and full detail', () => {
  const user = { tags: ['25岁', '计划首购', '15-18万', '城市', '未归类'] };
  const taxonomy = [
    { category: '个人属性', name: '年龄', values: ['25岁'] },
    { category: '经济属性', name: '预算', values: ['15-18万'] },
    { category: '出行属性', name: '常驻城市', values: ['城市'] },
    { category: '拥车属性', name: '购车计划', values: ['计划首购'] },
  ];

  assert.deepEqual(buildUserDataPresentation(user, taxonomy, 3), {
    summary: '25岁用户，城市居住；首次购车，预算15-18万。',
    chips: ['25岁', '计划首购', '15-18万'],
    remaining: 2,
    categories: [
      { category: '个人属性', primary: '25岁', count: 1, details: ['年龄：25岁'] },
      { category: '经济属性', primary: '15-18万', count: 1, details: ['预算：15-18万'] },
      { category: '出行属性', primary: '城市', count: 1, details: ['常驻城市：城市'] },
      { category: '拥车属性', primary: '计划首购', count: 1, details: ['购车计划：计划首购'] },
    ],
  });
});

test('buildConsumerSamplePreview keeps only users matching every consumer condition', () => {
  const template = {
    linkedTags: ['25-35岁', '新能源车偏好', '预算15-25万', '一线/新一线城市'],
  };
  const users = [
    { id: 'U-1', tags: ['25岁', '计划首购', '15-18万', '城市'], status: '启用' },
    { id: 'U-2', tags: ['20岁', '计划首购', '15-18万', '城市'], status: '启用' },
    { id: 'U-3', tags: ['25岁', '计划首购', '20-30万', '城市'], status: '停用' },
  ];

  assert.deepEqual(buildConsumerSamplePreview(template, users), [
    { ...users[0], matchedTags: ['25岁', '计划首购', '15-18万', '城市'] },
  ]);
});

test('buildConsumerSamplePreview maps first-purchase and lower-tier aliases to consumer conditions', () => {
  const template = {
    linkedTags: ['预算15-25万', '首次购车用户', '三线及以下潜客'],
  };
  const users = [
    { id: 'U-4', tags: ['15-18万', '计划首购', '城市'], status: '启用' },
  ];

  assert.deepEqual(buildConsumerSamplePreview(template, users), [
    { ...users[0], matchedTags: ['15-18万', '计划首购', '城市'] },
  ]);
});

test('buildConsumerConditionCoverage calculates each condition from available system users', () => {
  const template = { linkedTags: ['预算15-25万', '首次购车用户'] };
  const users = [
    { id: 'U-1', tags: ['15-18万', '计划首购'], status: '启用' },
    { id: 'U-2', tags: ['20-30万'], status: '启用' },
    { id: 'U-3', tags: ['15-18万', '计划首购'], status: '停用' },
  ];

  assert.deepEqual(buildConsumerConditionCoverage(template, users), [
    { condition: '预算15-25万', count: 2, rate: 100 },
    { condition: '首次购车用户', count: 1, rate: 50 },
  ]);
});

test('buildConsumerUsage keeps only task and QA history records for the active template', () => {
  const template = { name: '价格敏感型用户' };
  const tasks = [
    { id: 'T-1', consumer: '价格敏感型用户', status: '已完成' },
    { id: 'T-2', consumer: '科技尝鲜者', status: '进行中' },
  ];
  const qaRecords = [
    { id: 'QA-1', consumer: '价格敏感型用户' },
    { id: 'QA-2', consumer: '科技尝鲜者' },
  ];

  assert.deepEqual(buildConsumerUsage(template, tasks, qaRecords), {
    tasks: [tasks[0]],
    qaRecords: [qaRecords[0]],
    completedCount: 1,
    runningCount: 0,
  });
});
