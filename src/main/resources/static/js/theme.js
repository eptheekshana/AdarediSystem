(function () {
    const STORAGE_KEY = 'bedsheet-theme';
    const root = document.documentElement;

    function canUseStorage() {
        try {
            const key = '__theme_test__';
            localStorage.setItem(key, '1');
            localStorage.removeItem(key);
            return true;
        } catch (e) {
            return false;
        }
    }

    const storageAvailable = canUseStorage();

    function systemTheme() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function getStoredTheme() {
        if (!storageAvailable) {
            return null;
        }
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function setStoredTheme(theme) {
        if (!storageAvailable) {
            return;
        }
        try {
            localStorage.setItem(STORAGE_KEY, theme);
        } catch (e) {
            // ignore
        }
    }

    function getTheme() {
        const stored = getStoredTheme();
        if (stored === 'light' || stored === 'dark') {
            return stored;
        }
        return systemTheme();
    }

    function syncToggle(theme) {
        const buttons = document.querySelectorAll('[data-theme-toggle]');
        buttons.forEach((button) => {
            button.setAttribute('aria-pressed', String(theme === 'dark'));
            button.setAttribute('aria-label', theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
            button.title = theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode';
            const icon = button.querySelector('.theme-toggle-icon');
            const label = button.querySelector('.theme-toggle-label');
            if (icon) {
                icon.textContent = theme === 'dark' ? '☀' : '☾';
            }
            if (label) {
                label.textContent = theme === 'dark' ? 'Light mode' : 'Dark mode';
            }
        });
    }

    function applyTheme(theme, persist) {
        const resolved = theme === 'dark' ? 'dark' : 'light';
        root.dataset.theme = resolved;
        root.style.colorScheme = resolved;
        syncToggle(resolved);
        if (persist) {
            setStoredTheme(resolved);
        }
        window.dispatchEvent(new CustomEvent('themechange', { detail: { theme: resolved } }));
    }

    function init() {
        applyTheme(getTheme(), false);

        document.addEventListener('click', (event) => {
            if (!(event.target instanceof Element)) {
                return;
            }
            const button = event.target.closest('[data-theme-toggle]');
            if (!button) {
                return;
            }
            const nextTheme = root.dataset.theme === 'dark' ? 'light' : 'dark';
            applyTheme(nextTheme, true);
        });

        if (window.matchMedia) {
            const media = window.matchMedia('(prefers-color-scheme: dark)');
            const handler = () => {
                if (!getStoredTheme()) {
                    applyTheme(systemTheme(), false);
                }
            };
            if (typeof media.addEventListener === 'function') {
                media.addEventListener('change', handler);
            } else if (typeof media.addListener === 'function') {
                media.addListener(handler);
            }
        }

        window.addEventListener('storage', (event) => {
            if (event.key === STORAGE_KEY) {
                applyTheme(getTheme(), false);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

