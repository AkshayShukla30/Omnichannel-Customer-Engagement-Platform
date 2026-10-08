(function () {
  const $ = (id) => document.getElementById(id);
  const palette = ['#0e5a64', '#4a4fa3', '#b3261e', '#7a8794', '#c98a1b', '#2f8f5b'];
  const charts = {};

  function chart(id, type, options) {
    charts[id] = new Chart($(id), {
      type,
      data: { labels: [], datasets: [{ data: [], backgroundColor: palette, borderWidth: 0 }] },
      options: Object.assign({ responsive: true, maintainAspectRatio: false, plugins: { legend: { display: type === 'doughnut' } } }, options),
    });
  }

  function update(id, labels, values) {
    const c = charts[id];
    c.data.labels = labels;
    c.data.datasets[0].data = values;
    c.update();
  }

  function formatSeconds(seconds) {
    if (!seconds) return '-';
    const m = Math.floor(seconds / 60);
    return m + 'm ' + String(Math.round(seconds % 60)).padStart(2, '0') + 's';
  }

  function pretty(name) {
    return name.charAt(0) + name.slice(1).toLowerCase().replace(/_/g, ' ');
  }

  async function load() {
    const days = $('days').value;
    try {
      const [aht, agents, queue, dispositions] = await Promise.all([
        api('/api/reports/handle-time?days=' + days),
        api('/api/reports/interactions-per-agent?days=' + days),
        api('/api/reports/queue-length'),
        api('/api/reports/dispositions?days=' + days),
      ]);

      update('chart-aht', aht.map((r) => pretty(r.skill)), aht.map((r) => r.avgSeconds));
      update('chart-agents', agents.map((r) => r.agent), agents.map((r) => r.interactions));
      update('chart-queue', queue.map((r) => pretty(r.skill)), queue.map((r) => r.waiting));
      update('chart-dispositions', dispositions.map((r) => pretty(r.disposition)), dispositions.map((r) => r.total));

      const closed = aht.reduce((sum, r) => sum + r.interactions, 0);
      const weighted = aht.reduce((sum, r) => sum + r.avgSeconds * r.interactions, 0);
      $('kpi-closed').textContent = String(closed);
      $('kpi-aht').textContent = closed ? formatSeconds(weighted / closed) : '-';
      $('kpi-queue').textContent = String(queue.reduce((sum, r) => sum + r.waiting, 0));
      const top = agents[0];
      $('kpi-top').textContent = top && top.interactions > 0 ? top.agent : '-';
      $('stamp').textContent = 'Updated ' + new Date().toLocaleTimeString();
      $('message').hidden = true;
    } catch (e) {
      if (e.status === 401) {
        window.location.href = '/';
        return;
      }
      $('message').textContent = e.message;
      $('message').hidden = false;
    }
  }

  chart('chart-aht', 'bar', { plugins: { legend: { display: false } } });
  chart('chart-agents', 'bar', { indexAxis: 'y', plugins: { legend: { display: false } } });
  chart('chart-queue', 'bar', { scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }, plugins: { legend: { display: false } } });
  chart('chart-dispositions', 'doughnut', {});

  $('days').addEventListener('change', load);
  load();
  setInterval(load, 5000);
})();
