/**
 * Tiny reactive state store for the dashboard.
 *
 * Holds the current time-range (fromMs, toMs) and granularity,
 * and notifies subscribers when any value changes.
 *
 * Mutations within the same microtask are coalesced into
 * a single notification to prevent double-refreshes.
 */

const listeners = [];
let _notifyQueued = false;

export const state = {
    fromMs: Date.now() - 86_400_000, // Default to last 24h
    toMs: 0, // 0 = open-ended (no upper bound)
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
    setFrom(ms) {
        if (this.fromMs === ms) return;
        this.fromMs = ms;
        this._scheduleNotify();
    },

    /** Update the time-range upper bound (epoch ms, or 0 for open-ended). */
    setTo(ms) {
        if (this.toMs === ms) return;
        this.toMs = ms;
        this._scheduleNotify();
    },

    /** Update the bucket granularity (e.g. 'hour' or 'minute'). */
    setGranularity(gran) {
        if (this.granularity === gran) return;
        this.granularity = gran;
        this._scheduleNotify();
    },

    /**
     * Batch-update from, to, and granularity, firing listeners only once.
     * toMs and granularity are optional - omit to leave unchanged.
     */
    update(fromMs, granularity, toMs = 0) {
        const changed = this.fromMs !== fromMs
            || this.toMs !== toMs
            || this.granularity !== granularity;
        this.fromMs = fromMs;
        this.toMs = toMs;
        this.granularity = granularity;
        if (changed) this._scheduleNotify();
    },

    _scheduleNotify() {
        if (_notifyQueued) return;
        _notifyQueued = true;
        queueMicrotask(() => {
            _notifyQueued = false;
            for (const fn of listeners) {
                try {
                    fn();
                } catch (e) {
                    console.error('State listener error:', e);
                }
            }
        });
    },
};