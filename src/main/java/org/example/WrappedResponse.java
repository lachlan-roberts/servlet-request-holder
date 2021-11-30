package org.example;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WrappedResponse extends HttpServletResponseWrapper
{
    private final HttpServletResponse _response;

    public WrappedResponse(HttpServletResponse response)
    {
        super(response);
        _response = response;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        // TODO: Override this and set wrapped WriteListener.
        return super.getOutputStream();
    }
}
