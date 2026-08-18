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
 * Makes the Auravis technology stack visible to visitors on the product overview and dashboard.
 * Only technologies currently used by, or directly implemented in, the project are shown.
 *
 * @author Tejas Shah
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 30)
public class TechnologyShowcaseFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !("/".equals(path) || "/index.html".equals(path) || "/dashboard.html".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        BufferingResponse wrapped = new BufferingResponse(response);
        filterChain.doFilter(request, wrapped);
        String html = wrapped.body();
        if (html.contains("</main>")) {
            html = html.replace("</main>", showcase() + "</main>");
        } else if (html.contains("</body>")) {
            html = html.replace("</body>", showcase() + "</body>");
        }
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String showcase() {
        return """
            <style>
              .auravis-tech-showcase{margin-top:18px;background:linear-gradient(180deg,#0f1e33,#0b1829);border:1px solid #234469;border-radius:14px;padding:18px;color:#f2f7ff;font-family:Inter,system-ui,Arial}
              .auravis-tech-showcase h2{margin:4px 0 6px;font-size:20px}.auravis-tech-showcase .lead{color:#9bb0cc;font-size:13px;line-height:1.55;max-width:980px}
              .auravis-tech-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-top:14px}.auravis-tech-group{background:#132743;border:1px solid #234469;border-radius:12px;padding:13px}.auravis-tech-group h3{font-size:12px;margin:0 0 9px;color:#d9eaff}
              .auravis-tech-chips{display:flex;gap:6px;flex-wrap:wrap}.auravis-tech-chip{font-size:10px;padding:5px 8px;border-radius:999px;background:#102b49;border:1px solid #28527b;color:#cfe6ff}
              .auravis-tech-flow{margin-top:13px;padding:12px;background:#081425;border:1px solid #234469;border-radius:10px;color:#bcd2ec;font:11px ui-monospace,monospace;line-height:1.8}
              @media(max-width:1000px){.auravis-tech-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.auravis-tech-grid{grid-template-columns:1fr}}
            </style>
            <section class="auravis-tech-showcase" id="technology-stack">
              <div style="color:#35e6ad;font-size:11px;font-weight:800;letter-spacing:.12em">ENGINEERING SHOWCASE</div>
              <h2>Technology Behind Auravis</h2>
              <div class="lead">Auravis is a Java-first AI engineering portfolio project combining requirement intelligence, RAG, agentic orchestration, deterministic UAT automation, persistent evidence and cloud delivery.</div>
              <div class="auravis-tech-grid">
                <div class="auravis-tech-group"><h3>Backend & Architecture</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Java 17+</span><span class="auravis-tech-chip">Spring Boot 3.5</span><span class="auravis-tech-chip">Spring Data JPA</span><span class="auravis-tech-chip">REST APIs</span><span class="auravis-tech-chip">Maven</span></div></div>
                <div class="auravis-tech-group"><h3>AI & Agentic Engineering</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">OpenAI-compatible API</span><span class="auravis-tech-chip">RAG</span><span class="auravis-tech-chip">AI Agents</span><span class="auravis-tech-chip">Agent Orchestration</span><span class="auravis-tech-chip">Deterministic Fallback</span></div></div>
                <div class="auravis-tech-group"><h3>QA Automation</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Playwright for Java</span><span class="auravis-tech-chip">UAT Automation</span><span class="auravis-tech-chip">Assertions</span><span class="auravis-tech-chip">Screenshots / Evidence</span><span class="auravis-tech-chip">Excel + JSON Export</span></div></div>
                <div class="auravis-tech-group"><h3>Data & Knowledge</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">PostgreSQL 16</span><span class="auravis-tech-chip">pgvector-ready</span><span class="auravis-tech-chip">JPA Persistence</span><span class="auravis-tech-chip">RAG Knowledge Store</span><span class="auravis-tech-chip">Execution History</span></div></div>
                <div class="auravis-tech-group"><h3>Cloud & DevOps</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Docker</span><span class="auravis-tech-chip">Docker Compose</span><span class="auravis-tech-chip">GitHub Actions</span><span class="auravis-tech-chip">GHCR</span><span class="auravis-tech-chip">AWS EC2</span></div></div>
                <div class="auravis-tech-group"><h3>Edge & Delivery</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Nginx</span><span class="auravis-tech-chip">HTTPS / TLS</span><span class="auravis-tech-chip">DuckDNS</span><span class="auravis-tech-chip">Health Checks</span><span class="auravis-tech-chip">Rollback</span></div></div>
                <div class="auravis-tech-group"><h3>Product Experience</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Responsive Web UI</span><span class="auravis-tech-chip">TEJAS Product Guide</span><span class="auravis-tech-chip">Browser Speech Recognition</span><span class="auravis-tech-chip">Traffic Analytics</span><span class="auravis-tech-chip">Mission Dashboard</span></div></div>
                <div class="auravis-tech-group"><h3>Engineering Controls</h3><div class="auravis-tech-chips"><span class="auravis-tech-chip">Controlled Tool Boundaries</span><span class="auravis-tech-chip">Auditability</span><span class="auravis-tech-chip">Evidence-backed Decisions</span><span class="auravis-tech-chip">No UI Secrets</span></div></div>
              </div>
              <div class="auravis-tech-flow">Business Requirement → RAG / Knowledge → Intelligent Test Design → Java Orchestration → Playwright Execution → PostgreSQL Evidence → QA Decision → Docker / GitHub Actions → AWS EC2</div>
            </section>
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
