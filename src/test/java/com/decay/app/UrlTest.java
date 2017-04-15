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
package com.decay.app;

import app.utils.URLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UrlTest {

    @Test
    public void isValid() {
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("https://www.baidu.com"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0309/2567.html"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("https://github.com/wangwang4git/just-do/blob/197bc5ad559343643f04fb86708925fdbe1d3300/AndroidLeak/%E6%89%8BQ%E4%B8%AD%E7%9A%84%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E6%A3%80%E6%B5%8B%E6%A8%A1%E5%9D%97.md"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0309/2567.html"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://101.201.65.221/api/resource/download/jm.png"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://101.201.65.221"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://z.cn"));
        Assert.assertTrue(URLUtils.isNetworkUrlStrict("http://stackoverflow.com/questions/33139849/retrofit-2-0-post-method-with-body-is-string?answertab=votes"));
    }
}
