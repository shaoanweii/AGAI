package com.voc.service.trhird.model;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentParamModel implements Serializable {

    @Builder.Default
    private String msg_type = "post";

    private Content content;

}
