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


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo( name = "split", defaultPhase = LifecyclePhase.PACKAGE )
public class SBSplitterMojo extends AbstractMojo {
    
    @Parameter(property = "sbFile", defaultValue = "target/${project.build.finalName}.jar", required = false )
    private String filename;
    @Parameter(property = "destDir", defaultValue = "target/sb/", required = false )
    private String destDir; 
    @Parameter(property = "sourceLibFolder", defaultValue = "BOOT-INF/lib/", required = false )
    private String sourceLibFolder; 
    @Parameter(property = "destLibFolder", defaultValue = "BOOT-INF/lib/", required = false )
    private String destLibFolder; 
    @Parameter(property = "destAppFolder", defaultValue = "BOOT-INF/app/", required = false )
    private String destAppFolder; 
    
    @Parameter(property = "cpFile", defaultValue = "BOOT-INF/classes/classpath.txt", required = false )
    private String cpFile; 
    @Parameter(property = "cpScript", defaultValue = "BOOT-INF/classpath.sh", required = false )
    private String cpScript; 
    @Parameter(property = "cpClassesPrefix", defaultValue = "./", required = false )
    private String cpClassesPrefix; 
    @Parameter(property = "cpAppPrefix", defaultValue = "app/", required = false )
    private String cpAppPrefix; 
    @Parameter(property = "cpLibPrefix", defaultValue = "lib/", required = false )
    private String cpLibPrefix; 
    
    @Parameter(property = "appModuleNames", required = false )
    private String[] appModuleNames = new String[]{"activation-1.1.1.jar"};
    @Parameter(property = "appModulePackages", required = false )
    private String[] appModulePackages = new String[]{"camp.xit"};
    

    public void execute() throws MojoExecutionException {
        SBSplitter splitter = new SBSplitter();
        splitter.setAppModuleNames(appModuleNames);
        splitter.setAppModulePackages(appModulePackages);
        splitter.setCpAppPrefix(cpAppPrefix);
        splitter.setCpClassesPrefix(cpClassesPrefix);
        splitter.setCpFile(cpFile);
        splitter.setCpLibPrefix(cpLibPrefix);
        splitter.setCpScript(cpScript);
        splitter.setDestAppFolder(destAppFolder);
        splitter.setDestDir(destDir);
        splitter.setDestLibFolder(destLibFolder);
        splitter.setFilename(filename);
        splitter.setSourceLibFolder(sourceLibFolder);
        getLog().info("configuration" + splitter.config());
        getLog().info("splitting " + splitter.getFilename() + " to " + splitter.getDestDir());
        splitter.split();
        getLog().info("splitting done");
    }
}
