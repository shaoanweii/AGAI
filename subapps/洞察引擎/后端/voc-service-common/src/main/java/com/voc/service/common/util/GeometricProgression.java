package com.voc.service.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @Title: GeometricProgression
 * @Package: com.voc.service.common.util
 * @Description: 对等数列计算
 * @Author: cuick
 * @Date: 2024/5/30 15:42
 * @Version:1.0
 */
public class GeometricProgression {
    static Integer[] actions = ArrayUtil.addAll(new Integer[]{2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072});

    static {
        actions = CollUtil.sort(CollUtil.toList(actions), Comparator.reverseOrder()).toArray(new Integer[0]);
    }

    /**
     * 1,2,4,8,16,32,64,128,256,512,1024
     *
     * @param n
     * @return
     */
    public static Set<Integer> split(final Integer n) {
        Integer m = n;
        Set<Integer> list = CollUtil.newLinkedHashSet();
        for (int i = 0; i < actions.length; i++) {
            final Integer f = actions[i];
            if (m >= f) {
                list.add(f);
                m -= f;
            }
            if (m == 0) {
                break;
            }
        }

        return list;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        long l = System.currentTimeMillis();
        List<Integer> workIds = CollUtil.newArrayList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        for (Integer workId : workIds) {
            System.out.println(GeometricProgression.split(workId));
        }
        System.out.println(System.currentTimeMillis()-l);
    }
}
