/*
 * Copyright 2019 Anton Straka
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
package sk.antons.sbsplitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import static jdk.vm.ci.sparc.SPARC.f2;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author antons
 */
public class Copy {

    private String from;    
    private String to;

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public void copy(Log log) {
        log.info("[SB split] copy from " + from);
        File fromFile = new File(from);
        if(!fromFile.exists()) {
            log.info("[SB split] copy from not exists");
            return;
        }
        log.info("[SB split] copy to " + to);
        File toFile = new File(to);
        int count = 0;
        if(fromFile.isDirectory()) {
            count = copyFolder(log, fromFile, toFile);
        } else {
            if(toFile.exists() && toFile.isDirectory()) {
                log.error("[SB split] copy file must target another file");
                throw new IllegalArgumentException("copy to " + to + " must be a file");
            }
            copyFile(log, fromFile, toFile);
            count = 1;
        }
        log.info("[SB split] copy file count: " + count);
    }
    
    private void copyFile(Log log, File fromFile, File toFile) {
        File parent = toFile.getParentFile();
        if(!parent.exists()) parent.mkdirs();
        try {
            FileInputStream is = new FileInputStream(fromFile);
            FileOutputStream os = new FileOutputStream(toFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) { os.write(buffer, 0, length); }
            os.flush();
            os.close();
            is.close();
            toFile.setLastModified(fromFile.lastModified());
        } catch(Exception e) {
            log.error("[SB split] unable to copy " + fromFile + " " + toFile);
            throw new IllegalArgumentException(e);
        }
    }
    
    private int copyFolder(Log log, File fromFile, File toFile) {
        if(!toFile.exists()) toFile.mkdirs();
        File[] children = fromFile.listFiles();
        if(children == null) return 0;
        int count = 0;
        for(File file : children) {
            if(file.isDirectory()) {
                count = count + copyFolder(log, file, new File(toFile.getAbsolutePath() + "/" + file.getName()));
            } else {
                copyFile(log, file, new File(toFile.getAbsolutePath() + "/" + file.getName()));
                count++;
            }
        }
        return count;
    }
}
