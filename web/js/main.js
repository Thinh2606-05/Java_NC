let currentInput = null;
let lastMiningResult = null;
let lastVerificationReport = null;
let loadedPresets = [];

document.addEventListener('DOMContentLoaded', async () => {
    setupTabs();
    setupLanguageSwitcher();

    // Khởi tạo ngôn ngữ mặc định (Tiếng Việt)
    I18N.setLanguage(I18N.currentLang);

    await loadPresets();
    await loadPresetData('lab1');

    document.getElementById('btn-run-mine').addEventListener('click', handleRunMine);
    document.getElementById('btn-run-verify').addEventListener('click', handleRunVerify);
    document.getElementById('preset-select').addEventListener('change', (e) => loadPresetData(e.target.value));

    // Lắng nghe sự kiện đổi ngôn ngữ để cập nhật nội dung động
    document.addEventListener('languageChanged', () => {
        updatePresetSelectLabels();
        const btnMine = document.getElementById('btn-run-mine');
        if (!btnMine.disabled) btnMine.textContent = I18N.t('btn_run_mine');
        const btnVerify = document.getElementById('btn-run-verify');
        if (!btnVerify.disabled) btnVerify.textContent = I18N.t('btn_run_verify');

        if (lastMiningResult) {
            renderMiningResults(lastMiningResult);
            if (lastMiningResult.trace) renderTraceSteps(lastMiningResult.trace);
        }
        if (lastVerificationReport) {
            renderVerificationReport(lastVerificationReport);
        }
    });
});

function setupLanguageSwitcher() {
    document.querySelectorAll('.lang-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const lang = e.currentTarget.getAttribute('data-lang');
            I18N.setLanguage(lang);
        });
    });
}

function setupTabs() {
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

            tab.classList.add('active');
            const targetId = tab.getAttribute('data-tab');
            document.getElementById(targetId).classList.add('active');
        });
    });
}

async function loadPresets() {
    try {
        loadedPresets = await API.getPresets();
        updatePresetSelectLabels();
    } catch (e) {
        console.error('Failed to load presets:', e);
    }
}

function updatePresetSelectLabels() {
    const select = document.getElementById('preset-select');
    const curVal = select.value || 'lab1';
    select.innerHTML = '';
    loadedPresets.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.id;
        opt.textContent = p.id + (p.hasHandRun ? I18N.t('handrun_available') : '');
        select.appendChild(opt);
    });
    if (curVal) select.value = curVal;
}

async function loadPresetData(id) {
    try {
        const data = await API.getPreset(id);
        currentInput = data;
        renderConfigAndData(data);
    } catch (e) {
        console.error('Failed to load preset data:', e);
    }
}

