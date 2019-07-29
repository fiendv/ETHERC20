package com.hundsun.trade.gateway.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
public class HttpClientUtil {

    private static Logger                            log                   = LoggerFactory
        .getLogger(HttpClientUtil.class);

    /** url */
    public static final String                       url                   = "http://47.92.80.205:3002/";
    /**
     * 编码格式.
     */
    public static final String                       CHARSET               = "UTF-8";

    /**
     * HTTP HEADER字段 Authorization应填充字符串Bearer
     */
    public static final String                       BEARER                = "Bearer ";

    private static CloseableHttpClient               httpClient            = null;
    /** 连接超时时间 */
    public final static int                          connectTimeout        = 20000;

    /** socket连接超时时间 */
    public final static int                          socketTimeout         = 25000;

    /** 发送请求相应时间 */
    public final static int                          requestTimeout        = 20000;
    /** 允许管理器限制最大连接数 ，还允许每个路由器针对某个主机限制最大连接数。 */
    public static PoolingHttpClientConnectionManager cm                    = new PoolingHttpClientConnectionManager();
    /**
     * 最大连接数
     */
    public final static int                          MAX_TOTAL_CONNECTIONS = 500;
    /**
     * 每个路由最大连接数 访问每个目标机器 算一个路由 默认 2个
     */
    public final static int                          MAX_ROUTE_CONNECTIONS = 80;

