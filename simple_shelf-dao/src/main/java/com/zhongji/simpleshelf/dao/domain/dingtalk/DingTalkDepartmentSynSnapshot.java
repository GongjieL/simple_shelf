package com.zhongji.simpleshelf.dao.domain.dingtalk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName ding_talk_department_syn_snapshot
 */
@TableName(value ="ding_talk_department_syn_snapshot")
@Data
public class DingTalkDepartmentSynSnapshot implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    /**
     * 人事系统数据
     */
    private String hrContent;

    /**
     * 人事系统和钉钉对应关系
     */
    private String dingTalkMap;

    /**
     * 差异点
     */
    private String modifiedData;

    /**
     * 比较的id
     */
    private Long comparedId;

    @TableField(exist = false)
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
        DingTalkDepartmentSynSnapshot other = (DingTalkDepartmentSynSnapshot) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getHrContent() == null ? other.getHrContent() == null : this.getHrContent().equals(other.getHrContent()))
            && (this.getDingTalkMap() == null ? other.getDingTalkMap() == null : this.getDingTalkMap().equals(other.getDingTalkMap()))
            && (this.getModifiedData() == null ? other.getModifiedData() == null : this.getModifiedData().equals(other.getModifiedData()))
            && (this.getComparedId() == null ? other.getComparedId() == null : this.getComparedId().equals(other.getComparedId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getHrContent() == null) ? 0 : getHrContent().hashCode());
        result = prime * result + ((getDingTalkMap() == null) ? 0 : getDingTalkMap().hashCode());
        result = prime * result + ((getModifiedData() == null) ? 0 : getModifiedData().hashCode());
        result = prime * result + ((getComparedId() == null) ? 0 : getComparedId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", hrContent=").append(hrContent);
        sb.append(", dingTalkMap=").append(dingTalkMap);
        sb.append(", modifiedData=").append(modifiedData);
        sb.append(", comparedId=").append(comparedId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}