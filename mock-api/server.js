const express = require('express');
const fs      = require('fs');
const path    = require('path');

const app  = express();
const PORT = 3000;

app.use(express.json());

const DB_PATH = path.join(__dirname, 'db.json');
const loadDb  = () => JSON.parse(fs.readFileSync(DB_PATH, 'utf8'));
const saveDb  = (db) => fs.writeFileSync(DB_PATH, JSON.stringify(db, null, 2));

const MOCK_TOKEN   = 'mock-access-token-cic';
const MOCK_REFRESH = 'mock-refresh-token-cic';

// ── Image helper functions ─────────────────────────────────────────────────
// These are the "mock image functions" — compute image URLs from entity data

function avatarUrl(user) {
    if (user.avatar) return user.avatar;
    const bg    = user.role === 'Administrator' ? '00D1FF' : user.role === 'Mentor' ? '8B5CF6' : '22C55E';
    const color = '0A0C10';
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(user.username)}&background=${bg}&color=${color}&size=200&bold=true&format=svg`;
}

function bannerUrl(event) {
    if (event.banner_url) return event.banner_url;
    const seeds = {
        CTF:        '25',   // dark dramatic shot
        Workshop:   '20',   // architecture/tech
        Bootcamp:   '10',   // intense environment
        Meeting:    '64',   // clean office
        Conference: '48',   // large venue
    };
    const seed = seeds[event.type] || String(event.id).charCodeAt(0) % 80 + 1;
    return `https://picsum.photos/seed/${seed}/800/400`;
}

function injectImages(db) {
    return {
        ...db,
        users:  db.users.map(u  => ({ ...u,  avatar:     avatarUrl(u) })),
        events: db.events.map(e => ({ ...e,  banner_url: bannerUrl(e) })),
    };
}

// ── Auth ───────────────────────────────────────────────────────────────────

app.post('/auth/login', (req, res) => {
    const { email, password } = req.body || {};
    if (!email || !password) return res.status(400).json({ error: 'email and password required' });
    const db   = injectImages(loadDb());
    const user = db.users.find(u => u.email === email) || db.users[0];
    res.json({ access_token: MOCK_TOKEN, refresh_token: MOCK_REFRESH, user });
});

app.post('/auth/register', (req, res) => {
    const { username, email, password } = req.body || {};
    if (!username || !email || !password) return res.status(400).json({ error: 'All fields required' });
    const db      = loadDb();
    const newUser = { id: 'user-' + Date.now(), discord_id: null, username, email,
                      avatar: null, role: 'Member', xp: 0, level: 1,
                      created_at: new Date().toISOString() };
    db.users.push(newUser);
    saveDb(db);
    res.status(201).json({ access_token: MOCK_TOKEN, refresh_token: MOCK_REFRESH,
                           user: { ...newUser, avatar: avatarUrl(newUser) } });
});

app.post('/auth/discord', (req, res) => {
    const db = injectImages(loadDb());
    res.json({ access_token: MOCK_TOKEN, refresh_token: MOCK_REFRESH, user: db.users[0] });
});

app.post('/auth/refresh', (req, res) => {
    const db = injectImages(loadDb());
    res.json({ access_token: MOCK_TOKEN, refresh_token: MOCK_REFRESH, user: db.users[0] });
});

app.post('/auth/logout', (req, res) => res.sendStatus(204));

// ── Users ──────────────────────────────────────────────────────────────────

app.get('/users', (req, res) => {
    res.json(injectImages(loadDb()).users);
});

app.get('/users/me', (req, res) => {
    res.json(injectImages(loadDb()).users[0]);
});

app.get('/users/:id', (req, res) => {
    const db   = injectImages(loadDb());
    const user = db.users.find(u => u.id === req.params.id) || db.users[0];
    res.json(user);
});

app.patch('/users/me', (req, res) => {
    const db = loadDb();
    Object.assign(db.users[0], req.body);
    saveDb(db);
    res.json({ ...db.users[0], avatar: avatarUrl(db.users[0]) });
});

app.patch('/users/:id', (req, res) => {
    const db  = loadDb();
    const idx = db.users.findIndex(u => u.id === req.params.id);
    if (idx === -1) return res.status(404).json({ error: 'Not found' });
    Object.assign(db.users[idx], req.body);
    saveDb(db);
    res.json({ ...db.users[idx], avatar: avatarUrl(db.users[idx]) });
});

// ── Events ─────────────────────────────────────────────────────────────────

app.get('/events', (req, res) => res.json(injectImages(loadDb()).events));

