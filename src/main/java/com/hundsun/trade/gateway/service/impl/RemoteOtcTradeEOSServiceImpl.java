/**
 * 
 */
package com.hundsun.trade.gateway.service.impl;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.hundsun.trade.gateway.utils.HttpClientUtil;
import com.hundsun.trade.remoting.api.request.munandao.OtcTradeRequest;
import com.hundsun.trade.remoting.api.response.munandao.OtcTradeResponse;
import com.hundsun.trade.remoting.api.service.munandao.otcTrade.RemoteOtcTradeEOSService;

@Component
public class RemoteOtcTradeEOSServiceImpl implements RemoteOtcTradeEOSService {
	
	@Value("${eosURL}")
	private String eosURL;
	
	@PostConstruct
	private void init(){
		log.info("################start#######" + eosURL);
	}
	
	/**
     * LOG.
     */
    @SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(RemoteOtcTradeEOSServiceImpl.class);

	/**
     * 返回0 成功
     * 返回9999 发送请求前失败
     * 返回9998 发送请求后解析失败或返回失败
     */
	@Override
	public OtcTradeResponse payOut(OtcTradeRequest request) {
		log.info("################startpay##################"+request.getTransOutNo());
		OtcTradeResponse response = new OtcTradeResponse();
		response.setErrorNO(9999);
		
    	String postUrl = eosURL + "/v1/transfer_out";
    	String check = checkAddress(request.getAddress());
    	if( check!= null){
    		response.setErrorInfo(check);
    		return response;
    	}
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("contract", request.getLoginIp());
    	map.put("to", request.getAddress());
    	map.put("amount", request.getTradeCoinCode());
    	map.put("memo", request.getType());
    	map.put("request_id", request.getTransOutNo());
    	
        String jsonString = null;
        response.setErrorNO(9998);
		try {			
			jsonString = HttpClientUtil.sendPost(postUrl, map);
		} catch (ClientProtocolException e) {
			log.error("", e);
			response.setErrorInfo("http post error:"+e.getMessage());
			return response;
		} catch (IOException e) {
			log.error("", e); 
			response.setErrorInfo("http post error:"+e.getMessage());
			return response;
		}
        try {
        	Map<String, Object> respMap = JSONObject.parseObject(jsonString, Map.class);
        	String bizNo = (String) respMap.get("txid");
        	if(StringUtils.isNotBlank(bizNo)){
        		response.setErrorNO(0);
            	response.setErrorInfo(bizNo);
        	}else{
        		String info = String.valueOf(respMap.get("message"));
                response.setErrorInfo(info);
        	}
        }catch(Exception e){
        	log.error("", e);
        	response.setErrorInfo(jsonString);
        }
		return response;
	}

	@Override
	public OtcTradeResponse queryPayMatchId(OtcTradeRequest request) {
		OtcTradeResponse response = new OtcTradeResponse();
		response.setErrorNO(9999);
		
        String postUrl2 = eosURL + "/v1/get_transfer";
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("txid", request.getTransOutNo());
        
        String jsonString2 = null;        
		try {
			jsonString2 = HttpClientUtil.sendPost(postUrl2, map2);
		} catch (ClientProtocolException e) {
			log.error("", e);
			response.setErrorInfo("http post error:"+e.getMessage());
			return response;
		} catch (IOException e) {
			log.error("", e);
			response.setErrorInfo("http post error:"+e.getMessage());
			return response;
		}
        response.setErrorNO(0);
        try {
        	Map<String, Object> respMap = JSONObject.parseObject(jsonString2, Map.class);        	        	
        	String status = (String) respMap.get("status");
        	if(StringUtils.isBlank(status)){
           		String info = String.valueOf(respMap.get("message"));
                response.setErrorInfo(info);
        	}
        	response.setSuccessInfo(status);
        }catch(Exception e){
        	log.error("", e);
        	response.setErrorInfo(jsonString2);
        }
		return response;
	}
	
	private String checkAddress(String address){
    	String getUrl = eosURL + "/v1/account/exist?account=" + address;
    	try {
    		String resultJson = HttpClientUtil.sendGet(getUrl, null, null, null, null, null);
        	Map<String, Object> respMap = JSONObject.parseObject(resultJson, Map.class);
        	Object exist = respMap.get("exist");
        	if(exist != null && (Boolean)exist == true){
        		return null;
        	}else{
        		return "地址不存在";
        	}
        }catch(Exception e){
        	log.error("", e);
        	return e.getMessage();
        }
	}

}
