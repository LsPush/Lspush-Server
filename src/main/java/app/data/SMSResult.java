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
package app.data;

public class SMSResult {
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == 200;
    }

    public String getResult() {
        switch (status) {
            case 200:
                return "验证成功";
            case 512:
                return "服务器拒绝访问，或者拒绝操作";
            case 513:
                return "求Appkey不存在或被禁用。";
            case 514:
                return "权限不足";
            case 515:
                return "服务器内部错误";
            case 517:
                return "缺少必要的请求参数";
            case 518:
                return "请求中用户的手机号格式不正确（包括手机的区号）";
            case 519:
                return "请求发送验证码次数超出限制";
            case 520:
                return "无效验证码。";
            case 526:
                return "余额不足";
            default:
                return "其他情况，错误码：" + status;
        }
    }
}
