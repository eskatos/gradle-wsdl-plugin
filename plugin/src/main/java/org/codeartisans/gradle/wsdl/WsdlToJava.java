package org.codeartisans.gradle.wsdl;

import groovy.lang.Closure;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerConfiguration;
import org.gradle.workers.WorkerExecutor;

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
        getWorkerExecutor().submit( WsImportRunnable.class, new Action<WorkerConfiguration>() {
            @Override
            public void execute( WorkerConfiguration config ) {
                config.setDisplayName( "Import WSDL " + wsdl.getName() + " into " + wsdl.getPackageName() );
                config.setParams( wsImportArgumentsFor( wsdl ) );
                config.setIsolationMode( IsolationMode.CLASSLOADER );
                config.classpath( jaxwsToolsConfiguration.getFiles() );
            }
        } );
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
        if (wsdl.getExtraArgs() != null) {
            arguments.addAll(Arrays.asList(wsdl.getExtraArgs().split(" ")));
        }
        if( getProject().getLogger().isDebugEnabled() ) {
            arguments.add( "-Xdebug" );
        } else {
            arguments.add( "-quiet" );
        }
        arguments.add( wsdlFile.getAbsolutePath() );
        return arguments;
    }

    @Inject
    protected WorkerExecutor getWorkerExecutor() {
        throw new UnsupportedOperationException();
    }
}
