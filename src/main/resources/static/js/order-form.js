// order-form.js — Reactive Order Calculation & Dynamic Row Engine

(function () {
    const container = document.getElementById('itemsContainer');
    const addBtn = document.getElementById('addItemBtn');
    const discountInput = document.getElementById('discountInput');
    const deliveryInput = document.getElementById('deliveryChargeInput');
    const codInput = document.getElementById('codValueInput');
    if (!container || !addBtn) return;

    const PILLOWCASE_PRICE = 220;
    const PILLOWCASE_PROFIT = 125;

    const bedsheetTypes = Object.keys(window.SELLING || {});
    const bedSizes = bedsheetTypes.length ? Object.keys(window.SELLING[bedsheetTypes[0]] || {}) : [];

    function formatMoney(value) {
        return 'LKR ' + Number(value || 0).toLocaleString('en-LK');
    }

    function lineValues(row) {
        const type = row.querySelector('.type-select')?.value;
        const size = row.querySelector('.size-select')?.value;
        const qty = Math.max(parseInt(row.querySelector('.qty-input')?.value || '1', 10), 1);
        const pillowcaseQty = Math.max(parseInt(row.querySelector('.pillowcase-input')?.value || '0', 10), 0);
        
        if (!type || !size || !window.SELLING[type] || window.SELLING[type][size] == null) {
            return {
                sell: pillowcaseQty * PILLOWCASE_PRICE,
                profit: pillowcaseQty * PILLOWCASE_PROFIT,
                pillowcaseQty
            };
        }
        
        const unitSell = Number(window.SELLING[type][size]);
        const unitProfit = Number(window.PROFITS[type][size]);
        return {
            sell: unitSell * qty + (pillowcaseQty * PILLOWCASE_PRICE),
            profit: unitProfit * qty + (pillowcaseQty * PILLOWCASE_PROFIT),
            pillowcaseQty
        };
    }

    function updateRowPreview(row) {
        const { sell, profit, pillowcaseQty } = lineValues(row);
        const sellEl = row.querySelector('.sell-preview');
        const profitEl = row.querySelector('.profit-preview');
        const pillowcaseEl = row.querySelector('.pillowcase-preview');

        if (sellEl) sellEl.innerHTML = 'Sell: <strong>' + formatMoney(sell) + '</strong>';
        if (profitEl) profitEl.innerHTML = 'Profit: <strong class="profit-cell">' + formatMoney(profit) + '</strong>';
        if (pillowcaseEl) {
            if (pillowcaseQty > 0) {
                pillowcaseEl.style.display = 'inline-block';
                pillowcaseEl.innerHTML = 'Pillowcases: ' + pillowcaseQty + ' &times; ' + formatMoney(PILLOWCASE_PRICE);
            } else {
                pillowcaseEl.style.display = 'none';
            }
        }
    }

    function updateTotals() {
        let totalSell = 0;
        let grossProfit = 0;
        
        container.querySelectorAll('.item-row').forEach(row => {
            const v = lineValues(row);
            totalSell += v.sell;
            grossProfit += v.profit;
            updateRowPreview(row);
        });

        const discount = Math.max(parseFloat(discountInput?.value || '0'), 0);
        const net = Math.max(grossProfit - discount, 0);
        const marginPct = totalSell > 0 ? ((net / totalSell) * 100).toFixed(1) : '0.0';

        const sellNode = document.getElementById('totalSelling');
        const grossNode = document.getElementById('grossProfit');
        const netNode = document.getElementById('netProfit');
        const marginNode = document.getElementById('netMarginPct');

        if (sellNode) sellNode.textContent = formatMoney(totalSell);
        if (grossNode) grossNode.textContent = formatMoney(grossProfit);
        if (netNode) netNode.textContent = formatMoney(net);
        if (marginNode) marginNode.textContent = marginPct + '%';

        // Auto-suggest COD value if empty or zero: total selling + delivery charge
        if (codInput && (!codInput.value || parseFloat(codInput.value) === 0)) {
            const delivery = Math.max(parseFloat(deliveryInput?.value || '0'), 0);
            if (totalSell > 0) {
                codInput.placeholder = 'Suggested: ' + (totalSell + delivery);
            }
        }
    }

    function reindexRows() {
        container.querySelectorAll('.item-row').forEach((row, index) => {
            const badge = row.querySelector('.item-row-badge');
            if (badge) badge.textContent = 'Product #' + (index + 1);

            row.querySelectorAll('[name]').forEach(input => {
                input.name = input.name.replace(/items\[\d+]/, 'items[' + index + ']');
                if (input.id) {
                    input.id = input.id.replace(/items\d+/, 'items' + index);
                }
            });

            const removeBtn = row.querySelector('.remove-item');
            if (removeBtn) {
                removeBtn.style.display = index === 0 && container.querySelectorAll('.item-row').length === 1 ? 'none' : 'inline-flex';
            }
        });
    }

    function bindRow(row) {
        row.querySelectorAll('.type-select, .size-select, .qty-input, .pillowcase-input').forEach(el => {
            el.addEventListener('change', updateTotals);
            el.addEventListener('input', updateTotals);
        });
        
        const removeBtn = row.querySelector('.remove-item');
        if (removeBtn) {
            removeBtn.addEventListener('click', () => {
                row.remove();
                reindexRows();
                updateTotals();
            });
        }
    }

    function createRow(index) {
        const row = document.createElement('div');
        row.className = 'item-row';
        const typeOptions = bedsheetTypes.map(t => `<option value="${t}">${t}</option>`).join('');
        const sizeOptions = bedSizes.map(s => `<option value="${s}">${s.replace(/_/g, ' ')}</option>`).join('');

        row.innerHTML = `
            <div class="item-row-head">
                <span class="item-row-badge">Product #${index + 1}</span>
                <button type="button" class="btn subtle remove-item" style="padding: 0.25rem 0.6rem; font-size: 0.8rem;">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                    Remove
                </button>
            </div>
            <div class="grid three-col">
                <label>Bedsheet Type *
                    <select name="items[${index}].bedsheetType" class="type-select" required>${typeOptions}</select>
                </label>
                <label>Bed Size *
                    <select name="items[${index}].bedSize" class="size-select" required>${sizeOptions}</select>
                </label>
                <label>Quantity
                    <input type="number" min="1" name="items[${index}].quantity" class="qty-input" value="1" required>
                </label>
                <label>Extra Pillowcases (Rs. 220 sell, Rs. 125 profit)
                    <input type="number" min="0" name="items[${index}].pillowcaseQuantity" class="pillowcase-input" value="0">
                </label>
                <label class="full">Bedding Design *
                    <input type="text" name="items[${index}].beddingDesign" placeholder="e.g. Royal Mandala, Ocean Breeze" required>
                </label>
                <div class="price-preview full">
                    <span class="sell-preview">Sell: —</span>
                    <span class="profit-preview">Profit: —</span>
                    <span class="pillowcase-preview" style="display: none;">Pillowcases: —</span>
                </div>
            </div>
        `;
        return row;
    }

    addBtn.addEventListener('click', () => {
        const index = container.querySelectorAll('.item-row').length;
        const row = createRow(index);
        container.appendChild(row);
        bindRow(row);
        reindexRows();
        updateTotals();
    });

    container.querySelectorAll('.item-row').forEach(bindRow);
    if (discountInput) discountInput.addEventListener('input', updateTotals);
    if (deliveryInput) deliveryInput.addEventListener('input', updateTotals);
    
    reindexRows();
    updateTotals();
})();
