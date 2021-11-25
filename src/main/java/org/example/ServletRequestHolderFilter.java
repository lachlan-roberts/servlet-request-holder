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

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest oldValue = REQUEST.get();
        try
        {
            REQUEST.set(req);
            super.doFilter(req, res, chain);
        }
        finally
        {
            REQUEST.set(oldValue);
        }
    }
}
