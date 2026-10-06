const API = {
    async getPresets() {
        const res = await fetch('/api/presets');
        return res.json();
    },

    async getPreset(id) {
        const res = await fetch(`/api/presets/${encodeURIComponent(id)}`);
        return res.json();
    },

    async runMining(payload) {
        const res = await fetch('/api/mine', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        return res.json();
    },

    async verify(id) {
        const res = await fetch(`/api/verify/${encodeURIComponent(id)}`);
        return res.json();
    },

    async getMembershipFunctions() {
        const res = await fetch('/api/membership-functions');
        return res.json();
    }
};
