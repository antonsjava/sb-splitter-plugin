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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author antons
 */
public class SBEnhancer {

    private MavenProject project;

    private String filename;
    private String destDir;

    private String appFolder = "app/";
    private String classesFolder = "classes/";
    private String libsFolder = "libs/";
    private String snapshotsFolder = "snapshots/";
    private String loaderFolder = "loader/";
    private String metaFolder = "meta/";


    public static SBEnhancer instance() { return new SBEnhancer(); }
    public MavenProject project() { return project; }
    public SBEnhancer project(MavenProject value) { this.project = value; return this; }
    public String filename() { return filename; }
    public SBEnhancer filename(String value) { this.filename = value; return this; }
    public String destDir() { return destDir; }
    public SBEnhancer destDir(String value) { this.destDir = value; return this; }
    public String appFolder() { return appFolder; }
    public SBEnhancer appFolder(String value) { this.appFolder = value; return this; }
    public String classesFolder() { return classesFolder; }
    public SBEnhancer classesFolder(String value) { this.classesFolder = value; return this; }
    public String libsFolder() { return libsFolder; }
    public SBEnhancer libsFolder(String value) { this.libsFolder = value; return this; }
    public String snapshotsFolder() { return snapshotsFolder; }
    public SBEnhancer snapshotsFolder(String value) { this.snapshotsFolder = value; return this; }
    public String loaderFolder() { return loaderFolder; }
    public SBEnhancer loaderFolder(String value) { this.loaderFolder = value; return this; }
    public String metaFolder() { return metaFolder; }
    public SBEnhancer metaFolder(String value) { this.metaFolder = value; return this; }

    public void enhance(Log log) {
        List<String> cp = loadCP();
        generateCPScript(cp);
        log.info("[SB split] classpath script generated");
        generateStartScript();
        log.info("[SB split] dummy start script generated");
        generateDockerfile1();
        generateDockerfile2();
        log.info("[SB split] dummy docker file generated");
        updateModificationDate(new File(destDir + appFolder + libsFolder), 0);
        log.info("[SB split] lib modules freeze modification time");
    }



    private void listFiles(File folder, String prefix, List<String> list) {
        File[] children = folder.listFiles();
        if(children == null) return;
        for(File file : children) {
            String neprefix = file.getName();
            if(prefix != null) neprefix = prefix + "/" + neprefix;
            if(file.isDirectory()) {
                listFiles(file, neprefix, list);
            } else { list.add(neprefix); }
        }
    }



