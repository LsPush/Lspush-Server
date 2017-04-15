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
package app.utils;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.web.util.UriComponentsBuilder;

public class URLUtils {
    /**
     * @return True iff the url is an http: url.
     */
    public static boolean isHttpUrl(String url) {
        return (null != url) &&
            (url.length() > 6) &&
            url.substring(0, 7).equalsIgnoreCase("http://");
    }

    /**
     * @return True iff the url is an https: url.
     */
    public static boolean isHttpsUrl(String url) {
        return (null != url) &&
            (url.length() > 7) &&
            url.substring(0, 8).equalsIgnoreCase("https://");
    }

    /**
     * @return True iff the url is a network url.
     */
    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }

    public static boolean isNetworkUrlStrict(String url) {
        return isNetworkUrl(url) && UrlValidator.getInstance().isValid(url);
    }

    /**
     * 服务器端不应该去兼容不合适的网址
     */
    @Deprecated
    public static String fixUrl(String url) {
        if (url != null && !url.startsWith("http")) {
            return url + "http://";
        }
        return url;
    }

    public static String smartUri(String old) {
        return UriComponentsBuilder.fromUriString(old)
            .replaceQueryParam("utm_source")
            .replaceQueryParam("utm_medium")
            .replaceQueryParam("utm_campaign")
            .replaceQueryParam("utm_term")
            .replaceQueryParam("utm_content")
            .replaceQueryParam("hmsr")
            .replaceQueryParam("comefrom")
            .build()
            .toUriString();
    }
}
