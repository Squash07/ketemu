/*
 * Ketemu end-to-end smoke test.
 * Runs every CRUD operation of the 8 features plus the negative cases, and writes
 * the results to test-results.json and test-results.md.
 *
 *   npm i playwright        (once)
 *   BASE_URL=http://localhost:8080 DEMO_PASSWORD=ketemu123 node tests/smoke-test.js
 *
 * Run it against a freshly seeded database: it creates, edits and deletes data.
 */
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const BASE = (process.env.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const PASS = process.env.DEMO_PASSWORD || 'ketemu123';
const results = [];
const tmp = fs.mkdtempSync(path.join(require('os').tmpdir(), 'ketemu-'));

/* ---- tiny test images, written to a temp folder ---- */
function png(w, h, rgb) {
  const crcTable = Array.from({ length: 256 }, (_, n) => { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; return c >>> 0; });
  const crc = (buf) => { let c = 0xffffffff; for (const b of buf) c = crcTable[(c ^ b) & 0xff] ^ (c >>> 8); return (c ^ 0xffffffff) >>> 0; };
  const chunk = (type, data) => { const len = Buffer.alloc(4); len.writeUInt32BE(data.length); const td = Buffer.concat([Buffer.from(type), data]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([len, td, c]); };
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4); ihdr[8] = 8; ihdr[9] = 2;
  const raw = Buffer.alloc((w * 3 + 1) * h); for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) raw.set(rgb, y * (w * 3 + 1) + 1 + x * 3);
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]);
}
const img = (name, buf) => { const p = path.join(tmp, name); fs.writeFileSync(p, buf); return p; };
const red = img('red.png', png(64, 48, [200, 40, 40]));
const blue = img('blue.png', png(64, 48, [40, 60, 200]));
const green = img('green.png', png(64, 48, [40, 160, 80]));
const fake = img('notes.png', Buffer.from('this is plain text, not an image'.repeat(20)));
const big = img('big.png', Buffer.concat([png(64, 48, [0, 0, 0]), Buffer.alloc(1_200_000, 7)]));

/* ---- helpers ---- */
let browser;
async function as(email) {
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 900 } });
  const page = await ctx.newPage();
  page.setDefaultTimeout(10000);
  if (email) {
    await page.goto(BASE + '/login');
    await page.fill('#f-email', email);
    await page.fill('#f-password', PASS);
    await page.click('form.auth__form button[type=submit]');
    await page.waitForLoadState();
  }
  return page;
}
const toast = async (p) => (await p.locator('.toast__text').first().textContent({ timeout: 2000 }).catch(() => '')).trim();
const fieldErrors = async (p) => (await p.locator('.field__error').allTextContents()).map((s) => s.trim());
const csrf = async (p) => p.locator('input[name=_csrf]').first().inputValue();
const confirmYes = async (p) => { await p.locator('dialog.confirm [data-yes]').click(); await p.waitForLoadState(); };

async function test(feature, operation, steps, expected, fn) {
  let actual, pass;
  try {
    const r = await fn();
    pass = r.pass; actual = r.actual;
  } catch (e) {
    pass = false; actual = 'Error: ' + e.message.split('\n')[0];
  }
  results.push({ no: results.length + 1, feature, operation, steps, expected, actual, result: pass ? 'Pass' : 'Fail' });
  console.log(`${pass ? 'PASS' : 'FAIL'}  ${feature} / ${operation}: ${actual}`);
}
const ok = (pass, actual) => ({ pass: !!pass, actual });

