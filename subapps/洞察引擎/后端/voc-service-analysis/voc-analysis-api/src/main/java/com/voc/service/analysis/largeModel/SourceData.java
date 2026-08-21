package com.voc.service.analysis.largeModel;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SourceData implements Serializable {
    private String dataSource;
    private String dataSource_type;
    private String title;
    @Builder.Default
    private String usage_scenario_src = "";
    private List<TopicText> topic_text;
    private String brand;
    private String series;
    private Object ext;
    private String  questType;
    private LocalDateTime create_time;

}
