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
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author antons
 */
public class SBSplitter {

    private String filename;
    private String destDir;

    private String appFolder = "app/";
    private String classesFolder = "classes/";
    private String libsFolder = "libs/";
    private String snapshotsFolder = "snapshots/";
    private String loaderFolder = "loader/";
    private String metaFolder = "meta/";


    public static SBSplitter instance() { return new SBSplitter(); }
    public String filename() { return filename; }
    public SBSplitter filename(String value) { this.filename = value; return this; }
    public String destDir() { return destDir; }
    public SBSplitter destDir(String value) { this.destDir = value; return this; }
    public String appFolder() { return appFolder; }
    public SBSplitter appFolder(String value) { this.appFolder = value; return this; }
    public String classesFolder() { return classesFolder; }
    public SBSplitter classesFolder(String value) { this.classesFolder = value; return this; }
    public String libsFolder() { return libsFolder; }
    public SBSplitter libsFolder(String value) { this.libsFolder = value; return this; }
    public String snapshotsFolder() { return snapshotsFolder; }
    public SBSplitter snapshotsFolder(String value) { this.snapshotsFolder = value; return this; }
    public String loaderFolder() { return loaderFolder; }
    public SBSplitter loaderFolder(String value) { this.loaderFolder = value; return this; }
    public String metaFolder() { return metaFolder; }
    public SBSplitter metaFolder(String value) { this.metaFolder = value; return this; }


    private void unzip() {
        try {
            File dest = new File(destDir);
            if(!dest.exists()) dest.mkdirs();
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(filename));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if(zipEntry.isDirectory()) {
                } else {
                    String name = zipEntry.getName();
                    File newFile = newFile(name);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) { fos.write(buffer, 0, len); }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }
    }

    private File newFile(String name) throws IOException {
        String fullname = destDir + appFolder;
        String simpleName = fullname;
        if(name.startsWith("BOOT-INF/lib/") && name.contains("-SNAPSHOT")) {
            fullname = fullname + snapshotsFolder + name.substring(13);
        } else if(name.startsWith("BOOT-INF/lib/")) {
            fullname = fullname + libsFolder + name.substring(13);
        } else if(name.startsWith("BOOT-INF/classes/")) {
            fullname = fullname + classesFolder + name.substring(17);
        } else if(name.startsWith("org/springframework")) {
            fullname = fullname + loaderFolder + name;
        } else {
            fullname = fullname + metaFolder + name;
        }


        File destFile = new File(fullname);

        String destFilePath = destFile.getCanonicalPath();

        File p = destFile.getParentFile();
        if(!p.exists()) p.mkdirs();

        return destFile;
    }



    public void split() {
        unzip();
    }

    public String config() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nsbFile: ").append(filename);
        sb.append("\ndestDir: ").append(destDir);
        sb.append("\nappFolder: ").append(appFolder);
        sb.append("\nlibsFolder: ").append(libsFolder);
        sb.append("\nsnapshotsFolder: ").append(snapshotsFolder);
        sb.append("\nloaderFolder: ").append(loaderFolder);
        sb.append("\nmetaFolder: ").append(metaFolder);
        return sb.toString();
    }


}
