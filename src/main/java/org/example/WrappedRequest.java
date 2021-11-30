package org.example;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class WrappedRequest extends HttpServletRequestWrapper
{
    private final HttpServletRequest _request;

    public WrappedRequest(HttpServletRequest request)
    {
        super(request);
        _request = request;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        return new WrappedAsyncContext(_request.startAsync(), this);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
    {
        return new WrappedAsyncContext(_request.startAsync(servletRequest, servletResponse), this);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        // TODO: Override this to set a custom wrapped ReadListener.
        return super.getInputStream();
    }
}
