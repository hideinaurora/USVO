package org.example.utils;

import com.alibaba.fastjson2.JSONObject;
import org.example.config.exception.CommonJsonException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;

@Service
public class WxUtils {

    @Resource
    private RedisUtil redisUtil;
    @Value("${wechat.miniapp.appid}")
    private String appId;
    @Value("${wechat.miniapp.secret}")
    private String appSecret;

    private String handleErrorCode(Long errCode) {
        switch (errCode.intValue()) {
            case 40029:
                return "code无效";
            case 45011:
                return "频率限制";
            case 40226:
                return "高风险等级用户";
            default:
                return "微信接口请求失败";
        }
    }

    public String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" + appId + "&secret=" + appSecret;
        JSONObject result = httpsRequest(url, "GET", null);
        if (result.containsKey("access_token")) {
            return result.getString("access_token");
        } else if (result.containsKey("errcode")) {
            throw new CommonJsonException(handleErrorCode(result.getLong("errcode")));
        } else {
            throw new CommonJsonException("微信接口请求失败");
        }
    }

    public String queryMiniOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + appId + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        JSONObject result = httpsRequest(url, "GET", null);
        if (result.containsKey("openid")) {
            return result.getString("openid");
        } else if (result.containsKey("errcode")) {
            throw new CommonJsonException(handleErrorCode(result.getLong("errcode")));
        } else {
            throw new CommonJsonException("微信接口请求失败");
        }
    }


    public static JSONObject httpsRequest(String requestUrl,
                                          String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们制定的信任管理器初始化
            TrustManager[] ttm = {new MyX509TrustManager()};
            SSLContext sslContent = SSLContext.getInstance("SSL", "SunJSSE");
            sslContent.init(null, ttm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContent.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpsUrlConn = (HttpsURLConnection) url
                    .openConnection();
            httpsUrlConn.setSSLSocketFactory(ssf);

            httpsUrlConn.setDoOutput(true);
            httpsUrlConn.setDoInput(true);
            httpsUrlConn.setUseCaches(false);
            // 设置请求方式(GET/POST)
            httpsUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpsUrlConn.connect();
            }

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpsUrlConn.getOutputStream();
                // 编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpsUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            httpsUrlConn.disconnect();
            System.out.println(buffer.toString());

            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException e) {
            e.printStackTrace();
            System.out.println("weixin server connection timed out");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
