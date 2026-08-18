package com.aiqa.ui;

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

/**
 * Provides one stable navigation model across Auravis pages and keeps dashboard
 * values refreshed from persisted backend APIs instead of page-load snapshots.
 * @author Tejas Shah
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 40)
public class AuravisUiConsistencyFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !("/".equals(path) || "/index.html".equals(path) || "/auravis.html".equals(path)
                || "/dashboard.html".equals(path) || "/execution-center.html".equals(path)
                || "/real-world-impact.html".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        chain.doFilter(request, wrapped);
        String html = wrapped.body();
        if (html.contains("</body>")) html = html.replace("</body>", enhancement() + "</body>");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String enhancement() {
        return """
<style>
#auravis-global-nav{position:fixed;top:12px;right:18px;z-index:9000;display:flex;gap:5px;flex-wrap:wrap;justify-content:flex-end;max-width:76vw;padding:6px;background:rgba(8,20,37,.96);border:1px solid #234469;border-radius:12px;box-shadow:0 8px 28px rgba(0,0,0,.25)}
#auravis-global-nav a{color:#9bb0cc;text-decoration:none;font:700 11px Inter,system-ui,Arial;padding:7px 9px;border-radius:8px;border:1px solid transparent}#auravis-global-nav a:hover{color:#fff;background:#12355d}#auravis-global-nav a.active{color:#fff;background:#143a66;border-color:#299fff}#auravis-live-state{font:700 10px Inter,system-ui,Arial;color:#35e6ad;align-self:center;padding:0 5px}
@media(max-width:760px){#auravis-global-nav{position:relative;top:auto;right:auto;max-width:none;margin:10px;justify-content:center}}
</style>
<nav id="auravis-global-nav" aria-label="Auravis navigation">
<a data-path="/" href="/">Overview</a><a data-path="/auravis.html" href="/auravis.html">New Mission</a><a data-path="/dashboard.html" href="/dashboard.html">Mission Dashboard</a><a data-path="/execution-center.html" href="/execution-center.html">Execution Center</a><a data-path="/real-world-impact.html" href="/real-world-impact.html">Knowledge & Impact</a><span id="auravis-live-state"></span>
</nav>
<script>(()=>{
 const path=(location.pathname==='/'||location.pathname==='/index.html')?'/':location.pathname;
 document.querySelectorAll('#auravis-global-nav a').forEach(a=>a.classList.toggle('active',a.dataset.path===path));
 const live=document.getElementById('auravis-live-state');
 const setText=(id,v)=>{const e=document.getElementById(id);if(e&&v!==undefined&&v!==null)e.textContent=v};
 async function refreshMissionSummary(){
   if(path!=='/dashboard.html')return;
   try{
     const r=await fetch('/api/pipeline/runs',{cache:'no-store'}); if(!r.ok)return;
     const runs=await r.json(); if(!Array.isArray(runs)||!runs.length)return;
     const latest=runs[0];
     if(latest.resultJson){try{const d=JSON.parse(latest.resultJson);setText('mReq',d.requirements?.length??d.totalRequirements??'—');setText('mTests',d.totalTests??d.testCases?.length??'—');setText('mExec',(d.passedTests??0)+(d.failedTests??0));setText('mIssues',d.failedTests??'—');setText('mDecision',d.qualityGate?.decision??d.decision??'—');}catch(e){}}
   }catch(e){}
 }
 async function refresh(){
   if(path!=='/dashboard.html')return;
   try{if(typeof loadStats==='function')await loadStats();if(typeof loadRuns==='function')await loadRuns();await refreshMissionSummary();live.textContent='● Live';live.title='Last refreshed '+new Date().toLocaleTimeString();}catch(e){live.textContent='Refresh delayed';}
 }
 if(path==='/dashboard.html'){refresh();setInterval(refresh,10000);window.addEventListener('focus',refresh);document.addEventListener('visibilitychange',()=>{if(!document.hidden)refresh()});}
})();
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
