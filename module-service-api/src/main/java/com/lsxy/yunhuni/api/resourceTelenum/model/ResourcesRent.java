package com.lsxy.yunhuni.api.resourceTelenum.model;

import com.lsxy.framework.api.base.IdEntity;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.yunhuni.api.app.model.App;

import javax.persistence.*;
import java.util.Date;

/**
 * 租户号码租用
 * Created by zhangxb on 2016/7/1.
 */
@Entity
@Table(schema="db_lsxy_bi_yunhuni",name = "tb_bi_resources_rent")
public class ResourcesRent extends IdEntity {
    private Tenant tenant;//tenant_id所属租户
    private App app;//app_id 关联应用
    private ResourceTelenum resourceTelenum;//res_id资源对象（号码）

    private String resName;//res_name 资源名称,
    private String resType;//res_type 1-号码资源 2-线路资源 3-待扩展,
    private Date rentDt;//rent_dt 租用时间,
    private Date rentExpire;//rent_expire到期时间,
    private Integer rentStatus;//rent_status租用状态,

    @Column(name = "res_type")
    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }
    @Column(name = "res_name")
    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }
    @Column(name = "rent_dt")
    public Date getRentDt() {
        return rentDt;
    }

    public void setRentDt(Date rentDt) {
        this.rentDt = rentDt;
    }
    @Column(name = "rent_expire")
    public Date getRentExpire() {
        return rentExpire;
    }
    public void setRentExpire(Date rentExpire) {
        this.rentExpire = rentExpire;
    }

    @Column(name = "rent_status")
    public Integer getRentStatus() {
        return rentStatus;
    }

    public void setRentStatus(Integer rentStatus) {
        this.rentStatus = rentStatus;
    }

    @OneToOne
    @JoinColumn(name = "res_id")
    public ResourceTelenum getResourceTelenum() {
        return resourceTelenum;
    }

    public void setResourceTelenum(ResourceTelenum resourceTelenum) {
        this.resourceTelenum = resourceTelenum;
    }

    @OneToOne
    @JoinColumn(name = "app_id")
    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}