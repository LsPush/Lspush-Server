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
package app.module.collect;

import app.config.ErrorCode;
import app.data.Collect;
import app.data.Response;
import app.utils.URLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fetch")
public class FetchController {
    private final FetchService fetchService;

    @Autowired
    public FetchController(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    /**
     * Note: <delete>服务器端在接受url信息时，如果url中含有#，读取的时候#会被忽略，所以需要将#替换为%23
     * 除了 stackoverflow 其他网站都是不会存在问题的。</delete>
     * http://www.jb51.net/article/36838.htm
     */
    @GetMapping("/getUrlInfo")
    public Response<Collect> getUrlInfo(@RequestParam(value = "url") String url) {
        if (!URLUtils.isNetworkUrlStrict(url)) {
            return ErrorCode.error(ErrorCode.INVALID_URL);
        }
        return Response.create(fetchService.getUrlInfo(url));
    }
}
