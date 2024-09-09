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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;


@Mojo( name = "split", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class SBSplitterMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "dockerFile", defaultValue = "src/main/docker/Dockerfile", required = false )
    private String dockerFile;
    @Parameter(property = "sbFile", defaultValue = "target/${project.build.finalName}.jar", required = false )
    private String filename;
    @Parameter(property = "destDir", defaultValue = "target/sb/", required = false )
    private String destDir;

    @Parameter(property = "appDir", defaultValue = "app/", required = false )
    private String appFolder;
    @Parameter(property = "libsDir", defaultValue = "libs/", required = false )
    private String libsFolder;
    @Parameter(property = "snapshotsDir", defaultValue = "snapshots/", required = false )
    private String snapshotsFolder;
    @Parameter(property = "loaderDir", defaultValue = "loader/", required = false )
    private String loaderFolder;
    @Parameter(property = "metaDir", defaultValue = "meta/", required = false )
    private String metaFolder;


    @Parameter(property = "copies", required = false )
    private Copy[] copies = null;

	private static String initProperty(String value, String defaultValue, boolean endswithslash) {
		if(value == null) value = defaultValue;
		if(endswithslash && (!value.endsWith("/"))) value = value + "/";
        return value;
	}

	private void initProperties() {

        destDir = initProperty(destDir, "target/sb/", true);
        appFolder = initProperty(appFolder, "app/", true);
        libsFolder = initProperty(libsFolder, "libs/", true);
        snapshotsFolder = initProperty(snapshotsFolder, "snapshots/", true);
        loaderFolder = initProperty(loaderFolder, "loader/", true);
        metaFolder = initProperty(metaFolder, "meta/", true);

	}

    public void execute() throws MojoExecutionException {
        initProperties();
        SBSplitter splitter = SBSplitter.instance()
            .filename(filename)
            .destDir(destDir)
            .appFolder(appFolder)
            .libsFolder(libsFolder)
            .snapshotsFolder(snapshotsFolder)
            .loaderFolder(loaderFolder)
            .metaFolder(metaFolder);
        printConf();
        getLog().info("[SB split] splitting " + splitter.filename()+ " to " + splitter.destDir());
        splitter.split();
        getLog().info("[SB split] splitting done");
        SBEnhancer enhancer = SBEnhancer.instance()
            .project(project)
            .filename(filename)
            .destDir(destDir)
            .appFolder(appFolder)
            .libsFolder(libsFolder)
            .snapshotsFolder(snapshotsFolder)
            .loaderFolder(loaderFolder)
            .metaFolder(metaFolder);
        enhancer.enhance(getLog());
        getLog().info("[SB split] enhancing done");
        if(copies != null) {
            for(Copy copy : copies) {
                getLog().info("");
                copy.copy(getLog());
            }
        }

        try {
            File f = new File(dockerFile);
            if(f.exists()) Files.copy(f.toPath(), new File(destDir + "/Dockerfile").toPath(), StandardCopyOption.REPLACE_EXISTING);
            getLog().info("[SB split] docker file replaced");
        } catch(Exception e) {
            throw new IllegalStateException("unable to copy Dockerfile " + dockerFile, e);
        }


    }

    private void printConf() {
        getLog().info("[SB split] conf sbFile: " + filename);
        getLog().info("[SB split] conf destDir: " + destDir);
        getLog().info("");
        getLog().info("[SB split] conf appFolder: " + appFolder);
        getLog().info("[SB split] conf libsFolder: " + libsFolder);
        getLog().info("[SB split] conf snapshotsFolder: " + snapshotsFolder);
        getLog().info("[SB split] conf loaderFolder: " + loaderFolder);
        getLog().info("[SB split] conf metaFolder: " + metaFolder);
        getLog().info("");

        if((copies != null) && (copies.length > 0)) {
            getLog().info("");
            for(Copy copy : copies) {
                getLog().info("[SB split] conf copy");
                getLog().info("           from: " + copy.getFrom());
                getLog().info("           to:   " + copy.getTo());
            }
        }
        getLog().info("");
    }
}
