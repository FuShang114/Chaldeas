let debugState = {
    sessionId: null,
    steps: [],
    currentIndex: -1
};

let callGraphChart = null;

function safeJsonParse(text) {
    if (!text || !text.trim()) return null;
    try {
        return JSON.parse(text);
    } catch (e) {
        // 当作原始值处理
        return text;
    }
}

function formatJson(obj) {
    try {
        return JSON.stringify(obj, null, 2);
    } catch (e) {
        return String(obj);
    }
}

function setupSend() {
    const btn = document.getElementById('send-btn');
    const confirmBtn = document.getElementById('confirm-btn');
    const nextBtn = document.getElementById('next-btn');
    // 调试（Send）：执行 + 建立会话，可使用 Next
    btn.addEventListener('click', async () => {
        const target = document.getElementById('target-method').value.trim();
        const inputRaw = document.getElementById('input-json').value;
        const input = safeJsonParse(inputRaw);
        document.getElementById('output-json').textContent = '';
        document.getElementById('step-info').textContent = '执行中...';
        nextBtn.disabled = true;

        try {
            const resp = await fetch('/api/debug/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetMethod: target, input })
            });
            const data = await resp.json();
            if (!data.success) {
                document.getElementById('step-info').textContent = '错误：' + data.error;
                return;
            }
            debugState.sessionId = data.sessionId;
            debugState.steps = data.steps || [];
            debugState.currentIndex = 0;
            nextBtn.disabled = debugState.steps.length <= 1;
            renderStep(data.step);
            renderGraph();
        } catch (e) {
            document.getElementById('step-info').textContent = '请求失败：' + e;
        }
    });

    // 确定：仅分析调用链路，不建立调试会话，不启用 Next
    confirmBtn.addEventListener('click', async () => {
        const target = document.getElementById('target-method').value.trim();
        const inputRaw = document.getElementById('input-json').value;
        const input = safeJsonParse(inputRaw);
        document.getElementById('step-info').textContent = '分析调用链路中...';
        document.getElementById('output-json').textContent = '';
        nextBtn.disabled = true;
        debugState.sessionId = null;

        try {
            const resp = await fetch('/api/debug/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetMethod: target, input })
            });
            const data = await resp.json();
            if (!data.success) {
                document.getElementById('step-info').textContent = '错误：' + data.error;
                return;
            }
            // 只取 steps 来渲染拓扑，不进入调试会话
            debugState.steps = data.steps || [];
            debugState.currentIndex = debugState.steps.length > 0 ? 0 : -1;
            renderGraph();
            document.getElementById('step-info').textContent = '调用链路分析完成（未进入调试模式）';
        } catch (e) {
            document.getElementById('step-info').textContent = '请求失败：' + e;
        }
    });
}

function setupNext() {
    const nextBtn = document.getElementById('next-btn');
    nextBtn.addEventListener('click', async () => {
        if (!debugState.sessionId) return;
        try {
            const resp = await fetch('/api/debug/next', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: debugState.sessionId })
            });
            const data = await resp.json();
            if (!data.success) {
                document.getElementById('step-info').textContent = '错误：' + data.error;
                return;
            }
            debugState.steps = data.steps || debugState.steps;
            // 当前索引由后端根据会话状态维护，这里直接从步骤中推断最大 index
            if (data.step && typeof data.step.index === 'number') {
                debugState.currentIndex = data.step.index;
            } else if (debugState.currentIndex < debugState.steps.length - 1) {
                debugState.currentIndex++;
            }
            renderStep(data.step);
            renderGraph();
        } catch (e) {
            document.getElementById('step-info').textContent = '请求失败：' + e;
        }
    });
}

function renderStep(step) {
    if (!step) {
        document.getElementById('step-info').textContent = '无步骤数据';
        document.getElementById('output-json').textContent = '';
        return;
    }
    const header = [
        `方法：${step.className}#${step.methodName}`,
        `来源：${step.sourceType}`,
        `开始时间：${step.startTime || '-'}`,
        `结束时间：${step.endTime || '-'}`,
        `执行时长：${step.durationMillis != null ? step.durationMillis + ' ms' : '-'}`
    ].join('\n');
    document.getElementById('step-info').textContent = header;
    document.getElementById('args-json').textContent = formatJson(step.args);
    if (step.error) {
        document.getElementById('output-json').textContent = '错误：\n' + step.error;
    } else {
        document.getElementById('output-json').textContent = formatJson(step.returnValue);
    }
}

