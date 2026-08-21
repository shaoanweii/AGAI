package com.voc.service.analysis.core.v2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

@Service
public class SelectdbApiServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(SelectdbApiServiceImpl.class);

    public boolean streamInsert(InputStream input) {
        String label = "label_" + System.currentTimeMillis();
        try {
            // ===== 1. 配置参数（生产环境应注入）=====
            final String dorisUser = "root";
            final String dorisPassword = "lqJEm6cqxK4vG0Z5zqX7";
            final String feHost = "172.16.71.48:8031";
            final String database = "voc_ms_td";
            final String table = "voc_anal_flow_model_tags_result_data_full_ext";

            // ===== 2. 构建认证头 =====
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                    (dorisUser + ":" + dorisPassword).getBytes()
            );

            // ===== 3. 关键修复：触发自动 100-continue =====
            // ===== 4. 关键修复3：强制 HTTP/1.1 + 禁用 Expect =====
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1) // 必须 HTTP/1.1
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            // ===== 4. 构建请求（不设置 Expect 头！）=====
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + feHost + "/api/" + database + "/" + table + "/_stream_load"))
                    .header("Authorization", authHeader)
                    .header("Expect", "100-continue")
                    .header("label", label)
                    .header("format", "json")
                    .header("strip_outer_array", "true")
                    .header("jsonpaths", "[\\\"$.id\\\",\\\"$.publishTime\\\", \\\"$.dataId\\\",\\\"$.channelCatagory\\\", \\\"$.channelCode\\\",\\\"$.channelName\\\", \\\"$.brandCode\\\",\\\"$.brandName\\\", \\\"$.carSeriesCode\\\"," +
                            "\\\"$.carSeriesName\\\", \\\"$.modelName\\\",\\\"$.contentType\\\", \\\"$.title\\\",\\\"$.content\\\", \\\"$.sentiment\\\",\\\"$.intention\\\", \\\"$.dataCreateTime\\\",\\\"$.createTime\\\"," +
                            "\\\"$.updateTime\\\",\\\"$.isOuter\\\", \\\"$.hotWord\\\",\\\"$.keywords\\\", \\\"$.originalTextScene\\\",\\\"$.marketId\\\", \\\"$.competitiveType\\\",\\\"$.isCore\\\", \\\"$.seriesFactory\\\"," +
                            "\\\"$.automark\\\", \\\"$.oneId\\\",\\\"$.userJourney1\\\", \\\"$.userJourney2\\\",\\\"$.userJourney3\\\", \\\"$.usageScenarioFirst\\\",\\\"$.usageScenarioSecond\\\", \\\"$.d2cResponsibleDept\\\"," +
                            "\\\"$.d2cAccountableDept\\\", \\\"$.d2cCcDept\\\",\\\"$.custGlobalId\\\", \\\"$.custClassify\\\",\\\"$.custMainPhone\\\", \\\"$.isCarOwner\\\",\\\"$.custAge\\\", \\\"$.custAgeGroup\\\",\\\"$.custName\\\"," +
                            "\\\"$.custGender\\\",\\\"$.custHighEducaion\\\", \\\"$.marrigeStatue\\\",\\\"$.familyIncome\\\", \\\"$.isExchangeFlg\\\",\\\"$.purchaseCarTimes\\\", \\\"$.isMemberFlg\\\",\\\"$.custProvinceCode\\\"," +
                            "\\\"$.custProvince\\\",\\\"$.custCityCode\\\", \\\"$.custCity\\\",\\\"$.custType\\\", \\\"$.custLivedProv\\\",\\\"$.custLivedCity\\\", \\\"$.custProfession\\\",\\\"$.vhlVin\\\", \\\"$.vhlColorName\\\"," +
                            "\\\"$.vhlProductDate\\\", \\\"$.vhlOfflineDate\\\",\\\"$.vhlIsAbroad\\\", \\\"$.vhlDisCh\\\",\\\"$.vhlDisMt\\\", \\\"$.vhlEngClsf\\\",\\\"$.vhlEngSeris\\\", \\\"$.vhlVehType\\\",\\\"$.vhlCountry\\\"," +
                            "\\\"$.vhlBdClsf\\\",\\\"$.vhlSegMt\\\", \\\"$.vhlPowClsf\\\",\\\"$.vhlFuClsf\\\", \\\"$.vhlModlSt\\\",\\\"$.vhlStdPlntCode\\\", \\\"$.dlrOcId\\\",\\\"$.dlrOcCode\\\", \\\"$.dlrOcName\\\"," +
                            "\\\"$.dlrOcProvinceCode\\\", \\\"$.dlrOcProvince\\\",\\\"$.dlrOcCityCode\\\", \\\"$.dlrOcCity\\\",\\\"$.dlrDcId\\\", \\\"$.dlrDcCode\\\",\\\"$.dlrDcName\\\", \\\"$.dlrDcProvinceCode\\\"," +
                            "\\\"$.dlrDcProvince\\\", \\\"$.dlrDcCityCode\\\",\\\"$.dlrDcCity\\\", \\\"$.dlrMcId\\\",\\\"$.dlrMcCode\\\", \\\"$.dlrMcName\\\",\\\"$.dlrMcProvinceCode\\\", \\\"$.dlrMcProvince\\\"," +
                            "\\\"$.dlrMcCityCode\\\", \\\"$.dlrMcCity\\\",\\\"$.isWsaterArmy\\\", \\\"$.isManagerFocused\\\",\\\"$.isBigV\\\", \\\"$.authorId\\\",\\\"$.authorNick\\\", \\\"$.isMainPost\\\",\\\"$.originalLink\\\"," +
                            "\\\"$.viewCount\\\",\\\"$.commentCount\\\", \\\"$.likeCount\\\",\\\"$.shareCount\\\", \\\"$.favoriteCount\\\",\\\"$.workOrderId\\\", \\\"$.questId\\\",\\\"$.questType\\\", \\\"$.questAnswerScore\\\"," +
                            "\\\"$.questBusinessType\\\", \\\"$.questBusinessScenario\\\",\\\"$.tagAccuracy\\\", \\\"$.tagCustomerIssueClassification\\\",\\\"$.tagIssueSeverity\\\", \\\"$.tagCodeStatus\\\"," +
                            "\\\"$.tagBusinessDomain\\\", \\\"$.tagEventClarity\\\",\\\"$.tagHighValueFlag\\\", \\\"$.tagComplaintFlagNeedingReply\\\",\\\"$.tagComplaintFlagNeedingPrtvMsg\\\"," +
                            "\\\"$.tagHighQualityVocFlag\\\",\\\"$.tagNewEnergyOrFuel\\\", \\\"$.tagNeedForvclosedLoop\\\",\\\"$.tagSort\\\", \\\"$.topic\\\",\\\"$.topicText\\\", \\\"$.opinion\\\",\\\"$.cptTagFirstCode\\\"," +
                            "\\\"$.cptTagSecondCode\\\",\\\"$.cptTagThreeCode\\\", \\\"$.cptTagFourCode\\\",\\\"$.cptTagFirst\\\", \\\"$.cptTagSecond\\\",\\\"$.cptTagThree\\\", \\\"$.cptTagFour\\\",\\\"$.ujyTagFirstCode\\\"," +
                            "\\\"$.ujyTagSecondCode\\\",\\\"$.ujyTagThreeCode\\\", \\\"$.ujyTagFourCode\\\",\\\"$.ujyTagFirst\\\", \\\"$.ujyTagSecond\\\",\\\"$.ujyTagThree\\\", \\\"$.ujyTagFour\\\",\\\"$.cmaTagFirstCode\\\"," +
                            "\\\"$.cmaTagSecondCode\\\",\\\"$.cmaTagThreeCode\\\", \\\"$.cmaTagFourCode\\\",\\\"$.cmaTagFirst\\\", \\\"$.cmaTagSecond\\\",\\\"$.cmaTagThree\\\", \\\"$.cmaTagFour\\\",\\\"$.domTagFirstCode\\\"," +
                            "\\\"$.domTagSecondCode\\\",\\\"$.domTagThreeCode\\\", \\\"$.domTagFourCode\\\",\\\"$.domTagFirst\\\", \\\"$.domTagSecond\\\",\\\"$.domTagThree\\\", \\\"$.domTagFour\\\",\\\"$.npsTagFirstCode\\\"," +
                            "\\\"$.npsTagSecondCode\\\",\\\"$.npsTagThreeCode\\\", \\\"$.npsTagFourCode\\\",\\\"$.npsTagFirst\\\", \\\"$.npsTagSecond\\\",\\\"$.npsTagThree\\\", \\\"$.npsTagFour\\\",\\\"$.vtrTagFirstCode\\\"," +
                            "\\\"$.vtrTagSecondCode\\\",\\\"$.vtrTagThreeCode\\\", \\\"$.vtrTagFourCode\\\",\\\"$.vtrTagFirst\\\", \\\"$.vtrTagSecond\\\",\\\"$.vtrTagThree\\\", \\\"$.vtrTagFour\\\",\\\"$.abandon\\\"," +
                            "\\\"$.sourceDataId\\\",\\\"$.highQuality\\\"," +
                            "\\\"$.retweetedUrl\\\",\\\"$.retweetedUserId\\\",\\\"$.retweetedUserName\\\",\\\"$.retweetedContent\\\",\\\"$.retweetedTitle\\\",\\\"$.retweetedTime\\\"," +
                            "\\\"$.commentUserName\\\",\\\"$.commentUrl\\\",\\\"$.commentUserId\\\",\\\"$.oneIdRisk\\\"," +
                            "\\\"$.adType\\\",\\\"$.attributeTagCode\\\",\\\"$.attributeTagName\\\",\\\"$.emotionalLevel\\\"," +
                            "\\\"$.insertDt\\\"]")
                    .header("columns", "id,publish_time,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title," +
                            "content,sentiment,intention,data_create_time,create_time,update_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,is_core,series_factory," +
                            "automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept," +
                            "cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income," +
                            "is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city," +
                            "cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type," +
                            "vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code," +
                            "dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city," +
                            "dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id," +
                            "author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score," +
                            "quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain," +
                            "tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel," +
                            "tag_need_forvclosed_loop,tag_sort,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first," +
                            "cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three," +
                            "ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code," +
                            "dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code," +
                            "nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code," +
                            "vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,source_data_id,high_quality," +
                            "retweeted_url,retweeted_user_id,retweeted_user_name,retweeted_content,retweeted_title,retweeted_time,comment_user_name,comment_user_id,comment_url,one_id_risk," +
                            "ad_type,attribute_tag_code,attribute_tag_name,emotional_level,insert_dt")
                    .header("timeout", "600")
                    .header("max_filter_ratio", "0.1")
                    .PUT(createLargeBodyPublisher(input)) // 关键：确保请求体 > 1KB
                    .build();

            // ===== 5. 执行请求 =====
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // ===== 6. 结果处理 =====
            log.info("Doris Response: {}", response.body());
            return handleDorisResponse(response, label);

        } catch (Exception e) {
            log.error("🔥 Stream Load 失败 (Label: {})", label, e);
            return false;
        }
    }

    /**
     * 关键方法：确保请求体 > 1KB 以触发自动 100-continue
     */
    private HttpRequest.BodyPublisher createLargeBodyPublisher(InputStream input) throws IOException {
        // 1. 读取全部内容
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(temp)) != -1) {
            buffer.write(temp, 0, bytesRead);
        }
        byte[] bodyBytes = buffer.toByteArray();
        input.close();

        // 2. 验证大小（Doris 要求 > 1KB 才触发 100-continue）
        if (bodyBytes.length < 1024) {
            log.warn("⚠️ 请求体 < 1KB ({} bytes)，可能无法触发 100-continue", bodyBytes.length);
            // 补充空格确保 > 1KB
            String jsonStr = new String(bodyBytes);
            jsonStr += " ".repeat(1024 - bodyBytes.length);
            bodyBytes = jsonStr.getBytes();
        }

        // 3. 使用 ByteArrayBodyPublisher（自动触发 100-continue）
        return HttpRequest.BodyPublishers.ofByteArray(bodyBytes);
    }

    private boolean handleDorisResponse(HttpResponse<String> response, String label) {
        if (response.statusCode() == 200) {
            String body = response.body();
            if (body.contains("\"Status\": \"Success\"")) {
                log.info("✅ 导入成功! Label: {}", label);
                return true;
            }
            log.error("❌ Doris 业务失败 (Label: {}): {}", label, extractDorisError(body));
            return false;
        }
        log.error("❌ HTTP 失败 (Label: {}), 状态码: {}", label, response.statusCode());
        return false;
    }

    /**
     * 提取Doris错误信息
     */
    private static String extractDorisError(String resp) {
        int start = resp.indexOf("\"Message\": \"");
        if (start > 0) {
            start += 12;
            int end = resp.indexOf("\"", start);
            return (end > start) ? resp.substring(start, end) : "响应解析失败";
        }
        return resp.contains("Label Already Exists") ? "Label重复" : "未知错误: " + resp;
    }
}