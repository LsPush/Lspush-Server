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
package app.module.cloud;

import app.module.collect.WebPageUtil;
import app.utils.FileUtils;
import app.utils.URLUtils;
import com.google.common.hash.Hashing;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class CloudSpeedupService {
    private final Logger log = LoggerFactory.getLogger(CloudSpeedupService.class);
    public final String DIRECTORY;
    public final String DOWNLOAD_PATH;
    private static final int TIME_OUT = 150 * 1000;
    private static final ConcurrentHashMap<String, Future<String>> waitList = new ConcurrentHashMap<>(20);

    @Autowired
    public CloudSpeedupService(Environment env) {
        String serverUrl = env.getProperty("lspush.serverUrl");
        DOWNLOAD_PATH = serverUrl + "/api/cloudspeed/download/";
        DIRECTORY = env.getProperty("app.path.cloudspeed");
    }

    public String download(String address) throws InterruptedException, ExecutionException {
        address = URLUtils.smartUri(address);
        String filename = generateFilename(address);

        Future<String> task = waitList.get(filename);
        // 清理已完成任务
        if (task != null && task.isDone()) {
            waitList.remove(filename);
            task = null;
        }
        if (task == null) {
            FileUtils.ensurePath(DIRECTORY);
            File file = FileUtils.findFile(DIRECTORY, filename);
            if (file != null) {
                return filename;
            } else {
                // 清除已完成任务
                expireTasks();
                // 如果当前下载任务数超过20条
                if (waitList.size() >= 20) {
                    return null;
                } else {
                    task = download(address, filename);
                    waitList.put(filename, task);
                }
            }
        }
        while (true) {
            if (task.isDone()) {
                return task.get();
            }
            Thread.sleep(500);
        }
    }

    public void expireTasks() {
        List<String> removeList = new ArrayList<>(8);
        for (Map.Entry<String, Future<String>> entry : waitList.entrySet()) {
            Future<String> task = entry.getValue();
            if (task != null && task.isDone()) {
                removeList.add(entry.getKey());
            }
        }
        for (String key : removeList) {
            waitList.remove(key);
        }
    }

    @Async
    private Future<String> download(String address, String filename) {
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

            // ip host verify
            HostnameVerifier hv = (urlHostName, session) -> urlHostName.equals(session.getPeerHost());

            // set ip host verify
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", WebPageUtil.GOOGLE_USER_AGENT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);
            conn.connect();

            // get result
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String result = saveFile(filename, conn.getInputStream());
                return new AsyncResult<>(result);
            } else {
                log.error(conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (Exception e) {
            getLog().error("cloud speed download", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
        return new AsyncResult<>(null);
    }

    private String saveFile(String filename, InputStream in) {
        try {
            FileUtils.ensurePath(DIRECTORY);

            String filepath = Paths.get(DIRECTORY, filename).toString();
            File file = new File(filepath);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            FileCopyUtils.copy(in, out);

            String ext = detectFileExt(file);
            if (ext != null && ext.endsWith(".gif")) {
                saveFrame(file);
            }
            return filename;
        } catch (Exception e) {
            getLog().error("saveFile", e);
            return null;
        }
    }

    private String saveFrame(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            String newFileName = file.getName() + "_frame";
            String filepath = Paths.get(DIRECTORY, newFileName).toString();
            boolean frame = ImageIO.write(bufferedImage, "png", new File(filepath));
            if (frame) {
                return newFileName;
            }
        } catch (IOException ioe) {
            getLog().error("saveFrame", ioe);
        }
        return null;
    }

    private String generateFilename(String address) {
        String murData = Hashing.murmur3_128().hashString(address, StandardCharsets.UTF_8).toString();
        String sipData = Hashing.sipHash24().hashString(address, StandardCharsets.UTF_8).toString();
        return new StringBuilder().append(murData).append(sipData).toString();
    }

    private String detectFileExt(File file) {
        try {
            ContentInfoUtil util = new ContentInfoUtil();
            ContentInfo fileInfo = util.findMatch(file);
            String[] exts = fileInfo.getContentType().getFileExtensions();
            if (exts != null && exts.length > 0) {
                return "." + exts[0];
            }
        } catch (IOException e) {
            getLog().error("detectFileExt", e);
        }
        return null;
    }

    public Logger getLog() {
        return log != null ? log : LoggerFactory.getLogger("CloudSpeedupService");
    }
}