    static {
        cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);// 设置最大路由数
        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);// 最大连接数

        /**
         * 大量的构造器设计模式，很多的配置都不建议直接new出来，而且相关的API也有所改动，例如连接参数，
         * 以前是直接new出HttpConnectionParams对象后通过set方法逐一设置属性， 现在有了构造器，可以通过如下方式进行构造：
         * SocketConfig.custom().setSoTimeout(100000).build();
         */
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        cm.setDefaultSocketConfig(socketConfig);
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BEST_MATCH).setExpectContinueEnabled(true)
            .setStaleConnectionCheckEnabled(true).setRedirectsEnabled(true).build();
        // CodingErrorAction.IGNORE指示通过删除错误输入、向输出缓冲区追加 coder
        // 的替换值和恢复编码操作来处理编码错误的操作。
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8)
            .setMalformedInputAction(CodingErrorAction.IGNORE)
            .setUnmappableInputAction(CodingErrorAction.IGNORE).build();
        httpClient = HttpClients.custom().setConnectionManager(cm)
            .setDefaultRequestConfig(defaultRequestConfig)
            .setDefaultConnectionConfig(connectionConfig).build();
    }

    public static String sendPost(String url, Map<String, String> params, String charSet,
                                  String charsetReturn, HttpHost proxy, String authorization,
                                  String interfacename) throws ClientProtocolException,
                                                        IOException {
        HttpPost post = new HttpPost(url);
        Builder builder = RequestConfig.custom();
        if (proxy != null) {
            builder.setProxy(proxy);
            RequestConfig requestConfig = builder.setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(requestTimeout)
                .setExpectContinueEnabled(false).setRedirectsEnabled(true).build();
            post.setConfig(requestConfig);
        }
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Authorization", authorization);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            int n = 0;
            for (Entry<String, String> set : params.entrySet()) {
                if (n == 0) {
                    n++;
                    sb.append(set.getKey() + "=" + set.getValue());
                } else {
                    sb.append("&" + set.getKey() + "=" + set.getValue());
                }
                nvps.add(new BasicNameValuePair(set.getKey(), set.getValue()));
            }
        }
        post.setEntity(new UrlEncodedFormEntity(nvps, charSet));
        LogUtils.log("\n功能名称：" + interfacename + "\n" + "post  url = ["
                     + (url.endsWith("?") ? url : url + "?") + sb.toString() + "]",
            log);
        HttpResponse response = httpClient.execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, charsetReturn);
                LogUtils.log("result = " + result, log);
                return result;

            }
        } catch (Exception e) {
            LogUtils.log("HttpClient   请求 http状态码 status = [" + status + "]  获取HttpEntity ", e,
                log);
        } finally {
            if (entity != null) {
                entity.getContent().close();
            }
        }
        return null;
    }

    public static String sendPost(String url, Map<String, Object> params) throws ClientProtocolException,
                                                        IOException {
        HttpPost post = new HttpPost(url);
        Builder builder = RequestConfig.custom();

        post.setHeader("Content-Type", "application/json");
        
        String json = JSONObject.toJSONString(params);
        StringEntity se = new StringEntity(json, "utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        post.setEntity(se);
        
        LogUtils.log("\n功能名称：\n" + "post  url = ["
                     + (url.endsWith("?") ? url : url + "?") + json + "]",
            log);
        HttpResponse response = httpClient.execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, CHARSET);
                LogUtils.log("result = " + result, log);
                return result;
            }
        } catch (Exception e) {
            LogUtils.log("HttpClient   请求 http状态码 status = [" + status + "]  获取HttpEntity ", e,
                log);
        } finally {
            if (entity != null) {
                entity.getContent().close();
            }
        }
        return null;
    }

    /**
     * get请求
     * 
     * @param url
     * @param params
     * @param charSet
     * @return
     */
    public static String sendGet(String url, Map<String, String> params, String charSet,
                                 HttpHost proxy, String authorization, String interfacename) 
                                 throws ClientProtocolException, IOException {
            if (charSet == null){
            	charSet = CHARSET;
            }                
            StringBuffer urlbuf = new StringBuffer(url);
            if (params != null) {
                int n = 0;
                for (Entry<String, String> set : params.entrySet()) {
                    if (!urlbuf.toString().contains("?")) {
                        urlbuf.append("?");
                    }
                    if (n != 0) {
                        urlbuf.append("&");
                    }
                    urlbuf.append(set.getKey()).append("=").append(set.getValue());
                    n++;
                }
            }
            LogUtils.log("get = " + urlbuf.toString(), log);
            HttpGet get = new HttpGet(urlbuf.toString());
            get.setHeader("Content-Type", "application/x-www-form-urlencoded");
            get.setHeader("Authorization", authorization);
            // HttpUriRequest get = new HttpGet(urlbuf.toString());
            Builder builder = RequestConfig.custom();
            if (proxy != null) {
                builder.setProxy(proxy);
            }

            RequestConfig defaultConfig = builder.setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(requestTimeout)
                .setExpectContinueEnabled(false).setRedirectsEnabled(true).build();
            get.setConfig(defaultConfig);

            HttpResponse response = httpClient.execute(get);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = null;
            try {
                entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity, charSet);
                    LogUtils.log("result = " + result, log);//如果error_no字段不为0，表示业务出错。
                    return result;
                }
            } catch (Exception e) {
                LogUtils.log("HttpClient   请求 http状态码 status = [" + status + "]  ", e, log);
            } finally {
                if (entity != null) {
                    entity.getContent().close();
                }
            }
        return null;
    }

    public static String linkparams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            int n = 0;
            for (Entry<String, String> set : params.entrySet()) {
                if (n == 0) {
                    n++;
                    sb.append(set.getKey() + "=" + set.getValue());
                } else {
                    sb.append("&" + set.getKey() + "=" + set.getValue());
                }
            }
        }

        return sb.toString();
    }

    /**
     * cifangf 是对"App Key:App Secret"进行 Base64 编码后的字符串（区分大小写，包含冒号，但不包含双引号,采用
     * UTF-8 编码）。 例如: Authorization: Basic eHh4LUtleS14eHg6eHh4LXNlY3JldC14eHg=
     * 其中App Key和App Secret可在开放平台上创建应用后获取。
     */
    public static String Base64(String keySecret) throws UnsupportedEncodingException {
        //App Key:App Secret
        log.debug("App Key:App Secret  " + keySecret);
        byte[] encodeBase64 = Base64.encodeBase64(keySecret.getBytes(HttpClientUtil.CHARSET));
        return "Basic " + new String(encodeBase64);
    }

    public static class LogUtils {

        public static void log(String msg, Logger log) {
            System.out.println(msg);
            log.info(msg);
        }

        public static void log(String msg, Exception e, Logger log) {
            System.out.println(msg + " 异常 message = [" + e.getMessage() + "]");
            log.info(msg + " 异常 message = [" + e.getMessage() + "]", e);
        }

        public static void error(String msg, Exception e, Logger log) {
            System.out.println(msg + " 异常 message = [" + e.getMessage() + "]");
            log.error(msg + " 异常 message = [" + e.getMessage() + "]", e);
        }

    }
    
    public static void main(String[] args){
//    	String postUrl = "http://120.77.176.182:3389/v1/transfer_out";
//    	Map<String, Object> map = new HashMap<String, Object>();
////    	map.put("contract", "eosio.token");
//    	map.put("contract", "testtesttest");
//    	map.put("to", "cochainworld");
//    	map.put("amount", "10.0000 CW");
//    	map.put("memo", "000012000002");
//    	map.put("request_id", "KDF987UWD777124");
//    	
//        String jsonString = null;
//		try {
//			jsonString = HttpClientUtil.sendPost(postUrl, map);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println(jsonString);
    	
//    	87405eb736ccf45d32517a64031b5069c30b3ef2ba8f693c46578c018e1bf8e1
    	
//        String postUrl2 = "http://120.77.176.182:3389/v1/get_transfer";
//        Map<String, Object> map2 = new HashMap<String, Object>();
//        map2.put("txid", "87405eb736ccf45d32517a64031b5069c30b3ef2ba8f693c46578c018e1bf8e5");
//        
//        String jsonString2 = null;
//		try {
//			jsonString2 = HttpClientUtil.sendPost(postUrl2, map2);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println(jsonString2);
//        Map<String, Object> respMap = JSONObject.parseObject(jsonString2, Map.class);
//        String status = (String) respMap.get("status");
//        if(StringUtils.isBlank(status)){
//        	status = String.valueOf(respMap.get("message"));
//        }
//        System.out.println(status);
    	
    	String getUrl = "http://120.77.176.182:3389/v1/account/exist?account=cochainworld";
    	Map<String, Object> map = new HashMap<String, Object>();
//    	map.put("contract", "eosio.token");
    	map.put("contract", "testtesttest");
    	map.put("to", "cochainworld");
    	map.put("amount", "10.0000 CW");
    	map.put("memo", "000012000002");
    	map.put("request_id", "KDF987UWD777124");
    	
        String jsonString = null;
		try {
			jsonString = HttpClientUtil.sendGet(getUrl, null,null,null,null,null);
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println(jsonString);
    	
//    	ExecutorService executorService = Executors.newFixedThreadPool(5);
//    	for(int i=0;i<10000;i++){
//    		final int j = i;
//    		executorService.execute(new Runnable(){
//    			public void run() {
//    				System.out.println(j);
//    			}
//    		});        		
//    	}
//    	executorService.shutdown();
//    	System.out.println("######################");
    }

}
