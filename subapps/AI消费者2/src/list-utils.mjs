export function groupUserTagsByCategory(tags, rows) {
  const tagSet = new Set(tags);

  return rows.reduce((groups, row) => {
    const selectedValues = row.values.filter((value) => tagSet.has(value));
    if (selectedValues.length) {
      groups[row.category] = [
        ...(groups[row.category] || []),
        `${row.name}：${selectedValues.join('、')}`,
      ];
    }
    return groups;
  }, {});
}

export function paginate(items, requestedPage, pageSize = 20) {
  const pageCount = Math.max(1, Math.ceil(items.length / pageSize));
  const page = Math.min(Math.max(requestedPage, 1), pageCount);

  return {
    items: items.slice((page - 1) * pageSize, page * pageSize),
    pageCount,
    page,
  };
}

export function summarizeTags(tags, limit = 3) {
  return {
    visible: tags.slice(0, limit),
    remaining: Math.max(0, tags.length - limit),
  };
}

export const qaAttachmentAcceptedExtensions = Object.freeze(['xlsx', 'doc', 'docx', 'md', 'csv', 'txt']);

export function isQaAttachmentFilenameSupported(filename) {
  const normalizedName = String(filename || '').trim();
  const extension = normalizedName.includes('.') ? normalizedName.split('.').pop().toLowerCase() : '';

  return qaAttachmentAcceptedExtensions.includes(extension);
}

export function buildUserDataPresentation(user, rows, chipLimit = 3) {
  const categories = [];
  const seenCategories = new Set();
  const tagSet = new Set(user.tags);
  const attributes = new Map();

  rows.forEach((row) => {
    const values = row.values.filter((value) => tagSet.has(value));
    if (values.length) attributes.set(row.name, values);
    if (seenCategories.has(row.category)) return;
    seenCategories.add(row.category);
    const details = rows
      .filter((candidate) => candidate.category === row.category)
      .flatMap((candidate) => {
        const values = candidate.values.filter((value) => tagSet.has(value));
        return values.length ? [`${candidate.name}：${values.join('、')}`] : [];
      });

    if (details.length) {
      const primary = details[0].split('：')[1].split('、')[0];
      categories.push({ category: row.category, primary, count: details.length, details });
    }
  });

  return {
    summary: buildProfileNarrative(attributes),
    chips: user.tags.slice(0, chipLimit),
    remaining: Math.max(0, user.tags.length - chipLimit),
    categories,
  };
}

function buildProfileNarrative(attributes) {
  const valueFor = (...names) => names.flatMap((name) => attributes.get(name) || []);
  const firstValue = (...names) => valueFor(...names)[0];
  const age = firstValue('年龄');
  const gender = firstValue('性别');
  const education = firstValue('教育水平');
  const area = firstValue('居住城市', '居住区域', '常驻城市');
  const industry = firstValue('行业类型');
  const maritalStatus = firstValue('婚姻状况');
  const childCount = firstValue('小孩数量');
  const housing = firstValue('住房状况');
  const preference = firstValue('消费观');
  const commute = firstValue('日常通勤方式');
  const purchasePlan = firstValue('未来1-3年购车计划', '购车计划');
  const budget = firstValue('购车预算', '购车价位', '预算');
  const channel = firstValue('汽车信息获取主要渠道');
  const portrait = [];
  const traits = [];

  if (age && gender) portrait.push(`${age}${gender === '女' ? '女性' : gender === '男' ? '男性' : gender}`);
  else if (age) portrait.push(`${age}用户`);
  if (education) portrait.push(`${education}学历`);
  if (area) portrait.push(`${area}居住`);
  if (industry && industry !== '其他') portrait.push(`${industry}从业`);
  if (maritalStatus) portrait.push(maritalStatus === '已婚' ? '已婚家庭' : maritalStatus);
  if (childCount && childCount !== '0') portrait.push(`育有${childCount === '1人' ? '1名' : childCount === '2人' ? '2名' : childCount}子女`);
  if (housing) portrait.push(housing === '租房' ? '目前租房' : housing);

  const preferenceCopy = {
    '科技引领消费': '偏好科技与智能产品',
    '尝鲜消费': '乐于尝新',
    '品质消费': '注重品质',
    '性价比': '看重性价比',
  };
  if (preference) traits.push(preferenceCopy[preference] || `偏好${preference}`);
  if (commute) traits.push(`日常${commute}通勤`);
  const planCopy = {
    '计划首购': '首次购车',
    '计划增购': '计划增购车辆',
    '计划换购': '计划换购车辆',
  };
  if (purchasePlan) traits.push(planCopy[purchasePlan] || purchasePlan);
  if (budget) traits.push(`预算${budget}`);
  if (channel === '汽车垂媒') traits.push('关注汽车内容');

  const firstSentence = portrait.slice(0, 3).join('，');
  const secondSentence = traits.slice(0, 3).join('，');
  if (firstSentence && secondSentence) return `${firstSentence}；${secondSentence}。`;
  return `${firstSentence || secondSentence || '暂无标签特征'}。`;
}

function tagMatchesCondition(tag, condition) {
  if (tag === condition) return true;

  const ageCondition = condition.match(/^(\d+)-(\d+)岁$/);
  const ageTag = tag.match(/^(\d+)岁$/);
  if (ageCondition && ageTag) {
    const [, min, max] = ageCondition;
    const age = Number(ageTag[1]);
    return age >= Number(min) && age <= Number(max);
  }

  const budgetCondition = condition.match(/^预算(\d+)-(\d+)万$/);
  const budgetTag = tag.match(/^(\d+)-(\d+)万$/);
  if (budgetCondition && budgetTag) {
    const [, min, max] = budgetCondition;
    const [, tagMin, tagMax] = budgetTag;
    return Number(tagMin) <= Number(max) && Number(tagMax) >= Number(min);
  }

  if (condition === '新能源车偏好') {
    return ['电动自行车', '计划首购', '计划增购', '计划换购'].includes(tag);
  }

  if (condition === '首次购车用户') return tag === '计划首购';
  if (condition === '三线及以下潜客') return tag === '城市';

  return condition === '一线/新一线城市' && tag === '城市';
}

export function buildConsumerSamplePreview(template, users) {
  return users
    .filter((user) => user.status === '启用')
    .map((user) => ({
      ...user,
      matchedTags: user.tags.filter((tag) => template.linkedTags.some((condition) => tagMatchesCondition(tag, condition))),
    }))
    .filter((user) => template.linkedTags.every((condition) => user.tags.some((tag) => tagMatchesCondition(tag, condition))));
}

export function buildConsumerConditionCoverage(template, users) {
  const availableUsers = users.filter((user) => user.status === '启用');

  return template.linkedTags.map((condition) => {
    const count = availableUsers.filter((user) => user.tags.some((tag) => tagMatchesCondition(tag, condition))).length;
    return {
      condition,
      count,
      rate: availableUsers.length ? Math.round(count / availableUsers.length * 100) : 0,
    };
  });
}

export function buildConsumerUsage(template, tasks, qaRecords) {
  const matchedTasks = tasks.filter((task) => task.consumer === template.name);
  const matchedQaRecords = qaRecords.filter((record) => record.consumer === template.name);

  return {
    tasks: matchedTasks,
    qaRecords: matchedQaRecords,
    completedCount: matchedTasks.filter((task) => task.status === '已完成').length,
    runningCount: matchedTasks.filter((task) => task.status === '进行中').length,
  };
}
