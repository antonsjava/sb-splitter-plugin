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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author antons
 */
public class SBSplitter {

    private String filename;
    private String destDir; 
    private String sourceLibFolder; 
    private String destLibFolder; 
    private String destAppFolder; 
    
    private String cpFile; 
    private String cpScript; 
    private String cpClassesPrefix; 
    private String cpAppPrefix; 
    private String cpLibPrefix; 
    
    private String[] appModuleNames;
    private String[] appModulePackages;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getDestDir() { return destDir; }
    public void setDestDir(String destDir) { this.destDir = destDir; }
    public String getSourceLibFolder() { return sourceLibFolder; }
    public void setSourceLibFolder(String sourceLibFolder) { this.sourceLibFolder = sourceLibFolder; }
    public String getDestLibFolder() { return destLibFolder; }
    public void setDestLibFolder(String destLibFolder) { this.destLibFolder = destLibFolder; }
    public String getDestAppFolder() { return destAppFolder; }
    public void setDestAppFolder(String destAppFolder) { this.destAppFolder = destAppFolder; }
    public String getCpFile() { return cpFile; }
    public void setCpFile(String cpFile) { this.cpFile = cpFile; }
    public String getCpScript() { return cpScript; }
    public void setCpScript(String cpScript) { this.cpScript = cpScript; }
    public String getCpClassesPrefix() { return cpClassesPrefix; }
    public void setCpClassesPrefix(String cpClassesPrefix) { this.cpClassesPrefix = cpClassesPrefix; }
    public String getCpAppPrefix() { return cpAppPrefix; }
    public void setCpAppPrefix(String cpAppPrefix) { this.cpAppPrefix = cpAppPrefix; }
    public String getCpLibPrefix() { return cpLibPrefix; }
    public void setCpLibPrefix(String cpLibPrefix) { this.cpLibPrefix = cpLibPrefix; }
    public String[] getAppModuleNames() { return appModuleNames; }
    public void setAppModuleNames(String[] appModuleNames) { this.appModuleNames = appModuleNames; }
    public String[] getAppModulePackages() { return appModulePackages; }
    public void setAppModulePackages(String[] appModulePackages) { this.appModulePackages = appModulePackages; }


    
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
                    File newFile = newFile(dest, zipEntry);
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

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        String fullname = zipEntry.getName();
        File destFile = new File(destinationDir, fullname);
         
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
         
        if (!destFilePath.startsWith(destDirPath + File.separator)) { throw new IllegalArgumentException("Entry is outside of the target dir: " + zipEntry.getName()); }

        File p = destFile.getParentFile();
        if(!p.exists()) p.mkdirs();
         
        return destFile;
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

    private boolean isApplicationModule(String filename) {
        boolean rv = isApplicationModuleByName(filename);
        if(rv) return true;
        return isApplicationModuleByPackage(filename);
    }
    
    private boolean isApplicationModuleByName(String filename) {
        if(appModuleNames == null) return false;
        for(String appName : appModuleNames) { if(appName.equals(filename)) return true; }
        return false;
    }
    
    private boolean isApplicationModuleByPackage(String filename) {
        if(appModulePackages == null) return false;
        Set<String> packages = listPackages(filename);
        for(String appName : appModulePackages) { if(packages.contains(appName)) return true; }
        return false;
    }

    private Set<String> listPackages(String filename) {
        Set<String> set = new TreeSet<>();
        try {
            filename = destDir + sourceLibFolder + filename;
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(filename));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if(zipEntry.isDirectory()) {
                    String name = zipEntry.getName();
                    name = name.replace('/', '.');
                    if(name.startsWith(".")) name = name.substring(1);
                    if(name.endsWith(".")) name = name.substring(0, name.length()-1);
                    set.add(name);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }
        return set;
    }

