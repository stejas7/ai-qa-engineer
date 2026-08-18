package com.aiqa.analytics;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Injects the traffic summary into the existing static dashboard without duplicating the dashboard UI. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class DashboardTrafficWidgetFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/dashboard.html".equals(request.getRequestURI()) || !"GET".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        filterChain.doFilter(request, wrapped);

        String html = wrapped.body();
        if (html.contains("</main>")) {
            html = html.replace("</main>", widget() + "</main>");
        }
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String widget() {
        return """
            <div class="section card" id="siteTrafficCard">
              <h2>Site Traffic</h2>
              <div class="sub">Privacy-friendly Auravis traffic analytics. Raw IP addresses are not stored.</div>
              <div class="stat-grid" style="margin-top:13px">
                <div class="stat"><strong id="trafficTotal">—</strong><small>Total Visits</small></div>
                <div class="stat"><strong id="trafficToday">—</strong><small>Visits Today</small></div>
                <div class="stat"><strong id="trafficUnique">—</strong><small>Unique Visitors</small></div>
                <div class="stat"><strong id="trafficTopPage">—</strong><small>Most Visited Page</small></div>
              </div>
              <div id="trafficBars" style="display:grid;grid-template-columns:repeat(7,1fr);gap:8px;align-items:end;height:130px;margin-top:18px"></div>
            </div>
            <script>
            (async()=>{try{
              const r=await fetch('/api/analytics/stats'); if(!r.ok)return; const s=await r.json();
              trafficTotal.textContent=s.totalVisits; trafficToday.textContent=s.visitsToday;
              trafficUnique.textContent=s.uniqueVisitors; trafficTopPage.textContent=s.mostVisitedPage;
              const max=Math.max(1,...s.last7Days.map(x=>x.visits));
              trafficBars.innerHTML=s.last7Days.map(x=>'<div style="text-align:center"><div style="height:92px;display:flex;align-items:flex-end;justify-content:center"><div title="'+x.date+' — '+x.visits+' visits" style="width:70%;min-height:4px;height:'+Math.max(4,x.visits*88/max)+'px;background:linear-gradient(180deg,var(--blue),var(--green));border-radius:6px 6px 2px 2px"></div></div><b style="font-size:11px">'+x.visits+'</b><div class="sub" style="font-size:10px">'+x.label+'</div></div>').join('');
            }catch(e){console.warn('Traffic analytics unavailable',e)}})();
            </script>
            """;
    }

    private static final class BufferingResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private PrintWriter writer;

        BufferingResponse(HttpServletResponse response) { super(response); }

        @Override public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener listener) {}
                @Override public void write(int b) { buffer.write(b); }
            };
        }

        @Override public PrintWriter getWriter() {
            if (writer == null) writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
            return writer;
        }

        String body() {
            if (writer != null) writer.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
