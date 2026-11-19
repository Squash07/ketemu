-- Ketemu seed data. Safe to run more than once.
-- Passwords are not stored here: seeded accounts start with the marker 'PENDING'
-- and the application hashes the demo password with bcrypt on first start.

INSERT INTO users (nrp, full_name, email, password_hash, phone, role) VALUES
  ('SKK0001',    'Hadi Prasetyo', 'admin@ketemu.test', 'PENDING', '031-5994251', 'admin'),
  ('5026231045', 'Rani Pratiwi',     'rani@ketemu.test',  'PENDING', '081234567801', 'student'),
  ('5025231117', 'Ayu Lestari',      'ayu@ketemu.test',   'PENDING', '081234567802', 'student'),
  ('5022231009', 'Bima Santoso',     'bima@ketemu.test',  'PENDING', NULL,           'student')
ON CONFLICT (email) DO NOTHING;

INSERT INTO categories (name, icon) VALUES
  ('Wallets and cards',     'wallet'),
  ('Electronics',           'charger'),
  ('Keys',                  'key'),
  ('Bags',                  'bag'),
  ('Bottles and umbrellas', 'umbrella'),
  ('Books and stationery',  'book'),
  ('Clothing',              'shirt'),
  ('Other',                 'box')
ON CONFLICT (name) DO NOTHING;

INSERT INTO locations (name, zone, is_skk_post) VALUES
  ('Gerbang Utama SKK post',          'Main gate',      true),
  ('Perpustakaan Pusat',              'Central campus', true),
  ('Asrama Mahasiswa',                'Dormitories',    true),
  ('Masjid Manarul Ilmi',             'Central campus', false),
  ('Kantin Pusat',                    'Central campus', false),
  ('Research Center',                 'North campus',   false),
  ('Bundaran ITS',                    'Main gate',      false),
  ('Departemen Teknik Informatika',   'East campus',    false),
  ('Departemen Teknik Elektro',       'East campus',    false),
  ('Departemen Teknik Kimia',         'West campus',    false)
ON CONFLICT (name) DO NOTHING;

-- Item codes continue from the midterm numbering, so the first seeded tag is K-0398.
SELECT setval('items_id_seq', 397, true) WHERE NOT EXISTS (SELECT 1 FROM items);

INSERT INTO items (reporter_id, category_id, location_id, kind, title, description, location_detail, event_date, held_at_id, status, created_at, updated_at)
SELECT u.id, c.id, l.id, v.kind, v.title, v.description, v.detail, current_date - v.days_ago, h.id, v.status,
       now() - make_interval(days => v.days_ago, hours => v.seq), now() - make_interval(days => v.days_ago, hours => v.seq)
FROM (VALUES
  (1,  'ayu@ketemu.test',  'Wallets and cards',     'Bundaran ITS',                  'found', 'KTM card, Teknik Kimia',            'Student card lying on the grass by the roundabout, the name on it is clearly readable. Handed to the main gate post the same afternoon.', 'grass near the bus stop', 10, 'Gerbang Utama SKK post', 'returned'),
  (2,  'ayu@ketemu.test',  'Books and stationery',  'Research Center',               'lost',  'Calculus textbook, Purcell 9th ed.', 'Blue cover, my name written on the first page. Probably left it on a table in the lobby.', 'ground floor lobby', 12, NULL, 'closed'),
  (3,  'rani@ketemu.test', 'Electronics',           'Departemen Teknik Informatika', 'lost',  'Wired black earphones',             'Cheap wired earphones with a tangled cable, but the left bud has a small blue dot of nail polish.', 'Lab Pemrograman 2', 11, NULL, 'returned'),
  (4,  'bima@ketemu.test', 'Other',                 'Masjid Manarul Ilmi',           'found', 'Silver ring with engraving',        'Thin silver ring found near the taps. There is a short engraving inside. Tell me what it says to claim it.', 'ablution area, men''s side', 8, 'Gerbang Utama SKK post', 'open'),
  (5,  'rani@ketemu.test', 'Electronics',           'Perpustakaan Pusat',            'lost',  '65 W USB-C laptop charger',         'White Lenovo charger with a long cable and a strip of washi tape near the plug. Left it plugged in under a desk.', 'floor 3, quiet room', 7, NULL, 'open'),
  (6,  'bima@ketemu.test', 'Books and stationery',  'Departemen Teknik Elektro',     'found', 'Casio fx-991 calculator',           'Grey Casio fx-991ID Plus left on a desk after the morning exam. Back cover has initials scratched into it.', 'room 102', 6, 'Perpustakaan Pusat', 'open'),
  (7,  'rani@ketemu.test', 'Electronics',           'Research Center',               'found', 'White earbuds case',                'Charging case only, no earbuds inside. Slight crack on the hinge.', 'lobby, near the stairs', 5, 'Perpustakaan Pusat', 'claimed'),
  (8,  'ayu@ketemu.test',  'Bottles and umbrellas', 'Kantin Pusat',                  'lost',  'Matte green tumbler with stickers', '750 ml tumbler, matte green, with a Surabaya zoo sticker and a cat sticker on the side.', 'tables near the juice stall', 4, NULL, 'open'),
  (9,  'ayu@ketemu.test',  'Keys',                  'Departemen Teknik Informatika', 'found', 'Motorbike key on a red lanyard',    'Honda key with a red lanyard and a small padlock key on the same ring.', 'motorbike parking, back row', 3, 'Gerbang Utama SKK post', 'open'),
  (10, 'bima@ketemu.test', 'Wallets and cards',     'Perpustakaan Pusat',            'lost',  'Black wallet, KTM inside',          'Black faux-leather wallet. Inside are my KTM, a BCA debit card and some cash. I think it slipped out of my bag between the shelves.', 'floor 2, reading room', 2, NULL, 'open'),
  (11, 'rani@ketemu.test', 'Clothing',              'Asrama Mahasiswa',              'found', 'Blue denim jacket',                 'Medium size denim jacket left on the drying rack for three days. The dorm SKK post is keeping it.', 'block C laundry room', 2, 'Asrama Mahasiswa', 'open'),
  (12, 'rani@ketemu.test', 'Bottles and umbrellas', 'Masjid Manarul Ilmi',           'found', 'Grey umbrella, wooden handle',      'Left on the north porch after Jumatan. Folding, about 60 cm closed, one bent rib. I handed it to the SKK post at the main gate.', 'north porch', 1, 'Gerbang Utama SKK post', 'open'),
  (13, 'bima@ketemu.test', 'Bags',                  'Kantin Pusat',                  'lost',  'Navy backpack with laptop sleeve',  'Navy Eiger backpack, no laptop inside, but my lab notebooks and a pencil case are. Keychain of a small rubber duck.', 'outdoor seating area', 1, NULL, 'open'),
  (14, 'ayu@ketemu.test',  'Wallets and cards',     'Bundaran ITS',                  'found', 'ID lanyard, Teknik Sipil',          'Blue lanyard with a department badge holder, the card slot is empty.', 'pedestrian crossing', 0, 'Gerbang Utama SKK post', 'open')
) AS v(seq, email, category, place, kind, title, description, detail, days_ago, held_at, status)
JOIN users u      ON u.email = v.email
JOIN categories c ON c.name  = v.category
JOIN locations l  ON l.name  = v.place
LEFT JOIN locations h ON h.name = v.held_at
WHERE NOT EXISTS (SELECT 1 FROM items)
ORDER BY v.seq;

