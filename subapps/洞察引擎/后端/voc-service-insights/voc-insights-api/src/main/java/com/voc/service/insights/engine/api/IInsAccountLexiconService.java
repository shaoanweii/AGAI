package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.model.InsAccountLexiconModel;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 16:11
 * @描述:
 **/
public interface IInsAccountLexiconService {
    /**
     * 保存账户词库详情
     * @param insAccountLexicon
     */
    void saveAccountLexiconDetails(InsAccountLexiconModel insAccountLexicon);
    /**
     * 更新账户词库详情
     * @param insAccountLexicon
     */
    void updateAccountLexiconDetails(InsAccountLexiconModel insAccountLexicon);
    /**
     * 根据id查询账户词库详情
     * @param insAccountLexicon
     * @return
     */
    InsAccountLexiconVo findAccountLexiconInfo(InsAccountLexiconModel insAccountLexicon);
    /**
     * 查询账户词库列表
     * @param insAccountLexicon
     * @return
     */
    IPage<InsAccountLexiconVo> findAccountLexiconList(InsAccountLexiconModel insAccountLexicon);

    /**
     * 改变账户词库状态
     * @param insAccountLexicon
     */
    void changeAccountLexiconStatus(InsAccountLexiconModel insAccountLexicon);

    Map<String, Integer> countByResourceIds(Set<String> ids);

    List<InsAccountLexiconVo> findAllAccountLexiconList();
}
