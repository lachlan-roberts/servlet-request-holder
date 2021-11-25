package servlet.impl;

import java.util.Collections;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import servlet.ServletContainer;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

public class UndertowContainer implements ServletContainer
{
    private Undertow server;
    private final DeploymentInfo servletBuilder;

    public UndertowContainer()
    {
        servletBuilder = deployment()
            .setClassLoader(UndertowContainer.class.getClassLoader())
            .setContextPath("/")
            .setDeploymentName("test.war");
    }

    @Override
    public void start() throws Exception
    {
        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect("/"))
            .addPrefixPath("/", servletHandler);
        server = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(path)
            .build();
        server.start();
    }

    @Override
    public void stop()
    {
        if (server != null)
        {
            server.stop();
            server = null;
        }
    }

    @Override
    public void addServlet(Class<? extends Servlet> servlet, String pathSpec)
    {
        servletBuilder.addServlets(servlet(servlet)
            .addMapping(pathSpec)
            .setLoadOnStartup(1)
            .setAsyncSupported(true)
        );
    }

    @Override
    public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass)
    {
        servletBuilder.addServletContainerInitializer(new ServletContainerInitializerInfo(sciClass, Collections.emptySet()));
    }
}
