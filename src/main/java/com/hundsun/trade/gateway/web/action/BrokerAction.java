/**
 * BrokerAction.java
 *
 * 功  能： 经纪商管理
 * 类名： InstiAction.java
 *
 *   ver       变更日                   部门             责任人                  变更内容
 * ──────────────────────────────────────────────────────
 *   V1.00   2014-2-17  恒生电子         huzf     初版
 *
 */
package com.hundsun.trade.gateway.web.action;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hundsun.jres.common.cep.cfgmng.ConfigManagerFactory;
import com.hundsun.jres.common.share.dataset.DatasetService;
import com.hundsun.jres.interfaces.cep.cfgmng.IConfigChangeProcessor;
import com.hundsun.jres.interfaces.cep.cfgmng.IConfigInfoProvider;
import com.hundsun.jres.interfaces.pluginFramework.ISysEvent;
import com.hundsun.jres.interfaces.share.dataset.IDataset;
import com.hundsun.trade.remoting.api.service.munandao.otcTrade.RemoteOtcTradeEOSService;

/**
 * 类描述：商户信息管理Action类.
 * 
 * @author huzf
 * @version 1.0 2014-2-17 改订
 * @since 1.0
 * 
 */
@Controller
@RequestMapping("/remoting")
public class BrokerAction {
	
	/**
     * LOG.
     */
    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(BrokerAction.class);
	
	@Autowired
	private RemoteOtcTradeEOSService remoteOtcTradeEOSService;
	
	@RequestMapping(value = "/list.htm")
	public void listBrokerForPage(ModelMap model) {

	}
	
	@RequestMapping(value = "/addT2.htm")
	public void addT2Member(ModelMap model) {
		
		IConfigChangeProcessor  p =  ConfigManagerFactory.createConfigChangeProcessor();
		HashMap<String,String> paraMap = new HashMap<String,String>();
		paraMap.put("nodeName", "biz-server");//ares-app-config.xml中必须配置有此parentName
		paraMap.put("nodeNo", "1");
		paraMap.put("ip", "127.0.0.1");
		paraMap.put("port", "9150");
		p.executeChangeEvent(ISysEvent.CONFIG_T2MEMBER_ADD, paraMap, null);
		
	}
	@RequestMapping(value = "/addRoute.htm")
	public void addRoute(ModelMap model) {
		
		IConfigChangeProcessor  p =  ConfigManagerFactory.createConfigChangeProcessor();
		HashMap<String,String> paraMap = new HashMap<String,String>();
		paraMap.put("nodeName", "biz-server");//ares-app-config.xml中必须配置有此parentName
		paraMap.put("functionId", "100001");
		p.executeChangeEvent(ISysEvent.CONFIG_ROUTE_ADD, paraMap, null);
		
	}
	@RequestMapping(value = "/updateRoute.htm")
	public void updateRoute(ModelMap model) {
		
		IConfigChangeProcessor  p =  ConfigManagerFactory.createConfigChangeProcessor();
		HashMap<String,String> paraMap = new HashMap<String,String>();
		paraMap.put("nodeName", "biz-server");//ares-app-config.xml中必须配置有此parentName
		paraMap.put("functionId", "100001");
		p.executeChangeEvent(ISysEvent.CONFIG_ROUTE_UPDATE, paraMap, null);
		
	}
	@RequestMapping(value = "/delRoute.htm")
	public void deleteRoute(ModelMap model) {
		
		IConfigChangeProcessor  p =  ConfigManagerFactory.createConfigChangeProcessor();
		HashMap<String,String> paraMap = new HashMap<String,String>();
		paraMap.put("nodeName", "biz-server");//ares-app-config.xml中必须配置有此parentName
		paraMap.put("functionId", "100001");
		p.executeChangeEvent(ISysEvent.CONFIG_ROUTE_DEL, paraMap, null);
		
	}
	@RequestMapping(value = "/getConnNodes.htm")
	public void getConnNodes(ModelMap model) {
		
		IConfigInfoProvider infoProvider = ConfigManagerFactory.createConfigInfoProvider();
		IDataset ds = infoProvider.queryConnectedNodes();
		if(ds!=null){
			DatasetService.printDataset(ds);
		}
		
	}
}
