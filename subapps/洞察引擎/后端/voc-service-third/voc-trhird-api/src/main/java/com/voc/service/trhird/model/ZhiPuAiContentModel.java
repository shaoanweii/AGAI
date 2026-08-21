package com.voc.service.trhird.model;


import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ZhiPuAiContentModel implements Serializable {


    private String customId;
    @Builder.Default
    private List<Vehicle> vehicleList = new ArrayList<>();

    @Data
    public static class Vehicle {
        @SerializedName("vehicle_brand")
        String vehicleBrand;
        @SerializedName("vehicle_model")
        String vehicleModel;
        List<Viewpoint> viewpoints;
    }

    @Data
    public static class Viewpoint {
        @SerializedName("scenario")
        String scenario;
        @SerializedName("subject")
        String subject;
        @SerializedName("aspect")
        String aspect;
        @SerializedName("desc")
        String desc;
        @SerializedName("sentiment")
        String sentiment;
        @SerializedName("intent")
        String intent;
        @SerializedName("confidence")
        String confidence;
    }

}