function renderConfigAndData(data) {
    document.getElementById('cfg-ws').value = data.config.windowSize;
    document.getElementById('cfg-delta').value = data.config.delta;
    document.getElementById('cfg-pruning').value = data.config.pruningStrategy;

    // Render Items
    const itemsTbody = document.getElementById('items-tbody');
    itemsTbody.innerHTML = '';
    data.items.forEach(it => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td><strong>${it.item}</strong></td><td>${it.eu}</td><td>${it.m}</td>`;
        itemsTbody.appendChild(tr);
    });

    // Render Transactions
    const txnsTbody = document.getElementById('txns-tbody');
    txnsTbody.innerHTML = '';
    data.transactions.forEach(t => {
        const tr = document.createElement('tr');
        const itemsStr = t.items.map(i => `${i.item}(${i.qty})`).join(' ');
        tr.innerHTML = `<td><strong>${t.tid}</strong></td><td>B${t.batch}</td><td>P${t.period}</td><td>${itemsStr}</td>`;
        txnsTbody.appendChild(tr);
    });
}

async function handleRunMine() {
    const btn = document.getElementById('btn-run-mine');
    btn.disabled = true;
    btn.textContent = I18N.t('btn_mining');

    // Update config from UI inputs
    currentInput.config.windowSize = parseInt(document.getElementById('cfg-ws').value, 10);
    currentInput.config.delta = parseFloat(document.getElementById('cfg-delta').value);
    currentInput.config.pruningStrategy = document.getElementById('cfg-pruning').value;

    try {
        const result = await API.runMining(currentInput);
        lastMiningResult = result;
        renderMiningResults(result);
        renderTraceSteps(result.trace);

        // Chuyển sang tab results
        document.querySelector('[data-tab="tab-results"]').click();
    } catch (e) {
        alert('Mining failed: ' + e.message);
    } finally {
        btn.disabled = false;
        btn.textContent = I18N.t('btn_run_mine');
    }
}

function renderMiningResults(result) {
    const container = document.getElementById('results-container');
    container.innerHTML = '';

    const totalPatterns = result.windows.reduce((acc, w) => acc + w.patterns.length, 0);

    const summaryBox = document.createElement('div');
    summaryBox.className = 'summary-stats';
    summaryBox.innerHTML = `
        <div class="stat-box">
            <div class="num">${result.windows.length}</div>
            <div class="lbl">${I18N.t('stat_windows_mined')}</div>
        </div>
        <div class="stat-box">
            <div class="num">${totalPatterns}</div>
            <div class="lbl">${I18N.t('stat_total_patterns')}</div>
        </div>
        <div class="stat-box">
            <div class="num">${Number(currentInput.config.delta).toFixed(2)}</div>
            <div class="lbl">${I18N.t('stat_threshold')}</div>
        </div>
    `;
    container.appendChild(summaryBox);

    result.windows.forEach(w => {
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
            <div class="card-title">
                <span>${I18N.t('card_window_title', { num: w.windowNumber, count: w.patterns.length })}</span>
                <span class="badge badge-primary">${w.elapsedMs} ms</span>
            </div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 8%;">${I18N.t('th_rank')}</th>
                        <th style="width: 37%;">${I18N.t('th_pattern')}</th>
                        <th style="width: 25%;">${I18N.t('th_tfur')}</th>
                        <th style="width: 15%;">${I18N.t('th_start_period')}</th>
                        <th style="width: 15%;">${I18N.t('th_status')}</th>
                    </tr>
                </thead>
                <tbody>
                    ${w.patterns.map((p, idx) => `
                        <tr>
                            <td>${idx + 1}</td>
                            <td><strong>${p.label}</strong></td>
                            <td><code>${Number(p.tFUR).toFixed(4)}</code></td>
                            <td>P${p.startPeriod}</td>
                            <td><span class="badge badge-pass">${I18N.t('badge_result')}</span></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
        container.appendChild(card);
    });
}

function renderTraceSteps(trace) {
    const container = document.getElementById('trace-container');
    container.innerHTML = '';

    trace.forEach(ev => {
        const item = document.createElement('div');
        item.className = 'card';
        let content = '';

        if (ev.type === 'BatchArrived') {
            content = `<div class="card-title">${I18N.t('trace_batch_arrived', { batch: ev.batchNumber, window: ev.windowNumber })}</div>`;
        } else if (ev.type === 'TransactionScanned') {
            content = `
                <div class="card-title">${I18N.t('trace_txn_scanned', { tid: ev.tid, batch: ev.batchNumber, period: ev.periodNumber })}</div>
                <p>TFU: <strong>${Number(ev.tfu).toFixed(2)}</strong> | MTFU: <strong>${Number(ev.mtfu).toFixed(2)}</strong></p>
            `;
        } else if (ev.type === 'BatchRemoved') {
            const count = ev.removedEntries ? ev.removedEntries.length : 0;
            content = `
                <div class="card-title" style="color: var(--danger)">${I18N.t('trace_batch_removed', { batch: ev.batchNumber })}</div>
                <p>${I18N.t('trace_batch_removed_desc', { count })}</p>
            `;
        } else if (ev.type === 'RestructureCompleted') {
            content = `
                <div class="card-title" style="color: var(--warning)">${I18N.t('trace_restructure', { window: ev.windowNumber })}</div>
                <p>${I18N.t('trace_sigma_tfu')} <strong>${Number(ev.sigmaTfu).toFixed(2)}</strong></p>
                <p>${I18N.t('trace_pruned_items')} <code>${ev.prunedItems.join(', ') || 'None'}</code></p>
                <p>${I18N.t('trace_processing_order')} <strong>${ev.processingOrder.join(' &lt; ')}</strong></p>
            `;
        } else if (ev.type === 'WindowMined') {
            content = `
                <div class="card-title" style="color: var(--success)">${I18N.t('trace_window_mined', { window: ev.windowNumber })}</div>
                <p>${I18N.t('trace_window_mined_desc', { count: ev.resultCount })}</p>
            `;
        } else {
            return;
        }

        item.innerHTML = content;
        container.appendChild(item);
    });
}

