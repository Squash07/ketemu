# 3. Daftar Fitur dan Operasi CRUD

Ketemu memiliki delapan fitur CRUD utama, masing-masing dengan operasi Create, Read, Update, dan Delete yang lengkap, ditambah tiga fitur pendukung. Kolom "Rute" menunjukkan URL yang menangani operasi tersebut. Semua operasi yang mengubah data memakai metode `POST` dengan token CSRF dan mengikuti pola *Post/Redirect/Get*, sehingga memuat ulang halaman tidak mengirim formulir dua kali. Pesan hasil operasi ditampilkan sebagai *toast* di bagian atas halaman.

Peran pengguna:

- **Tamu** dapat melihat papan utama, halaman barang, dan foto, tetapi harus masuk untuk melakukan operasi lain.
- **Mahasiswa** dapat mengelola akun, laporan, foto, klaim, komentar, daftar pantau, dan notifikasinya sendiri.
- **Admin (petugas meja SKK)** memiliki semua hak mahasiswa, ditambah pengelolaan kategori, lokasi, pengguna, buku besar meja (*desk ledger*), serta moderasi laporan dan komentar milik siapa pun.

## 3.1 Fitur 1: Akun dan Profil

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Pendaftaran akun dengan NRP, nama lengkap, email, nomor telepon (opsional), dan kata sandi minimal 8 karakter yang diketik dua kali. Setelah berhasil, pengguna langsung masuk. | `GET/POST /register` |
| Read | Halaman profil menampilkan identitas pengguna dan daftar laporan miliknya. Profil ringkas pelapor juga tampil pada halaman barang. | `GET /profile` |
| Update | Mengubah nama, NRP, email, dan telepon. Mengganti kata sandi dengan memasukkan kata sandi lama. | `POST /profile/edit`, `POST /profile/password` |
| Delete | Menghapus akun setelah mengetik ulang kata sandi. Seluruh laporan, klaim, dan komentar milik pengguna ikut terhapus. Admin terakhir tidak dapat menghapus akunnya agar sistem tidak kehilangan pengelola. | `POST /profile/delete` |
| Pendukung | Masuk dan keluar sesi. | `GET/POST /login`, `POST /logout` |

## 3.2 Fitur 2: Laporan Barang

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Membuat laporan barang hilang atau temuan: judul, kategori, lokasi, detail tempat, tanggal kejadian, deskripsi, pos penitipan (untuk barang temuan), dan hingga tiga foto. Sistem memberi kode tag `K-NNNN`. | `GET /items/new`, `POST /items/new` |
| Read | Papan utama dengan filter jenis (All, Lost, Found), kategori, lokasi, pencarian kata kunci, dan paginasi 12 tag per halaman. Laporan berstatus `closed` tidak tampil di papan. Halaman detail menampilkan galeri, stempel status, komentar, dan klaim. | `GET /`, `GET /items/{id}` |
| Update | Mengubah isi laporan. Mengubah status: menutup laporan (`close`), membuka kembali (`reopen`), dan menandai barang hilang sudah ditemukan kembali (`recovered`). | `POST /items/{id}/edit`, `/close`, `/reopen`, `/recovered` |
| Delete | Menghapus laporan beserta foto, klaim, komentar, dan entri daftar pantau yang terkait (setelah konfirmasi dialog). | `POST /items/{id}/delete` |

Hanya pelapor atau admin yang dapat mengubah dan menghapus laporan. Aturan ini diperiksa di `ItemService.requireReporter`.

## 3.3 Fitur 3: Foto Barang

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Mengunggah hingga tiga foto JPG, PNG, atau WebP berukuran maksimal 1 MB per foto, dengan pratinjau sebelum dikirim. | bagian dari `POST /items/new` dan `POST /items/{id}/edit` |
| Read | Galeri dan *thumbnail* pada halaman barang, foto sampul pada kartu di papan utama. Isi gambar dilayani dengan `ETag` dan *cache* peramban. | `GET /photo/{id}` |
| Update | Mengganti berkas sebuah foto dan memilih foto sampul. | `POST /photos/{id}/replace`, `POST /photos/{id}/cover` |
| Delete | Menghapus satu foto (setelah konfirmasi dialog). | `POST /photos/{id}/delete` |

## 3.4 Fitur 4: Klaim dan Serah Terima

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Mengajukan klaim atas barang temuan dengan bukti kepemilikan (minimal 15 karakter) dan pos SKK yang diusulkan untuk serah terima. Pengguna menerima tiket klaim bernomor, misalnya `K-0409-01`. | `POST /items/{id}/claim` |
| Read | Daftar "My claims" dalam bentuk potongan tiket, halaman tiket klaim lengkap, dan daftar klaim masuk pada halaman barang milik penemu. | `GET /claims`, `GET /claims/{id}` |
| Update | Pengaju mengubah bukti selama klaim masih *pending*. Penemu menyetujui atau menolak klaim. Setelah barang diserahkan, penemu menandai barang sudah dikembalikan (stempel "Returned"). | `POST /claims/{id}/edit`, `/approve`, `/reject`, `POST /items/{id}/returned` |
| Delete | Pengaju membatalkan klaim. Klaim tidak dihapus secara fisik, tetapi berstatus *cancelled* agar riwayat tetap tercatat. | `POST /claims/{id}/cancel` |

