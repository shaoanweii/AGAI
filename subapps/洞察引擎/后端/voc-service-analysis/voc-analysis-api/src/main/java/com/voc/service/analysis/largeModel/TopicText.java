package com.voc.service.analysis.largeModel;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TopicText implements Serializable {
    private String role;
    private String content;

}
