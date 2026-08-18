package com.aiqa.ui;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Stable Auravis navigation and live dashboard refresh. @author Tejas Shah */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 40)
public class AuravisUiConsistencyFilter extends OncePerRequestFilter {
    @Override protected boolean shouldNotFilter(HttpServletRequest request){
        if(!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String p=request.getRequestURI();
        return !("/".equals(p)||"/index.html".equals(p)||"/technology.html".equals(p)||"/auravis.html".equals(p)||"/dashboard.html".equals(p)||"/execution-center.html".equals(p)||"/real-world-impact.html".equals(p));
    }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        BufferingResponse w=new BufferingResponse(res);chain.doFilter(req,w);String html=w.body();if(html.contains("</body>"))html=html.replace("</body>",enhancement()+"</body>");byte[] b=html.getBytes(StandardCharsets.UTF_8);res.setContentLength(b.length);res.setContentType("text/html;charset=UTF-8");res.getOutputStream().write(b);
    }
    private String enhancement(){return """
<style>
#auravis-global-nav{position:fixed;top:12px;left:50%;transform:translate(-50%,0);z-index:9000;display:flex;gap:5px;flex-wrap:wrap;justify-content:center;width:max-content;max-width:calc(100% - 32px);padding:6px;background:rgba(8,20,37,.96);border:1px solid #234469;border-radius:12px;box-shadow:0 8px 28px rgba(0,0,0,.22);transition:opacity .22s ease,transform .22s ease;opacity:1;pointer-events:auto}
#auravis-global-nav.auravis-nav-hidden{opacity:0;transform:translate(-50%,-18px);pointer-events:none}
#auravis-global-nav a{color:#9bb0cc;text-decoration:none;font:700 11px Inter,system-ui,Arial;padding:7px 9px;border-radius:8px;border:1px solid transparent}#auravis-global-nav a:hover{color:#fff;background:#12355d}#auravis-global-nav a.active{color:#fff;background:#143a66;border-color:#299fff}#auravis-global-nav a.showcase{color:#35e6ad;border-color:#2b765c}#auravis-global-nav a.showcase.active{color:#07111f;background:linear-gradient(90deg,#35e6ad,#299fff);border-color:transparent}#auravis-live-state{font:700 10px Inter,system-ui,Arial;color:#35e6ad;align-self:center;padding:0 5px}
@media(max-width:760px){#auravis-global-nav{top:8px;width:calc(100% - 20px);max-width:none}}
@media(prefers-reduced-motion:reduce){#auravis-global-nav{transition:none}}
</style>
<nav id="auravis-global-nav" aria-label="Auravis navigation"><a class="showcase" data-path="/technology.html" href="/technology.html">Engineering Showcase</a><a data-path="/" href="/">Overview</a><a data-path="/auravis.html" href="/auravis.html">New Mission</a><a data-path="/dashboard.html" href="/dashboard.html">Mission Dashboard</a><a data-path="/execution-center.html" href="/execution-center.html">Execution Center</a><a data-path="/real-world-impact.html" href="/real-world-impact.html">Knowledge & Impact</a><span id="auravis-live-state"></span></nav>
<script>(()=>{const nav=document.getElementById('auravis-global-nav');const path=(location.pathname==='/'||location.pathname==='/index.html')?'/':location.pathname;document.querySelectorAll('#auravis-global-nav a').forEach(a=>a.classList.toggle('active',a.dataset.path===path));let hideTimer;const hide=()=>{if(nav&&!nav.matches(':hover')&&!nav.contains(document.activeElement))nav.classList.add('auravis-nav-hidden')};const show=()=>{if(!nav)return;nav.classList.remove('auravis-nav-hidden');clearTimeout(hideTimer);hideTimer=setTimeout(hide,1800)};['mousemove','pointermove','touchstart','keydown','scroll'].forEach(evt=>window.addEventListener(evt,show,{passive:true}));nav?.addEventListener('mouseenter',()=>{clearTimeout(hideTimer);nav.classList.remove('auravis-nav-hidden')});nav?.addEventListener('mouseleave',show);nav?.addEventListener('focusin',()=>{clearTimeout(hideTimer);nav.classList.remove('auravis-nav-hidden')});nav?.addEventListener('focusout',show);show();const live=document.getElementById('auravis-live-state');const setText=(id,v)=>{const e=document.getElementById(id);if(e&&v!==undefined&&v!==null)e.textContent=v};async function summary(){if(path!=='/dashboard.html')return;try{const r=await fetch('/api/pipeline/runs',{cache:'no-store'});if(!r.ok)return;const runs=await r.json();if(!Array.isArray(runs)||!runs.length)return;const latest=runs[0];if(latest.resultJson){try{const d=JSON.parse(latest.resultJson);setText('mReq',d.requirements?.length??d.totalRequirements??'—');setText('mTests',d.totalTests??d.testCases?.length??'—');setText('mExec',(d.passedTests??0)+(d.failedTests??0));setText('mIssues',d.failedTests??'—');setText('mDecision',d.qualityGate?.decision??d.decision??'—')}catch(e){}}}catch(e){}}async function refresh(){if(path!=='/dashboard.html')return;try{if(typeof loadStats==='function')await loadStats();if(typeof loadRuns==='function')await loadRuns();await summary();live.textContent='● Live';live.title='Last refreshed '+new Date().toLocaleTimeString()}catch(e){live.textContent='Refresh delayed'}}if(path==='/dashboard.html'){refresh();setInterval(refresh,10000);window.addEventListener('focus',refresh);document.addEventListener('visibilitychange',()=>{if(!document.hidden)refresh()})}})();</script>
""";}
    private static final class BufferingResponse extends HttpServletResponseWrapper{private final ByteArrayOutputStream buffer=new ByteArrayOutputStream();private PrintWriter writer;BufferingResponse(HttpServletResponse r){super(r);}@Override public ServletOutputStream getOutputStream(){return new ServletOutputStream(){public boolean isReady(){return true;}public void setWriteListener(WriteListener l){}public void write(int b){buffer.write(b);}};}@Override public PrintWriter getWriter(){if(writer==null)writer=new PrintWriter(new OutputStreamWriter(buffer,StandardCharsets.UTF_8));return writer;}String body(){if(writer!=null)writer.flush();return buffer.toString(StandardCharsets.UTF_8);}}
}
