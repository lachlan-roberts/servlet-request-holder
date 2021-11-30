package org.example;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletRequestHolderFilter extends HttpFilter
{
    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();

    public static HttpServletRequest getRequest()
    {
        return REQUEST.get();
    }

    static void setRequest(HttpServletRequest request)
    {
        REQUEST.set(request);
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest oldValue = REQUEST.get();
        try
        {
            // We wrap the request and response so we can intercept any async callbacks.
            WrappedRequest wrappedRequest = new WrappedRequest(req);
            WrappedResponse wrappedResponse = new WrappedResponse(res);

            REQUEST.set(wrappedRequest);
            super.doFilter(wrappedRequest, wrappedResponse, chain);
        }
        finally
        {
            REQUEST.set(oldValue);
        }
    }
}
