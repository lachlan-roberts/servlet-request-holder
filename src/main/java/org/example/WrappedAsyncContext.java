package org.example;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WrappedAsyncContext implements AsyncContext
{
    private final AsyncContext _asyncContext;
    private final HttpServletRequest _request;

    public WrappedAsyncContext(AsyncContext asyncContext, HttpServletRequest request)
    {
        _asyncContext = asyncContext;
        _request = request;
    }

    @Override
    public ServletRequest getRequest()
    {
        return _asyncContext.getRequest();
    }

    @Override
    public ServletResponse getResponse()
    {
        return _asyncContext.getResponse();
    }

    @Override
    public boolean hasOriginalRequestAndResponse()
    {
        return _asyncContext.hasOriginalRequestAndResponse();
    }

    @Override
    public void dispatch()
    {
        _asyncContext.dispatch();
    }

    @Override
    public void dispatch(String path)
    {
        _asyncContext.dispatch(path);
    }

    @Override
    public void dispatch(ServletContext context, String path)
    {
        _asyncContext.dispatch(context, path);
    }

    @Override
    public void complete()
    {
        _asyncContext.complete();
    }

    @Override
    public void start(Runnable run)
    {
        _asyncContext.start(() ->
        {
            HttpServletRequest oldReq = ServletRequestHolderFilter.getRequest();
            try
            {
                ServletRequestHolderFilter.setRequest(_request);
                run.run();
            }
            finally
            {
                ServletRequestHolderFilter.setRequest(oldReq);
            }
        });
    }

    @Override
    public void addListener(AsyncListener listener)
    {
        _asyncContext.addListener(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse)
    {
        _asyncContext.addListener(listener, servletRequest, servletResponse);
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException
    {
        return _asyncContext.createListener(clazz);
    }

    @Override
    public void setTimeout(long timeout)
    {
        _asyncContext.setTimeout(timeout);
    }

    @Override
    public long getTimeout()
    {
        return _asyncContext.getTimeout();
    }
}