UPDATE items SET code = 'K-' || lpad(id::text, 4, '0') WHERE code IS NULL;

INSERT INTO comments (item_id, author_id, body, created_at, updated_at)
SELECT i.id, u.id, v.body, now() - make_interval(hours => v.hours_ago), now() - make_interval(hours => v.hours_ago)
FROM (VALUES
  ('Grey umbrella, wooden handle', 'ayu@ketemu.test',  'Saw a grey umbrella like this at Kantin Pusat on Monday, could be a different one.', 20),
  ('Grey umbrella, wooden handle', 'bima@ketemu.test', 'Mine has a cracked handle. I will check with the main gate post tomorrow.', 6),
  ('Black wallet, KTM inside',     'rani@ketemu.test', 'Someone handed a black wallet to the library return desk around 4 pm. Worth asking them before it goes to SKK.', 30),
  ('65 W USB-C laptop charger',    'bima@ketemu.test', 'There was a white charger on the floor 3 charging bench yesterday evening.', 50),
  ('Casio fx-991 calculator',      'rani@ketemu.test', 'If it is the one from the Kalkulus exam, the proctor also collected two pencil cases.', 70)
) AS v(title, email, body, hours_ago)
JOIN items i ON i.title = v.title
JOIN users u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM comments);

INSERT INTO claims (item_id, claimant_id, proof, meet_location_id, status, created_at, decided_at)
SELECT i.id, u.id, v.proof, l.id, v.status, now() - make_interval(days => v.days_ago),
       CASE WHEN v.status = 'pending' THEN NULL ELSE now() - make_interval(days => v.days_ago - 1) END
FROM (VALUES
  ('White earbuds case',      'bima@ketemu.test', 'The case has a crack on the hinge and a tiny "B" scratched on the bottom. The serial number is in my phone''s Bluetooth settings.', 'Perpustakaan Pusat', 'approved', 4),
  ('Casio fx-991 calculator', 'ayu@ketemu.test',  'My initials A.L. are scratched on the back cover, and the battery lid is held with clear tape.', 'Perpustakaan Pusat', 'pending', 2)
) AS v(title, email, proof, meet, status, days_ago)
JOIN items i     ON i.title = v.title
JOIN users u     ON u.email = v.email
JOIN locations l ON l.name  = v.meet
WHERE NOT EXISTS (SELECT 1 FROM claims);

INSERT INTO watchlist (user_id, item_id, note)
SELECT u.id, i.id, v.note
FROM (VALUES
  ('ayu@ketemu.test',  'Black wallet, KTM inside',     'Bima''s wallet, told him I would keep an eye out'),
  ('bima@ketemu.test', 'Grey umbrella, wooden handle', 'Could be mine, check the handle'),
  ('rani@ketemu.test', 'Matte green tumbler with stickers', NULL)
) AS v(email, title, note)
JOIN users u ON u.email = v.email
JOIN items i ON i.title = v.title
ON CONFLICT DO NOTHING;

INSERT INTO notifications (user_id, item_id, message, is_read, created_at)
SELECT u.id, i.id, v.message, v.is_read, now() - make_interval(hours => v.hours_ago)
FROM (VALUES
  ('bima@ketemu.test', 'Casio fx-991 calculator', 'Ayu Lestari filed a claim on your item K-0403.', false, 48),
  ('bima@ketemu.test', 'White earbuds case',      'Your claim on K-0404 was approved. Pick it up at Perpustakaan Pusat.', true, 96),
  ('rani@ketemu.test', 'White earbuds case',      'You approved a claim on K-0404. Meet at Perpustakaan Pusat.', true, 96),
  ('ayu@ketemu.test',  'Black wallet, KTM inside', 'Rani Pratiwi commented on an item in your watchlist.', false, 30)
) AS v(email, title, message, is_read, hours_ago)
JOIN users u ON u.email = v.email
JOIN items i ON i.title = v.title
WHERE NOT EXISTS (SELECT 1 FROM notifications);
