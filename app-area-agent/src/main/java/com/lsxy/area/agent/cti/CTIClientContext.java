package com.lsxy.area.agent.cti;

import com.lsxy.app.area.cti.Commander;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.core.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by tandy on 16/8/5.
 * cti 客户端  环境 用来管理多个客户端连接
 */
@Component
public class CTIClientContext {

    public static final String KEY_CTI_CLUSTER = "hesong:ipsc:cluster:nodeid";
    public static final String KEY_CTI__NODE_PREFIX = "hesong:ipsc:cluster:node";

    @Autowired
    private RedisCacheService cacheService;

    //服务器连接 <ips>
    private Set<String> servers = new HashSet<>();
    //CTI节点 通过CTI事件初始化
    private Map<String,CTINode> nodes = new TreeMap<>();

    public void init(){
        if(logger.isDebugEnabled()){
            logger.debug("初始化CTI配置 From Redis");
        }
        loadConfig();
    }
    /**
     * 加载CTI配置
     */
    public void loadConfig(){
        Set<String> ipscNodes = cacheService.smembers(KEY_CTI_CLUSTER);
        if(logger.isDebugEnabled()){
            logger.debug("load cti cluster config : {}",ipscNodes);
        }
        ipscNodes.forEach((snode)->{
            String key = KEY_CTI__NODE_PREFIX + ":" + snode;
            String sServer = (String) cacheService.hget(key,"ip");
            if(logger.isDebugEnabled()){
                logger.debug("cti node {} ip is {}",snode,sServer);
            }
            if(StringUtil.isNotEmpty(sServer)){
                sServer = sServer.substring(0,sServer.indexOf("-"));
                if(!servers.contains(sServer)){
                    servers.add(sServer);
                }
                if(!nodes.keySet().contains(snode)){
                    nodes.put(snode,new CTINode(snode,sServer));
                }
            }
        });
    }


    private static final Logger logger = LoggerFactory.getLogger(CTIClientContext.class);
    public HashMap<String,Commander> clients = new HashMap<>();


    /**
     * 注册commander对象到环境中
     * @param serverIp
     * @param commander
     */
    public void registerCommander(String serverIp, Commander commander) {
        if(logger.isDebugEnabled()){
            logger.debug("注册CTICommander{}:{}",serverIp,commander);
        }

        clients.put(serverIp, commander);
        for(String key:nodes.keySet()){
            CTINode node = nodes.get(key);
            if(logger.isDebugEnabled()){
                logger.debug("注册CTICommand到节点 {} vs {}",node.getUnitId(),commander.getConnectingUnitId());
            }
            if(node.getUnitId().equals(commander.getConnectingUnitId()+"")){
                if(logger.isDebugEnabled()){
                    logger.debug("注册CTICommander到节点:{}->{}",node.getId(),commander);
                }
                node.setCtiCommander(commander);
            }
        }
    }

