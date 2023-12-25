package com.zhongji.simpleshelf.util;

import com.zhongji.simpleshelf.common.enums.TimeEnum;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * 获取间隔时间
     *
     * @param date
     * @param timeEnum
     * @param interval
     * @return
     */
    public static LocalDate getIntervalTime(LocalDate date, TimeEnum timeEnum, Integer interval) {
        //格式化
        LocalDate zeroTimeDate = LocalDate.parse(formatter.format(date), formatter);
        if (TimeEnum.DAY.equals(timeEnum)) {
            return zeroTimeDate.plusDays(interval);
        } else if (TimeEnum.WEEK.equals(timeEnum)) {
            LocalDate localDate = zeroTimeDate.plusWeeks(interval);
            return localDate.with(DayOfWeek.MONDAY);
        } else if (TimeEnum.MONTH.equals(timeEnum)) {
            LocalDate localDate = zeroTimeDate.plusMonths(interval);
            return localDate.withDayOfMonth(1);
        } else if (TimeEnum.QUARTER.equals(timeEnum)) {
            int currentMonth = date.getMonthValue();
            // 计算当前季度
            int currentQuarter = (currentMonth - 1) / 3 + 1;
            //当前季度的初始
            int currentQuarterMonth = (currentQuarter - 1) * 3 + 1;
            LocalDate localDate = LocalDate.of(date.getYear(), currentQuarterMonth, 1);
            return localDate.plusMonths(interval * 3);
        } else if (TimeEnum.YEAR.equals(timeEnum)) {
            LocalDate localDate = zeroTimeDate.plusYears(interval);
            return localDate.withDayOfYear(1);
        }
        return null;
    }


    /**
     * 季度
     *
     * @param currentQuarter
     * @param interval
     * @return
     */
    public static int getQuarter(int currentQuarter, int interval) {
        interval = interval % 4;
        if (currentQuarter + interval > 0) {
            return (currentQuarter - 1 + interval) % 4 + 1;
        } else {
            return 4 + currentQuarter + interval;
        }
    }


    /**
     * 季度间隔年份
     *
     * @param currentQuarter
     * @param interval
     * @return
     */
    public static int getQuarterYear(int currentQuarter, int interval) {
        int intervalYear = (currentQuarter + interval - 1) / 4;
        //负
        if (currentQuarter + interval <= 0) {
            intervalYear = (currentQuarter + interval) / 4 - 1;
        }
        return intervalYear;
    }


}
