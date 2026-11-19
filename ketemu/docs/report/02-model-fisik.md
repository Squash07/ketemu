# 2. Model Data Fisik

Model fisik diimplementasikan pada **PostgreSQL 16**. Seluruh definisi tabel terdapat pada berkas `src/main/resources/db/schema.sql` dan dijalankan otomatis oleh `AppInit` setiap kali aplikasi dinyalakan. Karena seluruh perintah memakai `CREATE TABLE IF NOT EXISTS` dan `CREATE INDEX IF NOT EXISTS`, skrip tersebut aman dijalankan berulang kali tanpa menghapus data. Data contoh (`seed.sql`) hanya dimuat sekali, yaitu ketika tabel `users` belum ada.

Konvensi yang dipakai di semua tabel:

- Kunci primer berupa `SERIAL` (bilangan bulat yang bertambah otomatis), kecuali `watchlist` yang memakai kunci gabungan.
- Nilai berhingga (enumerasi) disimpan sebagai `VARCHAR` dengan batasan `CHECK`, sehingga nilai di luar daftar ditolak oleh basis data, bukan hanya oleh aplikasi.
- Kolom waktu memakai `TIMESTAMP` dengan nilai bawaan `now()`.

## 2.1 Tabel `users`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas internal pengguna |
| nrp | VARCHAR(12) | UNIQUE, NOT NULL | NRP mahasiswa atau nomor petugas SKK |
| full_name | VARCHAR(100) | NOT NULL | Nama lengkap yang ditampilkan pada laporan dan klaim |
| email | VARCHAR(120) | UNIQUE, NOT NULL | Dipakai untuk masuk; perbandingan dilakukan tanpa membedakan huruf besar dan kecil |
| password_hash | VARCHAR(72) | NOT NULL | Hasil hash bcrypt (cost 10), tidak pernah menyimpan kata sandi asli |
| phone | VARCHAR(20) | boleh NULL | Nomor telepon opsional (angka, spasi, +, tanda kurung, atau tanda hubung) |
| role | VARCHAR(10) | NOT NULL, DEFAULT 'student', CHECK IN ('student','admin') | Peran pengguna |
| created_at | TIMESTAMP | NOT NULL, DEFAULT now() | Waktu pendaftaran |

## 2.2 Tabel `categories`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas kategori |
| name | VARCHAR(40) | UNIQUE, NOT NULL | Nama kategori, tidak boleh kembar |
| icon | VARCHAR(30) | NOT NULL | Nama ikon dari himpunan ikon aplikasi (misalnya `phone`, `wallet`, `key`) |
| created_at | TIMESTAMP | DEFAULT now() | Waktu kategori ditambahkan |

## 2.3 Tabel `locations`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas lokasi |
| name | VARCHAR(80) | UNIQUE, NOT NULL | Nama lokasi di kampus |
| zone | VARCHAR(40) | boleh NULL | Zona atau kawasan (misalnya "Main gate" atau "Central campus") |
| is_skk_post | BOOLEAN | NOT NULL, DEFAULT false | Menandai lokasi yang menjadi pos SKK untuk penitipan dan serah terima |
| created_at | TIMESTAMP | DEFAULT now() | Waktu lokasi ditambahkan |

## 2.4 Tabel `items`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas laporan |
| code | VARCHAR(10) | UNIQUE | Kode tag publik `K-NNNN`, diisi tepat setelah baris dibuat berdasarkan `id` (`ItemDao.insert`) |
| reporter_id | INT | NOT NULL, FK → users(id) ON DELETE CASCADE | Pelapor |
| category_id | INT | NOT NULL, FK → categories(id) ON DELETE RESTRICT | Kategori barang |
| location_id | INT | NOT NULL, FK → locations(id) ON DELETE RESTRICT | Lokasi barang hilang atau ditemukan |
| kind | VARCHAR(5) | NOT NULL, CHECK IN ('lost','found') | Jenis laporan |
| title | VARCHAR(80) | NOT NULL | Judul singkat barang (divalidasi 3–80 karakter) |
| description | TEXT | boleh NULL | Ciri-ciri barang (maksimal 2000 karakter) |
| location_detail | VARCHAR(120) | boleh NULL | Detail tempat, misalnya "meja dekat jendela lantai 2" |
| event_date | DATE | NOT NULL | Tanggal barang hilang atau ditemukan (tidak boleh di masa depan) |
| held_at_id | INT | FK → locations(id) ON DELETE SET NULL | Pos SKK tempat barang temuan dititipkan |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'open', CHECK IN ('open','claimed','returned','closed') | Status siklus hidup laporan |
| created_at | TIMESTAMP | DEFAULT now() | Waktu laporan dibuat |
| updated_at | TIMESTAMP | DEFAULT now() | Diperbarui setiap kali isi atau status laporan berubah |

