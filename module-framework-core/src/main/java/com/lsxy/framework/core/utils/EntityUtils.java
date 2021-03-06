package com.lsxy.framework.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

/**
 * 复制实体对象属性
 * 排除空属性的赋值
 * 排除实体固有属性默认属性
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class EntityUtils extends org.apache.commons.beanutils.BeanUtils {

	protected static final Log logger = LogFactory.getLog(EntityUtils.class);

	public static final String[] excludeCopyProperties=new String[]{"deleted","createTime","lastTime","version","sortNo"};

	private EntityUtils() {
	}


	/**
	 * 复制javabean属性,可指定是否将null值的属性进行复制
	 * @param dest
	 * @param orig
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void  copyProperties(Object dest, Object orig) throws IllegalAccessException,
			InvocationTargetException {

		PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

		// Validate existence of the specified beans
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}

		// Copy the properties, converting as necessary
		if (orig instanceof DynaBean) {
			DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass()
					.getDynaProperties();
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if(ArrayUtils.contains(excludeCopyProperties,name)){
					continue;
				}
				// Need to check isReadable() for WrapDynaBean
				// (see Jira issue# BEANUTILS-61)
				if (propertyUtilsBean.isReadable(orig, name)
						&& propertyUtilsBean.isWriteable(dest, name)) {
					Object value = ((DynaBean) orig).get(name);
					copyProperty(dest, name, value);
				}
			}
		} else if (orig instanceof Map) {
			Iterator entries = ((Map) orig).entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String name = (String) entry.getKey();
				if(ArrayUtils.contains(excludeCopyProperties,name)){
					continue;
				}
				if (propertyUtilsBean.isWriteable(dest, name)) {
					copyProperty(dest, name, entry.getValue());
				}
			}
		} else /* if (orig is a standard JavaBean) */{
			PropertyDescriptor[] origDescriptors = propertyUtilsBean
					.getPropertyDescriptors(orig);
			for (int i = 0; i < origDescriptors.length; i++) {
				
				String name = origDescriptors[i].getName();
				if(ArrayUtils.contains(excludeCopyProperties,name)){
					continue;
				}
				if ("class".equals(name)) {
					continue; // No point in trying to set an object's class
				}
				if (propertyUtilsBean.isReadable(orig, name)
						&& propertyUtilsBean.isWriteable(dest, name)) {
					try {
						Object value = propertyUtilsBean.getSimpleProperty(
								orig, name);
//						System.out.println("------------name:"+name+" \t  ;value:"+propertyUtilsBean.getSimpleProperty(
//								orig, name));
						if (value == null) {
							continue;
						}
						copyProperty(dest, name, value);
					} catch (NoSuchMethodException e) {
						// Should not happen
					}
				}
			}
		}

	}

}
