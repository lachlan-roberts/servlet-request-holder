package servlet.impl;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlet.ServletContainer;

public class JettyContainer implements ServletContainer
{
    private final Server server;
    private final ServletContextHandler contextHandler;

    public JettyContainer()
    {
        server = new Server();
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.addConnector(serverConnector);

        contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);
    }

    @Override
    public void start() throws Exception
    {
        server.start();
    }

    @Override
    public void stop() throws Exception
    {
        server.stop();
    }

    @Override
    public void addServlet(Class<? extends Servlet> servlet, String pathSpec)
    {
        ServletHolder servletHolder = contextHandler.addServlet(servlet, pathSpec);
        servletHolder.setAsyncSupported(true);
        servletHolder.setInitOrder(1);
    }

    @Override
    public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass) throws Exception
    {
        contextHandler.addBean(new ServletContextHandler.Initializer(contextHandler, sciClass.getDeclaredConstructor().newInstance()));
    }
}
