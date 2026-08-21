package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.data.entity.LargeDigitaFilesEntity;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/6/11 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface LargeDigitaFileMapper extends BaseMapper<LargeDigitaFilesEntity> {

    IPage<LargeDigitaFilesEntity> getFileList(IPage<LargeDigitaFilesEntity> page, @Param("model") LargeDigitaFilesModel model);
    LargeDigitaFilesEntity getFile(LargeDigitaFilesEntity entity);
    List<String> findUserIds();
    int updateAttachmentDownloadRecord(LargeDigitaFilesModel  model);
    List<InsDownAccountInfoAuthVo> findVisibleUserList(@Param("userIds") List<String> userIds);
}
