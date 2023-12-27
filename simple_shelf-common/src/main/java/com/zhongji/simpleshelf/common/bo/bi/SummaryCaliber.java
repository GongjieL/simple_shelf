package com.zhongji.simpleshelf.common.bo.bi;


/**
 * 统计口径
 */
public class SummaryCaliber {
    /**
     * 当前/上一个/下一个
     */
    private String timeDesc;

    /**
     * 时间单位
     */
    private String timeUnit;

    /**
     * 时间的具体(比如2022-10-01~2022-10-11)
     */
    private String timeDetail;

    public String getTimeDesc() {
        return timeDesc;
    }

    public void setTimeDesc(String timeDesc) {
        this.timeDesc = timeDesc;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTimeDetail() {
        return timeDetail;
    }

    public void setTimeDetail(String timeDetail) {
        this.timeDetail = timeDetail;
    }
}
