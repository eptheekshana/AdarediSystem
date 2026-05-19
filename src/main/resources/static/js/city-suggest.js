(function () {
    const cityInput = document.getElementById('nearestCityInput');
    const districtInput = document.getElementById('districtInput');
    const list = document.getElementById('citySuggestions');
    if (!cityInput || !list) {
        return;
    }

    let timer;
    let cities = [];
    let activeIndex = -1;

    function hideList() {
        list.hidden = true;
        list.innerHTML = '';
        activeIndex = -1;
        cities = [];
    }

    function renderList() {
        list.innerHTML = '';
        if (cities.length === 0) {
            const empty = document.createElement('li');
            empty.className = 'city-suggestion empty';
            empty.textContent = 'No matching cities — try another spelling';
            list.appendChild(empty);
            list.hidden = false;
            return;
        }
        cities.forEach((city, index) => {
            const li = document.createElement('li');
            li.className = 'city-suggestion' + (index === activeIndex ? ' active' : '');
            li.dataset.index = String(index);
            li.innerHTML = '<strong>' + escapeHtml(city.name) + '</strong>'
                + '<span>' + escapeHtml(city.stateName) + '</span>';
            li.addEventListener('mousedown', (e) => {
                e.preventDefault();
                selectCity(city);
            });
            list.appendChild(li);
        });
        list.hidden = false;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function selectCity(city) {
        cityInput.value = city.name;
        if (districtInput && city.stateName) {
            districtInput.value = city.stateName;
        }
        hideList();
    }

    function fetchCities(query) {
        fetch('/api/curfox/cities?q=' + encodeURIComponent(query))
            .then((r) => {
                if (!r.ok) {
                    throw new Error('search failed');
                }
                return r.json();
            })
            .then((data) => {
                cities = data;
                activeIndex = data.length > 0 ? 0 : -1;
                renderList();
            })
            .catch(() => {
                cities = [];
                const li = document.createElement('li');
                li.className = 'city-suggestion empty';
                li.textContent = 'Could not load cities — check Royal Express connection';
                list.innerHTML = '';
                list.appendChild(li);
                list.hidden = false;
            });
    }

    cityInput.addEventListener('input', () => {
        clearTimeout(timer);
        const q = cityInput.value.trim();
        if (q.length < 2) {
            hideList();
            return;
        }
        timer = setTimeout(() => fetchCities(q), 250);
    });

    cityInput.addEventListener('keydown', (e) => {
        if (list.hidden || cities.length === 0) {
            return;
        }
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIndex = Math.min(activeIndex + 1, cities.length - 1);
            renderList();
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIndex = Math.max(activeIndex - 1, 0);
            renderList();
        } else if (e.key === 'Enter' && activeIndex >= 0) {
            e.preventDefault();
            selectCity(cities[activeIndex]);
        } else if (e.key === 'Escape') {
            hideList();
        }
    });

    document.addEventListener('click', (e) => {
        if (!cityInput.contains(e.target) && !list.contains(e.target)) {
            hideList();
        }
    });
})();
