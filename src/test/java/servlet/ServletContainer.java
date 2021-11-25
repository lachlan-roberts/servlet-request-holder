package servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;

public interface ServletContainer
{
    void start() throws Exception;

    void stop() throws Exception;

    void addServlet(Class<? extends Servlet> servlet, String pathSpec);

    void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass) throws Exception;
}