    public void moveFile(String oldname, String newname) {
        if(oldname.equals(newname)) return;
        try {
            File f = new File(oldname);
            File f2 = new File(newname);
            File p = f2.getParentFile();
            if(!p.exists()) p.mkdir();
            FileInputStream is = new FileInputStream(f);
            FileOutputStream os = new FileOutputStream(f2);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) { os.write(buffer, 0, length); }
            os.flush();
            os.close();
            is.close();
            f.delete();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

    }

    public List<String> loadCP() {
        List<String> rv = new ArrayList<>();
        File f = new File(destDir + cpFile);
        if(!f.exists()) return rv;
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
        return rv;
    }
    
    public void generateCPScript(List<String> cp, List<String> appFiles, List<String> libFiles) {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n\n");
        sb.append("SBCP=").append(cpClassesPrefix).append("classes");
        for(String string : cp) {
            if(appFiles.contains(string)) {
                sb.append(":").append(cpAppPrefix).append(string);
                appFiles.remove(string);
            }
        }
        for(String string : appFiles) { sb.append(":XX").append(cpAppPrefix).append(string); }
        for(String string : cp) {
            if(libFiles.contains(string)) {
                sb.append(":").append(cpLibPrefix).append(string);
                libFiles.remove(string);
            }
        }
        for(String string : libFiles) { sb.append(":XX").append(cpLibPrefix).append(string); }
        sb.append("\n\nexport SBCP\n");
        try {
            File f = new File(destDir + cpScript);
            File p = f.getParentFile();
            if(!p.exists()) p.mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch(Exception e) { throw new IllegalArgumentException(e); }

    }

    public void split(Log log) {
        unzip();
        log.info("[SB split] unzipped");
        List<String> appFiles = new ArrayList<>();
        List<String> libFiles = new ArrayList<>();
        List<String> files = new ArrayList<>();
        listFiles(new File(destDir + sourceLibFolder), null, files);
        for(String name : files) {
            String oldname = destDir + sourceLibFolder + name;
            String newname = null;
            if(isApplicationModule(name)) {
                newname = destDir + destAppFolder + name;
                appFiles.add(name);
            } else {
                newname = destDir + destLibFolder + name;
                libFiles.add(name);
            }
            moveFile(oldname, newname);
        }
        log.info("[SB split] lib modules splitted. lib count: " + libFiles.size() + " app count: " + appFiles.size());
        List<String> cp = loadCP();
        generateCPScript(cp, appFiles, libFiles);
        log.info("[SB split] classpath script generated");
        updateModificationDate(new File(destDir + destLibFolder), 0);
        log.info("[SB split] lib modules freeze modification time");
    }

    public String config() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nsbFile: ").append(filename);
        sb.append("\ndestDir: ").append(destDir);
        sb.append("\nsourceLibFolder: ").append(sourceLibFolder);
        sb.append("\ndestLibFolder: ").append(destLibFolder);
        sb.append("\ndestAppFolder: ").append(destAppFolder);
        sb.append("\n");
        sb.append("\ncpFile: ").append(cpFile);
        sb.append("\ncpScript: ").append(cpScript);
        sb.append("\ncpClassesPrefix: ").append(cpClassesPrefix);
        sb.append("\ncpAppPrefix: ").append(cpAppPrefix);
        sb.append("\ncpLibPrefix: ").append(cpLibPrefix);
        if((appModuleNames != null) && (appModuleNames.length > 0)) {
            sb.append("\n");
            for(String appModuleName : appModuleNames) {
                sb.append("\nappModuleName: ").append(appModuleName);
            }
        }
        if((appModulePackages != null) && (appModulePackages.length > 0)) {
            sb.append("\n");
            for(String appModuleName : appModulePackages) {
                sb.append("\nappModulePackage: ").append(appModuleName);
            }
        }
    
        return sb.toString();
    }
    
    private void updateModificationDate(File folder, long time) {
        folder.setLastModified(time);
        File[] children = folder.listFiles();
        if(children == null) return;
        for(File file : children) {
            file.setLastModified(time);
            if(file.isDirectory()) {
                updateModificationDate(file, time);
            }
        }
    }
}
