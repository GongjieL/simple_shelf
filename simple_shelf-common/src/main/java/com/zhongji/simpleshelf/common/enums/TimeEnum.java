package com.zhongji.simpleshelf.common.enums;


import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 统计口径
 */
public enum TimeEnum {


    DAY("DAY", "当日", new Function<LocalDate, LocalDate>() {

        @Override
        public LocalDate apply(LocalDate date) {
            LocalDate localDate = date.plusDays(1);
            String format = localDate.format(formatter);
            return LocalDate.parse(format, formatter);
        }
    }),
    WEEK("WEEK", "本周", new Function<LocalDate, LocalDate>() {
        @Override
        public LocalDate apply(LocalDate date) {
            LocalDate localDate = date.plusDays(7);
            String format = localDate.format(formatter);
            return LocalDate.parse(format, formatter);
        }
    }),
    MONTH("MONTH", "月", new Function<LocalDate, LocalDate>() {
        @Override
        public LocalDate apply(LocalDate date) {
            //获取月份
            Month nowMonth = date.getMonth().plus(1);
            int year = date.getYear();
            if (date.getMonth().getValue() >= nowMonth.getValue()) {
                year = year + 1;
            }
            return LocalDate.of(year, nowMonth, 1);
        }
    }),
    QUARTER("QUARTER", "季度", new Function<LocalDate, LocalDate>() {
        @Override
        public LocalDate apply(LocalDate date) {
            //获取月份
            Month nowMonth = date.getMonth().plus(3);
            int year = date.getYear();
            if (date.getMonth().getValue() >= nowMonth.getValue()) {
                year = year + 1;
            }
            return LocalDate.of(year, nowMonth, 1);
        }
    }),
    YEAR("WEEK", "年", new Function<LocalDate, LocalDate>() {
        @Override
        public LocalDate apply(LocalDate date) {
            //获取月份
            Month nowMonth = date.getMonth().plus(12);
            int year = date.getYear() + 1;
            return LocalDate.of(year, nowMonth, 1);
        }
    }),


    ;

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String code;

    private String name;


    /**
     * 增加的天数/月数
     */

    private Function<LocalDate, LocalDate> getEndDate;

    TimeEnum(String code, String name, Function<LocalDate, LocalDate> getEndDate) {
        this.code = code;
        this.name = name;
        this.getEndDate = getEndDate;
    }

    public static TimeEnum getByCode(String subCode) {
        for (TimeEnum value : values()) {
            if (value.getCode().equals(subCode)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Function<LocalDate, LocalDate> getGetEndDate() {
        return getEndDate;
    }

}
