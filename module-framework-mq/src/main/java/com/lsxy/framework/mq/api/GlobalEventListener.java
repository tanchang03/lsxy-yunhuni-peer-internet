package com.lsxy.framework.mq.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;

/**
 * 全局事件监听
 * @author tandy
 *
 */
public class GlobalEventListener {
	private static final Log logger = LogFactory.getLog(GlobalEventListener.class);
	
	private AbstractMQConsumer consumer;
	
	
	public AbstractMQConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(AbstractMQConsumer consumer) {
		this.consumer = consumer;
	}

	public void init() throws JMSException {
		logger.debug("GlobalEventListener init");
		consumer.start();
		logger.debug("GlobalEventListener started successfull");
	}
	
	public void destroy(){
		 consumer.destroy();
	}



}
