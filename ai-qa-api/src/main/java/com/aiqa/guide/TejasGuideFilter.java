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
 * Voice v3 keeps microphone dictation but deliberately disables text-to-speech.
 * The guide uses contextual deterministic conversation without exposing credentials in the browser.
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
                || "/execution-center.html".equals(path)
                || "/real-world-impact.html".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        chain.doFilter(request, wrapped);
        String html = wrapped.body();
        if (html.contains("</body>")) html = html.replace("</body>", widget() + "</body>");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String widget() {
        return """
<style>
#tejas-launch{position:fixed;right:22px;bottom:22px;z-index:9998;width:58px;height:58px;border-radius:50%;border:1px solid rgba(53,230,173,.55);background:radial-gradient(circle at 35% 30%,#35e6ad,#299fff 48%,#5937b7);box-shadow:0 10px 34px rgba(41,159,255,.35);color:#07111f;font-size:24px;font-weight:900;cursor:pointer}
#tejas-panel{position:fixed;right:22px;bottom:92px;z-index:9999;width:min(410px,calc(100vw - 28px));height:560px;display:none;grid-template-rows:auto auto 1fr auto;background:#0b1829;border:1px solid #234469;border-radius:18px;box-shadow:0 22px 70px rgba(0,0,0,.45);overflow:hidden;color:#f2f7ff;font-family:Inter,system-ui,Arial}#tejas-panel.open{display:grid}
#tejas-head{display:flex;justify-content:space-between;align-items:center;padding:14px 15px;background:linear-gradient(135deg,#102b49,#10213a);border-bottom:1px solid #234469}.tejas-brand{display:flex;gap:10px;align-items:center}.tejas-avatar{width:38px;height:38px;border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 35% 30%,#35e6ad,#299fff 50%,#5937b7);color:#07111f;font-weight:1000}.tejas-name{font-weight:900}.tejas-sub{font-size:10px;color:#9bb0cc}.tejas-x{background:transparent!important;color:#cfe2f8!important;border:0!important;font-size:20px!important;padding:5px 8px!important;cursor:pointer}
#tejas-voice-status{display:flex;align-items:center;gap:7px;padding:7px 14px;background:#081425;border-bottom:1px solid #173755;color:#9bb0cc;font-size:10px}.voice-dot{width:7px;height:7px;border-radius:50%;background:#35e6ad}.voice-dot.listening{background:#fb7185;box-shadow:0 0 0 4px rgba(251,113,133,.15)}
#tejas-messages{padding:14px;overflow:auto;display:flex;flex-direction:column;gap:10px}.tejas-msg{max-width:90%;padding:10px 12px;border-radius:12px;line-height:1.5;font-size:13px;white-space:pre-wrap}.tejas-bot{align-self:flex-start;background:#132743;border:1px solid #234469}.tejas-user{align-self:flex-end;background:#173755;border:1px solid #2b5f91}.tejas-quick{display:flex;gap:6px;flex-wrap:wrap;margin-top:8px}.tejas-quick button{background:#102b49!important;color:#cfe2f8!important;border:1px solid #234469!important;border-radius:999px!important;padding:7px 9px!important;font-size:11px!important;cursor:pointer}
#tejas-inputbar{display:grid;grid-template-columns:1fr auto auto;gap:7px;padding:11px;border-top:1px solid #234469;background:#081425}#tejas-input{border:1px solid #234469;background:#0b1829;color:#f2f7ff;border-radius:10px;padding:10px 11px;min-width:0}#tejas-send,#tejas-mic{border:0;border-radius:10px;padding:9px 11px;font-weight:900;background:linear-gradient(90deg,#35e6ad,#299fff);color:#07111f;cursor:pointer}#tejas-mic.listening{background:#fb7185;box-shadow:0 0 0 3px rgba(251,113,133,.25)}
@media(max-width:600px){#tejas-launch{right:14px;bottom:14px}#tejas-panel{right:14px;bottom:82px;height:min(590px,78vh)}}
</style>
<button id="tejas-launch" title="Open TEJAS — Auravis Guide" aria-label="Open TEJAS guide">T</button>
<section id="tejas-panel" aria-label="TEJAS Auravis product guide">
  <div id="tejas-head"><div class="tejas-brand"><div class="tejas-avatar">T</div><div><div class="tejas-name">TEJAS</div><div class="tejas-sub">Auravis Product Guide • Conversation v3</div></div></div><button class="tejas-x" id="tejas-close" aria-label="Close">×</button></div>
  <div id="tejas-voice-status"><span id="tejas-voice-dot" class="voice-dot"></span><span id="tejas-voice-text">Voice input ready — responses stay text-only</span></div>
  <div id="tejas-messages"></div>
  <div id="tejas-inputbar"><input id="tejas-input" autocomplete="off" placeholder="Ask TEJAS about Auravis…"><button id="tejas-mic" title="Start voice input">🎙</button><button id="tejas-send">Send</button></div>
</section>
<script>(()=>{
const $=id=>document.getElementById(id),panel=$('tejas-panel'),launch=$('tejas-launch'),close=$('tejas-close'),msgs=$('tejas-messages'),input=$('tejas-input'),send=$('tejas-send'),mic=$('tejas-mic'),voiceText=$('tejas-voice-text'),voiceDot=$('tejas-voice-dot'),page=location.pathname;
let listening=false,recognition=null,lastTopic='general';
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
const add=(text,who='bot')=>{const d=document.createElement('div');d.className='tejas-msg '+(who==='user'?'tejas-user':'tejas-bot');d.innerHTML=esc(text);msgs.appendChild(d);msgs.scrollTop=msgs.scrollHeight;return d};
const quick=(host,items)=>{const q=document.createElement('div');q.className='tejas-quick';items.forEach(x=>{const b=document.createElement('button');b.textContent=x;b.onclick=()=>ask(x);q.appendChild(b)});host.appendChild(q)};
const status=(text,isListening=false)=>{voiceText.textContent=text;voiceDot.classList.toggle('listening',isListening);mic.classList.toggle('listening',isListening);mic.textContent=isListening?'■':'🎙';mic.title=isListening?'Stop voice input':'Start voice input'};
const intro=()=>{let hint='I can explain what Auravis solves, how to use it, the technology stack, roadmap and current milestone.';if(page.includes('auravis'))hint='You are on New Mission. I can guide you through requirement upload, target URL and what Auravis does next.';if(page.includes('dashboard'))hint='You are on Mission Dashboard. I can explain metrics, technologies, mission status and roadmap.';if(page.includes('execution-center'))hint='You are on Execution Center. I can explain applications, execution history, PASS/FAIL and evidence.';if(page.includes('real-world-impact'))hint='You are reading the real-world problem Auravis is designed to solve. Ask me what changes when the roadmap is complete.';const d=add('Hi, I’m TEJAS. '+hint);quick(d,['How do I start?','What problem does Auravis solve?','Show tech stack','Explain roadmap','What happens after M8?'])};
const conversational=q=>{
 const x=q.toLowerCase().trim();
 if(/^(hi|hello|hey|good morning|good afternoon|good evening)\b/.test(x)){lastTopic='general';return 'Hello 👋 I can help you understand Auravis as a product, not just navigate the screen. You can ask what problem it solves, how a mission works, why RAG is used, what each milestone adds, or which technologies power it.'}
 if(x.includes('real world')||x.includes('problem solve')||x.includes('problem does')||x.includes('business problem')){lastTopic='impact';return 'Auravis targets the gap between a business requirement and reliable release confidence. Today that journey can involve manual requirement interpretation, test-case writing, automation coding, repeated execution, failure analysis and regression. Auravis is being built to connect those activities into one traceable autonomous UAT workflow while keeping execution and safety controls deterministic.'}
 if(x.includes('after m8')||x.includes('when complete')||x.includes('fully complete')||x.includes('final product')||x.includes('whole development')){lastTopic='future';return 'When the roadmap is complete, the intended flow is: provide a business requirement and UAT target → Auravis retrieves project knowledge → specialized agents plan tests → Playwright executes them → recoverable automation failures can self-heal safely → historical runs improve regression selection → CI/CD receives an evidence-backed QA recommendation. Humans still own product risk, governance and exceptional decisions.'}
 if(x.includes('start')||x.includes('upload')||x.includes('new mission')){lastTopic='start';return 'Go to New Mission, upload a .txt, .md, .docx or .pdf requirement and provide the target UAT URL. Auravis then analyzes the requirement, retrieves relevant knowledge, generates test scenarios and executes supported flows. After completion, use Mission Dashboard and Execution Center to review evidence and results.'}
 if(x.includes('technology')||x.includes('tech stack')||x.includes('what tech')||x.includes('tools used')){lastTopic='tech';return 'Current Auravis stack includes Java 17+, Spring Boot 3.5, Spring Data JPA, Maven, REST APIs, PostgreSQL 16, pgvector-ready knowledge storage, RAG, OpenAI-compatible AI integration with deterministic fallback, AI-agent/orchestration foundations, Playwright for Java, Docker, Docker Compose, GitHub Actions, GHCR, AWS EC2, Nginx, HTTPS, DuckDNS and browser Speech Recognition for TEJAS voice input.'}
 if(x.includes('rag')||x.includes('knowledge retrieval')){lastTopic='rag';return 'RAG stands for Retrieval-Augmented Generation. Auravis stores project knowledge and retrieves relevant context before AI-style requirement or test reasoning. The purpose is to ground decisions in project-specific information rather than relying only on generic model knowledge.'}
 if(x.includes('m1')){lastTopic='roadmap';return 'M1 establishes the Autonomous Mission: one requirement-driven workflow that moves through analysis, test creation, execution and QA outcome.'}
 if(x.includes('m2')){lastTopic='roadmap';return 'M2 adds the Knowledge / RAG foundation so requirement and QA reasoning can use persisted project context.'}
 if(x.includes('m3')||x.includes('test generation')){lastTopic='roadmap';return 'M3 adds intelligent test generation: functional, negative, boundary, business-rule and risk-based scenarios with traceability plus Excel and JSON exports.'}
 if(x.includes('m4')){lastTopic='roadmap';return 'M4 turns generated tests into auditable execution with richer Playwright actions, multi-application targets, screenshots/evidence and persisted execution history.'}
 if(x.includes('m5')||x.includes('agentic')||x.includes('agent orchestration')){lastTopic='roadmap';return 'M5 introduces a common agent contract, persisted AgentRun/AgentStep activity and specialized responsibilities such as requirement, knowledge, planning, execution, diagnosis and QA decision. Java remains the control layer instead of giving agents unrestricted system access.'}
 if(x.includes('m6')||x.includes('self heal')||x.includes('self-heal')||x.includes('healing')){lastTopic='roadmap';return 'M6 focuses on safe self-healing. Auravis should classify failures, identify whether a problem is recoverable automation breakage, score healing candidates, retry within strict limits and preserve an audit trail. Genuine business failures must not be rewritten just to make a test pass.'}
 if(x.includes('m7')||x.includes('regression')){lastTopic='roadmap';return 'M7 is Regression & Learning Intelligence: use historical missions, failures and validated recoveries to choose smarter regression scope and reuse trusted knowledge.'}
 if(x.includes('m8')||x.includes('ci/cd')||x.includes('quality gate')){lastTopic='roadmap';return 'M8 connects Auravis to delivery pipelines so a release can be evaluated from requirement evidence and execution results, producing an auditable quality recommendation for CI/CD.'}
 if(x.includes('roadmap')||x.includes('milestone')){lastTopic='roadmap';return 'The roadmap is M1 Autonomous Mission → M2 Knowledge/RAG → M3 Intelligent Test Generation → M4 Advanced Automation → M5 Agentic Orchestration → M6 Self-Healing → M7 Regression & Learning Intelligence → M8 Autonomous CI/CD Quality Gate. The product UI should only mark a milestone complete when its implementation and deployment are genuinely ready.'}
 if(x.includes('playwright')||x.includes('browser')||x.includes('execute test')){lastTopic='execution';return 'Playwright is the deterministic browser execution layer. Auravis can plan with AI concepts, but Java services decide which actions are allowed and Playwright performs those actions, assertions and evidence capture.'}
 if(x.includes('openai')||x.includes('api key')||x.includes('credential')){lastTopic='security';return 'Auravis supports OpenAI-compatible integration with a deterministic fallback. API keys and credentials should stay in environment variables or deployment secrets, never in the UI, README or source repository.'}
 if(x.includes('dashboard')||x.includes('metric')||x.includes('visit')){lastTopic='dashboard';return 'Mission Dashboard summarizes requirement processing, generated tests, execution status, QA outcomes, recent missions and privacy-friendly site traffic. It is also becoming the portfolio-facing technology showcase for visitors coming from LinkedIn.'}
 if(x.includes('who are you')||x.includes('your name')||x==='tejas'){lastTopic='general';return 'I’m TEJAS, the built-in Auravis product guide. I help explain the product, architecture, roadmap and workflow. I am not one of the autonomous QA agents and I do not expose or handle deployment credentials in the browser.'}
 if(x.includes('why')&&lastTopic==='rag')return 'Because generic AI reasoning can miss project-specific terminology, rules and history. RAG gives the reasoning layer relevant local context before it produces an answer or test design.';
 if((x.includes('more')||x.includes('explain more')||x.includes('detail'))&&lastTopic==='impact')return 'A practical example: a product team uploads a checkout requirement. Auravis should identify acceptance criteria, retrieve checkout knowledge, generate happy-path/negative/boundary tests, execute them against UAT, capture screenshots, diagnose failures and eventually return a release recommendation. That reduces repeated manual hand-offs while preserving evidence.';
 if((x.includes('more')||x.includes('explain more')||x.includes('detail'))&&lastTopic==='future')return 'The important end-state is not a chatbot that writes tests. It is a controlled QA system that understands intent, coordinates specialized agents, performs deterministic execution, learns from historical outcomes and integrates into delivery while keeping evidence and governance visible.';
 return 'I did not find an exact built-in answer for that yet. Try asking about the real-world problem, technology stack, how to start a mission, RAG, Playwright, M1–M8, deployment, security or what Auravis becomes when the roadmap is complete.';
};
const ask=q=>{q=(q||input.value).trim();if(!q)return;add(q,'user');input.value='';status('Thinking about your question…');setTimeout(()=>{const a=conversational(q),d=add(a);if(lastTopic==='impact'||lastTopic==='future')quick(d,['Explain more','How is this different from test automation?','What happens after M8?']);else if(lastTopic==='tech')quick(d,['Why Java?','What is RAG?','Why Playwright?']);else if(lastTopic==='roadmap')quick(d,['What is M5?','What is M6?','What happens after M8?']);status('Voice input ready — responses stay text-only')},100)};
launch.onclick=()=>{panel.classList.add('open');if(!msgs.children.length)intro();input.focus()};close.onclick=()=>{if(listening&&recognition)recognition.stop();panel.classList.remove('open')};send.onclick=()=>ask();input.addEventListener('keydown',e=>{if(e.key==='Enter')ask()});
const SR=window.SpeechRecognition||window.webkitSpeechRecognition;
if(!SR){mic.disabled=true;mic.style.opacity='.45';status('Voice input is not supported by this browser — text chat is available.')}else{
 recognition=new SR();recognition.lang='en-IN';recognition.continuous=true;recognition.interimResults=true;recognition.maxAlternatives=1;let finalText='';
 recognition.onstart=()=>{listening=true;finalText='';status('Listening… tap ■ when finished',true)};
 recognition.onresult=e=>{let interim='';for(let i=e.resultIndex;i<e.results.length;i++){const t=e.results[i][0].transcript;if(e.results[i].isFinal)finalText+=(finalText?' ':'')+t.trim();else interim+=t}input.value=(finalText+(interim?' '+interim:'')).trim();status(interim?'Listening… '+interim.trim():'Listening… tap ■ when finished',true)};
 recognition.onend=()=>{listening=false;status(input.value.trim()?'Transcript ready — review/edit, then press Send':'Voice input ready — responses stay text-only')};
 recognition.onerror=e=>{listening=false;const messages={'not-allowed':'Microphone permission denied. Allow microphone access in browser settings.','service-not-allowed':'Speech service is blocked by the browser.','no-speech':'No speech detected. Tap the microphone and try again.','audio-capture':'No microphone was detected.','network':'Speech recognition network error. Please retry.'};status(messages[e.error]||('Voice recognition error: '+e.error))};
 mic.onclick=()=>{if(listening){try{recognition.stop()}catch(e){}return}try{recognition.start()}catch(e){status('Unable to start microphone. Please wait a moment and retry.')}};
}
})();</script>
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
