# checksum-maven-plugin
A maven plugin to generate checksum of project resources and expose it as project property to be used by other plugins.

### Usage Example
```
<build>
<plugins>

	<plugin>
		<groupId>br.dodo</groupId>
		<artifactId>checksum-maven-plugin</artifactId>
		<version>0.0.3-SNAPSHOT</version>
		<executions>
			<execution>
			    	<id>checksum css</id>
				<phase>process-resources</phase>
				<goals>
					<goal>generate-checksum</goal>
				</goals>
				<configuration>
				  <propertyName>csschecksum</propertyName>
				  <folder>src/main/resources</folder>
				  <extensions>
				      <param>css</param>
				  </extensions>
				</configuration>
			</execution>

			<execution>
			   	<id>checksum js</id>
				<phase>process-resources</phase>
				<goals>
				    <goal>generate-checksum</goal>
				</goals>
				<configuration>
				  <propertyName>jschecksum</propertyName>
				  <folder>src/main/resources</folder>
				  <extensions>
				      <param>js</param>
				  </extensions>
				</configuration>
		       </execution>
		</executions>
	</plugin>
	
  </plugins>
  
  ...
</build>
```

