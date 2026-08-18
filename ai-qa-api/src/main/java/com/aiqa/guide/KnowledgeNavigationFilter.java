package com.aiqa.guide;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Adds a consistent Knowledge navigation section to Auravis product pages. @author Tejas Shah */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 30)
public class KnowledgeNavigationFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !("/".equals(path)
                || "/index.html".equals(path)
                || "/auravis.html".equals(path)
                || "/dashboard.html".equals(path)
                || "/execution-center.html".equals(path)
                || "/real-world-impact.html".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        chain.doFilter(request, wrapped);
        String html = wrapped.body();
        if (html.contains("</aside>") && !html.contains("data-auravis-knowledge-nav")) {
            boolean active = "/real-world-impact.html".equals(request.getRequestURI());
            String nav = "<div data-auravis-knowledge-nav><div class=\"ng\">Knowledge</div><nav>"
                    + "<a " + (active ? "class=\"active\" " : "")
                    + "href=\"/real-world-impact.html\">Real-World Problem & Future</a></nav></div>";
            html = html.replace("</aside>", nav + "</aside>");
        }
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private static final class BufferingResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private PrintWriter writer;
        BufferingResponse(HttpServletResponse response) { super(response); }
        @Override public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener listener) { }
                @Override public void write(int b) { buffer.write(b); }
            };
        }
        @Override public PrintWriter getWriter() {
            if (writer == null) writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
            return writer;
        }
        String body() { if (writer != null) writer.flush(); return buffer.toString(StandardCharsets.UTF_8); }
    }
}
