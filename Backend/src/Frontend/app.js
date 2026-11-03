document.addEventListener('DOMContentLoaded', () => {
  marked.setOptions({
    highlight: function (code, lang) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext';
      return hljs.highlight(code, { language }).value;
    },
    breaks: true
  });

  const form = document.getElementById('research-form');
  const submitBtn = document.getElementById('submit-btn');
  const btnText = submitBtn.querySelector('.btn-text');
  const spinner = submitBtn.querySelector('.spinner');
  const outputContainer = document.getElementById('output-container');
  const copyBtn = document.getElementById('copy-btn');
  const reportTitle = document.getElementById('report-title');
  const historyList = document.getElementById('history-list');

  let rawMarkdown = '';
  let activeEventSource = null;

  loadHistory();

  form.addEventListener('submit', (e) => {
    e.preventDefault();

    const topic = document.getElementById('topic').value.trim();
    const mode = document.getElementById('mode').value;

    if (!topic) return;

    if (activeEventSource) {
      activeEventSource.close();
    }

    setLoading(true);
    reportTitle.textContent = topic;
    outputContainer.innerHTML = `
      <div id="stream-logs" style="background: #090d16; padding: 1.25rem; border-radius: 8px; border: 1px solid #334155; font-family: monospace; font-size: 0.9rem; color: #38bdf8;">
        <p>🚀 Initiating research process...</p>
      </div>`;

    const logsContainer = document.getElementById('stream-logs');

    // Connect to SSE Endpoint
    const streamUrl = `http://localhost:8080/api/research/stream?query=${encodeURIComponent(topic)}&mode=${mode}&maxIterations=2`;
    activeEventSource = new EventSource(streamUrl);

    // 1. Progress Status Listener
    activeEventSource.addEventListener('status', (event) => {
      const logLine = document.createElement('p');
      logLine.style.marginTop = '0.5rem';
      logLine.textContent = `> ${event.data}`;
      logsContainer.appendChild(logLine);
      logsContainer.scrollTop = logsContainer.scrollHeight;
    });

    // 2. Completion Listener
    activeEventSource.addEventListener('complete', (event) => {
      rawMarkdown = event.data || '# No Content Returned';
      outputContainer.innerHTML = marked.parse(rawMarkdown);
      copyBtn.disabled = false;
      setLoading(false);
      activeEventSource.close();
      activeEventSource = null;

      loadHistory();
    });

    // 3. Error Listener
    activeEventSource.addEventListener('error', (err) => {
      outputContainer.innerHTML = `
        <div style="color: #ef4444; padding: 1rem; background: #450a0a; border-radius: 6px;">
          <h3>Error Generating Report</h3>
          <p>Stream connection interrupted or backend execution error.</p>
        </div>`;
      setLoading(false);
      if (activeEventSource) {
        activeEventSource.close();
        activeEventSource = null;
      }
    });
  });

  copyBtn.addEventListener('click', () => {
    if (!rawMarkdown) return;
    navigator.clipboard.writeText(rawMarkdown).then(() => {
      const originalText = copyBtn.textContent;
      copyBtn.textContent = 'Copied!';
      setTimeout(() => copyBtn.textContent = originalText, 2000);
    });
  });

  async function loadHistory() {
    if (!historyList) return;

    try {
      const res = await fetch('http://localhost:8080/api/history');
      if (!res.ok) return;
      const reports = await res.json();

      if (!Array.isArray(reports) || reports.length === 0) {
        historyList.innerHTML = '<li class="history-placeholder">No past research yet</li>';
        return;
      }

      historyList.innerHTML = reports.map(item => `
        <li class="history-item" data-id="${item.id}" title="${escapeHtml(item.query)}">
          ${escapeHtml(item.query)}
        </li>
      `).join('');

      document.querySelectorAll('.history-item').forEach(el => {
        el.addEventListener('click', () => loadHistoricalReport(el.dataset.id));
      });

    } catch (err) {
      console.error('Failed to load history:', err);
    }
  }

  async function loadHistoricalReport(id) {
    try {
      outputContainer.innerHTML = '<p class="placeholder-text">Loading historical report...</p>';
      const res = await fetch(`http://localhost:8080/api/history/${id}`);
      if (!res.ok) return;
      
      const data = await res.json();

      reportTitle.textContent = data.query;
      rawMarkdown = data.finalReport || '# No Content';
      outputContainer.innerHTML = marked.parse(rawMarkdown);
      copyBtn.disabled = false;
    } catch (err) {
      console.error('Failed to fetch historical report:', err);
    }
  }

  function setLoading(isLoading) {
    submitBtn.disabled = isLoading;
    if (isLoading) {
      btnText.textContent = 'Researching...';
      spinner.classList.remove('hidden');
    } else {
      btnText.textContent = 'Generate Report';
      spinner.classList.add('hidden');
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
});