package com.voc.service.trhird.model;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Content implements Serializable {
    @Builder.Default
    private ContentPost post = new ContentPost();

}
