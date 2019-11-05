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
    
	private static String initProperty(String value, String defaultValue, boolean endswithslash) {
		if(value == null) value = defaultValue;
		if(endswithslash && (!value.endsWith("/"))) value = value + "/"; 
        return value;
	}

	private void initProperties() {
        
        destDir = initProperty(destDir, "target/sb/", true);
        sourceLibFolder = initProperty(sourceLibFolder, "BOOT-INF/lib/", true);
        destLibFolder = initProperty(destLibFolder, "BOOT-INF/lib/", true);
        destAppFolder = initProperty(destAppFolder, "BOOT-INF/app/", true);
    
        cpFile = initProperty(cpFile, "BOOT-INF/classes/classpath.txt", false);
        cpScript = initProperty(cpScript, "BOOT-INF/classpath.sh", false);
        cpClassesPrefix = initProperty(cpClassesPrefix, "./", true);
        cpAppPrefix = initProperty(cpAppPrefix, "app/", true);
        cpLibPrefix = initProperty(cpLibPrefix, "lib/", true);
		
	}

    public void execute() throws MojoExecutionException {
        initProperties();
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
        printConf();
        getLog().info("splitting " + splitter.getFilename() + " to " + splitter.getDestDir());
        splitter.split();
        getLog().info("splitting done");
    }

    private void printConf() {
        getLog().info("sbFile: " + filename);
        getLog().info("destDir: " + destDir);
        getLog().info("sourceLibFolder: " + sourceLibFolder);
        getLog().info("destLibFolder: " + destLibFolder);
        getLog().info("destAppFolder: " + destAppFolder);
        getLog().info("");
        getLog().info("cpFile: " + cpFile);
        getLog().info("cpScript: " + cpScript);
        getLog().info("cpClassesPrefix: " + cpClassesPrefix);
        getLog().info("cpAppPrefix: " + cpAppPrefix);
        getLog().info("cpLibPrefix: " + cpLibPrefix);

        if((appModuleNames != null) && (appModuleNames.length > 0)) {
            getLog().info("");
            for(String appModuleName : appModuleNames) {
                getLog().info("appModuleName: " + appModuleName);
            }
        }
        if((appModulePackages != null) && (appModulePackages.length > 0)) {
            getLog().info("");
            for(String appModuleName : appModulePackages) {
                getLog().info("appModulePackage: " + appModuleName);
            }
        }
    }
}
