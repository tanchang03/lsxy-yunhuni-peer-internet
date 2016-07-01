package com.lsxy.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 读取系统配置的工具方法
 * @author tanchang
 *
 */
public class SystemConfig {
	private static Log logger=LogFactory.getLog(SystemConfig.class);
	private static Properties pp = new Properties();
	static{
		InputStream is = null;

		if(logger.isDebugEnabled()){
			logger.debug(String.format("加载全局配置文件:%s",Constants.DEFAULT_CONFIG_FILE));
		}

		try
		{
			is = SystemConfig.class.getClassLoader().getResourceAsStream(Constants.DEFAULT_CONFIG_FILE);
		}catch(Exception e)
		{
			e.printStackTrace();;
		}
		try
		{
			if(is == null)
				is = SystemConfig.class.getResourceAsStream(Constants.DEFAULT_CONFIG_FILE);
		}catch(Exception e)
		{
			e.printStackTrace();;
		}
		if(is == null){
			logger.error("加载配置文件失败,类路径中没有找到配置文件:"+Constants.DEFAULT_CONFIG_FILE);
		}else{
			try {
				pp.load(is);
			} catch (IOException e) {
				logger.error("加载配置文件失败:"+Constants.DEFAULT_CONFIG_FILE,e);
			}
		}

	}
	


	
	
	/**
	 * 
	 *描述：取得指定属性的值
	 *时间：2010-1-12
	 *作者：谭畅
	 *参数：
	 *	@param name 参数名称
	 *返回值:
	 *	@return 返回对应属性的值
	 *抛出异常：
	 */
	public static String getProperty(String name){
		return getProperty(name, null);
	}
	
	/**
	 * 
	 *描述：取得指定属性的值,如果没有配置该值，则给出一个默认的配置值
	 *时间：2010-1-12
	 *作者：谭畅
	 *参数：
	 *	@param name 参数名称
	 *返回值:
	 *	@return 返回对应属性的值
	 *抛出异常：
	 */
	public static String getProperty(String name,String defaultValue){
		String value = null;
		if(pp != null){
			value = pp.getProperty(name);
		}
		if(value != null && !value.equals("")){
			value = value.trim();
		}else{
			value = defaultValue;
		}
		return value;
	}


	/**
	 * 获取版本时间戳
	 * 版本时间戳主要用于脚本文件或者css样式等静态文件添加后缀，如果在不改变的情况下，
	 * 时间戳也不发生变化，此时浏览器会加载浏览器缓存的静态文件
	 * 如果js文件发生变化，则需要强制浏览器从服务器端获取最新版本的js文件
	 * @return
	 */
	public static String getVersionStamp(){
		if(isDevelopModel()){
			return new Date().getTime()+"";
		}else{
			return SystemConfig.getProperty("global.versionStamp",new Date().getTime()+"");
		}
	}

	/**
	 * 是否开发模式
	 * @return
	 */
	public static boolean isDevelopModel(){
		return getProperty("global.developModel","false").equals("true");
	}

}