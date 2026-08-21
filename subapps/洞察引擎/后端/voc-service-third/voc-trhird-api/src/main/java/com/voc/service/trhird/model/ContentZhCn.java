package com.voc.service.trhird.model;


import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentZhCn implements Serializable {

    private String title;

    private List<List<ContentInfoModel>> content;
}
