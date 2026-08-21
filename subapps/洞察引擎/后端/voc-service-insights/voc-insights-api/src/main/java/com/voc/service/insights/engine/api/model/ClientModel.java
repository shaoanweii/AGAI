package com.voc.service.insights.engine.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ClientModel
 * @createTime 2024年01月31日 9:16
 * @Copyright futong
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientModel implements Serializable {

    public String id;
    public String code;
}
