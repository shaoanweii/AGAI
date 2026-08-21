package com.voc.service.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Entity
public class AppModel implements Serializable {

    //  @Id
//  @GeneratedValue
    public String id;

    //  @Column(unique = true)
    public String appId;

    public String note;

    public String urls;

}
