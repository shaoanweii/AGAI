package com.voc.service.trhird.model;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentInfoModel implements Serializable {

    private String tag;

    private String text;

}
