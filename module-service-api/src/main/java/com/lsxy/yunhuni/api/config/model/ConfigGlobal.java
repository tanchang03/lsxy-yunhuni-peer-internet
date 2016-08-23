package com.lsxy.yunhuni.api.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 配置类
 * Created by zhangxb on 2016/8/23.
 */
@Entity
@Table(schema="db_lsxy_bi_yunhuni",name = "tb_bi_yy_config_global")
public class ConfigGlobal {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_MESSAGE_AUTH_ONESELE_SUCCESS = "1";
    public static final String TYPE_MESSAGE_AUTH_ONESELE_FILE = "3";
    public static final String TYPE_MESSAGE_AUTH_COMPANY_SUCCESS = "2";
    public static final String TYPE_MESSAGE_AUTH_COMPANY_FILE = "4";
    public static final String TYPE_MESSAGE_VOICE_PLAY_SUCCESS = "5";
    public static final String TYPE_MESSAGE_VOICE_PLAY_FAIL = "6";
    public static final String TYPE_MESSAGE_INVOCE_APPLY_SUCCESS = "7";
    public static final String TYPE_MESSAGE_INVOCE_APPLY_FAIL = "8";
    public static final String TYPE_MESSAGE_ARREARS= "9";
    public static final String TYPE_MESSAGE_FEEDBACK= "10";
    private String type;//配置项类型
    private String name;//配置项名称
    private String value;//配置项值
    private Date expireDt;//配置项有效期
    private String remark;//备注
    private String enabled;//on off
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    @Column(name = "expire_dt")
    public Date getExpireDt() {
        return expireDt;
    }

    public void setExpireDt(Date expireDt) {
        this.expireDt = expireDt;
    }
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    @Column(name = "enabled")
    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
