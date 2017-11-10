package org.codeartisans.gradle.wsdl;

import java.io.Serializable;
import org.gradle.api.Named;

public class Wsdl implements Named, Serializable {

    private String name;
    private Object wsdl;
    private String packageName;
    private String extraArgs;

    public Wsdl() {}

    public Wsdl( String name ) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Object getWsdl() {
        return wsdl;
    }

    public void setWsdl( Object wsdl ) {
        this.wsdl = wsdl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName( String packageName ) {
        this.packageName = packageName;
    }

    public String getExtraArgs() {
        return extraArgs;
    }

    public void setExtraArgs(String extraArgs) {
        this.extraArgs = extraArgs;
    }

    @Override
    public String toString() {
        return name + " WSDL to " + packageName;
    }
}
