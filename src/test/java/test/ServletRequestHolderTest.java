package test;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.example.ServletRequestHolderFilter;
import org.example.ServletRequestHolderInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import servlet.ServletContainer;
import servlet.impl.JettyContainer;
import servlet.impl.TomcatContainer;
import servlet.impl.UndertowContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ServletRequestHolderTest
{
    private HttpClient _client;
    private ServletContainer _container;

    @BeforeEach
    public void before() throws Exception
    {
        _client = new HttpClient();
        _client.start();
    }

    @AfterEach
    public void after() throws Exception
    {
        _client.stop();
        if (_container != null)
            _container.stop();
    }

    public static class TestServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        {
            HttpServletRequest heldRequest = ServletRequestHolderFilter.getRequest();
            assertThat(heldRequest, equalTo(req));
        }
    }

    public static class AsyncServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        {
            AsyncContext asyncContext = req.startAsync();
            asyncContext.dispatch("/test");
        }
    }

    public static class AsyncRunnableServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        {
            AsyncContext asyncContext = req.startAsync();
            asyncContext.start(() ->
            {
                try
                {
                    HttpServletRequest heldRequest = ServletRequestHolderFilter.getRequest();
                    assertThat(heldRequest, equalTo(req));
                }
                catch (Throwable t)
                {
                    resp.setStatus(500);
                }
                finally
                {
                    asyncContext.complete();
                }
            });
        }
    }

    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void testRequest(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(TestServlet.class, "/");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080");
        assertThat(response.getStatus(), equalTo(200));
    }

    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void testAsync(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(TestServlet.class, "/test");
        _container.addServlet(AsyncServlet.class, "/async");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080/async");
        assertThat(response.getStatus(), equalTo(200));
    }

    @Disabled("Doesn't seem to be a way to do this within the Servlet API")
    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void testAsyncRunnable(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(AsyncRunnableServlet.class, "/asyncRunnable");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080/asyncRunnable");
        assertThat(response.getStatus(), equalTo(200));
    }
}
