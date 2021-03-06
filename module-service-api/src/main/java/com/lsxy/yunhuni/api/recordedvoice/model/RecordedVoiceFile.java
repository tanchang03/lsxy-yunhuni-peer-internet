package com.lsxy.yunhuni.api.recordedvoice.model;

import com.lsxy.framework.api.base.IdEntity;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.app.model.App;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Created by liups on 2016/6/29.
 */
@Entity
@Where(clause = "deleted=0")
@Table(schema="db_lsxy_bi_yunhuni",name = "tb_bi_recored_voice_file")
public class RecordedVoiceFile extends IdEntity {
    private String url;     //文件URL
    private App app;        //所属APP
    private Double size;    //文件大小
    private CallSession callSession; //所属会话

    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @ManyToOne
    @JoinColumn(name = "app_id")
    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Column(name = "size")
    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    @OneToOne
    @JoinColumn(name = "session_id")
    public CallSession getCallSession() {
        return callSession;
    }

    public void setCallSession(CallSession callSession) {
        this.callSession = callSession;
    }
}