// 渲染调用链拓扑图
function renderGraph() {
    const dom = document.getElementById('call-graph');
    if (!dom || typeof echarts === 'undefined') {
        return;
    }
    if (!callGraphChart) {
        callGraphChart = echarts.init(dom);
    }
    const chart = callGraphChart;
    if (!debugState.steps || debugState.steps.length === 0) {
        chart.clear();
        chart.setOption({ title: { text: '暂无调用步骤', left: 'center' } });
        return;
    }
    const nodes = debugState.steps.map((s, idx) => {
        let color = '#d1d5db'; // pending
        if (s.hasError) {
            color = '#ef4444'; // red
        } else if (idx < debugState.currentIndex) {
            color = '#16a34a'; // green
        } else if (idx === debugState.currentIndex) {
            color = '#f97316'; // orange
        }
        return {
            id: s.id,
            name: s.name,
            symbolSize: 40,
            itemStyle: { color }
        };
    });
    const links = [];
    for (let i = 0; i < nodes.length - 1; i++) {
        links.push({ source: nodes[i].id, target: nodes[i + 1].id });
    }
    chart.clear();
    chart.setOption({
        animation: false,
        tooltip: { show: true },
        series: [{
            type: 'graph',
            layout: 'force',
            roam: true,
            symbol: 'circle',
            data: nodes,
            links,
            label: {
                show: true,
                formatter: params => params.name
            },
            lineStyle: {
                color: '#9ca3af'
            }
        }]
    });

    chart.off('click');
    chart.on('click', params => {
        const name = params.data && params.data.name ? params.data.name : '';
        const panel = document.getElementById('analysis-content');
        panel.textContent = name
            ? `方法：${name}\n\n这里预留给大模型做方法级分析与预测结果对比等信息。`
            : '点击左侧拓扑中的节点，这里将展示该方法的分析信息（预留区域）。';
    });
}

async function loadTree() {
    const container = document.getElementById('tree-container');
    container.textContent = '';
    if (!debugState.sessionId) {
        container.textContent = '尚未开始调试会话';
        return;
    }
    try {
        const resp = await fetch('/api/debug/tree?sessionId=' + encodeURIComponent(debugState.sessionId));
        const data = await resp.json();
        if (!data.success) {
            container.textContent = '加载失败：' + data.error;
            return;
        }
        const tree = data.tree;
        const div = document.createElement('div');
        div.textContent = tree.name + ' (' + tree.sourceType + ')';
        container.appendChild(div);
    } catch (e) {
        container.textContent = '请求失败：' + e;
    }
}

async function loadTopology() {
    const dom = document.getElementById('topology-chart');
    const chart = echarts.init(dom);
    if (!debugState.sessionId) {
        chart.setOption({ title: { text: '尚未开始调试会话' } });
        return;
    }
    try {
        const resp = await fetch('/api/debug/topology?sessionId=' + encodeURIComponent(debugState.sessionId));
        const data = await resp.json();
        if (!data.success) {
            chart.setOption({ title: { text: '加载失败：' + data.error } });
            return;
        }
        const nodes = data.nodes || [];
        const edges = data.edges || [];
        chart.setOption({
            title: { text: '调用拓扑', left: 'center' },
            tooltip: {},
            series: [{
                type: 'graph',
                layout: 'force',
                roam: true,
                data: nodes.map(n => ({
                    name: n.name,
                    value: n.id,
                    symbolSize: 60,
                    itemStyle: {
                        color: n.sourceType === 'PROJECT' ? '#16a34a' : '#facc15'
                    }
                })),
                links: edges.map(e => ({ source: e.sourceId, target: e.targetId })),
                label: { show: true }
            }]
        });
    } catch (e) {
        chart.setOption({ title: { text: '请求失败：' + e } });
    }
}

function initApp() {
    setupSend();
    setupNext();
}

window.addEventListener('DOMContentLoaded', initApp);

