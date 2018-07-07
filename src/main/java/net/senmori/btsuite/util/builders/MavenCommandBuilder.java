package net.senmori.btsuite.util.builders;

import net.senmori.btsuite.Builder;
import net.senmori.btsuite.util.LogHandler;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.List;

public class MavenCommandBuilder {

    Invoker invoker = Builder.getInvoker();
    InvocationRequest request = new DefaultInvocationRequest();

    private MavenCommandBuilder() {
        invoker = Builder.getInvoker();
        request = new DefaultInvocationRequest();

        invoker.setOutputHandler((str) -> LogHandler.info(str));
        invoker.setErrorHandler((err) -> LogHandler.error(err));
    }

    public static MavenCommandBuilder builder() {
        return new MavenCommandBuilder();
    }

    public static MavenCommandBuilder copy(MavenCommandBuilder other) {
        MavenCommandBuilder builder = new MavenCommandBuilder();
        builder.invoker = other.invoker;
        builder.request = other.request;
        return builder;
    }

    public MavenCommandBuilder setMavenOpts(String opts) {
        request.setMavenOpts(opts);
        return this;
    }

    public MavenCommandBuilder setInteractiveMode(boolean interactive) {
        request.setBatchMode( ! interactive );
        return this;
    }

    public MavenCommandBuilder setBaseDirectory(File baseDirectory) {
        request.setBaseDirectory(baseDirectory);
        return this;
    }

    public MavenCommandBuilder setGoals(List<String> list) {
        request.setGoals(list);
        return this;
    }

    public InvocationResult execute() {
        try {
            return invoker.execute(request);
        } catch ( MavenInvocationException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
