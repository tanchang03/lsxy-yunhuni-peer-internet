package com.lsxy.framework.api.tenant.model;

import com.lsxy.framework.api.base.IdEntity;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * 租户
 * 
 * @author tandy
 * 
 */
@Entity
@Where(clause = "deleted=0")
@Table(schema="db_lsxy_base",name = "tb_base_tenant")
public class Tenant extends IdEntity {
	public static final int AUTH_COMPANY=1;//认证类型-企业认证
	public static final int AUTH_ONESELF=0;//认证类型-个人认证
	public static final int AUTH_ONESELF_WAIT = 6;//个人等待审核
	public static final int AUTH_WAIT = 0;//企业等待审核
	public static final int AUTH_NO = 100;//未认证
	public static final int AUTH_UPGRADE_WAIT = 3;//个人升级企业等待
	public static final int AUTH_UPGRADE_SUCCESS = 4;//个人升级企业成功
	public static final int AUTH_UPGRADE_FAIL = 5;//个人升级企业失败
	public static final int AUTH_COMPANY_SUCCESS = 2;//企业认证成功
	public static final int AUTH_ONESELF_SUCCESS = 1;//个人认证成功
	public static final int AUTH_COMPANY_FAIL = -2;//企业认证失败
	public static final int AUTH_ONESELF_FAIL = -1;//个人认证失败
	public static final Integer[] AUTH_STATUS = new Integer[]{1,2,3,4,5};//已认证状态集合
	private static final long serialVersionUID = 1L;
	//是否实名
	private Integer isRealAuth;

	//租户识别码
	private String tenantUid;

	private String registerUserId; //注册的账号ID

	private String tenantName;

	@Column(name = "is_real_auth")
	public Integer getIsRealAuth() {
		return isRealAuth;
	}

	public void setIsRealAuth(Integer isRealAuth) {
		this.isRealAuth = isRealAuth;
	}

	@Column(name = "tid")
	public String getTenantUid() {
		return tenantUid;
	}

	public void setTenantUid(String tenantUid) {
		this.tenantUid = tenantUid;
	}

	@Column(name = "reg_user_id")
	public String getRegisterUserId() {
		return registerUserId;
	}

	public void setRegisterUserId(String registerUserId) {
		this.registerUserId = registerUserId;
	}

	@Column(name = "tenant_name")
	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
}
