package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
// ... existing code ...
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_ext_ins_tag_system_final_mv")
public class AysExtInsTagSystemEntity implements Serializable {
    @TableField(value = "topic")
    String topic;
    @TableField(value = "id")
    String id;
    @TableField(value = "topic_text")
    String topicText;
    @TableField(value = "cpt_tag_first_code")
    String cptTagFirstCode;
    @TableField(value = "cpt_tag_first")
    String cptTagFirst;
    @TableField(value = "cpt_tag_second_code")
    String cptTagSecondCode;
    @TableField(value = "cpt_tag_second")
    String cptTagSecond;
    @TableField(value = "cpt_tag_three_code")
    String cptTagThreeCode;
    @TableField(value = "cpt_tag_three")
    String cptTagThree;
    @TableField(value = "cpt_tag_four_code")
    String cptTagFourCode;
    @TableField(value = "cpt_tag_four")
    String cptTagFour;
    @TableField(value = "ujy_tag_first_code")
    String ujyTagFirstCode;
    @TableField(value = "ujy_tag_first")
    String ujyTagFirst;
    @TableField(value = "ujy_tag_second_code")
    String ujyTagSecondCode;
    @TableField(value = "ujy_tag_second")
    String ujyTagSecond;
    @TableField(value = "ujy_tag_three_code")
    String ujyTagThreeCode;
    @TableField(value = "ujy_tag_three")
    String ujyTagThree;
    @TableField(value = "ujy_tag_four_code")
    String ujyTagFourCode;
    @TableField(value = "ujy_tag_four")
    String ujyTagFour;
    @TableField(value = "cma_tag_first_code")
    String cmaTagFirstCode;
    @TableField(value = "cma_tag_first")
    String cmaTagFirst;
    @TableField(value = "cma_tag_second_code")
    String cmaTagSecondCode;
    @TableField(value = "cma_tag_second")
    String cmaTagSecond;
    @TableField(value = "cma_tag_three_code")
    String cmaTagThreeCode;
    @TableField(value = "cma_tag_three")
    String cmaTagThree;
    @TableField(value = "cma_tag_four_code")
    String cmaTagFourCode;
    @TableField(value = "cma_tag_four")
    String cmaTagFour;
    @TableField(value = "dom_tag_first_code")
    String domTagFirstCode;
    @TableField(value = "dom_tag_first")
    String domTagFirst;
    @TableField(value = "dom_tag_second_code")
    String domTagSecondCode;
    @TableField(value = "dom_tag_second")
    String domTagSecond;
    @TableField(value = "dom_tag_three_code")
    String domTagThreeCode;
    @TableField(value = "dom_tag_three")
    String domTagThree;
    @TableField(value = "dom_tag_four_code")
    String domTagFourCode;
    @TableField(value = "dom_tag_four")
    String domTagFour;
    @TableField(value = "vtr_tag_first_code")
    String vtrTagFirstCode;
    @TableField(value = "vtr_tag_first")
    String vtrTagFirst;
    @TableField(value = "vtr_tag_second_code")
    String vtrTagSecondCode;
    @TableField(value = "vtr_tag_second")
    String vtrTagSecond;
    @TableField(value = "vtr_tag_three_code")
    String vtrTagThreeCode;
    @TableField(value = "vtr_tag_three")
    String vtrTagThree;
    @TableField(value = "vtr_tag_four_code")
    String vtrTagFourCode;
    @TableField(value = "vtr_tag_four")
    String vtrTagFour;
    @TableField(value = "nps_tag_first_code")
    String npsTagFirstCode;
    @TableField(value = "nps_tag_first")
    String npsTagFirst;
    @TableField(value = "nps_tag_second_code")
    String npsTagSecondCode;
    @TableField(value = "nps_tag_second")
    String npsTagSecond;
    @TableField(value = "nps_tag_three_code")
    String npsTagThreeCode;
    @TableField(value = "nps_tag_three")
    String npsTagThree;
    @TableField(value = "nps_tag_four_code")
    String npsTagFourCode;
    @TableField(value = "nps_tag_four")
    String npsTagFour;
    @TableField(value = "tag_parent_id")
    String tagParentId;
    @TableField(value = "tag_type")
    String tagType;
    @TableField(value = "tag_attribute")
    String tagAttribute;
    @TableField(value = "energy_type")
    String energyType;
    @TableField(value = "car_type")
    String carType;
    @TableField(value = "tag_status")
    String tagStatus;
    @TableField(value = "tag_description")
    String tagDescription;
    @TableField(value = "seriousness")
    String seriousness;
    @TableField(value = "user_journey1")
    String userJourney1;
    @TableField(value = "user_journey2")
    String userJourney2;
    @TableField(value = "user_journey3")
    String userJourney3;
    @TableField(value = "scenario_attr")
    String scenarioAttr;
    @TableField(value = "event_clarity")
    String eventClarity;
    @TableField(value = "d2c_responsible_dept")
    String d2CResponsibleDept;
    @TableField(value = "d2c_cc_dept")
    String d2CCcDept;
    @TableField(value = "d2c_accountable_dept")
    String d2CAccountableDept;
    @TableField(value = "create_time")
    LocalDateTime createTime;
    @TableField(value = "update_time")
    LocalDateTime updateTime;
    @TableField(value = "create_user")
    String createUser;
    @TableField(value = "update_user")
    String updateUser;
    @TableField(value = "app_client")
    String appClient;
    @TableField(value = "sort")
    String sort;
    @TableField(value = "level")
    String level;
    @TableField(value = "emotion")
    String emotion;
    @TableField(value = "intention")
    String intention;
    @TableField(value = "tag_accuracy")
    String tagAccuracy;
    @TableField(value = "tag_customer_issue_classification")
    String tagCustomerIssueClassification;
    @TableField(value = "tag_issue_severity")
    String tagIssueSeverity;
    @TableField(value = "tag_code_status")
    String tagCodeStatus;
    @TableField(value = "tag_business_domain")
    String tagBusinessDomain;
    @TableField(value = "tag_high_value_flag")
    String tagHighValueFlag;
    @TableField(value = "tag_complaint_flag_needing_reply")
    String tagComplaintFlagNeedingReply;
    @TableField(value = "tag_high_quality_voc_flag")
    String tagHighQualityVocFlag;
    @TableField(value = "tag_new_energy_or_fuel")
    String tagNewEnergyOrFuel;
    @TableField(value = "tag_need_forvclosed_loop")
    String tagNeedForvclosedLoop;
}
