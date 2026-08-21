package com.voc.service.insights.engine.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0.0
 * @ClassName InternationalVo.java
 * @Description
 * @createTime 2023年01月09日 15:10
 * @Copyright futong
 */
@Data
public class InternationalVo  implements Serializable {
    String key;
    String textCn;
    String textEn;
}
