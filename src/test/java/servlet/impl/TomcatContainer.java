package servlet.impl;

import java.io.File;
import java.util.Collections;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import servlet.ServletContainer;

public class TomcatContainer implements ServletContainer
{
    private final Tomcat tomcat;
    private final Context context;

    public TomcatContainer()
    {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();
        context = tomcat.addContext("", new File(".").getAbsolutePath());
    }

    @Override
    public void start() throws Exception
    {
        tomcat.start();
    }

    @Override
    public void stop() throws Exception
    {
        tomcat.stop();
        tomcat.destroy();
    }

    @Override
    public void addServlet(Class<? extends Servlet> servlet, String pathSpec)
    {
        String servletName = servlet.getName() + "-" + Integer.toHexString(this.hashCode());
        Wrapper wrapper = Tomcat.addServlet(context, servletName, servlet.getName());
        wrapper.setAsyncSupported(true);
        wrapper.setLoadOnStartup(1);
        context.addServletMappingDecoded(pathSpec, servletName);
    }

    @Override
    public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass) throws Exception
    {
        context.addServletContainerInitializer(sciClass.getDeclaredConstructor().newInstance(), Collections.emptySet());
    }
}