    public List<String> loadCP() {
        List<String> rv = new ArrayList<>();
        File f = new File(destDir + appFolder + metaFolder + "/BOOT-INF/classpath.txt");
        if(f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                String line = reader.readLine();
                if(line != null) {
                    String[] data = line.split(":");
                    for(String string : data) {
                        if(string == null) continue;
                        string = string.trim();
                        if("".equals(string)) continue;
                        File ff = new File(string);
                        rv.add(ff.getName());
                    }
                }
            } catch(Exception e) { throw new IllegalArgumentException(e); }
        } else {
            try {
                List<String> items = project.getRuntimeClasspathElements();
                for(String string : items) {
                    if(string == null) continue;
                    string = string.trim();
                    if("".equals(string)) continue;
                    if(string.endsWith("classes")) continue;
                    File ff = new File(string);
                    rv.add(ff.getName());
                }
            } catch(DependencyResolutionRequiredException ex) {
            }
        }
        return rv;
    }

    private static String removeSlash(String value) {
        if(value.endsWith("/")) value = value.substring(0, value.length()-1);
        return value;
    }

    public void generateDockerfile1() {
        String startclass = "Application";
        File meta = new File(destDir + appFolder + metaFolder + "/META-INF/MANIFEST.MF");
        if(meta.exists()) {
            try(FileInputStream fis = new FileInputStream(meta);) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                String line = reader.readLine();
                while(line != null) {
                    if(line.startsWith("Start-Class: ")) {
                        startclass = line.substring(13);
                        break;
                    }
                    line = reader.readLine();
                }
            } catch(Exception e) { throw new IllegalArgumentException(e); }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("FROM eclipse-temurin:21-jdk-alpine\n");
        sb.append("\n");
        sb.append("COPY ./").append(appFolder).append(libsFolder).append(" /app/").append(libsFolder).append("\n");
        sb.append("COPY ./").append(appFolder).append(loaderFolder).append(" /app/").append(loaderFolder).append("\n");
        sb.append("COPY ./").append(appFolder).append(snapshotsFolder).append(" /app/").append(snapshotsFolder).append("\n");
        sb.append("COPY ./").append(appFolder).append(metaFolder).append(" /app/").append(metaFolder).append("\n");
        sb.append("COPY ./").append(appFolder).append(classesFolder).append(" /app/").append(classesFolder).append("\n");
        sb.append("COPY ./").append(appFolder).append("cp.arg").append(" /app/").append("cp.arg").append("\n");
        sb.append("\n");
        sb.append("WORKDIR /app\n");
        sb.append("\n");
        sb.append("ENTRYPOINT [\"java\", \"-Djava.security.egd=file:/dev/./urandom\", \"@cp.arg\", \"").append(startclass).append("\"]\n");

        try {
            File f = new File(destDir + "/Dockerfile.template1");
            File p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

    }

    public void generateDockerfile2() {
        String startclass = "Application";
        File meta = new File(destDir + appFolder + metaFolder + "/META-INF/MANIFEST.MF");
        if(meta.exists()) {
            try(FileInputStream fis = new FileInputStream(meta);) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                String line = reader.readLine();
                while(line != null) {
                    if(line.startsWith("Start-Class: ")) {
                        startclass = line.substring(13);
                        break;
                    }
                    line = reader.readLine();
                }
            } catch(Exception e) { throw new IllegalArgumentException(e); }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("FROM eclipse-temurin:21-jdk-alpine\n");
        sb.append("\n");
        sb.append("COPY ./").append(appFolder).append(libsFolder).append(" /app/BOOT-INF/lib/").append("\n");
        sb.append("COPY ./").append(appFolder).append(loaderFolder).append(" /app/").append("\n");
        sb.append("COPY ./").append(appFolder).append(snapshotsFolder).append(" /app/BOOT-INF/lib/").append("\n");
        sb.append("COPY ./").append(appFolder).append(metaFolder).append(" /app/").append("\n");
        sb.append("COPY ./").append(appFolder).append(classesFolder).append(" /app/BOOT-INF/classes/").append("\n");
        sb.append("\n");
        sb.append("WORKDIR /app\n");
        sb.append("\n");
        sb.append("ENTRYPOINT [\"java\", \"-Djava.security.egd=file:/dev/./urandom\", \"org.springframework.boot.loader.launch.JarLauncher\"]\n");

        try {
            File f = new File(destDir + "/Dockerfile");
            File p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
            f = new File(destDir + "/Dockerfile.template2");
            p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

    }

    public void generateStartScript() {
        StringBuilder sb2 = new StringBuilder();

        String startclass = "Application";
        File meta = new File(destDir + appFolder + metaFolder + "/META-INF/MANIFEST.MF");
        if(meta.exists()) {
            try(FileInputStream fis = new FileInputStream(meta);) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                String line = reader.readLine();
                while(line != null) {
                    if(line.startsWith("Start-Class: ")) {
                        startclass = line.substring(13);
                        break;
                    }
                    line = reader.readLine();
                }
            } catch(Exception e) { throw new IllegalArgumentException(e); }
        }
        sb2.append("#!/bin/bash\n");
        sb2.append("java @cp.arg ").append(startclass);

        try {
            File f = new File(destDir + appFolder + "/start.sh");
            File p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb2.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

    }

    public void generateCPScript(List<String> cp) {
        StringBuilder sb2 = new StringBuilder();



        sb2.append("-cp \"./").append(removeSlash(classesFolder)).append("\\\n");

        for(String string : cp) {

            String name = string;
            int pos = name.lastIndexOf("/");
            if(pos > -1) name = name.substring(pos+1);
            if(name.contains("-SNAPSHOT")) {
                sb2.append(":./").append(snapshotsFolder).append(name).append("\\\n");
            } else {
                sb2.append(":./").append(libsFolder).append(name).append("\\\n");
            }
        }
        sb2.append(":./").append(loaderFolder).append("\\\n");
        sb2.append(":./").append(metaFolder).append("\\\n");

        sb2.append("\"");


        try {
            File f = new File(destDir + appFolder + "/cp.arg");
            File p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb2.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

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

    private void updateModificationDate(File folder, long time) {
        folder.setLastModified(time);
        File[] children = folder.listFiles();
        if(children == null) return;
        for(File file : children) {
            file.setLastModified(time);
            if(file.isDirectory()) { updateModificationDate(file, time); }
        }
    }
}
