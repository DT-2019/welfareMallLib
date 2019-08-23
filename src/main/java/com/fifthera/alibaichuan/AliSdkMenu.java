package com.fifthera.alibaichuan;

import android.app.Activity;

import com.fifthera.ecmall.utils.MobCookieManager;

import org.apache.commons.ecodec.binary.Hex;
import org.apache.commons.ecodec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class AliSdkMenu {
    private static AliSdkMenu instance;
    private String _m_h5_tk = "";
    private Activity activity;

    public static AliSdkMenu getInstance(Activity activity) {
        if (instance == null) {
            instance = new AliSdkMenu(activity);
        }
        return instance;
    }

    public AliSdkMenu(Activity activity) {
        this.activity = activity;
        MobCookieManager.init(activity);
    }

    /**
     * 解析商品
     *
     * @param goodsId
     * @return
     */
    public String deCodeTBGoods(String goodsId) {
        String result = "";
        HttpURLConnection connection = null;
        String url2 = "http://api.m.taobao.com/h5/com.taobao.redbull.getpassworddetail/1.0?v=1.0&api=com.taobao.redbull.getpassworddetail&appKey=12574478&t=";
        try {
            URL url = new URL(url2 + System.currentTimeMillis());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            MobCookieManager.setCookies(url.getHost(), connection.getHeaderFields());
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> setCookies = headerFields.get("Set-Cookie");
            int resultCode = connection.getResponseCode();
            connection.disconnect();
            if (resultCode == HttpURLConnection.HTTP_OK) {
                for (String s : setCookies) {
                    String[] strings = s.split(";");
                    for (String subS : strings) {
                        if (subS.contains("_m_h5_tk=")) {
                            _m_h5_tk = subS.substring(9, subS.length());
                            break;
                        }
                    }
                }
                if (_m_h5_tk.contains("_")) {
                    _m_h5_tk = _m_h5_tk.substring(0, _m_h5_tk.indexOf("_"));
                }
                long currentTime = System.currentTimeMillis();

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("{\"id\":\"").append(goodsId).append("\",");
                stringBuffer.append("\"scm\":\"1007.14551.119137.0\",");
                stringBuffer.append("\"pvid\":\"358b9453-34b9-4a36-a009-59016840511c\",");
                stringBuffer.append("\"track_params\":\"{\\\"mtx_ab\\\":\\\"1\\\",\\\"mtx_sab\\\":\\\"3\\\"}\",");
                stringBuffer.append("\"spm\":\"a215s.7406091.guessitem.guessitem-0\",");
                stringBuffer.append("\"locate\":\"guessitem-item\",");
                stringBuffer.append("\"rmdChannelCode\":\"guessULike\",");
                stringBuffer.append("\"utparam\":\"{\\\"x_object_type\\\":\\\"item\\\",\\\"mtx_ab\\\":1,\\\"mtx_sab\\\":3,\\\"scm\\\":\\\"1007.14551.119137.0\\\",\\\"x_object_id\\\":").append(goodsId).append("}\",");
                stringBuffer.append("\"itemNumId\":\"").append(goodsId).append("\",");
                stringBuffer.append("\"exParams\":\"{\\\"id\\\":\\\"").append(goodsId).append("\\\",\\\"scm\\\":\\\"1007.14551.119137.0\\\",\\\"pvid\\\":\\\"358b9453-34b9-4a36-a009-59016840511c\\\",\\\"track_params\\\":\\\"{\\\\\\\"mtx_ab\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"mtx_sab\\\\\\\":\\\\\\\"3\\\\\\\"}\\\",\\\"spm\\\":\\\"a215s.7406091.guessitem.guessitem-0\\\",\\\"locate\\\":\\\"guessitem-item\\\",\\\"rmdChannelCode\\\":\\\"guessULike\\\",\\\"utparam\\\":\\\"{\\\\\\\"x_object_type\\\\\\\":\\\\\\\"item\\\\\\\",\\\\\\\"mtx_ab\\\\\\\":1,\\\\\\\"mtx_sab\\\\\\\":3,\\\\\\\"scm\\\\\\\":\\\\\\\"1007.14551.119137.0\\\\\\\",\\\\\\\"x_object_id\\\\\\\":").append(goodsId).append("}\\\"}\",");
                stringBuffer.append("\"detail_v\":\"8.0.0\",");
                stringBuffer.append("\"utdid\":\"1\"}");

                String data = stringBuffer.toString();
                String tempKey = _m_h5_tk + "&" + currentTime + "&12574478&" + data;
                String sign = new String(Hex.encodeHex(DigestUtils.md5(tempKey)));
                data = URLEncoder.encode(data, "UTF-8");
                String url1 = "https://h5api.m.taobao.com/h5/mtop.taobao.detail.getdetail/6.0/?jsv=2.5.1&appKey=12574478&t=" + currentTime +
                        "&api=mtop.taobao.detail.getdetail&v=6.0&isSec=0&ecode=0&AntiFlood=true&AntiCreep=true&H5Request=true&ttid=2018%40taobao_h5_9.9.9&type=jsonp&dataType=jsonp&callback=mtopjsonp1"
                        + "&sign=" + sign + "&data=" + data;

                URL newUrl = new URL(url1);
                HttpURLConnection connection2 = (HttpURLConnection) newUrl.openConnection();
                connection2.setRequestProperty("Cookie", MobCookieManager.getCookie(url.getHost()));
                connection2.setRequestMethod("GET");
                connection2.connect();
                int code = connection2.getResponseCode();
                if (code == 200) {
                    InputStream inputStream = connection2.getInputStream();
                    //创建一个BufferedReader，去读取结果流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String readLine;
                    StringBuffer buffer = new StringBuffer();
                    while ((readLine = reader.readLine()) != null) {
                        buffer.append(readLine);
                    }
                    //读取完结果流之后所得到的结果
                    String resultConn = buffer.toString();
                    reader.close();
                    inputStream.close();
                    connection2.disconnect();

                    return resultConn;
                }
            }

        } catch (Exception e) {

        }
        return result;
    }

}