app.get('/events/:id', (req, res) => {
    const db  = injectImages(loadDb());
    const evt = db.events.find(e => e.id === req.params.id);
    evt ? res.json(evt) : res.status(404).json({ error: 'Not found' });
});

app.post('/events', (req, res) => {
    const db  = loadDb();
    const evt = { id: 'evt-' + Date.now(), registered_count: 0, is_registered: false, ...req.body };
    db.events.push(evt);
    saveDb(db);
    res.status(201).json({ ...evt, banner_url: bannerUrl(evt) });
});

app.patch('/events/:id', (req, res) => {
    const db  = loadDb();
    const idx = db.events.findIndex(e => e.id === req.params.id);
    if (idx === -1) return res.status(404).json({ error: 'Not found' });
    Object.assign(db.events[idx], req.body);
    saveDb(db);
    res.json({ ...db.events[idx], banner_url: bannerUrl(db.events[idx]) });
});

app.delete('/events/:id', (req, res) => {
    const db  = loadDb();
    db.events = db.events.filter(e => e.id !== req.params.id);
    saveDb(db);
    res.sendStatus(204);
});

app.post('/events/:id/register',   (req, res) => res.sendStatus(200));
app.post('/events/:id/unregister', (req, res) => res.sendStatus(200));

// ── Resources ──────────────────────────────────────────────────────────────

app.get('/resources', (req, res) => {
    let list = loadDb().resources;
    if (req.query.category)   list = list.filter(r => r.category === req.query.category);
    if (req.query.difficulty)  list = list.filter(r => r.difficulty === req.query.difficulty);
    if (req.query.q) {
        const q = req.query.q.toLowerCase();
        list = list.filter(r => r.title.toLowerCase().includes(q) || r.description.toLowerCase().includes(q));
    }
    res.json(list);
});

app.get('/resources/:id', (req, res) => {
    const r = loadDb().resources.find(r => r.id === req.params.id);
    r ? res.json(r) : res.status(404).json({ error: 'Not found' });
});

app.post('/resources', (req, res) => {
    const db   = loadDb();
    const res_ = { id: 'res-' + Date.now(), created_at: new Date().toISOString(), ...req.body };
    db.resources.push(res_);
    saveDb(db);
    res.status(201).json(res_);
});

app.patch('/resources/:id', (req, res) => {
    const db  = loadDb();
    const idx = db.resources.findIndex(r => r.id === req.params.id);
    if (idx === -1) return res.status(404).json({ error: 'Not found' });
    Object.assign(db.resources[idx], req.body);
    saveDb(db);
    res.json(db.resources[idx]);
});

app.delete('/resources/:id', (req, res) => {
    const db     = loadDb();
    db.resources = db.resources.filter(r => r.id !== req.params.id);
    saveDb(db);
    res.sendStatus(204);
});

// ── Announcements ──────────────────────────────────────────────────────────

app.get('/announcements', (req, res) => {
    let list = loadDb().announcements;
    if (req.query.pinned === 'true') list = list.filter(a => a.is_pinned);
    res.json(list);
});

app.post('/announcements', (req, res) => {
    const db  = loadDb();
    const ann = { id: 'ann-' + Date.now(), created_at: new Date().toISOString(),
                  author_id: 'admin-001', ...req.body };
    db.announcements.unshift(ann);
    saveDb(db);
    res.status(201).json(ann);
});

app.patch('/announcements/:id', (req, res) => {
    const db  = loadDb();
    const idx = db.announcements.findIndex(a => a.id === req.params.id);
    if (idx === -1) return res.status(404).json({ error: 'Not found' });
    Object.assign(db.announcements[idx], req.body);
    saveDb(db);
    res.json(db.announcements[idx]);
});

app.delete('/announcements/:id', (req, res) => {
    const db         = loadDb();
    db.announcements = db.announcements.filter(a => a.id !== req.params.id);
    saveDb(db);
    res.sendStatus(204);
});

// ── Admin Stats ────────────────────────────────────────────────────────────

app.get('/admin/stats', (req, res) => {
    const db = loadDb();
    res.json({
        totalUsers:         db.users.length,
        totalEvents:        db.events.length,
        totalResources:     db.resources.length,
        totalAnnouncements: db.announcements.length
    });
});

// ── Start ──────────────────────────────────────────────────────────────────

app.listen(PORT, '0.0.0.0', () => {
    console.log(`\nCIC Mock API  →  http://0.0.0.0:${PORT}`);
    console.log(`From BlueStacks  →  http://192.168.121.1:${PORT}\n`);
    console.log('Admin:  admin@cic.local / any password');
    console.log('Member: demo@cic.local  / any password\n');
});
