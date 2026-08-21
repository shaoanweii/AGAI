package com.voc.service.analysis.risk.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 平台名称→渠道编码映射工具类（返回 Map 版本）
 * Map 结构：
 * - key: channelCode → 渠道编码（如 pd_post_bilibili）
 * - key: channelDesc → 渠道描述（如 B站）
 */
public class ChannelMappingConstant {

    // 定义 Map 固定 Key，避免硬编码
    private static final String KEY_CHANNEL_CODE = "channelCode";
    private static final String KEY_CHANNEL_DESC = "channelDesc";


    public static final Map<String, String> CHANGAN_BRANDS = Map.of(
            "长安引力", "A01",
            "长安凯程", "A02",
            "深蓝汽车", "A03",
            "阿维塔",   "A04",
            "长安启源", "A05"
    );

    public static final Map<String, String> BRAND_MAPPING = Map.of(
            "长安品牌（引力）", "长安引力",
            "长安品牌（凯程）", "长安凯程",
            "深蓝", "深蓝汽车",
            "阿维塔", "阿维塔",
            "长安品牌（启源）", "长安启源"
    );

    /**
     * 核心方法：根据平台名称获取渠道映射信息（返回 Map）
     *
     * @param platformName 原始平台名称（允许含空格、特殊符号）
     * @return Map<String, String> 包含 channelCode + channelDesc
     */
    public static Map<String, String> getPlatformMapping(String platformName) {
        // 空值兜底：空名称直接返回“其他”
        if (platformName == null || platformName.trim().isEmpty()) {
            return buildMappingMap("pd_post_qt", "其他");
        }

        // 标准化处理：去所有空格 + 转小写，统一匹配规则
        String normalizedName = platformName.trim().replaceAll("\\s+", "").toLowerCase();

        // -------------------------- 高优先级：组合条件匹配（多关键词） --------------------------
        // 规则6/7：车质网+论坛 → pd_post_czw_lt（车质网）
        if (normalizedName.contains("车质网") && normalizedName.contains("论坛")) {
            return buildMappingMap("pd_post_czw_lt", "车质网");
        }
        // 规则18/19：汽车之家+口碑 → pd_post_qczj_kbms（汽车之家）
        if (normalizedName.contains("汽车之家") && normalizedName.contains("口碑")) {
            return buildMappingMap("pd_post_qczj_kbms", "汽车之家");
        }
        // 规则16/17：汽车之家+论坛 → pd_post_qczj_lt（汽车之家）
        if (normalizedName.contains("汽车之家") && normalizedName.contains("论坛")) {
            return buildMappingMap("pd_post_qczj_lt", "汽车之家");
        }
        // 规则21-24：太平洋+论坛 → pd_post_tpyqc_lt（太平洋汽车）
        if (normalizedName.contains("太平洋") && normalizedName.contains("论坛")) {
            return buildMappingMap("pd_post_tpyqc_lt", "太平洋汽车");
        }
        // 规则32/33：新出行+-（全角/半角） → pd_post_xcx（新出行）
        if (normalizedName.contains("新出行") && (normalizedName.contains("-") || normalizedName.contains("－"))) {
            return buildMappingMap("pd_post_xcx", "新出行");
        }
        // 规则35/36：易车+论坛 → pd_post_yc_sq（易车）
        if (normalizedName.contains("易车") && normalizedName.contains("论坛")) {
            return buildMappingMap("pd_post_yc_sq", "易车");
        }
        // 规则4：车友圈（包含）→ pd_post_dcd_pl（懂车帝）
        if (normalizedName.contains("车友圈")) {
            return buildMappingMap("pd_post_dcd_pl", "懂车帝");
        }

        // -------------------------- 中优先级：单关键词直接匹配 --------------------------
        // 规则1：B站 → pd_post_bilibili（B站）
        if (normalizedName.contains("b站") || normalizedName.contains("哔哩哔哩")) {
            return buildMappingMap("pd_post_bilibili", "B站");
        }
        // 规则2：百度（非贴吧）→ pd_post_bdtb（百度，VOC二级渠道）
        if (normalizedName.contains("百度") && !normalizedName.contains("贴吧")) {
            return buildMappingMap("pd_post_bdtb", "百度");
        }
        // 规则5：车质网（无论坛）→ pd_post_czw_ts（车质网）
        if (normalizedName.contains("车质网")) {
            return buildMappingMap("pd_post_czw_ts", "车质网");
        }
        // 规则8：懂车帝 → pd_post_dcd_pl（懂车帝，VOC二级渠道）
        if (normalizedName.contains("懂车帝")) {
            return buildMappingMap("pd_post_dcd_pl", "懂车帝");
        }
        // 规则9：抖音 → pd_post_dy（抖音）
        if (normalizedName.contains("抖音")) {
            return buildMappingMap("pd_post_dy", "抖音");
        }
        // 规则10：黑猫投诉 → pd_post_hmts_ts（黑猫投诉）
        if (normalizedName.contains("黑猫投诉")) {
            return buildMappingMap("pd_post_hmts_ts", "黑猫投诉");
        }
        // 规则12：今日头条 → pd_post_jrtt（今日头条）
        if (normalizedName.contains("今日头条")) {
            return buildMappingMap("pd_post_jrtt", "今日头条");
        }
        // 规则13：快手 → pd_post_ks（快手）
        if (normalizedName.contains("快手")) {
            return buildMappingMap("pd_post_ks", "快手");
        }
        // 规则14：汽车门 → pd_post_qcmw_ts（汽车门网）
        if (normalizedName.contains("汽车门")) {
            return buildMappingMap("pd_post_qcmw_ts", "汽车门网");
        }
        // 规则15：汽车投诉网 → pd_post_qctsw_ts（汽车投诉网）
        if (normalizedName.contains("汽车投诉网")) {
            return buildMappingMap("pd_post_qctsw_ts", "汽车投诉网");
        }
        // 规则25/26：微博/新浪微博 → pd_post_wb（微博）
        if (normalizedName.contains("微博") || normalizedName.contains("新浪微博")) {
            return buildMappingMap("pd_post_wb", "微博");
        }
        // 规则30：西瓜视频 → pd_post_xgsp（今日头条）
        if (normalizedName.contains("西瓜视频")) {
            return buildMappingMap("pd_post_xgsp", "今日头条");
        }
        // 规则31：小红书 → pd_post_xhs（小红书）
        if (normalizedName.contains("小红书")) {
            return buildMappingMap("pd_post_xhs", "小红书");
        }
        // 规则34：新浪网 → pd_post_xlxw（新浪，VOC二级渠道）
        if (normalizedName.contains("新浪网")) {
            return buildMappingMap("pd_post_xlxw", "新浪");
        }
        // 规则37-51：知乎（所有变体）→ pd_post_zh（知乎）
        if (normalizedName.contains("知乎")) {
            return buildMappingMap("pd_post_zh", "知乎");
        }
        // 规则52：中国汽车质量网 → pd_post_zgqczlw_ts（中国汽车质量网）
        if (normalizedName.contains("中国汽车质量网")) {
            return buildMappingMap("pd_post_zgqczlw_ts", "中国汽车质量网");
        }
        // 规则53/54：中国网/中国网汽车 → pd_post_zgqcw（中国网汽车）
        if (normalizedName.contains("中国网")) {
            return buildMappingMap("pd_post_zgqcw", "中国网汽车");
        }
        // 规则29：微信视频号 → pub_post_wxspx（微信）
        if (normalizedName.contains("微信视频号")) {
            return buildMappingMap("pub_post_wxspx", "微信");
        }
        // 规则28：微信（非视频号）→ pd_post_wxgzh（微信）
        if (normalizedName.contains("微信")) {
            return buildMappingMap("pd_post_wxgzh", "微信");
        }

        // -------------------------- 低优先级：明确映射为“其他”的规则 --------------------------
        // 规则11：虎扑社区 → 其他
        if (normalizedName.contains("虎扑社区")) {
            return buildMappingMap("pd_post_qt", "其他");
        }
        // 规则20：全国12315互联网平台 → 其他
        if (normalizedName.contains("全国12315互联网平台")) {
            return buildMappingMap("pd_post_qt", "其他");
        }
        // 规则27：微头条 → 其他
        if (normalizedName.contains("微头条")) {
            return buildMappingMap("pd_post_qt", "其他");
        }
        // 规则55：中国消费者协会信息网 → 其他
        if (normalizedName.contains("中国消费者协会信息网")) {
            return buildMappingMap("pd_post_qt", "其他");
        }
        // 规则56：中国质量万里行 → 其他
        if (normalizedName.contains("中国质量万里行")) {
            return buildMappingMap("pd_post_qt", "其他");
        }

        // 终极兜底：未匹配任何规则 → 其他
        return buildMappingMap("pd_post_qt", "其他");
    }

    /**
     * 辅助方法：构建渠道映射 Map（统一封装，避免重复代码）
     *
     * @param code 渠道编码
     * @param desc 渠道描述
     * @return 不可修改的 Map（防止外部篡改）
     */
    private static Map<String, String> buildMappingMap(String code, String desc) {
        Map<String, String> map = new HashMap<>(2); // 初始容量2，提升性能
        map.put(KEY_CHANNEL_CODE, code);
        map.put(KEY_CHANNEL_DESC, desc);
        // 返回不可修改的 Map，增强安全性
        return Collections.unmodifiableMap(map);
    }

    // 测试方法：验证返回 Map 的正确性
    public static void main(String[] args) {
        // 测试用例1：B站
        Map<String, String> bilibiliMap = getPlatformMapping("中国消费者协会信息网");
        System.out.println("渠道编码：" + bilibiliMap.get(KEY_CHANNEL_CODE)); // pd_post_bilibili
        System.out.println("渠道描述：" + bilibiliMap.get(KEY_CHANNEL_DESC)); // B站
    }
}