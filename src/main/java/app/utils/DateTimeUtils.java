/*
 * Copyright 2016 TomeOkin
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

import org.joda.time.DateTime;

public class DateTimeUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private DateTimeUtils() {}

    public static String toDateTime(DateTime dateTime) {
        return dateTime.toString(DATE_TIME_FORMAT);
    }

    public static String toDate(DateTime dateTime) {
        // dateTime.toLocalDate().toString();
        return dateTime.toString(DATE_FORMAT);
    }
}
