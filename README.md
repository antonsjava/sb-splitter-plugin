
# sb-splitter-plugin

  The plugin to disassemble spring boot jar to help building layered docker image.

## Ideas

### Splitting SB jar
  
  SB jar is splited to ./target/sb to folowing parts
  - Dockerfile - dummy docker file used for creatinng layered image with another maven plugin
  - app/classes - content of classes folder from SB jar
  - app/libs - content of lib folder from SB jar which are not SNAPSHOTs
  - app/snapshots - content of lib folder from SB jar which are SNAPSHOTs
  - app/loader - loader classes from SB jar
  - app/meta - meta files from SB jar
  - app/cp.arg - classpath argument file for starting application
  - app/start.sh - dummy start script (not used)

### Assemble files for docker build

  You can use splited folder directly or you can reorganize them if you want using 'copy' elements. 

  It can be done by many plugins to simplify this I add possibility to configure set of pair 
   - from file - to file
   - from directory content - to directory content
  And the plugin copies specified files. 

### Docker build

  The plugin doesn't build docker image. You can use some other plugin or use maven exec plugin
  to start layered build process. (see example)

  It is resomanded to define own src/main/docker/Dockerfile which will replace genrated Dockerfile


## The plugin configuration

### Splitting

  - property dockerFile define location of Dockerfile to be copied to target directory. Default value is "src/main/docker/Dockerfile" 
  - property sbFile define location of SB file to be split. Default value is "target/${project.build.finalName}.jar" 
  - property destDir define directory where sbFile will be unzipped. Default value is "target/sb/" 
  - property appFolder define subdirectory of destDir where layer folderf will be created "app/" 
  - property libsDir define subdirectory of appDir where layer will be created "libs/" 
  - property snapshotsDir define subdirectory of appDir where layer will be created "snapshots/" 
  - property loaderDir define subdirectory of appDir where layer will be created "loader/" 
  - property metaDir define subdirectory of appDir where layer will be created "meta/" 
    
### Copying files
    
  - property copies define set of pairs from/to. They define which files are copied and where. If from is file to must be file. If from is directory to is also directory. 
    (example: <copies><copy><from>src/main/other-resources/assembly</from><to>target/tmp/assembly</to></copy></copies>)

## The plugin usage example

    <plugin> <!-- split spring boot jar -->
      <groupId>io.github.antonsjava</groupId>
      <artifactId>sb-splitter-plugin</artifactId>
      <version>LATESTVERSION</version>
      <executions>
        <execution>
          <goals>
            <goal>split</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

    <plugin> <!-- build docker image -->
		<groupId>io.fabric8</groupId>
		<artifactId>docker-maven-plugin</artifactId>
		<configuration>
			<pushRegistry>docker.xit.camp</pushRegistry>
			<images>
				<image>
					<name>a.name.of/image:${project.version}</name>
					<build><!-- use Dockerfile on directory generated folder>
						<dockerFileDir>${project.build.directory}/sb</dockerFileDir>
					</build>
				</image>
			</images>
			<verbose>true</verbose>
			<removeAll>true</removeAll>
		</configuration>
		<executions>
			<execution>
				<id>docker:build</id>
				<phase>install</phase>
				<goals>
					<goal>build</goal>
				</goals>
			</execution>
			<execution>
				<id>docker:push</id>
				<phase>deploy</phase>
				<goals>
					<goal>push</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
```

