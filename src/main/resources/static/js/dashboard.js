// dashboard.js — High-End Glassmorphic Chart.js Renderer for ADAREDI Operations

(function () {
    const isDark = () => document.documentElement.dataset.theme === 'dark';

    const getColors = () => {
        const dark = isDark();
        return {
            primary: dark ? '#818cf8' : '#4f46e5',
            primaryLight: dark ? 'rgba(129, 140, 248, 0.18)' : 'rgba(79, 70, 229, 0.12)',
            success: dark ? '#34d399' : '#059669',
            successLight: dark ? 'rgba(52, 211, 153, 0.18)' : 'rgba(5, 150, 105, 0.12)',
            purple: dark ? '#a78bfa' : '#8b5cf6',
            amber: dark ? '#fbbf24' : '#f59e0b',
            rose: dark ? '#fb7185' : '#f43f5e',
            teal: dark ? '#2dd4bf' : '#14b8a6',
            text: dark ? '#94a3b8' : '#64748b',
            titleText: dark ? '#f8fafc' : '#0f172a',
            border: dark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(226, 232, 240, 0.9)',
            grid: dark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(226, 232, 240, 0.6)',
            tooltipBg: dark ? 'rgba(17, 24, 39, 0.95)' : 'rgba(255, 255, 255, 0.95)',
        };
    };

    const fontConfig = {
        family: "'Plus Jakarta Sans', -apple-system, sans-serif",
        size: 11,
        weight: '600'
    };

    let charts = [];

    function renderAllCharts() {
        // Destroy previous instances if re-rendering on theme change
        charts.forEach(c => c.destroy());
        charts = [];

        const colors = getColors();

        // 1. Revenue & Profit Line Chart
        const revenueCanvas = document.getElementById('revenueChart');
        if (revenueCanvas && typeof chartData !== 'undefined' && chartData.daily && chartData.daily.labels.length > 0) {
            const ctx = revenueCanvas.getContext('2d');
            
            // Gradients
            const revGradient = ctx.createLinearGradient(0, 0, 0, 300);
            revGradient.addColorStop(0, colors.primaryLight);
            revGradient.addColorStop(1, 'rgba(79, 70, 229, 0)');

            const profitGradient = ctx.createLinearGradient(0, 0, 0, 300);
            profitGradient.addColorStop(0, colors.successLight);
            profitGradient.addColorStop(1, 'rgba(5, 150, 105, 0)');

            const revChart = new Chart(revenueCanvas, {
                type: 'line',
                data: {
                    labels: chartData.daily.labels,
                    datasets: [
                        {
                            label: 'Revenue',
                            data: chartData.daily.revenue,
                            borderColor: colors.primary,
                            backgroundColor: revGradient,
                            fill: true,
                            tension: 0.38,
                            pointRadius: 4,
                            pointBackgroundColor: colors.primary,
                            pointBorderColor: '#ffffff',
                            pointBorderWidth: 2,
                            pointHoverRadius: 7,
                            borderWidth: 2.5,
                        },
                        {
                            label: 'Net Profit',
                            data: chartData.daily.profit,
                            borderColor: colors.success,
                            backgroundColor: profitGradient,
                            fill: true,
                            tension: 0.38,
                            pointRadius: 4,
                            pointBackgroundColor: colors.success,
                            pointBorderColor: '#ffffff',
                            pointBorderWidth: 2,
                            pointHoverRadius: 7,
                            borderWidth: 2.5,
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: 'index', intersect: false },
                    plugins: {
                        legend: {
                            position: 'top',
                            align: 'end',
                            labels: {
                                color: colors.text,
                                font: fontConfig,
                                boxWidth: 10,
                                boxHeight: 10,
                                usePointStyle: true,
                                padding: 16
                            }
                        },
                        tooltip: {
                            backgroundColor: colors.tooltipBg,
                            titleColor: colors.titleText,
                            bodyColor: colors.text,
                            borderColor: colors.border,
                            borderWidth: 1,
                            padding: 12,
                            boxPadding: 6,
                            cornerRadius: 10,
                            titleFont: { family: fontConfig.family, size: 12, weight: '700' },
                            bodyFont: fontConfig,
                            callbacks: {
                                label: ctx => ` ${ctx.dataset.label}: LKR ${Number(ctx.raw).toLocaleString()}`
                            }
                        }
                    },
                    scales: {
                        x: {
                            ticks: { color: colors.text, font: fontConfig, maxTicksLimit: 12 },
                            grid: { color: colors.grid, drawBorder: false }
                        },
                        y: {
                            ticks: {
                                color: colors.text,
                                font: fontConfig,
                                callback: v => 'LKR ' + (Number(v) >= 1000 ? (v / 1000) + 'k' : v)
                            },
                            grid: { color: colors.grid, drawBorder: false }
                        }
                    }
                }
            });
            charts.push(revChart);
        }

        // 2. Product Mix Donut Chart
        const productCanvas = document.getElementById('productChart');
        if (productCanvas && typeof chartData !== 'undefined' && chartData.product && chartData.product.labels.length > 0) {
            const palette = [colors.primary, colors.success, colors.purple, colors.amber, colors.rose, colors.teal];
            const prodChart = new Chart(productCanvas, {
                type: 'doughnut',
                data: {
                    labels: chartData.product.labels,
                    datasets: [{
                        data: chartData.product.qty,
                        backgroundColor: palette,
                        borderWidth: 2,
                        borderColor: isDark() ? '#111827' : '#ffffff',
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '70%',
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: colors.text,
                                font: fontConfig,
                                boxWidth: 10,
                                boxHeight: 10,
                                usePointStyle: true,
                                padding: 14
                            }
                        },
                        tooltip: {
                            backgroundColor: colors.tooltipBg,
                            titleColor: colors.titleText,
                            bodyColor: colors.text,
                            borderColor: colors.border,
                            borderWidth: 1,
                            padding: 10,
                            cornerRadius: 8,
                            callbacks: {
                                label: ctx => ` ${ctx.label}: ${ctx.raw} units`
                            }
                        }
                    }
                }
            });
            charts.push(prodChart);
        }

        // 3. District Profit Bar Chart
        const districtCanvas = document.getElementById('districtChart');
        if (districtCanvas && typeof chartData !== 'undefined' && chartData.district && chartData.district.labels.length > 0) {
            const distChart = new Chart(districtCanvas, {
                type: 'bar',
                data: {
                    labels: chartData.district.labels.slice(0, 6),
                    datasets: [{
                        label: 'Net Profit (LKR)',
                        data: chartData.district.profit.slice(0, 6),
                        backgroundColor: colors.primary,
                        borderRadius: 6,
                        maxBarThickness: 32
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: colors.tooltipBg,
                            titleColor: colors.titleText,
                            bodyColor: colors.text,
                            borderColor: colors.border,
                            borderWidth: 1,
                            padding: 10,
                            cornerRadius: 8,
                            callbacks: {
                                label: ctx => ` Profit: LKR ${Number(ctx.raw).toLocaleString()}`
                            }
                        }
                    },
                    scales: {
                        x: {
                            ticks: { color: colors.text, font: fontConfig },
                            grid: { display: false }
                        },
                        y: {
                            ticks: {
                                color: colors.text,
                                font: fontConfig,
                                callback: v => 'LKR ' + (Number(v) >= 1000 ? (v / 1000) + 'k' : v)
                            },
                            grid: { color: colors.grid, drawBorder: false }
                        }
                    }
                }
            });
            charts.push(distChart);
        }
    }

    window.addEventListener('DOMContentLoaded', renderAllCharts);
    window.addEventListener('themechange', renderAllCharts);
})();
