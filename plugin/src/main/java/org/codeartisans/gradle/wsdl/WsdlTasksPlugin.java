package org.codeartisans.gradle.wsdl;

import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class WsdlTasksPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        Configuration jaxWsTools = project.getConfigurations().create( "jaxwsTools" );
        project.getDependencies().add( "jaxwsTools", "com.sun.xml.ws:jaxws-tools:2.2.10" );

        project.getTasks().withType( WsdlToJava.class, new Action<WsdlToJava>() {
            @Override
            public void execute( WsdlToJava task ) {

                if( task.getJaxwsToolsConfiguration() == null ) {
                    task.setJaxwsToolsConfiguration( jaxWsTools );
                }
                if( task.getOutputDirectory() == null ) {
                    task.setOutputDirectory( new File( project.getBuildDir(),
                                                       "generated-sources/" + task.getName() + "/java" ) );
                }
            }
        } );
    }
}