## 2.5 Tabel `item_photos`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas foto |
| item_id | INT | NOT NULL, FK → items(id) ON DELETE CASCADE | Laporan pemilik foto |
| content | BYTEA | NOT NULL | Isi berkas gambar (maksimal 1 MB) |
| mime_type | VARCHAR(30) | NOT NULL | `image/jpeg`, `image/png`, atau `image/webp`, ditentukan dari isi berkas |
| sort_order | SMALLINT | NOT NULL, DEFAULT 0 | Urutan tampil; nilai terkecil menjadi foto sampul |
| created_at | TIMESTAMP | DEFAULT now() | Waktu unggah |

## 2.6 Tabel `claims`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas klaim |
| item_id | INT | NOT NULL, FK → items(id) ON DELETE CASCADE | Barang yang diklaim |
| claimant_id | INT | NOT NULL, FK → users(id) ON DELETE CASCADE | Pengaju klaim |
| proof | TEXT | NOT NULL | Bukti kepemilikan (divalidasi 15–1000 karakter) |
| meet_location_id | INT | FK → locations(id) ON DELETE SET NULL | Pos SKK yang diusulkan untuk serah terima |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'pending', CHECK IN ('pending','approved','rejected','cancelled') | Status klaim |
| created_at | TIMESTAMP | DEFAULT now() | Waktu pengajuan |
| decided_at | TIMESTAMP | boleh NULL | Waktu klaim disetujui, ditolak, atau dibatalkan |

Indeks unik parsial `ux_claims_active ON claims (item_id, claimant_id) WHERE status IN ('pending','approved')` menjamin satu pengguna hanya memiliki satu klaim aktif untuk barang yang sama. Klaim yang sudah ditolak atau dibatalkan tidak terhitung, sehingga pengguna tetap dapat mengajukan ulang dengan bukti yang lebih kuat. Nomor tiket klaim seperti `K-0409-01` tidak disimpan sebagai kolom, melainkan dihitung dari urutan klaim pada barang tersebut.

## 2.7 Tabel `comments`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas komentar |
| item_id | INT | NOT NULL, FK → items(id) ON DELETE CASCADE | Laporan yang dikomentari |
| author_id | INT | NOT NULL, FK → users(id) ON DELETE CASCADE | Penulis komentar |
| body | TEXT | NOT NULL | Isi komentar (divalidasi 1–1000 karakter) |
| created_at | TIMESTAMP | DEFAULT now() | Waktu dibuat |
| updated_at | TIMESTAMP | DEFAULT now() | Waktu terakhir diubah; dipakai untuk menampilkan penanda "(edited)" |

## 2.8 Tabel `watchlist`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| user_id | INT | PK (gabungan), FK → users(id) ON DELETE CASCADE | Pengguna yang memantau |
| item_id | INT | PK (gabungan), FK → items(id) ON DELETE CASCADE | Laporan yang dipantau |
| note | VARCHAR(140) | boleh NULL | Catatan pribadi, hanya terlihat oleh pemiliknya |
| created_at | TIMESTAMP | DEFAULT now() | Waktu disimpan |

## 2.9 Tabel `notifications`

| Kolom | Tipe | Batasan | Keterangan |
|---|---|---|---|
| id | SERIAL | PRIMARY KEY | Identitas notifikasi |
| user_id | INT | NOT NULL, FK → users(id) ON DELETE CASCADE | Penerima |
| item_id | INT | FK → items(id) ON DELETE CASCADE | Laporan terkait (opsional) |
| message | VARCHAR(200) | NOT NULL | Isi pemberitahuan |
| is_read | BOOLEAN | NOT NULL, DEFAULT false | Penanda sudah dibaca |
| created_at | TIMESTAMP | DEFAULT now() | Waktu dibuat |

## 2.10 Indeks

