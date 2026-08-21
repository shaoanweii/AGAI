package com.voc.service.trhird.model;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ZhiPuDownloadAiModel implements Serializable {

    private String batchId;

    private String status;

    private String outputFileId;

    private String errorFileId;

}
