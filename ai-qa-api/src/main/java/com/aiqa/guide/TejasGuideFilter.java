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

/**
 * Injects the TEJAS product-guide chatbot into Auravis HTML pages.
 *
 * <p>TEJAS is deliberately implemented without exposing credentials in the browser. Version 1 uses
 * deterministic product guidance plus browser-native speech synthesis / recognition when supported.
 * It can later delegate deeper questions to the M5 agent/RAG backend behind controlled APIs.</p>
 *
 * @author Tejas Shah
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class TejasGuideFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !("/".equals(path)
                || "/index.html".equals(path)
                || "/auravis.html".equals(path)
                || "/dashboard.html".equals(path)
                || "/execution-center.html".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        filterChain.doFilter(request, wrapped);

        String html = wrapped.body();
        if (!html.contains("</body>")) {
            copyResponse(response, html);
            return;
        }

        html = html.replace("</body>", widget() + "</body>");
        copyResponse(response, html);
    }

    private void copyResponse(HttpServletResponse response, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String widget() {
        return """
            <style>
            #tejas-launch{position:fixed;right:22px;bottom:22px;z-index:9998;width:58px;height:58px;border-radius:50%;border:1px solid rgba(53,230,173,.55);background:radial-gradient(circle at 35% 30%,#35e6ad,#299fff 48%,#5937b7 100%);box-shadow:0 10px 34px rgba(41,159,255,.35);color:#07111f;font-size:24px;font-weight:900;cursor:pointer}
            #tejas-panel{position:fixed;right:22px;bottom:92px;z-index:9999;width:min(380px,calc(100vw - 28px));height:520px;display:none;grid-template-rows:auto 1fr auto;background:#0b1829;border:1px solid #234469;border-radius:18px;box-shadow:0 22px 70px rgba(0,0,0,.45);overflow:hidden;color:#f2f7ff;font-family:Inter,system-ui,Arial}
            #tejas-panel.open{display:grid}#tejas-head{display:flex;justify-content:space-between;align-items:center;padding:14px 15px;background:linear-gradient(135deg,#102b49,#10213a);border-bottom:1px solid #234469}.tejas-brand{display:flex;gap:10px;align-items:center}.tejas-avatar{width:38px;height:38px;border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 35% 30%,#35e6ad,#299fff 50%,#5937b7);color:#07111f;font-weight:1000}.tejas-name{font-weight:900}.tejas-sub{font-size:10px;color:#9bb0cc}.tejas-x{background:transparent!important;color:#9bb0cc!important;border:0!important;font-size:22px!important;padding:4px 8px!important}
            #tejas-messages{padding:14px;overflow:auto;display:flex;flex-direction:column;gap:10px}.tejas-msg{max-width:88%;padding:10px 12px;border-radius:12px;line-height:1.45;font-size:13px;white-space:pre-wrap}.tejas-bot{align-self:flex-start;background:#132743;border:1px solid #234469}.tejas-user{align-self:flex-end;background:#173755;border:1px solid #2b5f91}.tejas-quick{display:flex;gap:6px;flex-wrap:wrap;margin-top:6px}.tejas-quick button{background:#102b49!important;color:#cfe2f8!important;border:1px solid #234469!important;border-radius:999px!important;padding:7px 9px!important;font-size:11px!important}
            #tejas-inputbar{display:grid;grid-template-columns:1fr auto auto;gap:7px;padding:11px;border-top:1px solid #234469;background:#081425}#tejas-input{border:1px solid #234469;background:#0b1829;color:#f2f7ff;border-radius:10px;padding:10px 11px;min-width:0}#tejas-send,#tejas-mic{border:0;border-radius:10px;padding:9px 11px;font-weight:900;background:linear-gradient(90deg,#35e6ad,#299fff);color:#07111f;cursor:pointer}#tejas-mic.listening{box-shadow:0 0 0 3px rgba(251,113,133,.25);background:#fb7185}
            @media(max-width:600px){#tejas-launch{right:14px;bottom:14px}#tejas-panel{right:14px;bottom:82px;height:min(560px,72vh)}}
            </style>
            <button id="tejas-launch" title="Open TEJAS — Auravis Guide" aria-label="Open TEJAS guide">T</button>
            <section id="tejas-panel" aria-label="TEJAS Auravis product guide">
              <div id="tejas-head"><div class="tejas-brand"><div class="tejas-avatar">T</div><div><div class="tejas-name">TEJAS</div><div class="tejas-sub">AI Product Guide • Auravis</div></div></div><button class="tejas-x" id="tejas-close" aria-label="Close">×</button></div>
              <div id="tejas-messages"></div>
              <div id="tejas-inputbar"><input id="tejas-input" autocomplete="off" placeholder="Ask about Auravis…"><button id="tejas-mic" title="Voice input">🎙</button><button id="tejas-send">Send</button></div>
            </section>
            <script>
            (()=>{
              const panel=document.getElementById('tejas-panel'),launch=document.getElementById('tejas-launch'),close=document.getElementById('tejas-close'),msgs=document.getElementById('tejas-messages'),input=document.getElementById('tejas-input'),send=document.getElementById('tejas-send'),mic=document.getElementById('tejas-mic');
              const page=location.pathname;
              const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
              const add=(text,who='bot')=>{const d=document.createElement('div');d.className='tejas-msg '+(who==='user'?'tejas-user':'tejas-bot');d.innerHTML=esc(text);msgs.appendChild(d);msgs.scrollTop=msgs.scrollHeight;return d};
              const say=text=>{try{if(!('speechSynthesis'in window))return;window.speechSynthesis.cancel();const u=new SpeechSynthesisUtterance(text);u.rate=1;window.speechSynthesis.speak(u)}catch(e){}}
              const intro=()=>{let hint='I can explain the roadmap, architecture, RAG, test generation, execution, agents and self-healing.';if(page.includes('auravis'))hint='Upload a business requirement and provide a UAT target. I can explain what Auravis will do next.';if(page.includes('dashboard'))hint='This dashboard shows requirements, missions, traffic and execution progress. Ask me what any metric means.';if(page.includes('execution-center'))hint='This page manages UAT targets and execution evidence. I can explain PASS/FAIL history or the M4 execution model.';const d=add('Hi, I’m TEJAS — your Auravis product guide. '+hint);const q=document.createElement('div');q.className='tejas-quick';['How do I start?','Explain roadmap','What is RAG?','What is M5?','What is M6?'].forEach(x=>{const b=document.createElement('button');b.textContent=x;b.onclick=()=>ask(x);q.appendChild(b)});d.appendChild(q)};
              const answer=q=>{const x=q.toLowerCase();if(x.includes('start')||x.includes('upload'))return 'Start from New Mission. Upload a .txt, .md, .docx or .pdf business requirement, provide the UAT application URL, then Auravis analyzes the requirement, retrieves knowledge, generates tests and executes supported UAT flows.';if(x.includes('rag')||x.includes('knowledge'))return 'RAG means Retrieval-Augmented Generation. Auravis retrieves relevant persisted project knowledge before requirement/test reasoning so outputs can be grounded in project context instead of only a generic model response.';if(x.includes('roadmap'))return 'Auravis roadmap: M1 Autonomous Mission, M2 Knowledge/RAG, M3 Intelligent Test Generation, M4 Advanced Automation & Multi-App Support, M5 Agentic Orchestration, M6 Self-Healing, M7 Regression & Learning Intelligence, and M8 Autonomous CI/CD Quality Gate.';if(x.includes('m5')||x.includes('agent'))return 'M5 introduces specialized agents coordinated through a controlled Java orchestration layer. Requirement, knowledge, planning, execution, diagnosis and QA-decision responsibilities are separated and their activity is persisted for auditability.';if(x.includes('m6')||x.includes('heal'))return 'M6 focuses on safe self-healing and smart recovery. Auravis should classify automation failures, generate safe locator/retry candidates, apply confidence gates, retry only eligible technical failures, and preserve a complete healing audit trail.';if(x.includes('playwright')||x.includes('execution'))return 'Auravis uses Playwright for deterministic browser execution. AI/agents may plan or diagnose, but Java services keep control of permitted actions, assertions, retries and evidence capture.';if(x.includes('test case')||x.includes('test generation')||x.includes('m3'))return 'M3 converts requirement intelligence into functional, negative, boundary, business-rule and risk-based test scenarios, with traceability and Excel/JSON export.';if(x.includes('dashboard')||x.includes('metric'))return 'The Mission Dashboard is the operational view for requirement processing, generated tests, execution results, QA decisions, recent missions and privacy-friendly site traffic.';if(x.includes('openai')||x.includes('ai api'))return 'Auravis supports OpenAI-compatible AI integration with deterministic fallback. Credentials are never intended to be exposed in the UI or committed to the repository.';if(x.includes('who are you')||x.includes('your name')||x.includes('tejas'))return 'I’m TEJAS, the built-in Auravis product guide. I help explain the product, roadmap and current workflow. I am separate from the autonomous QA agents themselves.';return 'I can currently guide you through Auravis product usage, roadmap, RAG, test generation, Playwright execution, M5 agents and M6 self-healing. Deeper mission-specific conversational reasoning will be connected through controlled RAG/agent APIs in a later enhancement.'};
              const ask=q=>{q=(q||input.value).trim();if(!q)return;add(q,'user');input.value='';const a=answer(q);setTimeout(()=>{add(a);say(a)},120)};
              launch.onclick=()=>{panel.classList.add('open');if(!msgs.children.length)intro();input.focus()};close.onclick=()=>panel.classList.remove('open');send.onclick=()=>ask();input.addEventListener('keydown',e=>{if(e.key==='Enter')ask()});
              const SR=window.SpeechRecognition||window.webkitSpeechRecognition;if(!SR){mic.title='Voice input is not supported by this browser'}else{const r=new SR();r.lang='en-IN';r.interimResults=false;r.onstart=()=>mic.classList.add('listening');r.onend=()=>mic.classList.remove('listening');r.onerror=()=>mic.classList.remove('listening');r.onresult=e=>{input.value=e.results[0][0].transcript;ask()};mic.onclick=()=>{try{r.start()}catch(e){}}}
            })();
            </script>
            """;
    }

    private static final class BufferingResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private PrintWriter writer;

        BufferingResponse(HttpServletResponse response) { super(response); }

        @Override
        public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener listener) { }
                @Override public void write(int b) { buffer.write(b); }
            };
        }

        @Override
        public PrintWriter getWriter() {
            if (writer == null) writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
            return writer;
        }

        String body() {
            if (writer != null) writer.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