| Indeks | Kolom | Alasan |
|---|---|---|
| ix_items_kind_status | items (kind, status) | Filter papan utama "Lost / Found" sekaligus menyaring status |
| ix_items_category | items (category_id) | Filter kategori dan penghitungan pemakaian kategori sebelum dihapus |
| ix_items_location | items (location_id) | Filter lokasi dan penghitungan pemakaian lokasi sebelum dihapus |
| ix_items_created | items (created_at DESC) | Urutan "terbaru" pada papan utama |
| ix_item_photos_item | item_photos (item_id) | Mengambil galeri dan foto sampul per laporan |
| ix_claims_item | claims (item_id) | Menampilkan klaim masuk pada halaman barang |
| ix_comments_item | comments (item_id) | Menampilkan utas komentar pada halaman barang |
| ix_notifications_user | notifications (user_id, is_read) | Menghitung notifikasi belum dibaca pada setiap permintaan halaman |
| ux_claims_active | claims (item_id, claimant_id) parsial | Aturan satu klaim aktif per pengguna per barang |

## 2.11 Aturan kunci asing dan ON DELETE

Aturan penghapusan dipilih berdasarkan sifat hubungan antardata:

| Relasi | Aturan | Alasan |
|---|---|---|
| items.reporter_id → users | CASCADE | Ketika pengguna menghapus akunnya, laporan miliknya ikut terhapus. Halaman hapus akun menyampaikan konsekuensi ini secara eksplisit sebelum pengguna mengonfirmasi dengan kata sandi. |
| items.category_id → categories | RESTRICT | Kategori adalah data referensi. Menghapus kategori yang masih dipakai akan membuat laporan kehilangan klasifikasi, sehingga basis data menolaknya. Aplikasi memeriksa terlebih dahulu dan menampilkan pesan yang jelas, misalnya "3 items use Electronics. Move them to another category first." |
| items.location_id → locations | RESTRICT | Alasan yang sama dengan kategori. Pesan yang ditampilkan: "3 items were reported at Kantin Pusat. Move them to another location first." |
| items.held_at_id → locations | SET NULL | Lokasi penitipan bersifat opsional, sehingga laporan tetap sah walaupun pos SKK tersebut dihapus. |
| claims.meet_location_id → locations | SET NULL | Titik serah terima hanyalah usulan. Klaim tetap berlaku tanpa nilai ini. |
| item_photos.item_id, claims.item_id, comments.item_id, watchlist.item_id, notifications.item_id → items | CASCADE | Data turunan ini tidak memiliki arti tanpa laporannya. |
| claims.claimant_id, comments.author_id, watchlist.user_id, notifications.user_id → users | CASCADE | Data milik pengguna ikut terhapus ketika akun dihapus, sesuai prinsip bahwa pengguna berhak menghapus jejak datanya. |

## 2.12 Alasan menyimpan foto sebagai BYTEA

Foto disimpan langsung di kolom `item_photos.content` bertipe `BYTEA`, bukan sebagai berkas pada disk server. Keputusan ini diambil karena layanan hosting gratis Render memakai sistem berkas yang bersifat sementara (*ephemeral*): setiap kali aplikasi di-*deploy* ulang atau dinyalakan kembali setelah tertidur, berkas yang ditulis ke disk akan hilang. Dengan menyimpan foto di basis data Neon, foto tetap ada selama datanya ada, dan pencadangan basis data otomatis mencakup foto.

Konsekuensi dan mitigasinya:

- **Ukuran basis data bertambah.** Paket gratis Neon memiliki kuota penyimpanan terbatas. Untuk itu setiap foto dibatasi maksimal 1 MB dan maksimal 3 foto per laporan, sehingga satu laporan paling banyak menambah sekitar 3 MB.
- **Beban baca ke basis data.** Setiap foto dilayani oleh `PhotoServlet` dengan header `ETag` dan `Cache-Control: public, max-age=86400`. Peramban menyimpan foto selama satu hari dan selanjutnya cukup mengirim `If-None-Match`, yang dijawab `304 Not Modified` tanpa mengirim ulang isi gambar (lihat uji nomor 26 pada `05-pengujian.md`).
- **Query daftar tidak memuat isi foto.** Query papan utama hanya mengambil `id` foto sampul, sedangkan kolom `content` baru dibaca ketika gambar diminta, sehingga halaman daftar tetap ringan.
- **Keamanan isi berkas.** Jenis gambar ditentukan dari *magic bytes* isi berkas (`Uploads.sniff`), bukan dari ekstensi nama berkas, sehingga berkas teks yang diganti namanya menjadi `.png` akan ditolak.
