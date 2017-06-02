package org.codeartisans.gradle.wsdl;

import groovy.lang.Closure;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import org.gradle.util.ConfigureUtil;

public class WsdlToJava extends DefaultTask {

    private Configuration jaxwsToolsConfiguration;
    private final NamedDomainObjectContainer<Wsdl> wsdls = getProject().container( Wsdl.class );
    private File outputDirectory;

    @InputFiles
    public Configuration getJaxwsToolsConfiguration() {
        return jaxwsToolsConfiguration;
    }

    public void setJaxwsToolsConfiguration( Configuration jaxwsToolsConfiguration ) {
        this.jaxwsToolsConfiguration = jaxwsToolsConfiguration;
    }

    @Input
    public NamedDomainObjectContainer<Wsdl> getWsdls() {
        return wsdls;
    }

    // TODO Open Gradle issue
    public void wsdls( Closure configureClosure ) {
        ConfigureUtil.configure( configureClosure, wsdls );
    }

    @InputFiles
    public FileCollection getWsdlFiles() {
        ConfigurableFileCollection wsdlFiles = getProject().files();
        for( Wsdl wsdl : wsdls ) {
            wsdlFiles.from( wsdl.getWsdl() );
        }
        return wsdlFiles;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory ) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public void processWsdls() throws IOException {
        for( Wsdl wsdl : wsdls ) {
            processWsdl( wsdl );
        }
    }

    private void processWsdl( Wsdl wsdl ) {
        List<String> arguments = wsImportArgumentsFor( wsdl );

        ExecResult result = getProject().javaexec( new Action<JavaExecSpec>() {
            @Override
            public void execute( JavaExecSpec spec ) {
                spec.setArgs( arguments );
                spec.setClasspath( jaxwsToolsConfiguration );
                spec.setMain( "com.sun.tools.ws.WsImport" );
            }
        } );

        if( result.getExitValue() != 0 ) {
            throw new GradleException( "Error running wsimport, see output for details" );
        }
    }

    private List<String> wsImportArgumentsFor( Wsdl wsdl ) {
        File wsdlFile = getProject().file( wsdl.getWsdl() );

        List<String> arguments = new ArrayList<>();
        if( wsdl.getPackageName() != null ) {
            arguments.add( "-p" );
            arguments.add( wsdl.getPackageName() );
        }
        arguments.add( "-wsdllocation" );
        arguments.add( wsdlFile.getName() );
        arguments.add( "-s" );
        arguments.add( outputDirectory.getAbsolutePath() );
        arguments.add( "-extension" );
        arguments.add( "-Xnocompile" );
        if( getProject().getLogger().isDebugEnabled() ) {
            arguments.add( "-Xdebug" );
        } else {
            arguments.add( "-quiet" );
        }
        arguments.add( wsdlFile.getAbsolutePath() );
        return arguments;
    }
}