    /**
     * 获取一个有效的CTI客户端连接对象进行操作
     * 需要考虑:CTI负载情况  会话相关(会议成员需要被分配到同一个CTI服务)
     * 根据参考ID获取Node ，如果参考id为空，就采用负载最少的节点返回
     * @param referenceResId  参考资源id 资源ID可以解析出nodeid
     *                        eg:1.0.0-sys.call-11000016035539044
     * @return
     */
    public CTINode getAvalibleNode(String referenceResId) throws AvalibleCTINodeNotFoundException{
        CTINode node = null;
        if(StringUtil.isNotEmpty(referenceResId)){
            String nodeId = referenceResId.substring(0,referenceResId.indexOf("-"));
            node = nodes.get(nodeId);
            if(logger.isDebugEnabled()){
                logger.debug("根据参考资源ID{}找到对应的CTI节点:{}",referenceResId,node);
            }
            if(node == null){
                throw new AvalibleCTINodeNotFoundException(this,referenceResId);
            }
            return node;
        }
        //如果资源id为空，就查询负载最小的节点
        try {
            for (String key:nodes.keySet()) {
                CTINode xnode = nodes.get(key);
                if(!xnode.isReady()){
                    continue;
                }
                if(node == null){
                    node = xnode;
                }else{
                    if(xnode.getLoadValue() < node.getLoadValue()){
                        node = xnode;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("查找有效CTINODE时出现异常：",ex);
        }

        if (node == null) {
            logger.error("没有找到一个有效的CTI客户端");
            throw new AvalibleCTINodeNotFoundException(this,referenceResId);
        }else {
            if (logger.isDebugEnabled()) {
                logger.debug("找到负载最低的CTI节点:{},当前负载状况：{}" , node,nodes);
            }
        }
        return node;
    }


    /**
     * 是否是一个新的serverIp
     * 新的serverIp 是没有创建 commaonder对象的，如果有创建commander对象，需要在clients中注册
     * @param serverIp
     * @return
     */
    public boolean isNewServerFound(String serverIp) {
        return !this.clients.containsKey(serverIp);
    }

    public void remove(String ip) {
        clients.remove(ip);
    }

    public Set<String> getCTIServers() {
        return servers;
    }

    /**
     * 更新负载数据
     * @param nodeid
     * @param unitId
     * @param pid
     * @param loadData
* callin.count=116347,
* callout.count=116797,
* sip.in.total.num=1000,
* ch.total.num=2000,
* dsp.used.num=107,
* sip.callout.num=405,
* sip.callin.num=405,
* loadlevel=0,
* callin.num=405,
* sip.out.total.num=1000,
     */
    public CTINode updateNodeLoadData(String nodeid, Map<String, Integer> loadData) {
        CTINode node = nodes.get(nodeid);

        if(node != null) {
            node.setCinCount(loadData.get("callin.count"));
            node.setCoutCount(loadData.get("callout.count"));
            node.setCinNumber(loadData.get("callin.num"));
            node.setCoutNumber(loadData.get("callout.num"));

            node.setReady(true);

            if (logger.isDebugEnabled()) {
                logger.debug("CTI节点更新负载数据：{}", node);
            }
        }
        return node;
    }

    /**
     * 服务连接丢失
     * @param ip
     * @param unitid
     */
    public void ctiClientConnectionLost(String areaId,String ip, byte unitid) {
        if(logger.isDebugEnabled()){
            logger.debug("CTI服务连接丢失：{}",ip);
        }
        servers.remove(ip);
        clients.remove(ip);
        for (String key:nodes.keySet()) {
            if(key.startsWith(areaId+"."+unitid)){
                if(logger.isDebugEnabled()){
                    logger.debug("CTI服务连接丢失-清理节点: {}",key);
                }
                nodes.remove(key);
            }
        }
    }

    /**
     * 连接状态变化事件
     * @param ctiAreaId
     * @param unitId
     * @param clientId
     * @param status
*                   <li>0: 断开连接</li>
*                   <li>1: 新建连接</li>
     */
    public void connectStateChanged(String ctiAreaId, byte unitId, byte clientId, byte status) {
        String key = ctiAreaId + "." + unitId + "." + clientId;
        if(status == 0){ //如果有连接断开
            if(logger.isDebugEnabled()){
                logger.debug("CTI节点断开连接:{}.{}.{}",ctiAreaId,unitId,clientId);
            }
            this.nodes.remove(key);
        }else if(status == 1 || status == 2){  //如果有新建立的连接 或者初始已有连接
            if(!this.nodes.containsKey(key)){
                if(logger.isDebugEnabled()){
                    logger.debug("CTI新节点加入:{}.{}.{}",ctiAreaId,unitId,clientId);
                }
                String nodeRedisKey = KEY_CTI__NODE_PREFIX + ":" + key;
                String sServer = (String) cacheService.hget(nodeRedisKey,"ip");
                sServer = sServer.substring(0,sServer.indexOf("-"));
                nodes.put(key,new CTINode(key,sServer));
            }
        }
    }

    @Override
    public String toString() {
        return "CTIClientContext{" +
                ", servers=" + servers +
                ", nodes=" + nodes +
                ", clients=" + clients +
                '}';
    }

}
