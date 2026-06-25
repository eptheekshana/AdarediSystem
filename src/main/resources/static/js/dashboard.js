
// dashboard.js — Chart.js rendering for ADAREDI dashboard

(function () {
    const isDark = () => document.documentElement.dataset.theme === 'dark';

    const colors = {
        primary: '#4f46e5',
        primaryLight: 'rgba(79,70,229,0.15)',
        success: '#10b981',
        successLight: 'rgba(16,185,129,0.15)',
        purple: '#8b5cf6',
        amber: '#f59e0b',
        rose: '#f43f5e',
        teal: '#14b8a6',
        orange: '#f97316',
        cyan: '#06b6d4',
    };

    function getTextColor() { return isDark() ? '#94a3b8' : '#64748b'; }
    function getBorderColor() { return isDark() ? '#334155' : '#e2e8f0'; }
    function getGridColor() { return isDark() ? 'rgba(51,65,85,0.5)' : 'rgba(226,232,240,0.7)'; }

    const defaultFont = { family: "'Inter', sans-serif", size: 12 };

    // Revenue & Profit Line Chart
    const revenueCtx = document.getElementById('revenueChart');
    if (revenueCtx && typeof chartData !== 'undefined' && chartData.daily.labels.length > 0) {
        new Chart(revenueCtx, {
            type: 'line',
            data: {
                labels: chartData.daily.labels,
                datasets: [
                    {
                        label: 'Revenue',
                        data: chartData.daily.revenue,
                        borderColor: colors.primary,
                        backgroundColor: colors.primaryLight,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        borderWidth: 2,
                    },
                    {
                        label: 'Net Profit',
                        data: chartData.daily.profit,
                        borderColor: colors.success,
                        backgroundColor: colors.successLight,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        borderWidth: 2,
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { labels: { color: getTextColor(), font: defaultFont, boxWidth: 12, padding: 16 } },
                    tooltip: {
                        backgroundColor: isDark() ? '#1e293b' : '#ffffff',
                        titleColor: isDark() ? '#f8fafc' : '#0f172a',
                        bodyColor: isDark() ? '#94a3b8' : '#64748b',
                        borderColor: getBorderColor(),
                        borderWidth: 1,
                        padding: 12,
                        callbacks: {
                            label: ctx => ` ${ctx.dataset.label}: LKR ${Number(ctx.raw).toLocaleString()}`
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: getTextColor(), font: defaultFont, maxTicksLimit: 10 },
                        grid: { color: getGridColor() }
                    },
                    y: {
                        ticks: {
                            color: getTextColor(),
                            font: defaultFont,
                            callback: v => 'LKR ' + Number(v).toLocaleString()
                        },
                        grid: { color: getGridColor() }
                    }
                }
            }
        });
    }

    // Product Mix Donut Chart
    const productCtx = document.getElementById('productChart');
    if (productCtx && typeof chartData !== 'undefined' && chartData.product.labels.length > 0) {
        const palette = [colors.primary, colors.success, colors.purple, colors.amber, colors.rose, colors.teal, colors.orange, colors.cyan];
        new Chart(productCtx, {
            type: 'doughnut',
            data: {
                labels: chartData.product.labels,
                datasets: [{
                    data: chartData.product.qty,
                    backgroundColor: palette.slice(0, chartData.product.labels.length),
                    borderWidth: 2,
                    borderColor: isDark() ? '#1e293b' : '#ffffff',
                    hoverOffset: 6,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: getTextColor(), font: defaultFont, padding: 12, boxWidth: 12 }
                    },
                    tooltip: {
                        backgroundColor: isDark() ? '#1e293b' : '#ffffff',
                        titleColor: isDark() ? '#f8fafc' : '#0f172a',
                        bodyColor: isDark() ? '#94a3b8' : '#64748b',
                        borderColor: getBorderColor(),
                        borderWidth: 1,
                        padding: 12,
                        callbacks: {
                            label: ctx => ` ${ctx.label}: ${ctx.raw} units`
                        }
                    }
                }
            }
        });
    }

    // District Bar Chart
    const districtCtx = document.getElementById('districtChart');
    if (districtCtx && typeof chartData !== 'undefined' && chartData.district.labels.length > 0) {
        const topLabels = chartData.district.labels.slice(0, 8);
        const topData = chartData.district.profit.slice(0, 8);
        new Chart(districtCtx, {
            type: 'bar',
            data: {
                labels: topLabels,
                datasets: [{
                    label: 'Profit (LKR)',
                    data: topData,
                    backgroundColor: topLabels.map((_, i) => i === 0 ? colors.primary : colors.primaryLight.replace('0.15', '0.6')),
                    borderRadius: 6,
                    borderSkipped: false,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: isDark() ? '#1e293b' : '#ffffff',
                        titleColor: isDark() ? '#f8fafc' : '#0f172a',
                        bodyColor: isDark() ? '#94a3b8' : '#64748b',
                        borderColor: getBorderColor(),
                        borderWidth: 1,
                        padding: 12,
                        callbacks: {
                            label: ctx => ` LKR ${Number(ctx.raw).toLocaleString()}`
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            color: getTextColor(),
                            font: defaultFont,
                            callback: v => 'LKR ' + Number(v).toLocaleString()
                        },
                        grid: { color: getGridColor() }
                    },
                    y: {
                        ticks: { color: getTextColor(), font: defaultFont },
                        grid: { display: false }
                    }
                }
            }
        });
    }

    // Mark active nav link
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (currentPath.includes(link.getAttribute('href'))) {
            link.classList.add('active');
        }
    });
})();
