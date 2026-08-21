package com.voc.service.trhird.model;


import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResponseAiModel implements Serializable {

    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("body")
    private Body body;


    @Data
    public static class Body {
        @SerializedName("created")
        private long created;
        @SerializedName("usage")
        private Usage usage;
        @SerializedName("model")
        private String model;
        @SerializedName("id")
        private String id;
        @SerializedName("choices")
        private List<Choice> choices;


        @Data
        public static class Usage {
            @SerializedName("completion_tokens")
            private int completionTokens;
            @SerializedName("prompt_tokens")
            private int promptTokens;
            @SerializedName("total_tokens")
            private int totalTokens;
        }

        @Data
        public static class Choice {
            @SerializedName("finish_reason")
            private String finishReason;
            @SerializedName("index")
            private int index;
            @SerializedName("message")
            private Message message;

            @Data
            public static class Message {
                @SerializedName("role")
                private String role;
                @SerializedName("content")
                private String content;

            }
        }
    }
}
