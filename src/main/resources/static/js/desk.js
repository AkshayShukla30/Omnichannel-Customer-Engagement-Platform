(function () {
  const POLL_MS = 2000;
  const $ = (id) => document.getElementById(id);
  let pollTimer = null;
  let clockTimer = null;
  let currentInteractionId = null;
  let assignedAt = null;

  const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });
  const timeFormat = new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' });

  function showMessage(text, kind) {
    const box = $('message');
    box.textContent = text;
    box.className = 'banner ' + (kind || 'info');
    box.hidden = false;
    clearTimeout(showMessage.timer);
    showMessage.timer = setTimeout(() => { box.hidden = true; }, 5000);
  }

  function showLogin() {
    stopPolling();
    $('login-view').hidden = false;
    $('desk-view').hidden = true;
    $('who').hidden = true;
  }

  function showDesk() {
    $('login-view').hidden = true;
    $('desk-view').hidden = false;
    $('who').hidden = false;
    startPolling();
  }

  function startPolling() {
    stopPolling();
    refresh();
    pollTimer = setInterval(refresh, POLL_MS);
    clockTimer = setInterval(tickTimer, 1000);
  }

  function stopPolling() {
    clearInterval(pollTimer);
    clearInterval(clockTimer);
  }

  function tickTimer() {
    if (assignedAt) $('timer').textContent = formatClock((Date.now() - assignedAt) / 1000);
  }

  async function refresh() {
    try {
      render(await api('/api/desk/state'));
    } catch (e) {
      if (e.status === 401) showLogin();
      else showMessage(e.message, 'error');
    }
  }

  function render(state) {
    $('agent-name').textContent = state.agent.fullName;
    const status = $('status');
    status.value = state.agent.status;
    status.disabled = state.agent.status === 'BUSY';

    const interaction = state.interaction;
    $('idle').hidden = !!interaction;
    $('active').hidden = !interaction;
    $('idle-text').textContent = state.agent.status === 'OFFLINE'
      ? 'You are offline. Switch to Available to receive interactions.'
      : 'You are available. New calls and chats show up here automatically.';

    if (!interaction) {
      currentInteractionId = null;
      assignedAt = null;
      return;
    }

    if (interaction.id !== currentInteractionId) {
      currentInteractionId = interaction.id;
      assignedAt = new Date(interaction.assignedAt).getTime();
      $('note-text').value = '';
      $('disposition').value = '';
      renderInteractionHeader(interaction);
      renderCustomer(state.customer, state.customerError);
    }
    renderNotes(state.notes);
    tickTimer();
  }

  function renderInteractionHeader(i) {
    $('interaction-title').textContent = (i.channel === 'CALL' ? 'Incoming call' : 'Incoming chat') + ' #' + i.id;
    const chip = $('skill-chip');
    chip.textContent = i.skill.charAt(0) + i.skill.slice(1).toLowerCase();
    chip.className = 'chip skill-' + i.skill;
    $('priority-chip').hidden = i.priority !== 1;
  }

  function renderCustomer(customer, error) {
    $('customer-error').hidden = !error;
    $('customer-error').textContent = error || '';
    $('customer-panel').hidden = !customer;
    if (!customer) return;
    $('c-name').textContent = customer.fullName;
    $('c-id').textContent = customer.customerId;
    $('c-segment').textContent = customer.segment;
    $('c-phone').textContent = customer.phone || '-';
    $('c-email').textContent = customer.email || '-';
    $('c-account').textContent = customer.accountNumber;
    $('c-balance').textContent = money.format(customer.accountBalance);
    $('c-loans').textContent = String(customer.activeLoans);
    $('c-card').textContent = customer.cardType;
    $('c-source').textContent = 'Data from the "' + customer.source + '" adapter';
  }

  function renderNotes(notes) {
    const list = $('notes');
    list.replaceChildren();
    notes.forEach((n) => {
      const item = el('li', null, n.body);
      item.prepend(el('time', null, timeFormat.format(new Date(n.createdAt))));
      list.appendChild(item);
    });
    list.scrollTop = list.scrollHeight;
  }

  $('login-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: $('username').value.trim(), password: $('password').value }),
      });
      $('password').value = '';
      showDesk();
    } catch (e) {
      showMessage(e.message, 'error');
    }
  });

  $('logout').addEventListener('click', async () => {
    try {
      await api('/api/auth/logout', { method: 'POST' });
      showLogin();
    } catch (e) {
      showMessage(e.message, 'error');
    }
  });

  $('status').addEventListener('change', async (event) => {
    try {
      await api('/api/desk/status', { method: 'POST', body: JSON.stringify({ status: event.target.value }) });
      refresh();
    } catch (e) {
      showMessage(e.message, 'error');
      refresh();
    }
  });

  $('add-note').addEventListener('click', async () => {
    const text = $('note-text').value.trim();
    if (!text) return showMessage('Write something first', 'error');
    try {
      await api('/api/desk/interactions/' + currentInteractionId + '/notes', {
        method: 'POST',
        body: JSON.stringify({ text }),
      });
      $('note-text').value = '';
      refresh();
    } catch (e) {
      showMessage(e.message, 'error');
    }
  });

  $('close-interaction').addEventListener('click', async () => {
    const disposition = $('disposition').value;
    if (!disposition) return showMessage('Pick a disposition before closing', 'error');
    const note = $('note-text').value.trim();
    try {
      await api('/api/desk/interactions/' + currentInteractionId + '/close', {
        method: 'POST',
        body: JSON.stringify({ disposition, note: note || null }),
      });
      showMessage('Interaction closed', 'info');
      refresh();
    } catch (e) {
      showMessage(e.message, 'error');
    }
  });

  $('simulate').addEventListener('click', async () => {
    try {
      const created = await api('/api/simulator/interactions', {
        method: 'POST',
        body: JSON.stringify({ count: Number($('sim-count').value) }),
      });
      showMessage('Generated ' + created.length + ' interaction(s)', 'info');
      refresh();
    } catch (e) {
      showMessage(e.message, 'error');
    }
  });

  (async function init() {
    try {
      await api('/api/auth/me');
      showDesk();
    } catch (e) {
      showLogin();
    }
  })();
})();
