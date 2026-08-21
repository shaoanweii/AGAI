package com.voc.service.common.util;

import cn.hutool.core.codec.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author: cuick
 * @Date: 2022/6/20 18:47
 */
public class StringUtil {
    /**
     * 压缩字符串
     * @param str 要压缩的字符串
     * @return 压缩后的字符串
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        GZIPOutputStream gzipOutputStream=null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzipOutputStream != null) {
                try {
                    gzipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.encode(byteArrayOutputStream.toByteArray());
    }

    /**
     * 解压缩
     * @param compressedStr 压缩后的字符串
     * @return 解压后的字符串
     */
    public static String uncompress(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }
        byte[] compressed;
        String decompressed=null;
        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            compressed = Base64.decode(compressedStr);
            byteArrayInputStream = new ByteArrayInputStream(compressed);
            gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            byte[] buffer = new byte[1024];
            int offset;
            while ((offset = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, offset);
            }
            decompressed = byteArrayOutputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzipInputStream != null) {
                try {
                    gzipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return decompressed;
    }
}
