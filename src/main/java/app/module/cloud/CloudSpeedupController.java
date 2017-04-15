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

import app.utils.FileUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/api/cloudspeed")
public class CloudSpeedupController {
    public final String DIRECTORY;
    public final String DOWNLOAD_PATH;

    private final Logger log = LoggerFactory.getLogger("CloudSpeedupController");
    private final CloudSpeedupService cloudSpeedupService;

    @Autowired
    public CloudSpeedupController(CloudSpeedupService cloudSpeedupService, Environment env) {
        this.cloudSpeedupService = cloudSpeedupService;
        String serverUrl = env.getProperty("lspush.serverUrl");
        DOWNLOAD_PATH = serverUrl + "/api/cloudspeed/download/";
        DIRECTORY = env.getProperty("app.path.cloudspeed");
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, @RequestParam("fileUrl") String fileUrl,
        @RequestParam(value = "frame", defaultValue = "false", required = false) boolean frame) {

        try {
            String filename = cloudSpeedupService.download(fileUrl);
            File file = null;
            if (filename != null && filename.length() > 0) {
                if (frame) {
                    String frameName = filename + "_frame";
                    file = FileUtils.findFile(DIRECTORY, frameName);
                }
                if (file == null) {
                    file = FileUtils.findFile(DIRECTORY, filename);
                }
            }
            if (file == null) {
                log.info("request file is null, filename = {}", filename);
                // 对于服务器端下载失败的情况自动重定向到原始地址
                response.sendRedirect(fileUrl);
                //response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ContentInfoUtil util = new ContentInfoUtil();
            ContentInfo fileInfo = util.findMatch(file);

            response.setContentType(fileInfo.getMimeType());
            response.setContentLength((int) file.length());
            // add it will trigger download and specify a filename
            //response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            FileCopyUtils.copy(new BufferedInputStream(new FileInputStream(file)), out);
        } catch (Throwable t) {
            log.error("CloudSpeedupController.download", t);
            try {
                response.sendRedirect(fileUrl);
            } catch (IOException e) {
                log.error("CloudSpeedupController.download -> sendRedirect", t);
            }
        }
    }
}
