package com.voc.service.analysis.largeModel;



import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModelTopicRequest implements Serializable {
    private String topic_id;
    private SourceData source_data;
    private String workId;

}
