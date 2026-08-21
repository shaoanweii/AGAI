package com.voc.service.insights.engine.util;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/18 下午4:45
 * @描述:
 **/
public class DatetimeConverter implements Converter<String> {

    /**
     * 这里读的时候会调用
     *
     * @param cellData            excel数据 (NotNull)
     * @param contentProperty     excel属性 (Nullable)
     * @param globalConfiguration 全局配置 (NotNull)
     * @return 读取到内存中的数据
     */
    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        DateTimeFormat annotation = contentProperty.getField().getAnnotation(DateTimeFormat.class);
        String format = Objects.nonNull(annotation) ? annotation.value() : "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            if (CellDataTypeEnum.NUMBER.equals(cellData.getType())) {
                //如果是数字 小于0则 返回
                BigDecimal bd = cellData.getNumberValue();
                int days = bd.intValue();//天数
                int mills = (int) Math.round(bd.subtract(new BigDecimal(days)).doubleValue() * 24 * 3600);
                //获取时间
                Calendar c = Calendar.getInstance();
                c.set(1900, 0, -1);
                c.add(Calendar.DATE, days);
                int hour = mills / 3600;
                int minute = (mills - hour * 3600) / 60;
                int second = mills - hour * 3600 - minute * 60;
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, second);

                String format1 = sdf.format(c.getTime());
                return format1;
            }else if (CellDataTypeEnum.STRING.equals(cellData.getType())) {
                Date date = sdf.parse(cellData.getStringValue());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                return localDateTime.format(DateTimeFormatter.ofPattern(format));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
}
