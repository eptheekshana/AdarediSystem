(function () {
    if (typeof Chart === 'undefined' || !window.DASHBOARD_DATA) {
        return;
    }

    const data = window.DASHBOARD_DATA;
    const money = (v) => 'LKR ' + Number(v || 0).toLocaleString('en-LK');

    const charts = {};

    function cssVar(name) {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }

    function palette() {
        return [
            cssVar('--accent'),
            cssVar('--accent-2'),
            '#f59e0b',
            '#8b5cf6',
            '#ec4899',
            '#84cc16',
            '#ef4444',
            '#14b8a6'
        ];
    }

    function rgbVar(name, alpha) {
        return 'rgba(' + cssVar(name) + ', ' + alpha + ')';
    }

    function destroy(key) {
        if (charts[key]) {
            charts[key].destroy();
            charts[key] = null;
        }
    }

    function themeConfig() {
        return {
            text: cssVar('--text'),
            muted: cssVar('--muted'),
            border: cssVar('--border'),
            surface: cssVar('--surface-solid'),
            accent: cssVar('--accent'),
            accent2: cssVar('--accent-2')
        };
    }

    function commonOptions(theme, formatter) {
        return {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: theme.text
                    }
                },
                tooltip: {
                    backgroundColor: theme.surface,
                    titleColor: theme.text,
                    bodyColor: theme.text,
                    borderColor: theme.border,
                    borderWidth: 1,
                    callbacks: {
                        label: (ctx) => {
                            const label = ctx.dataset.label || ctx.label || '';
                            const value = formatter ? formatter(ctx.raw, ctx) : ctx.raw;
                            return label ? label + ': ' + value : String(value);
                        }
                    }
                }
            },
            scales: {}
        };
    }

    function renderCharts() {
        const theme = themeConfig();
        const colors = palette();

        Chart.defaults.color = theme.text;
        Chart.defaults.borderColor = theme.border;
        Chart.defaults.font.family = 'Inter, Segoe UI, system-ui, sans-serif';

        const trend = data.dailyTrend || [];
        destroy('trend');
        charts.trend = new Chart(document.getElementById('trendChart'), {
            type: 'line',
            data: {
                labels: trend.map((d) => d.date),
                datasets: [
                    {
                        label: 'Revenue',
                        data: trend.map((d) => d.revenue),
                        borderColor: colors[0],
                        backgroundColor: rgbVar('--accent-rgb', 0.16),
                        fill: true,
                        tension: 0.35,
                        pointRadius: 2
                    },
                    {
                        label: 'Net profit',
                        data: trend.map((d) => d.netProfit),
                        borderColor: colors[2],
                        backgroundColor: 'rgba(245, 158, 11, 0.12)',
                        fill: true,
                        tension: 0.35,
                        pointRadius: 2
                    }
                ]
            },
            options: {
                ...commonOptions(theme, (value) => money(value)),
                scales: {
                    x: {
                        ticks: { color: theme.muted },
                        grid: { color: theme.border }
                    },
                    y: {
                        ticks: {
                            color: theme.muted,
                            callback: (v) => money(v)
                        },
                        grid: { color: theme.border }
                    }
                }
            }
        });

        // Quantity chart (daily items sold)
        destroy('quantity');
        charts.quantity = new Chart(document.getElementById('quantityChart'), {
            type: 'bar',
            data: {
                labels: trend.map((d) => d.date),
                datasets: [{
                    label: 'Qty sold',
                    data: trend.map((d) => d.itemsSold || d.items || 0),
                    backgroundColor: colors[1]
                }]
            },
            options: {
                ...commonOptions(theme, (v) => Number(v).toLocaleString('en-LK')),
                scales: {
                    x: { ticks: { color: theme.muted }, grid: { color: theme.border } },
                    y: { ticks: { color: theme.muted, callback: (v) => Number(v).toLocaleString('en-LK') }, grid: { color: theme.border } }
                },
                plugins: { ...commonOptions(theme, (v) => Number(v).toLocaleString('en-LK')).plugins, legend: { display: false } }
            }
        });

        const districts = (data.byDistrict || []).slice(0, 8);
        destroy('district');
        charts.district = new Chart(document.getElementById('districtChart'), {
            type: 'bar',
            data: {
                labels: districts.map((d) => d.label),
                datasets: [{
                    label: 'Net profit',
                    data: districts.map((d) => d.profit),
                    backgroundColor: colors.slice(0, Math.max(districts.length, 1))
                }]
            },
            options: {
                ...commonOptions(theme, (value) => money(value)),
                scales: {
                    x: {
                        ticks: { color: theme.muted },
                        grid: { color: theme.border }
                    },
                    y: {
                        ticks: {
                            color: theme.muted,
                            callback: (v) => money(v)
                        },
                        grid: { color: theme.border }
                    }
                },
                plugins: {
                    ...commonOptions(theme, (value) => money(value)).plugins,
                    legend: { display: false }
                }
            }
        });

        const types = data.byBedsheetType || [];
        destroy('type');
        charts.type = new Chart(document.getElementById('typeChart'), {
            type: 'doughnut',
            data: {
                labels: types.map((t) => t.type),
                datasets: [{
                    data: types.map((t) => t.quantity),
                    backgroundColor: colors
                }]
            },
            options: {
                ...commonOptions(theme, (value, ctx) => ctx.dataset.label ? ctx.dataset.label + ': ' + value : value),
                plugins: {
                    ...commonOptions(theme, (value, ctx) => ctx.dataset.label ? ctx.dataset.label + ': ' + value : value).plugins,
                    legend: { labels: { color: theme.text } }
                }
            }
        });

        const sizes = data.byBedSize || [];
        destroy('size');
        charts.size = new Chart(document.getElementById('sizeChart'), {
            type: 'bar',
            data: {
                labels: sizes.map((s) => s.label),
                datasets: [{
                    label: 'Units sold',
                    data: sizes.map((s) => s.count),
                    backgroundColor: colors[1]
                }]
            },
            options: {
                ...commonOptions(theme, (value) => value),
                indexAxis: 'y',
                scales: {
                    x: {
                        ticks: {
                            color: theme.muted,
                            callback: (v) => Number(v).toLocaleString('en-LK')
                        },
                        grid: { color: theme.border }
                    },
                    y: {
                        ticks: { color: theme.muted },
                        grid: { color: theme.border }
                    }
                },
                plugins: {
                    ...commonOptions(theme, (value) => value).plugins,
                    legend: { display: false }
                }
            }
        });

        // Daily average order value
        destroy('avgOrder');
        charts.avgOrder = new Chart(document.getElementById('avgOrderChart'), {
            type: 'line',
            data: {
                labels: trend.map((d) => d.date),
                datasets: [{
                    label: 'Avg order value',
                    data: trend.map((d) => d.avgOrderValue),
                    borderColor: colors[3],
                    backgroundColor: 'rgba(139, 92, 246, 0.12)',
                    fill: true,
                    tension: 0.35,
                    pointRadius: 2
                }]
            },
            options: {
                ...commonOptions(theme, (value) => money(value)),
                scales: {
                    x: {
                        ticks: { color: theme.muted },
                        grid: { color: theme.border }
                    },
                    y: {
                        ticks: {
                            color: theme.muted,
                            callback: (v) => money(v)
                        },
                        grid: { color: theme.border }
                    }
                }
            }
        });

        // Daily profit margin percentage
        destroy('profitMargin');
        charts.profitMargin = new Chart(document.getElementById('profitMarginChart'), {
            type: 'line',
            data: {
                labels: trend.map((d) => d.date),
                datasets: [{
                    label: 'Profit margin %',
                    data: trend.map((d) => d.profitMarginPercent),
                    borderColor: colors[7],
                    backgroundColor: 'rgba(20, 184, 166, 0.12)',
                    fill: true,
                    tension: 0.35,
                    pointRadius: 2
                }]
            },
            options: {
                ...commonOptions(theme, (value) => (value || 0).toFixed(2) + '%'),
                scales: {
                    x: {
                        ticks: { color: theme.muted },
                        grid: { color: theme.border }
                    },
                    y: {
                        ticks: {
                            color: theme.muted,
                            callback: (v) => (v || 0).toFixed(2) + '%'
                        },
                        grid: { color: theme.border }
                    }
                }
            }
        });
    }

    renderCharts();

    window.addEventListener('themechange', renderCharts);
})();
