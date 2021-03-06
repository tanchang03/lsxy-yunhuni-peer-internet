package com.lsxy.framework.rpc.api;

import com.lsxy.framework.core.utils.StringUtil;
import com.lsxy.framework.core.utils.UUIDGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * RPC 请求对象
 * @author Administrator
 *
 */
public class RPCRequest extends  RPCMessage{

	private String name;		//RQ
	private String param;		//PM


	private Map<String,Object> paramMap;		//参数解析后放入map中以方便调用


	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		String sBody = this.getBody();
		return "R["+this.getSessionid()+"]["+ this.getTimestamp()+"]["+this.name+"]["+this.tryTimes+"]["+this.lastTryTimestamp+"]>>PM:" + this.param+">>SESSIONID:"+this.getSessionid() + ">>BODY:"+sBody;
	}

	/**
	 * 解析
	 */
	public void _parseParam() {
		paramMap = new HashMap<>();
		StringTokenizer st1 = new StringTokenizer(param, AND);
		while (st1.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st1.nextToken(), EQ);
			String name = st2.nextToken();
			String value = null;
			if(st2.hasMoreElements()){
				value = decode(st2.nextToken());
			}
			paramMap.put(name, value);
		}
	}
	/**
	 * 根据参数名称得到参数值
	 * @param name
	 * @return
	 */
	public Object getParameter(String name){
		if(paramMap==null){
			_parseParam();
		}
		return paramMap.get(name);
	}
	/**
	 * 获取参数,如果值为空，则返回默认值
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public Object getParameter(String name,Object defaultValue){
		Object ret = getParameter(name);
		if(ret == null){
			ret = defaultValue;
		}
		return ret;
	}
	public Map<String,Object > getParamMap() {
		if (this.paramMap == null && this.param != null)
		{
			this._parseParam();
		}
		return paramMap;
	}


	/**
	 * 创建新的请求对象,必须指定请求命令名称和请求参数
	 *
	 * @param name
	 * 		@see ServiceConstants 请求名称
	 * @param params
	 * 		参数使用url参数方案  param01=001&param002=002
	 * @return
	 * 			返回请求对象
	 */
	public static RPCRequest newRequest(String name,String params) {
		RPCRequest request = new RPCRequest();
		request.setSessionid(UUIDGenerator.uuid());
		request.setTimestamp(new Date().getTime());
		request.setName(name);
		request.setParam(params);
		return request;
	}

	/**
	 * 创建新的请求对象,必须指定请求命令名称和请求参数
	 * 请求参数以map形式传入,会最终转化为url模式
	 * @param name
	 * 		@see ServiceConstants 请求名称
	 * @param params
	 * 		参数使用url参数方案  map参数
	 * @return
	 * 			返回请求对象
	 */

	public static RPCRequest newRequest(String name,Map<String,Object> params) {
		StringBuffer sb = new StringBuffer();
		for (String key:params.keySet() ) {
			Object value = params.get(key);
			if(value == null){
				continue;
			}
			String v = encode(value.toString());
			sb.append(key + EQ + v + AND);
		}
		if(sb.length() > 0){
			sb.subSequence(0,sb.length()-1);
		}
		return newRequest(name,sb.toString());
	}


	/**
	 * 序列化request
	 * RQ:SESSIONID TIMESTAMP REQUESTNAME PARAMURL BODY
	 * @return
	 */
	@Override
	public String serialize() {
		StringBuffer sb = new StringBuffer("RQ:");
		sb.append(this.getSessionid());
		sb.append(" ");
		sb.append(this.getTimestamp());
		sb.append(" ");
		sb.append(this.getName());
		sb.append(" ");
		sb.append(this.getParam());
		sb.append("&body="+encode(this.getBody()));
		return sb.toString();
	}

	public static RPCRequest unserialize(String str){
		RPCRequest request = null;
		if(StringUtil.isNotEmpty(str) && str.matches("RQ:\\w{32}\\s\\d{13}+\\s\\w+\\s.*")){
			request = new RPCRequest();
			String[] parts = str.split(" ");
			request.setSessionid(parts[0].substring(3));
			request.setTimestamp(Long.valueOf(parts[1]));
			request.setName(parts[2]);
			if(parts.length>=4) {
				request.setParam(parts[3]);
			}
			if(request.getParameter("body") != null){
				request.setBody(request.getParameter("body").toString());
			}
		}
		return request;
	}

	public static void main(String[] args) {
//		String value = "RQ:810724b022c4800013339b20a0fc37b8 1481804145625 MN_CH_SYS_CALL max_answer_seconds=30&from_uri=02066304057&to_uri=02066304058@192.168.22.10&areaId =area001&max_ring_seconds=45&user_data=8a2d9fed590267f001590268e3900000&";
////		Pattern pt = Pattern.compile("RQ:\\w[32]\\s\\w+\\s.*");
//		System.out.println(value.matches("RQ:\\w{32}\\s\\d{13}+\\s\\w+\\s.*"));
//		String[] parts = value.split(" ");
//		String sessionid = parts[0].substring(3);
//		String timestamp = parts[1];
//		String name = parts[2];
//		if(parts.length>=4) {
//			String param = parts[3];
//			System.out.println(param);
//		}
//
//		System.out.println(sessionid);
//		System.out.println(name);
//		System.out.println(System.currentTimeMillis());
//
//		RPCRequest request = RPCRequest.unserialize(value);
//		System.out.println(request.getParam());
//		String value = "{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,value2:341234}";
//		String encodeValue =Base64Utils.encodeToString(value.getBytes());
//		System.out.println(StringUtil.encodeChars(value," ,$,{,:"));
//		String original = new String(Base64Utils.decodeFromString(encodeValue));
//		System.out.println(original);

		RPCRequest request = RPCRequest.newRequest("MN_CH_REQUEST","");
		request.setBody("{\"value\":\"hah,$%^&*( )ah\"}");
		String sReq = request.serialize();
		System.out.println(request.serialize());
		RPCRequest request2 = RPCRequest.unserialize(sReq);
		System.out.println(request2.getParam());

		RPCResponse response = RPCResponse.buildResponse(request);
		response.setMessage(RPCResponse.STATE_OK);
		response.setBody("{value:111,value2:341234}{value:/1/1/1/,value2:341234}{value:111,value2:341234}{value:111,value2:341234}{value:111,v alue2:341234}{valu e:111,value2: 341234}{value: 111,value2:34123 4}{value:111,va lue2:341234}");
		String xx = response.serialize();
		System.out.println(response.serialize());
		RPCResponse rep2 = RPCResponse.unserialize(xx);
		System.out.println(rep2.getBody());
	}


}