Aturan bisnis yang ditegakkan di `ClaimService`:

1. Hanya barang temuan (`found`) berstatus `open` yang dapat diklaim.
2. Pelapor tidak dapat mengklaim barangnya sendiri (ditolak dengan 403).
3. Satu pengguna hanya boleh memiliki satu klaim aktif per barang (didukung indeks unik parsial).
4. Ketika satu klaim disetujui, status barang menjadi `claimed` dan klaim lain yang masih *pending* otomatis ditolak dalam satu transaksi. Semua pihak menerima notifikasi.

## 3.5 Fitur 5: Komentar dan Laporan Penampakan

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Menulis komentar pada halaman barang, misalnya informasi pernah melihat barang tersebut. Pelapor mendapat notifikasi. | `POST /comments` |
| Read | Utas komentar berurutan pada halaman barang, dengan penanda "(edited)" untuk komentar yang telah diubah. | `GET /items/{id}` |
| Update | Penulis mengubah komentarnya sendiri. | `POST /comments/{id}/edit` |
| Delete | Penulis menghapus komentarnya sendiri. Admin dapat menghapus komentar siapa pun untuk moderasi. | `POST /comments/{id}/delete` |

## 3.6 Fitur 6: Daftar Pantau (Watchlist)

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Menyimpan sebuah laporan ke daftar pantau. Pengguna akan menerima notifikasi bila status laporan berubah. | `POST /watchlist/add` |
| Read | Halaman daftar pantau berisi kartu tag barang yang disimpan beserta catatannya. | `GET /watchlist` |
| Update | Menambah atau mengubah catatan pribadi, maksimal 140 karakter. | `POST /watchlist/note` |
| Delete | Menghapus laporan dari daftar pantau. | `POST /watchlist/remove` |

## 3.7 Fitur 7: Kategori (Admin)

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Menambah kategori dengan nama unik dan ikon. | `POST /admin/categories` |
| Read | Tabel kategori beserta jumlah laporan yang memakainya. | `GET /admin/categories` |
| Update | Mengubah nama dan ikon kategori. | `GET/POST /admin/categories/{id}/edit` |
| Delete | Menghapus kategori. Penghapusan diblokir dengan pesan yang jelas bila masih ada laporan yang memakai kategori tersebut. | `POST /admin/categories/{id}/delete` |

## 3.8 Fitur 8: Lokasi Kampus (Admin)

| Operasi | Deskripsi | Rute |
|---|---|---|
| Create | Menambah lokasi dengan nama, zona, dan penanda pos SKK. | `POST /admin/locations` |
| Read | Tabel lokasi beserta jumlah laporan dan penanda pos SKK. | `GET /admin/locations` |
| Update | Mengubah nama, zona, dan status pos SKK. | `GET/POST /admin/locations/{id}/edit` |
| Delete | Menghapus lokasi. Diblokir bila masih ada laporan yang tercatat di lokasi tersebut. | `POST /admin/locations/{id}/delete` |

## 3.9 Fitur pendukung

| Fitur | Operasi | Rute |
|---|---|---|
| **Notifikasi** | Read: daftar notifikasi dan jumlah belum dibaca pada navigasi. Update: tandai satu atau semua sudah dibaca. Delete: hapus satu atau bersihkan semua. Create dilakukan otomatis oleh `NotificationService` (klaim baru, klaim disetujui atau ditolak, komentar baru, perubahan status barang yang dipantau). | `GET /notifications`, `POST /notifications/read-all`, `/clear`, `/{id}/read`, `/{id}/delete` |
| **Manajemen pengguna (admin)** | Read: daftar pengguna dengan pencarian. Update: mengubah peran mahasiswa atau admin. Delete: menghapus akun pengguna lain. Admin tidak dapat menurunkan peran atau menghapus akunnya sendiri dari halaman ini. | `GET /admin/users`, `POST /admin/users/{id}/role`, `/{id}/delete` |
| **Buku besar meja (admin)** | Read: ringkasan jumlah laporan per status, jumlah laporan terbuka per jenis, jumlah klaim yang menunggu keputusan, dan 20 laporan terbaru beserta stempel statusnya. | `GET /admin` |

## 3.10 Ringkasan

| No | Fitur | C | R | U | D |
|---|---|:-:|:-:|:-:|:-:|
| 1 | Akun dan profil | ✓ | ✓ | ✓ | ✓ |
| 2 | Laporan barang | ✓ | ✓ | ✓ | ✓ |
| 3 | Foto barang | ✓ | ✓ | ✓ | ✓ |
| 4 | Klaim dan serah terima | ✓ | ✓ | ✓ | ✓ (pembatalan) |
| 5 | Komentar | ✓ | ✓ | ✓ | ✓ |
| 6 | Daftar pantau | ✓ | ✓ | ✓ | ✓ |
| 7 | Kategori | ✓ | ✓ | ✓ | ✓ |
| 8 | Lokasi kampus | ✓ | ✓ | ✓ | ✓ |
| + | Notifikasi | otomatis | ✓ | ✓ | ✓ |
| + | Manajemen pengguna | melalui pendaftaran | ✓ | ✓ | ✓ |
| + | Buku besar meja | – | ✓ | – | – |
