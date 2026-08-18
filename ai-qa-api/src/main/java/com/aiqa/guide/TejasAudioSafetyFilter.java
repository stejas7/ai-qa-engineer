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
 * Defensive browser-audio safety layer for TEJAS.
 * TEJAS supports microphone dictation only; chatbot responses must never be read aloud.
 * It also guarantees that closing/navigating away from TEJAS cancels any speech left
 * by an older cached client and stops active microphone recognition.
 *
 * @author Tejas Shah
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class TejasAudioSafetyFilter extends OncePerRequestFilter {

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
        if (html.contains("</body>")) {
            html = html.replace("</body>", safetyScript() + "</body>");
        }
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(bytes);
    }

    private String safetyScript() {
        return """
<script id="tejas-audio-safety-v4">
(() => {
  const cancelSpeech = () => {
    try {
      if ('speechSynthesis' in window) window.speechSynthesis.cancel();
    } catch (_) {}
  };

  // Cancel speech that may still be running from an older cached TEJAS version.
  cancelSpeech();

  const closeButton = document.getElementById('tejas-close');
  const panel = document.getElementById('tejas-panel');
  const mic = document.getElementById('tejas-mic');

  const hardStop = () => {
    cancelSpeech();
    // If voice dictation is active, use the existing microphone control to stop it.
    if (mic && mic.classList.contains('listening')) {
      try { mic.click(); } catch (_) {}
    }
  };

  if (closeButton) {
    closeButton.addEventListener('click', hardStop, true);
  }

  // Closing by any future UI mechanism must also silence legacy audio.
  if (panel && 'MutationObserver' in window) {
    new MutationObserver(() => {
      if (!panel.classList.contains('open')) hardStop();
    }).observe(panel, { attributes: true, attributeFilter: ['class'] });
  }

  document.addEventListener('visibilitychange', () => {
    if (document.hidden) hardStop();
  });
  window.addEventListener('pagehide', hardStop);
  window.addEventListener('beforeunload', hardStop);
})();
</script>
""";
    }

    private static final class BufferingResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private PrintWriter writer;

        BufferingResponse(HttpServletResponse response) {
            super(response);
        }

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
