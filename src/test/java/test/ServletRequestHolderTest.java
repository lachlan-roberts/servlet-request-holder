package test;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
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

    private static void sendError(HttpServletResponse response, Throwable t)
    {
        try
        {
            response.sendError(500, t.getMessage());
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
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

    public static class AsyncReadServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            AsyncContext asyncContext = req.startAsync();
            ServletInputStream inputStream = req.getInputStream();
            inputStream.setReadListener(new ReadListener()
            {

                @Override
                public void onDataAvailable() throws IOException
                {
                    HttpServletRequest heldRequest = ServletRequestHolderFilter.getRequest();
                    assertThat(heldRequest, equalTo(req));
                    inputStream.close();
                    asyncContext.complete();
                }

                @Override
                public void onAllDataRead() throws IOException
                {
                    HttpServletRequest heldRequest = ServletRequestHolderFilter.getRequest();
                    assertThat(heldRequest, equalTo(req));
                    inputStream.close();
                    asyncContext.complete();
                }

                @Override
                public void onError(Throwable t)
                {
                    sendError(resp, t);
                }
            });
        }
    }

    public static class AsyncWriteServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            AsyncContext asyncContext = req.startAsync();
            ServletOutputStream outputStream = resp.getOutputStream();
            outputStream.setWriteListener(new WriteListener()
            {
                @Override
                public void onWritePossible() throws IOException
                {
                    HttpServletRequest heldRequest = ServletRequestHolderFilter.getRequest();
                    assertThat(heldRequest, equalTo(req));
                    outputStream.close();
                    asyncContext.complete();
                }

                @Override
                public void onError(Throwable t)
                {
                    sendError(resp, t);
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
    public void testAsyncDispatch(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(TestServlet.class, "/test");
        _container.addServlet(AsyncServlet.class, "/async");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080/async");
        assertThat(response.getStatus(), equalTo(200));
    }

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

    @Disabled("Need to implement the wrapped ReadListener")
    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void testAsyncRead(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(AsyncReadServlet.class, "/asyncRead");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080/asyncRead");
        assertThat(response.getStatus(), equalTo(200));
    }

    @Disabled("Need to implement the wrapped WriteListener")
    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void testAsyncWrite(Class<? extends ServletContainer> containerClass) throws Exception
    {
        _container = containerClass.getDeclaredConstructor().newInstance();
        _container.addServletContainerInitializer(ServletRequestHolderInitializer.class);
        _container.addServlet(AsyncWriteServlet.class, "/asyncWrite");
        _container.start();

        ContentResponse response = _client.GET("http://localhost:8080/asyncWrite");
        assertThat(response.getStatus(), equalTo(200));
    }
}