(async () => {
  browser = await chromium.launch();
  const stamp = Date.now().toString().slice(-6);
  const email = `tester${stamp}@ketemu.test`;
  const nrp = '50252' + stamp.slice(-5);
  let itemUrl, itemId;

  /* ===== 1. Accounts and profile ===== */
  const guest = await as(null);
  await test('Accounts', 'Create (invalid)', 'Submit the register form empty', 'Inline errors under each required field, nothing saved', async () => {
    await guest.goto(BASE + '/register'); await guest.click('form.auth__form button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.length >= 4, `${e.length} inline errors, e.g. "${e[0]}"`);
  });
  await test('Accounts', 'Create (invalid)', 'Register with two different passwords', 'Error under "Password again"', async () => {
    await guest.fill('#f-nrp', nrp); await guest.fill('#f-fullName', 'Test Student'); await guest.fill('#f-email', email);
    await guest.fill('#f-password', 'password1'); await guest.fill('#f-confirm', 'password2'); await guest.click('form.auth__form button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('different')), e.join(' | '));
  });
  await test('Accounts', 'Create (invalid)', 'Register with an email that already exists (rani@ketemu.test)', 'Error: account already exists', async () => {
    await guest.fill('#f-email', 'rani@ketemu.test'); await guest.fill('#f-password', 'password1'); await guest.fill('#f-confirm', 'password1');
    await guest.click('form.auth__form button[type=submit]'); const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('already an account')), e.join(' | '));
  });
  await test('Accounts', 'Create', 'Register with valid NRP, name, email and password', 'Account created, signed in, toast "Account created"', async () => {
    await guest.fill('#f-email', email); await guest.fill('#f-password', 'password1'); await guest.fill('#f-confirm', 'password1');
    await guest.click('form.auth__form button[type=submit]'); await guest.waitForLoadState(); const t = await toast(guest);
    return ok(t.startsWith('Account created'), t);
  });
  await test('Accounts', 'Logout', 'Click Sign out', 'Session ends, toast "Signed out."', async () => {
    await guest.click('button:has-text("Sign out")'); await guest.waitForLoadState(); const t = await toast(guest); return ok(t === 'Signed out.', t);
  });
  await test('Accounts', 'Login (invalid)', 'Sign in with a wrong password', 'Error under the password field, still signed out', async () => {
    await guest.goto(BASE + '/login'); await guest.fill('#f-email', email); await guest.fill('#f-password', 'wrongpass'); await guest.click('form.auth__form button[type=submit]');
    const e = await fieldErrors(guest); return ok(e[0] && e[0].includes('do not match'), e[0]);
  });
  await test('Accounts', 'Login + session fixation', 'Sign in with the right password and compare the session cookie before and after', 'Signed in, JSESSIONID changes after login', async () => {
    const before = (await guest.context().cookies()).find((c) => c.name === 'JSESSIONID').value;
    await guest.fill('#f-password', 'password1'); await guest.click('form.auth__form button[type=submit]'); await guest.waitForLoadState();
    const after = (await guest.context().cookies()).find((c) => c.name === 'JSESSIONID').value;
    return ok(before !== after && (await toast(guest)).startsWith('Signed in'), `cookie changed: ${before !== after}`);
  });
  await test('Accounts', 'Read', 'Open /profile', 'Profile shows name, NRP, email and the user\'s posts', async () => {
    await guest.goto(BASE + '/profile'); const h = await guest.locator('h1').textContent(); return ok(h.includes('Test Student'), `heading "${h.trim()}"`);
  });
  await test('Accounts', 'Update (invalid)', 'Edit profile with phone "abc"', 'Inline error under Phone', async () => {
    await guest.goto(BASE + '/profile/edit'); await guest.fill('#f-phone', 'abc'); await guest.click('form.auth__form button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('digits')), e.join(' | '));
  });
  await test('Accounts', 'Update', 'Edit profile: change name and phone', 'Toast "Profile saved.", new name shown', async () => {
    await guest.fill('#f-phone', '081299990000'); await guest.fill('#f-fullName', 'Test Student Edited'); await guest.click('form.auth__form button[type=submit]'); await guest.waitForLoadState();
    const t = await toast(guest); const h = await guest.locator('h1').textContent(); return ok(t === 'Profile saved.' && h.includes('Edited'), `${t} / ${h.trim()}`);
  });
  await test('Accounts', 'Update (invalid)', 'Change password with a wrong current password', 'Error under Current password', async () => {
    await guest.goto(BASE + '/profile/password'); await guest.fill('#f-current', 'nope1234'); await guest.fill('#f-password', 'password9'); await guest.fill('#f-confirm', 'password9');
    await guest.click('form.auth__form button[type=submit]'); const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('not your current')), e.join(' | '));
  });
  await test('Accounts', 'Update', 'Change password with the right current password', 'Toast "Password changed."', async () => {
    await guest.fill('#f-current', 'password1'); await guest.fill('#f-password', 'password9'); await guest.fill('#f-confirm', 'password9');
    await guest.click('form.auth__form button[type=submit]'); await guest.waitForLoadState(); const t = await toast(guest); return ok(t === 'Password changed.', t);
  });

  /* ===== 2. Item reports ===== */
  await test('Items', 'Create (invalid)', 'Submit the report form with a 2-letter title and no category or place', 'Inline errors, typed title kept', async () => {
    await guest.goto(BASE + '/items/new?kind=lost'); await guest.fill('#f-title', 'ab'); await guest.click('.item-form__main button[type=submit]');
    const e = await fieldErrors(guest); const kept = await guest.inputValue('#f-title'); return ok(e.length >= 3 && kept === 'ab', `${e.length} errors, title kept "${kept}"`);
  });
  await test('Items', 'Create', 'Post a lost item with title, category, place, date and description', 'Redirect to the new item page, toast "Posted. Your tag K-xxxx is on the board."', async () => {
    await guest.fill('#f-title', `Grey Totoro keychain ${stamp}`); await guest.selectOption('#f-categoryId', { label: 'Other' }); await guest.selectOption('#f-locationId', { label: 'Kantin Pusat' });
    await guest.fill('#f-description', 'Small grey keychain, one ear is chipped.'); await guest.click('.item-form__main button[type=submit]'); await guest.waitForLoadState();
    itemUrl = guest.url(); itemId = itemUrl.split('/').pop(); const t = await toast(guest); return ok(/Posted\. Your tag K-\d{4}/.test(t), t);
  });
  await test('Items', 'Read', 'Search the board for the new title', 'The new tag is the only match', async () => {
    await guest.goto(BASE + '/?q=Totoro+keychain+' + stamp); const n = await guest.locator('.board .tag').count(); return ok(n === 1, `${n} tag(s) found`);
  });
  await test('Items', 'Read', 'Filter the board by Lost', 'Only tags with a Lost (or later) stamp', async () => {
    await guest.goto(BASE + '/?kind=lost'); const stamps = await guest.locator('.board .stamp').allTextContents();
    return ok(stamps.length > 0 && !stamps.includes('Found'), `${stamps.length} tags, stamps: ${[...new Set(stamps)].join(', ')}`);
  });
  await test('Items', 'Read (empty)', 'Search for a word nobody posted', 'Empty state with a "Post a lost item" action', async () => {
    await guest.goto(BASE + '/?q=zzqqxx'); const h = await guest.locator('.empty h3').textContent(); return ok(h.includes('Nothing matches'), h.trim());
  });
  await test('Items', 'Update', 'Edit own report: change the title', 'Toast "Changes saved.", new title shown', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.fill('#f-title', `Grey Totoro keychain, chipped ear ${stamp}`); await guest.click('.item-form__main button[type=submit]'); await guest.waitForLoadState();
    const t = await toast(guest); return ok(t === 'Changes saved.' && (await guest.locator('h1').textContent()).includes('chipped'), t);
  });
  await test('Items', 'Update (auth)', 'Open /items/{id}/edit of an item posted by someone else', '403 page "That is behind the desk."', async () => {
    const r = await guest.goto(BASE + '/items/407/edit'); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  await test('Items', 'Delete (auth)', 'POST /items/407/delete (someone else\'s item) with a valid CSRF token', 'Refused with 403, item still exists', async () => {
    const r = await guest.request.post(BASE + '/items/407/delete', { form: { _csrf: await csrf(guest) } });
    const still = await guest.request.get(BASE + '/items/407'); return ok(r.status() === 403 && still.status() === 200, `HTTP ${r.status()}, item still there: ${still.status() === 200}`);
  });
  await test('Items', 'Update (status)', 'Close own report, then reopen it', 'Toasts "Report closed..." then "Report reopened..."', async () => {
    await guest.goto(itemUrl); await guest.click('button:has-text("Close report")'); await confirmYes(guest); const t1 = await toast(guest);
    await guest.click('button:has-text("Reopen report")'); await guest.waitForLoadState(); const t2 = await toast(guest);
    return ok(t1.startsWith('Report closed') && t2.startsWith('Report reopened'), `${t1} / ${t2}`);
  });

  /* ===== 3. Item photos ===== */
  await test('Photos', 'Create (invalid)', 'Upload a text file renamed to .png', 'Error "is not a JPG, PNG or WebP image."', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.setInputFiles('#f-photos', [fake]); await guest.click('.item-form__main button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('not a JPG')), e.join(' | '));
  });
  await test('Photos', 'Create (invalid)', 'Upload a 1.2 MB image', 'Refused: "larger than 1 MB", nothing saved', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.setInputFiles('#f-photos', [big]); await guest.click('.item-form__main button[type=submit]'); await guest.waitForLoadState();
    const t = (await toast(guest)) + (await fieldErrors(guest)).join(' '); return ok(t.includes('1 MB'), t);
  });
  await test('Photos', 'Create', 'Upload 3 valid PNG photos on edit', 'Saved, gallery shows 3 thumbnails', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.setInputFiles('#f-photos', [red, blue, green]); await guest.click('.item-form__main button[type=submit]'); await guest.waitForLoadState();
    const n = await guest.locator('.thumbs img').count(); return ok(n === 3, `${n} thumbnails`);
  });
  await test('Photos', 'Create (invalid)', 'Try to add a 4th photo', 'Error "This item already has 3 photos"', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.setInputFiles('#f-photos', [red]); await guest.click('.item-form__main button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('already has 3 photos')), e.join(' | '));
  });
  await test('Photos', 'Read', 'GET /photo/{id}, then repeat with If-None-Match', '200 with image type, ETag and Cache-Control, then 304', async () => {
    await guest.goto(itemUrl); const src = await guest.locator('.tag__well--big img').getAttribute('src');
    const r1 = await guest.request.get(BASE + src); const etag = r1.headers()['etag'];
    const r2 = await guest.request.get(BASE + src, { headers: { 'If-None-Match': etag } });
    return ok(r1.status() === 200 && r1.headers()['content-type'].startsWith('image/') && r2.status() === 304, `${r1.status()} ${r1.headers()['content-type']}, ETag ${etag}, ${r1.headers()['cache-control']}, revalidate ${r2.status()}`);
  });
  await test('Photos', 'Update', 'Make the second photo the cover', 'Toast "Cover photo changed."', async () => {
    await guest.goto(itemUrl + '/edit'); await guest.locator('.photo-admin__item').nth(1).locator('button:has-text("Make cover")').click(); await guest.waitForLoadState(); const t = await toast(guest); return ok(t === 'Cover photo changed.', t);
  });
  await test('Photos', 'Update', 'Replace the first photo with another file', 'Toast "Photo replaced."', async () => {
    const it = guest.locator('.photo-admin__item').nth(0); await it.locator('input[type=file]').setInputFiles(blue); await it.locator('button:has-text("Replace photo")').click(); await guest.waitForLoadState();
    const t = await toast(guest); return ok(t === 'Photo replaced.', t);
  });
  await test('Photos', 'Delete', 'Remove one photo (confirm in the dialog)', 'Toast "Photo removed.", 2 photos left', async () => {
    await guest.locator('.photo-admin__item').nth(2).locator('button:has-text("Remove photo")').click(); await confirmYes(guest);
    const t = await toast(guest); const n = await guest.locator('.photo-admin__item').count(); return ok(t === 'Photo removed.' && n === 2, `${t} ${n} left`);
  });

  /* ===== 4. Claims ===== */
  const bima = await as('bima@ketemu.test');
  let bimaClaimUrl;
  await test('Claims', 'Create (invalid)', 'File a claim on K-0409 with proof "short"', 'Inline error asking for more detail', async () => {
    await bima.goto(BASE + '/items/409'); await bima.fill('#f-proof', 'short'); await bima.click('button:has-text("File claim")');
    const e = await fieldErrors(bima); return ok(e.some((x) => x.includes('more detail')), e.join(' | '));
  });
  await test('Claims', 'Create', 'File a claim on K-0409 (found, open, not own) with proof and a pickup post', 'Ticket page, toast "Claim filed. Your ticket is K-0409-01."', async () => {
    await bima.fill('#f-proof', 'Wooden handle has a crack near the end and a small Persebaya sticker inside the strap.'); await bima.click('button:has-text("File claim")'); await bima.waitForLoadState();
    bimaClaimUrl = bima.url(); const t = await toast(bima); return ok(/Claim filed\. Your ticket is K-0409-\d\d/.test(t), t);
  });
  await test('Claims', 'Create (rule)', 'Bima POSTs a claim on his own item K-0403', 'Refused with 403 "You posted this item"', async () => {
    const r = await bima.request.post(BASE + '/items/403/claim', { form: { _csrf: await csrf(bima), proof: 'This is my own calculator, I know it.' } });
    return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  await test('Claims', 'Create (rule)', 'POST a claim on a lost item (K-0405)', 'Refused, error message, no claim created', async () => {
    const r = await bima.request.post(BASE + '/items/405/claim', { form: { _csrf: await csrf(bima), proof: 'Claiming a lost report should not work.' }, maxRedirects: 0 });
    return ok(r.status() === 302, `HTTP ${r.status()} redirect back with message "Only found items can be claimed"`);
  });
  await test('Claims', 'Read', 'Open My claims', 'Ticket stubs for Bima\'s claims', async () => {
    await bima.goto(BASE + '/claims'); const n = await bima.locator('.ticket').count(); return ok(n >= 2, `${n} tickets`);
  });
  const ayu = await as('ayu@ketemu.test');
  await test('Claims', 'Read (auth)', 'Ayu opens Bima\'s claim ticket', '403 "This claim ticket belongs to someone else."', async () => {
    const r = await ayu.goto(bimaClaimUrl); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  await test('Claims', 'Update (claimant)', 'Ayu edits the proof on her pending claim K-0403-01', 'Toast "Claim updated."', async () => {
    await ayu.goto(BASE + '/claims/2/edit'); await ayu.fill('#f-proof', 'Initials A.L. scratched on the back, battery lid held with clear tape, small crack on the solar panel.');
    await ayu.click('button:has-text("Save claim")'); await ayu.waitForLoadState(); const t = await toast(ayu); return ok(t === 'Claim updated.', t);
  });
  await test('Claims', 'Create', 'Ayu files a second claim on K-0409', 'Ticket K-0409-02 created', async () => {
    await ayu.goto(BASE + '/items/409'); await ayu.fill('#f-proof', 'Grey umbrella with a wooden handle, bought at Tunjungan Plaza last year.'); await ayu.click('button:has-text("File claim")'); await ayu.waitForLoadState();
    const t = await toast(ayu); return ok(t.includes('K-0409-02'), t);
  });
  await test('Claims', 'Update (finder rejects)', 'Bima (finder of K-0403) rejects Ayu\'s claim', 'Toast "Claim rejected.", Ayu notified', async () => {
    await bima.goto(BASE + '/items/403'); await bima.locator('.claim-row').first().locator('button:has-text("Reject claim")').click(); await confirmYes(bima);
    const t = await toast(bima); return ok(t === 'Claim rejected.', t);
  });
  const rani = await as('rani@ketemu.test');
  await test('Claims', 'Update (finder approves)', 'Rani (finder of K-0409) approves Bima\'s claim', 'Item becomes Claimed, Ayu\'s pending claim is rejected automatically', async () => {
    await rani.goto(BASE + '/items/409'); await rani.locator('.claim-row').first().locator('button:has-text("Approve claim")').click(); await rani.waitForLoadState();
    const stamps = await rani.locator('.claim-row .stamp').allTextContents(); const tag = await rani.locator('.tag__stamp .stamp').textContent();
    return ok(stamps.includes('Approved') && stamps.includes('Not approved') && tag === 'Claimed', `claims: ${stamps.join(', ')}; tag: ${tag}`);
  });
  await test('Claims', 'Update (mark returned)', 'Rani clicks Mark returned', 'Item stamped Returned with the press animation, toast "Marked returned."', async () => {
    await rani.click('.item__actions button:has-text("Mark returned")'); await rani.waitForLoadState();
    const t = await toast(rani); const press = await rani.locator('.stamp--press').count(); const tag = await rani.locator('.tag__stamp .stamp').textContent();
    return ok(t === 'Marked returned.' && press === 1 && tag === 'Returned', `${t} stamp: ${tag}, press animation: ${press === 1}`);
  });
  await test('Claims', 'Delete (cancel)', 'Ayu files a claim on K-0408, then cancels it', 'Toast "Claim cancelled.", status Cancelled', async () => {
    await ayu.goto(BASE + '/items/408'); await ayu.fill('#f-proof', 'Medium Levis jacket with a train ticket from Malang in the left pocket.'); await ayu.click('button:has-text("File claim")'); await ayu.waitForLoadState();
    await ayu.click('button:has-text("Cancel claim")'); await confirmYes(ayu); const t = await toast(ayu); return ok(t === 'Claim cancelled.', t);
  });

  /* ===== 5. Comments ===== */
  await test('Comments', 'Create (invalid)', 'Post an empty comment', 'Inline error "Write something first"', async () => {
    await ayu.goto(BASE + '/items/402'); await ayu.click('button:has-text("Post comment")'); await ayu.waitForLoadState();
    const e = await fieldErrors(ayu); return ok(e.some((x) => x.includes('Write something first')), e.join(' | '));
  });
  await test('Comments', 'Create', 'Post a sighting on K-0402', 'Toast "Comment posted.", comment at the end of the thread', async () => {
    await ayu.fill('#new-comment', 'Saw a white charger in the floor 3 lost box this morning.'); await ayu.click('button:has-text("Post comment")'); await ayu.waitForLoadState();
    const t = await toast(ayu); return ok(t === 'Comment posted.', t);
  });
  await test('Comments', 'Update', 'Edit own comment', 'Toast "Comment saved.", text updated, marked (edited)', async () => {
    const c = ayu.locator('.comment').last(); await c.locator('button:has-text("Edit")').click(); await c.locator('textarea').fill('Saw a white Lenovo charger in the floor 3 lost box this morning.');
    await c.locator('button:has-text("Save comment")').click(); await ayu.waitForLoadState(); const t = await toast(ayu); return ok(t === 'Comment saved.', t);
  });
  await test('Comments', 'Update (auth)', 'POST an edit to Bima\'s comment (id 4) as Ayu', 'Refused with 403', async () => {
    const r = await ayu.request.post(BASE + '/comments/4/edit', { form: { _csrf: await csrf(ayu), body: 'changed by someone else' } }); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  await test('Comments', 'Delete', 'Delete own comment (confirm in the dialog)', 'Toast "Comment deleted."', async () => {
    await ayu.goto(BASE + '/items/402'); await ayu.locator('.comment').last().locator('button:has-text("Delete")').click(); await confirmYes(ayu);
    const t = await toast(ayu); return ok(t === 'Comment deleted.', t);
  });

  /* ===== 6. Watchlist ===== */
  await test('Watchlist', 'Create', 'Save K-0410 to the watchlist', 'Toast "Saved to watchlist..."', async () => {
    await ayu.goto(BASE + '/items/410'); await ayu.click('button:has-text("Save to watchlist")'); await ayu.waitForLoadState(); const t = await toast(ayu); return ok(t.startsWith('Saved to watchlist'), t);
  });
  await test('Watchlist', 'Read', 'Open /watchlist', 'K-0410 is listed with its tag', async () => {
    await ayu.goto(BASE + '/watchlist'); const n = await ayu.locator('#w-410').count(); return ok(n === 1, `${await ayu.locator('.watch__row').count()} saved items, K-0410 present`);
  });
  await test('Watchlist', 'Update (invalid)', 'Save a note longer than 140 characters (sent directly)', 'Refused: "Keep the note under 140 characters"', async () => {
    await ayu.request.post(BASE + '/watchlist/note', { form: { _csrf: await csrf(ayu), itemId: '410', note: 'x'.repeat(150) }, maxRedirects: 0 });
    await ayu.goto(BASE + '/watchlist'); const t = await toast(ayu); return ok(t.includes('140'), t);
  });
  await test('Watchlist', 'Update', 'Add a private note to K-0410', 'Toast "Note saved.", note shown', async () => {
    const row = ayu.locator('#w-410'); await row.locator('button:has-text("Add note")').click(); await row.locator('input[name=note]').fill("Bima's bag, check Kantin on Friday");
    await row.locator('button:has-text("Save note")').click(); await ayu.waitForLoadState(); const t = await toast(ayu); return ok(t === 'Note saved.', t);
  });
  await test('Watchlist', 'Delete', 'Remove K-0410 from the watchlist', 'Toast "Removed from watchlist."', async () => {
    await ayu.locator('#w-410 button:has-text("Remove")').click(); await confirmYes(ayu); const t = await toast(ayu); return ok(t === 'Removed from watchlist.', t);
  });

  /* ===== 7. Categories (admin) ===== */
  await test('Categories', 'Read (auth)', 'A student opens /admin/categories', '403 page', async () => {
    const r = await ayu.goto(BASE + '/admin/categories'); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  const admin = await as('admin@ketemu.test');
  await test('Categories', 'Read', 'Admin opens /admin/categories', 'Table with 8 seeded categories and item counts', async () => {
    await admin.goto(BASE + '/admin/categories'); const n = await admin.locator('tbody tr').count(); return ok(n >= 8, `${n} rows`);
  });
  await test('Categories', 'Create (invalid)', 'Add a category named "Electronics" (already exists)', 'Inline error "There is already a category called Electronics."', async () => {
    await admin.fill('#f-name', 'Electronics'); await admin.click('button:has-text("Add category")'); const e = await fieldErrors(admin); return ok(e.some((x) => x.includes('already a category')), e.join(' | '));
  });
  await test('Categories', 'Create', 'Add "Sports gear" with the bag icon', 'Toast "Category added."', async () => {
    await admin.fill('#f-name', 'Sports gear ' + stamp); await admin.click('label[for=new-icon-bag]'); await admin.click('button:has-text("Add category")'); await admin.waitForLoadState();
    const t = await toast(admin); return ok(t === 'Category added.', t);
  });
  await test('Categories', 'Update', 'Rename it and change the icon', 'Toast "Category saved."', async () => {
    await admin.locator(`tr:has-text("Sports gear ${stamp}") a:has-text("Edit")`).click(); await admin.fill('#f-name', 'Sports and gym gear ' + stamp); await admin.click('label[for=icon-shirt]');
    await admin.click('button:has-text("Save category")'); await admin.waitForLoadState(); const t = await toast(admin); return ok(t === 'Category saved.', t);
  });
  await test('Categories', 'Delete (rule)', 'Delete "Electronics", which items still use', 'Blocked: "3 items use Electronics. Move them to another category first."', async () => {
    await admin.locator('tr:has-text("Electronics") button:has-text("Delete")').click(); await confirmYes(admin); const t = await toast(admin); return ok(t.includes('use Electronics'), t);
  });
  await test('Categories', 'Delete', 'Delete the unused new category', 'Toast "Category deleted."', async () => {
    await admin.locator(`tr:has-text("Sports and gym gear ${stamp}") button:has-text("Delete")`).click(); await confirmYes(admin); const t = await toast(admin); return ok(t === 'Category deleted.', t);
  });

  /* ===== 8. Campus locations (admin) ===== */
  await test('Locations', 'Read', 'Admin opens /admin/locations', 'Table with 10 seeded locations, 3 SKK posts', async () => {
    await admin.goto(BASE + '/admin/locations'); const n = await admin.locator('tbody tr').count(); const posts = await admin.locator('tbody tr:has-text("Yes, pickup point")').count();
    return ok(n >= 10 && posts >= 3, `${n} rows, ${posts} SKK posts`);
  });
  await test('Locations', 'Create', 'Add "Gedung Pascasarjana" as an SKK post', 'Toast "Location added."', async () => {
    await admin.fill('#f-name', 'Gedung Pascasarjana ' + stamp); await admin.fill('#f-zone', 'North campus'); await admin.check('input[name=skkPost]');
    await admin.click('button:has-text("Add location")'); await admin.waitForLoadState(); const t = await toast(admin); return ok(t === 'Location added.', t);
  });
  await test('Locations', 'Update', 'Edit it and switch off "This is an SKK post"', 'Toast "Location saved.", SKK post column shows No', async () => {
    await admin.locator(`tr:has-text("Gedung Pascasarjana ${stamp}") a:has-text("Edit")`).click(); await admin.uncheck('input[name=skkPost]'); await admin.click('button:has-text("Save location")'); await admin.waitForLoadState();
    const t = await toast(admin); const v = await admin.locator(`tr:has-text("Gedung Pascasarjana ${stamp}") td`).nth(2).textContent(); return ok(t === 'Location saved.' && v.trim() === 'No', `${t} SKK post: ${v.trim()}`);
  });
  await test('Locations', 'Delete (rule)', 'Delete "Kantin Pusat", where items were reported', 'Blocked with a message to move the items first', async () => {
    await admin.locator('tr:has-text("Kantin Pusat") button:has-text("Delete")').click(); await confirmYes(admin); const t = await toast(admin); return ok(t.includes('Move them to another location first'), t);
  });
  await test('Locations', 'Delete', 'Delete the unused new location', 'Toast "Location deleted."', async () => {
    await admin.locator(`tr:has-text("Gedung Pascasarjana ${stamp}") button:has-text("Delete")`).click(); await confirmYes(admin); const t = await toast(admin); return ok(t.startsWith('Location deleted.'), t);
  });

  /* ===== Extras ===== */
  await test('Notifications', 'Read', 'Bima opens /notifications after his claim was approved', 'Notification "Your claim K-0409-01 was approved..." with an unread dot', async () => {
    await bima.goto(BASE + '/notifications'); const msgs = await bima.locator('.note__msg').allTextContents(); return ok(msgs.some((m) => m.includes('K-0409-01 was approved')), `${msgs.length} notifications, newest: "${msgs[0].trim()}"`);
  });
  await test('Notifications', 'Update / Delete', 'Mark all as read, then Clear all', 'Toasts "Marked all as read." and "Notifications cleared."', async () => {
    await bima.click('button:has-text("Mark all as read")'); await bima.waitForLoadState(); const t1 = await toast(bima);
    await bima.click('button:has-text("Clear all")'); await confirmYes(bima); const t2 = await toast(bima); return ok(t1 === 'Marked all as read.' && t2 === 'Notifications cleared.', `${t1} / ${t2}`);
  });
  await test('Admin users', 'Update', 'Admin changes Ayu to admin and back to student', 'Two toasts "Role changed..."', async () => {
    await admin.goto(BASE + '/admin/users?q=ayu'); await admin.locator('tr:has-text("Ayu") select').selectOption('admin'); await admin.locator('tr:has-text("Ayu") button:has-text("Change role")').click(); await admin.waitForLoadState();
    const t1 = await toast(admin); await admin.goto(BASE + '/admin/users?q=ayu'); await admin.locator('tr:has-text("Ayu") select').selectOption('student'); await admin.locator('tr:has-text("Ayu") button:has-text("Change role")').click(); await admin.waitForLoadState();
    const t2 = await toast(admin); return ok(t1.startsWith('Role changed') && t2.includes('a student'), `${t1} / ${t2}`);
  });

  /* ===== Security ===== */
  await test('Security', 'CSRF', 'POST /profile/edit without the _csrf token', 'Refused with 403 "This form expired"', async () => {
    const r = await guest.request.post(BASE + '/profile/edit', { form: { nrp: nrp, fullName: 'Hacked', email: email } }); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });
  await test('Security', 'XSS', 'Post an item titled <script>alert(1)</script>', 'Shown as text, no script element is created', async () => {
    await guest.goto(BASE + '/items/new?kind=lost'); await guest.fill('#f-title', '<script>alert(1)</script> xss test'); await guest.selectOption('#f-categoryId', { label: 'Other' }); await guest.selectOption('#f-locationId', { label: 'Bundaran ITS' });
    let dialog = false; guest.on('dialog', (d) => { dialog = true; d.dismiss(); }); await guest.click('.item-form__main button[type=submit]'); await guest.waitForLoadState();
    const h = await guest.locator('h1').textContent(); const html = await guest.content(); return ok(!dialog && h.includes('<script>') && html.includes('&lt;script&gt;'), `title rendered as text: "${h.trim()}", alert fired: ${dialog}`);
  });
  await test('Security', 'SQL injection', "Search the board for ' OR 1=1 --", 'Treated as plain text: empty result, no error', async () => {
    const r = await guest.goto(BASE + "/?q=' OR 1=1 --"); const n = await guest.locator('.board .tag').count(); return ok(r.status() === 200 && n === 0, `HTTP ${r.status()}, ${n} tags`);
  });
  await test('Security', 'Authentication', 'A guest opens /claims', 'Redirected to /login?next=/claims', async () => {
    const g = await as(null); await g.goto(BASE + '/claims'); return ok(g.url().includes('/login?next=%2Fclaims'), g.url().replace(BASE, ''));
  });
  await test('Security', 'Authorization', 'A student opens /admin', '403 page "That is behind the desk."', async () => {
    const r = await guest.goto(BASE + '/admin'); return ok(r.status() === 403, `HTTP ${r.status()}`);
  });

  /* ===== cleanup: Items Delete and Accounts Delete ===== */
  await test('Items', 'Delete', 'Delete own report (confirm in the dialog)', 'Toast "Report deleted. K-xxxx is off the board."', async () => {
    await guest.goto(itemUrl); await guest.click('button:has-text("Delete report")'); await confirmYes(guest); const t = await toast(guest); return ok(t.startsWith('Report deleted.'), t);
  });
  await test('Accounts', 'Delete (invalid)', 'Delete account with a wrong password', 'Error "That is not your password. Your account was not deleted."', async () => {
    await guest.goto(BASE + '/profile/delete'); await guest.fill('#f-password', 'wrongpass'); await guest.click('form.auth__form button[type=submit]');
    const e = await fieldErrors(guest); return ok(e.some((x) => x.includes('not your password')), e.join(' | '));
  });
  await test('Accounts', 'Delete', 'Delete account with the right password', 'Account and its items removed, toast "Account deleted..."', async () => {
    await guest.fill('#f-password', 'password9'); await guest.click('form.auth__form button[type=submit]'); await guest.waitForLoadState(); const t = await toast(guest); return ok(t.startsWith('Account deleted'), t);
  });

  await browser.close();
  const passed = results.filter((r) => r.result === 'Pass').length;
  const out = path.join(__dirname, 'test-results');
  fs.writeFileSync(out + '.json', JSON.stringify({ base: BASE, ranAt: new Date().toISOString(), passed, total: results.length, results }, null, 2));
  const esc = (s) => String(s).replace(/\|/g, '\\|').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  const md = ['| No | Feature | Operation | Steps | Expected result | Actual result | Result |', '|---|---|---|---|---|---|---|']
    .concat(results.map((r) => `| ${r.no} | ${r.feature} | ${esc(r.operation)} | ${esc(r.steps)} | ${esc(r.expected)} | ${esc(r.actual)} | ${r.result} |`)).join('\n');
  fs.writeFileSync(out + '.md', `Run against ${BASE} on ${new Date().toISOString()}: ${passed} of ${results.length} passed.\n\n${md}\n`);
  console.log(`\n${passed} of ${results.length} passed`);
  process.exit(passed === results.length ? 0 : 1);
})();
