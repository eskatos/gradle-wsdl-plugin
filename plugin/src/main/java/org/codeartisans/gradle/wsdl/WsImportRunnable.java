package org.codeartisans.gradle.wsdl;

import com.sun.tools.ws.wscompile.WsimportTool;
import java.util.List;
import javax.inject.Inject;

public class WsImportRunnable implements Runnable {

    private final List<String> arguments;

    @Inject
    public WsImportRunnable( List<String> arguments ) {
        this.arguments = arguments;
    }

    @Override
    public void run() {
        try {
            if( !new WsimportTool( System.out ).run( arguments.toArray( new String[ arguments.size() ] ) ) ) {
                throw new RuntimeException( "Unable to import WSDL, see output for more details" );
            }
        } catch( Throwable throwable ) {
            throw new RuntimeException( "Unable to import WSDL", throwable );
        }
    }
}
