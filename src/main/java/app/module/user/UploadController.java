package app.module.user;

import app.data.Response;
import app.utils.FileUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/resource")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private final String DIRECTORY;
    private final String DOWNLOAD_PATH;

    @Autowired
    public UploadController(Environment env) {
        String serverUrl = env.getProperty("lspush.serverUrl");
        DOWNLOAD_PATH = serverUrl + "/api/resource/download/";
        DIRECTORY = env.getProperty("app.path.upload");
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/upload")
    public Response<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String path = "";
        try {
            String filename = file.getOriginalFilename();
            FileUtils.ensurePath(DIRECTORY);
            String newFileName = FileUtils.generateImageFilename(filename);
            String filepath = Paths.get(DIRECTORY, newFileName).toString();

            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
            FileCopyUtils.copy(file.getInputStream(), out);
            path = DOWNLOAD_PATH + newFileName;
        } catch (Exception e) {
            log.error("upload", e);
            return Response.create(path);
        }

        return Response.create(path);
    }

    @GetMapping("/download/{filename:.*}")
    public void download(HttpServletResponse response, @PathVariable("filename") String filename)
        throws IOException {
        String filepath = Paths.get(DIRECTORY, filename).toString();
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        ContentInfoUtil util = new ContentInfoUtil();
        ContentInfo fileInfo = util.findMatch(file);
        response.setContentType(fileInfo.getMimeType());
        response.setContentLength((int) file.length());
        // add it will trigger download and specify a filename
        //response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        FileCopyUtils.copy(new BufferedInputStream(new FileInputStream(file)), out);
    }
}