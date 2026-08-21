/*
 * Copyright (c) 2011-2022, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.voc.service.common.pagination;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * 简单分页模型
 *
 * @author hubin
 * @since 2018-06-09
 */
@Data
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 8545996863226528798L;

    /**
     * 每页显示条数，默认 10
     */
    protected Integer pageSize = 10;

    /**
     * 当前页
     */
    protected Integer pageNum = 1;

    /**
     * 排序字段
     */
    protected String order;

    public Page(){}

    public void orderBy(QueryWrapper queryWrapper) {
        if (ObjectUtils.isNotEmpty(this.getOrder())) {
            String[] order = this.getOrder().split(StrUtil.COMMA);
            for (String str : order) {
                String[] split = str.split(StrUtil.SPACE);
                if ("asc".equalsIgnoreCase(split[1])) {
                    queryWrapper.orderByAsc(split[0]);
                } else {
                    queryWrapper.orderByDesc(split[0]);
                }
            }
            return;
        }
        // 默认使用创建时间倒叙排序
        queryWrapper.orderByDesc("create_time");
    }
}