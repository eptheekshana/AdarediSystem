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
        if (!storageAvailable) return null;
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function setStoredTheme(theme) {
        if (!storageAvailable) return;
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
        const isDark = theme === 'dark';
        const buttons = document.querySelectorAll('[data-theme-toggle]');
        buttons.forEach((button) => {
            button.setAttribute('aria-pressed', String(isDark));
            button.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
            button.title = isDark ? 'Switch to light mode' : 'Switch to dark mode';
            
            const iconSvg = button.querySelector('.theme-icon');
            if (iconSvg) {
                if (isDark) {
                    // Sun icon for dark mode
                    iconSvg.innerHTML = '<circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>';
                } else {
                    // Moon icon for light mode
                    iconSvg.innerHTML = '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>';
                }
            }
            
            const label = button.querySelector('.theme-toggle-label');
            if (label) {
                label.textContent = isDark ? 'Light mode' : 'Dark mode';
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
            if (!(event.target instanceof Element)) return;
            const button = event.target.closest('[data-theme-toggle]');
            if (!button) return;
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
