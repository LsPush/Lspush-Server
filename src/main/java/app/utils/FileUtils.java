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

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileUtils {
    private static String[] postfix = new String[]{
        ".jpg", ".jpeg", ".gif", ".svg", ".png", ".bmp", ".webp"
    };

    static {
        Arrays.sort(postfix);
    }

    private FileUtils() {}

    public static void ensurePath(String filepath) {
        File path = new File(filepath);
        if (!path.exists() || !path.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }
    }

    public static File findFile(String path, String filename) {
        String filepath = Paths.get(path, filename).toString();
        File file = new File(filepath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    public static String generateImageFilename(String old) {
        int equalIndex = old.lastIndexOf('=');
        if (equalIndex >= 0) {
            if (equalIndex == old.length() - 1) {
                old = generateFilename();
            } else {
                old = old.substring(equalIndex + 1);
            }
        }

        String prefix, filename = DateTime.now().toString("HHmmssSSS");
        int start = old.lastIndexOf('.');
        if (start != -1) {
            prefix = old.substring(start);
            String fi = old.substring(0, start);
            if (Arrays.binarySearch(postfix, prefix.toLowerCase()) < 0) {
                fi = generateFilename();
                prefix = ".jpg";
            }
            filename = String.format("%s_%s%s", fi, filename, prefix);
        } else {
            filename = old + "_" + filename + ".jpg";
        }
        return filename;
    }

    public static String generateFilename() {
        return RandomStringUtils.random(16, true, true);
    }
}
