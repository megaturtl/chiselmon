/**
 * Tiny reactive state store for the dashboard.
 *
 * Holds the current time-range (fromMs) and granularity,
 * and notifies subscribers when either changes.
 *
 * Mutations within the same microtask are coalesced into
 * a single notification to prevent double-refreshes.
 */

const listeners = [];
let _notifyQueued = false;

export const state = {
    fromMs: Date.now() - 86_400_000,
    granularity: 'hour',

    /** Subscribe to any state change. Returns an unsubscribe function. */
    onChange(fn) {
        listeners.push(fn);
        return () => {
            const i = listeners.indexOf(fn);
            if (i !== -1) listeners.splice(i, 1);
        };
    },

    /** Update the time-range origin (epoch ms, or 0 for all-time). */
    setRange(ms) {
        if (this.fromMs === ms) return;
        this.fromMs = ms;
        this._scheduleNotify();
    },

    /** Update the bucket granularity ('hour' or 'minute'). */
    setGranularity(gran) {
        if (this.granularity === gran) return;
        this.granularity = gran;
        this._scheduleNotify();
    },

    /** Batch-update range + granularity, firing listeners only once. */
    update(fromMs, granularity) {
        const changed = this.fromMs !== fromMs || this.granularity !== granularity;
        this.fromMs = fromMs;
        this.granularity = granularity;
        if (changed) this._scheduleNotify();
    },

    _scheduleNotify() {
        if (_notifyQueued) return;
        _notifyQueued = true;
        queueMicrotask(() => {
            _notifyQueued = false;
            for (const fn of listeners) {
                try { fn(); } catch (e) { console.error('State listener error:', e); }
            }
        });
    },
};