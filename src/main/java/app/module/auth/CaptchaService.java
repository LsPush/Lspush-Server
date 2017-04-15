/*
 * Copyright 2017 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.module.auth;

import app.config.LsPushProperties;
import app.data.PhoneData;
import app.data.SMSResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);
    private static final String CHECK_SMS = "https://webapi.sms.mob.com/sms/verify";

    private final LsPushProperties lsPushProperties;
    private final ObjectMapper jsonUtils;
    private static final int TIME_OUT = 10 * 1000;
    private ConcurrentHashMap<String, PhoneData> tickets = new ConcurrentHashMap<>(100);

    @Autowired
    public CaptchaService(LsPushProperties lsPushProperties, ObjectMapper jsonUtils) {
        this.lsPushProperties = lsPushProperties;
        this.jsonUtils = jsonUtils;
    }

    public String checkCaptcha(@Nonnull PhoneData phoneData) {
        SMSResult result = connCheckCaptcha(phoneData.getPhone(), phoneData.getCountryCode(), phoneData.getCaptcha());
        if (result != null && result.isSuccess()) {
            String ticket = RandomStringUtils.random(24, true, true);
            tickets.put(ticket, phoneData);
            return ticket;
        } else {
            log.info("check captcha failed: {}", phoneData.toString());
            if (result != null) {
                log.info(result.toString());
            }
            return null;
        }
    }

    public PhoneData queryTicket(String ticket) {
        return tickets.get(ticket);
    }

    public void removeTicket(String ticket) {
        tickets.remove(ticket);
    }

    private SMSResult connCheckCaptcha(String phone, String countryCode, String captcha) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("appkey=").append(lsPushProperties.getSmsKey());
            builder.append("&phone=").append(phone);
            builder.append("&zone=").append(countryCode);
            builder.append("&code=").append(captcha);

            String data = requestData(CHECK_SMS, builder.toString());
            return jsonUtils.readValue(data, SMSResult.class);
        } catch (Throwable cause) {
            log.error("checkCaptcha failed", cause);
        }
        return null;
    }

    public String requestData(String address, String params) {
        HttpURLConnection conn = null;
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}

                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }};

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());

            //ip host verify
            HostnameVerifier hv = (urlHostName, session) -> urlHostName.equals(session.getPeerHost());

            //set ip host verify
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);
            // set params ;post params
            if (params != null) {
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(params.getBytes(Charset.forName("UTF-8")));
                out.flush();
                out.close();
            }
            conn.connect();
            //get result
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return parseResult(conn.getInputStream());
            } else {
                System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) { conn.disconnect(); }
        }
        return null;
    }

    private String parseResult(InputStream inputStream) throws IOException {
        String result = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        in.close();
        return result;
    }
}
