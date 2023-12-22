package com.zhongji.simpleshelf.dao.domain.erpsid;

import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName CWFL_NEW
 */
@Data
public class CwflNew implements Serializable {
    /**
     * 
     */
    private String fl;

    /**
     * 
     */
    private String shuom;

    /**
     * 
     */
    private String xuanz;

    /**
     * 
     */
    private String gongylx;

    /**
     * 
     */
    private Double liyl;

    /**
     * 
     */
    private Double tgfltqq;

    /**
     * 
     */
    private String kemu;

    /**
     * 
     */
    private String hangxm;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CwflNew other = (CwflNew) that;
        return (this.getFl() == null ? other.getFl() == null : this.getFl().equals(other.getFl()))
            && (this.getShuom() == null ? other.getShuom() == null : this.getShuom().equals(other.getShuom()))
            && (this.getXuanz() == null ? other.getXuanz() == null : this.getXuanz().equals(other.getXuanz()))
            && (this.getGongylx() == null ? other.getGongylx() == null : this.getGongylx().equals(other.getGongylx()))
            && (this.getLiyl() == null ? other.getLiyl() == null : this.getLiyl().equals(other.getLiyl()))
            && (this.getTgfltqq() == null ? other.getTgfltqq() == null : this.getTgfltqq().equals(other.getTgfltqq()))
            && (this.getKemu() == null ? other.getKemu() == null : this.getKemu().equals(other.getKemu()))
            && (this.getHangxm() == null ? other.getHangxm() == null : this.getHangxm().equals(other.getHangxm()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFl() == null) ? 0 : getFl().hashCode());
        result = prime * result + ((getShuom() == null) ? 0 : getShuom().hashCode());
        result = prime * result + ((getXuanz() == null) ? 0 : getXuanz().hashCode());
        result = prime * result + ((getGongylx() == null) ? 0 : getGongylx().hashCode());
        result = prime * result + ((getLiyl() == null) ? 0 : getLiyl().hashCode());
        result = prime * result + ((getTgfltqq() == null) ? 0 : getTgfltqq().hashCode());
        result = prime * result + ((getKemu() == null) ? 0 : getKemu().hashCode());
        result = prime * result + ((getHangxm() == null) ? 0 : getHangxm().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fl=").append(fl);
        sb.append(", shuom=").append(shuom);
        sb.append(", xuanz=").append(xuanz);
        sb.append(", gongylx=").append(gongylx);
        sb.append(", liyl=").append(liyl);
        sb.append(", tgfltqq=").append(tgfltqq);
        sb.append(", kemu=").append(kemu);
        sb.append(", hangxm=").append(hangxm);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}