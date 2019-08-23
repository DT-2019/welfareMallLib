package com.fifthera.ecmall.utils;

import android.text.TextUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class NetworkRequestUtil {
    public HttpURLConnection getHttpURLConnection(String url) {
        try {
            URL url1 = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            return httpURLConnection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpURLConnection addHeader(HttpURLConnection connection, String key, String header) {
        connection.setRequestProperty(key, header);
        return connection;
    }

    public HttpURLConnection setRequestMethod(HttpURLConnection connection, String method) {
        try {
            connection.setRequestMethod(method);
            return connection;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String requestNetwork(HttpURLConnection connection) {
        String result = "";
        BufferedReader reader = null;
        connection.setReadTimeout(5000);

        try {
            if (connection.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                result = reader.readLine();
            } else if (connection.getResponseCode() == 302) {
                String location = connection.getHeaderField("Location");
                URL url = new URL(location);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    result = reader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    /**
     * 正常请求网络接口
     *
     * @param urlPath
     * @param Json
     * @return
     */
    public static String requestNetwork(String urlPath, String Json) { //方法要放在子线程中
        String result = "";
        BufferedReader reader = null;

        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("accept", "application/json");
            connection.setReadTimeout(5000);
            StringBuffer stringBuffer = new StringBuffer();
            if (Json != null && !TextUtils.isEmpty(Json)) {
                byte[] writebytes = Json.getBytes();
                connection.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(Json.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            if (connection.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str = null;
                while ((str = reader.readLine()) != null) {
                    stringBuffer.append(str).append("\n");
                }

                result = stringBuffer.toString();
            } else if (connection.getResponseCode() == 302) {
                String location = connection.getHeaderField("Location");

                url = new URL(location);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String str = null;
                    while ((str = reader.readLine()) != null) {
                        stringBuffer.append(str).append("\n");
                    }

                    result = stringBuffer.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 请求网络url接口
     *
     * @param urlPath
     * @return
     */
    public static String requestNetwork(String urlPath) {
        String result = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setRequestProperty("accept", "*/*");
            connection.connect();
            StringBuffer stringBuffer = new StringBuffer();
            if (connection.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str = null;
                while ((str = reader.readLine()) != null) {
                    stringBuffer.append(str).append("\n");
                }

                result = stringBuffer.toString();
            } else if (connection.getResponseCode() == 302) {
                String location = connection.getHeaderField("Location");

                url = new URL(location);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String str = null;
                    while ((str = reader.readLine()) != null) {
                        stringBuffer.append(str).append("\n");
                    }

                    result = stringBuffer.toString();
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
