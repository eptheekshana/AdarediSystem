(function () {
    const container = document.getElementById('itemsContainer');
    const addBtn = document.getElementById('addItemBtn');
    const discountInput = document.getElementById('discountInput');
    if (!container || !addBtn) return;

    const PILLOWCASE_PRICE = 220;
    const PILLOWCASE_PROFIT = 125;

    const bedsheetTypes = Object.keys(SELLING || {});
    const bedSizes = bedsheetTypes.length ? Object.keys(SELLING[bedsheetTypes[0]] || {}) : [];

    function formatMoney(value) {
        return 'LKR ' + Number(value || 0).toLocaleString('en-LK');
    }

    function lineValues(row) {
        const type = row.querySelector('.type-select')?.value;
        const size = row.querySelector('.size-select')?.value;
        const qty = Math.max(parseInt(row.querySelector('.qty-input')?.value || '1', 10), 1);
        const pillowcaseQty = Math.max(parseInt(row.querySelector('.pillowcase-input')?.value || '0', 10), 0);
        if (!type || !size || !SELLING[type] || SELLING[type][size] == null) {
            return { sell: pillowcaseQty * PILLOWCASE_PRICE, profit: pillowcaseQty * PILLOWCASE_PROFIT, pillowcaseQty };
        }
        const unitSell = Number(SELLING[type][size]);
        const unitProfit = Number(PROFITS[type][size]);
        return {
            sell: unitSell * qty + (pillowcaseQty * PILLOWCASE_PRICE),
            profit: unitProfit * qty + (pillowcaseQty * PILLOWCASE_PROFIT),
            pillowcaseQty
        };
    }

    function updateRowPreview(row) {
        const { sell, profit } = lineValues(row);
        const sellEl = row.querySelector('.sell-preview');
        const profitEl = row.querySelector('.profit-preview');
        const pillowcaseEl = row.querySelector('.pillowcase-preview');
        if (sellEl) sellEl.textContent = 'Selling: ' + formatMoney(sell);
        if (profitEl) profitEl.textContent = 'Profit: ' + formatMoney(profit);
        if (pillowcaseEl) {
            const pillowcaseQty = Math.max(parseInt(row.querySelector('.pillowcase-input')?.value || '0', 10), 0);
            pillowcaseEl.textContent = 'Pillow cases: ' + pillowcaseQty + ' × ' + formatMoney(PILLOWCASE_PRICE) + ' sell / ' + formatMoney(PILLOWCASE_PROFIT) + ' profit';
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
        const sellNode = document.getElementById('totalSelling');
        const grossNode = document.getElementById('grossProfit');
        const netNode = document.getElementById('netProfit');
        if (sellNode) sellNode.textContent = formatMoney(totalSell);
        if (grossNode) grossNode.textContent = formatMoney(grossProfit);
        if (netNode) netNode.textContent = formatMoney(net);
    }

    function reindexRows() {
        container.querySelectorAll('.item-row').forEach((row, index) => {
            row.querySelector('h3').textContent = 'Product ' + (index + 1);
            row.querySelectorAll('[name]').forEach(input => {
                input.name = input.name.replace(/items\[\d+]/, 'items[' + index + ']');
                input.id = input.id ? input.id.replace(/items\d+/, 'items' + index) : input.id;
            });
            const removeBtn = row.querySelector('.remove-item');
            if (removeBtn) removeBtn.style.display = index === 0 ? 'none' : 'inline-block';
        });
    }

    function bindRow(row) {
        row.querySelectorAll('.type-select, .size-select, .qty-input').forEach(el => {
            el.addEventListener('change', updateTotals);
            el.addEventListener('input', updateTotals);
        });
        row.querySelectorAll('.pillowcase-input').forEach(el => {
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
            <h3>Product ${index + 1}</h3>
            <div class="grid three-col">
                <label>Type *
                    <select name="items[${index}].bedsheetType" class="type-select" required>${typeOptions}</select>
                </label>
                <label>Bed size *
                    <select name="items[${index}].bedSize" class="size-select" required>${sizeOptions}</select>
                </label>
                <label>Qty
                    <input type="number" min="1" name="items[${index}].quantity" class="qty-input" value="1">
                </label>
                <label>Pillow cases (Rs. 220 sell, Rs. 125 profit each)
                    <input type="number" min="0" name="items[${index}].pillowcaseQuantity" class="pillowcase-input" value="0">
                </label>
                <label class="full">Bedding design *
                    <input type="text" name="items[${index}].beddingDesign" placeholder="e.g. Floral Blue" required>
                </label>
                <div class="price-preview full">
                    <span class="sell-preview">Selling: —</span>
                    <span class="profit-preview">Profit: —</span>
                    <span class="pillowcase-preview">Pillow cases: —</span>
                </div>
            </div>
            <button type="button" class="btn subtle remove-item">Remove</button>
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
    updateTotals();

})();
