package com.voc.service.common.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RepeatedlyReadRequestWrapper
 * @createTime 2024年01月04日 17:54
 * @Copyright futong
 */
public class RepeatedlyReadRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    /**
     *
     * @param request
     */
    public RepeatedlyReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        StringBuilder sb = new StringBuilder();
        InputStream ins = request.getInputStream();
        BufferedReader isr = null;
        try{
            if(ins != null){
                isr = new BufferedReader(new InputStreamReader(ins));
                char[] charBuffer = new char[128];
                int readCount;
                while((readCount = isr.read(charBuffer)) != -1){
                    sb.append(charBuffer,0,readCount);
                }
            }
        }catch (IOException e){
            throw e;
        }finally {
            if(isr != null) {
                isr.close();
            }
        }

        sb.toString();
        body = sb.toString();
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayIns = new ByteArrayInputStream(body.getBytes());
        ServletInputStream servletIns = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() {
                return byteArrayIns.read();
            }
        };
        return  servletIns;
    }
}
