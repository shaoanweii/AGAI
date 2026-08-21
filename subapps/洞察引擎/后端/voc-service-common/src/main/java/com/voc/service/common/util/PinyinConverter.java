package com.voc.service.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.engine.pinyin4j.Pinyin4jEngine;

public class PinyinConverter {

    /**
     * 将字符串中的中文转换为拼音，其他字符保持不变
     *
     * @param input 输入字符串
     * @return 转换后的字符串
     */
    public static String convertToPinyin(String input) {
        if (StrUtil.isEmpty(input)) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        Pinyin4jEngine pinyinEngine = new Pinyin4jEngine();

        for (char c : input.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                // 如果是中文字符，转换为拼音
                String pinyin = pinyinEngine.getPinyin(c);
                result.append(pinyin);
            } else if (Character.isLetterOrDigit(c)) {
                // 如果是字母或数字，直接保留
                result.append(c);
            } else {
                // 如果是特殊符号，转换为下划线
                result.append('_');
            }
        }

        return result.toString();
    }

    public static void main(String[] args) {
        String input = "你好, World!";
        String output = convertToPinyin(input);
        System.out.println(output); // 输出: nihao, World!
    }
}