async function handleRunVerify() {
    const btn = document.getElementById('btn-run-verify');
    btn.disabled = true;
    btn.textContent = I18N.t('btn_verifying');

    const presetId = document.getElementById('preset-select').value;
    try {
        const report = await API.verify(presetId);
        lastVerificationReport = report;
        renderVerificationReport(report);
        document.querySelector('[data-tab="tab-verify"]').click();
    } catch (e) {
        alert('Verification failed: ' + e.message);
    } finally {
        btn.disabled = false;
        btn.textContent = I18N.t('btn_run_verify');
    }
}

function renderVerificationReport(rep) {
    const container = document.getElementById('verify-container');
    container.innerHTML = `
        <div class="summary-stats">
            <div class="stat-box"><div class="num" style="color: ${rep.isFullyPassing ? 'var(--success)' : 'var(--danger)'}">
                ${rep.isFullyPassing ? '100%' : 'FAIL'}
            </div><div class="lbl">${I18N.t('stat_handrun_match')}</div></div>
            <div class="stat-box"><div class="num" style="color: var(--success)">${rep.passCount}</div><div class="lbl">${I18N.t('stat_exact_pass')}</div></div>
            <div class="stat-box"><div class="num" style="color: var(--warning)">${rep.roundingCount}</div><div class="lbl">${I18N.t('stat_rounding_tol')}</div></div>
            <div class="stat-box"><div class="num" style="color: var(--danger)">${rep.mismatchCount}</div><div class="lbl">${I18N.t('stat_mismatch')}</div></div>
        </div>

        <div class="card">
            <div class="card-title">
                <span>${I18N.t('card_verify_detail', { count: rep.totalChecks })}</span>
                <span id="verify-count-badge" class="badge badge-primary">${rep.totalChecks} checks</span>
            </div>
            <div style="display: flex; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap;">
                <input type="text" id="verify-search-input" class="form-control" style="flex: 2; min-width: 250px;" placeholder="${I18N.t('filter_search_placeholder')}">
                <select id="verify-status-filter" class="form-control" style="flex: 1; min-width: 180px;">
                    <option value="ALL">${I18N.t('filter_all_status')}</option>
                    <option value="PASS">${I18N.t('filter_pass_only')}</option>
                    <option value="ROUNDING">${I18N.t('filter_rounding_only')}</option>
                    <option value="MISMATCH">${I18N.t('filter_mismatch_only')}</option>
                </select>
            </div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 25%;">${I18N.t('th_group')}</th>
                        <th style="width: 30%;">${I18N.t('th_metric')}</th>
                        <th style="width: 15%;">${I18N.t('th_expected')}</th>
                        <th style="width: 15%;">${I18N.t('th_actual')}</th>
                        <th style="width: 15%;">${I18N.t('th_result')}</th>
                    </tr>
                </thead>
                <tbody id="verify-tbody"></tbody>
            </table>
        </div>
    `;

    const searchInput = document.getElementById('verify-search-input');
    const statusFilter = document.getElementById('verify-status-filter');
    const tbody = document.getElementById('verify-tbody');
    const countBadge = document.getElementById('verify-count-badge');

    function updateTable() {
        const query = searchInput.value.trim().toLowerCase();
        const status = statusFilter.value;

        const filtered = rep.checks.filter(c => {
            const matchStatus = (status === 'ALL') ||
                (status === 'PASS' && c.status === 'PASS') ||
                (status === 'ROUNDING' && c.status === 'ROUNDING') ||
                (status === 'MISMATCH' && (c.status === 'MISMATCH' || c.status === 'MISSING'));

            if (!matchStatus) return false;
            if (!query) return true;

            return (c.name && c.name.toLowerCase().includes(query)) ||
                   (c.groupDescription && c.groupDescription.toLowerCase().includes(query)) ||
                   (c.expected && c.expected.toLowerCase().includes(query)) ||
                   (c.actual && c.actual.toLowerCase().includes(query));
        });

        countBadge.textContent = `${filtered.length} / ${rep.totalChecks} checks`;

        tbody.innerHTML = filtered.map(c => {
            let badgeClass = 'badge-pass';
            if (c.status === 'ROUNDING') badgeClass = 'badge-rounding';
            else if (c.status === 'MISMATCH' || c.status === 'MISSING') badgeClass = 'badge-mismatch';

            return `
                <tr>
                    <td><small>${c.groupDescription}</small></td>
                    <td><strong>${c.name}</strong></td>
                    <td>${c.expected}</td>
                    <td><code>${c.actual}</code></td>
                    <td><span class="badge ${badgeClass}">${c.status}</span></td>
                </tr>
            `;
        }).join('');
    }

    searchInput.addEventListener('input', updateTable);
    statusFilter.addEventListener('change', updateTable);
    updateTable();
}
