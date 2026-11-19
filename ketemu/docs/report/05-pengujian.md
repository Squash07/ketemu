# 5. Hasil Pengujian Fungsional

## 5.1 Metode pengujian

Pengujian fungsional dilakukan secara *end-to-end* dengan skrip otomatis `tests/smoke-test.js`. Skrip ini menjalankan peramban Chromium tanpa tampilan (Playwright) dan berperan sebagai beberapa pengguna sekaligus, yaitu tamu, mahasiswa baru, Ayu, Bima, Rani, dan admin, untuk mengeklik tombol, mengisi formulir, dan mengirim permintaan HTTP layaknya pengguna sungguhan. Setiap kasus uji mencatat langkah, hasil yang diharapkan, hasil aktual yang dibaca langsung dari halaman (teks *toast*, pesan kesalahan, kode status HTTP, atau isi basis data yang tampil), serta status lulus atau gagal.

Cakupan pengujian meliputi seluruh operasi CRUD dari delapan fitur utama, fitur pendukung (notifikasi dan manajemen pengguna), serta pengujian keamanan. Kasus negatif sengaja dimasukkan sesuai ketentuan tugas: masukan tidak valid, mengubah atau menghapus laporan orang lain, mengklaim barang sendiri, menghapus kategori yang masih dipakai, dan membuka `/admin` sebagai mahasiswa.

**Lingkungan pengujian yang tercatat di bawah ini adalah lingkungan lokal**, bukan situs *live*:

| Komponen | Nilai |
|---|---|
| Waktu eksekusi | 19 November 2026, 09.34 WIB |
| Aplikasi | Tomcat 10.1 (JRE 17) dalam kontainer Docker, berkas `ROOT.war` hasil `mvn package` |
| Basis data | PostgreSQL 16 dalam kontainer Docker, dibuat ulang dari kosong lalu diisi `seed.sql` |
| Peramban uji | Chromium *headless* melalui Playwright |
| Hasil | **74 dari 74 kasus lulus** |

Kolom **"Hasil live"** sengaja dikosongkan (`[isi]`). Setelah aplikasi di-*deploy* ke Render dan Neon, skrip yang sama dapat dijalankan terhadap situs *live* dengan perintah berikut, lalu kolom tersebut diisi dari berkas `tests/test-results.md` yang dihasilkan:

```bash
npm install playwright
npx playwright install chromium
BASE_URL=https://<nama-layanan>.onrender.com DEMO_PASSWORD=<kata sandi demo> node tests/smoke-test.js
```

Catatan: skrip ini membuat dan menghapus data uji (akun baru, laporan, klaim, kategori, lokasi). Jalankan pada basis data yang baru di-*seed* agar kode tag dan nomor tiket sesuai dengan yang tercantum di tabel.

## 5.2 Ringkasan per fitur

| Fitur | Jumlah kasus | Kasus positif | Kasus negatif | Lulus |
|---|---:|---:|---:|---:|
| Akun | 14 | 6 | 8 | 14 |
| Laporan barang | 10 | 7 | 3 | 10 |
| Foto | 8 | 5 | 3 | 8 |
| Klaim | 12 | 8 | 4 | 12 |
| Komentar | 5 | 3 | 2 | 5 |
| Daftar pantau | 5 | 4 | 1 | 5 |
| Kategori | 7 | 4 | 3 | 7 |
| Lokasi | 5 | 4 | 1 | 5 |
| Notifikasi | 2 | 2 | 0 | 2 |
| Pengguna (admin) | 1 | 1 | 0 | 1 |
| Keamanan | 5 | 0 | 5 | 5 |
| **Total** | **74** | **44** | **30** | **74** |

Kasus negatif (masukan tidak valid, pelanggaran otorisasi, pelanggaran aturan bisnis, dan serangan) mencakup 30 dari 74 kasus atau sekitar 40 persen. Proporsi ini disengaja karena kualitas aplikasi CRUD tidak hanya ditentukan oleh keberhasilan alur normal, tetapi juga oleh kemampuannya menolak permintaan yang salah dengan aman dan dengan pesan yang dapat dipahami pengguna.

## 5.3 Tabel pengujian lengkap

Teks pada kolom "Hasil aktual" disalin apa adanya dari halaman aplikasi (antarmuka Ketemu berbahasa Inggris).

| No | Fitur | Operasi | Langkah | Hasil yang diharapkan | Hasil aktual (lokal) | Status | Hasil live |
|---|---|---|---|---|---|---|---|
| 1 | Akun | Create (input tidak valid) | Mengirim formulir pendaftaran kosong | Pesan kesalahan di bawah setiap kolom wajib, tidak ada data tersimpan | 5 inline errors, e.g. "Enter your NRP so the finder knows who is claiming." | Lulus | [isi] |
| 2 | Akun | Create (input tidak valid) | Mendaftar dengan dua kata sandi yang berbeda | Pesan kesalahan di bawah kolom "Password again" | The two passwords are different. Type the same one twice. | Lulus | [isi] |
| 3 | Akun | Create (input tidak valid) | Mendaftar dengan email yang sudah terdaftar (rani@ketemu.test) | Ditolak: akun sudah ada | There is already an account with this email. Sign in instead. | Lulus | [isi] |
| 4 | Akun | Create | Mendaftar dengan NRP, nama, email, dan kata sandi yang valid | Akun dibuat, langsung masuk, muncul toast "Account created" | Account created. You can post an item or file a claim now. | Lulus | [isi] |
| 5 | Akun | Logout | Menekan Sign out | Sesi berakhir, toast "Signed out." | Signed out. | Lulus | [isi] |
| 6 | Akun | Login (input tidak valid) | Masuk dengan kata sandi yang salah | Pesan kesalahan di bawah kolom kata sandi, tetap belum masuk | That email and password do not match. Check for typos, or create an account if you are new. | Lulus | [isi] |
| 7 | Akun | Login + session fixation | Masuk dengan kata sandi benar lalu membandingkan cookie sesi sebelum dan sesudah | Berhasil masuk dan nilai JSESSIONID berubah (perlindungan session fixation) | cookie changed: true | Lulus | [isi] |
| 8 | Akun | Read | Membuka /profile | Profil menampilkan nama, NRP, email, dan laporan pengguna | heading "Test Student" | Lulus | [isi] |
| 9 | Akun | Update (input tidak valid) | Mengubah profil dengan nomor telepon "abc" | Pesan kesalahan di bawah kolom Phone | Use digits only, for example 081234567890. | Lulus | [isi] |
| 10 | Akun | Update | Mengubah nama dan nomor telepon | Toast "Profile saved.", nama baru tampil | Profile saved. / Test Student Edited | Lulus | [isi] |
| 11 | Akun | Update (input tidak valid) | Mengganti kata sandi dengan kata sandi lama yang salah | Pesan kesalahan di bawah kolom Current password | That is not your current password. Try again. | Lulus | [isi] |
| 12 | Akun | Update | Mengganti kata sandi dengan kata sandi lama yang benar | Toast "Password changed." | Password changed. | Lulus | [isi] |
| 13 | Laporan barang | Create (input tidak valid) | Mengirim formulir laporan dengan judul 2 huruf tanpa kategori dan lokasi | Pesan kesalahan per kolom, judul yang diketik tetap ada | 3 errors, title kept "ab" | Lulus | [isi] |
| 14 | Laporan barang | Create | Membuat laporan barang hilang dengan judul, kategori, lokasi, tanggal, dan deskripsi | Diarahkan ke halaman barang baru dengan toast berisi kode tag | Posted. Your tag K-0412 is on the board. | Lulus | [isi] |
| 15 | Laporan barang | Read | Mencari judul laporan baru di papan | Tag baru menjadi satu-satunya hasil | 1 tag(s) found | Lulus | [isi] |
| 16 | Laporan barang | Read | Memfilter papan dengan Lost | Hanya tag dengan stempel Lost (atau status lanjutannya) | 6 tags, stamps: Lost, Returned | Lulus | [isi] |
| 17 | Laporan barang | Read (hasil kosong) | Mencari kata yang tidak pernah dilaporkan | Tampilan kosong dengan tombol "Post a lost item" | Nothing matches ‘zzqqxx’. | Lulus | [isi] |
| 18 | Laporan barang | Update | Mengubah judul laporan milik sendiri | Toast "Changes saved.", judul baru tampil | Changes saved. | Lulus | [isi] |
| 19 | Laporan barang | Update (otorisasi) | Membuka /items/{id}/edit milik orang lain | Halaman 403 "That is behind the desk." | HTTP 403 | Lulus | [isi] |
| 20 | Laporan barang | Delete (otorisasi) | Mengirim POST /items/407/delete (milik orang lain) dengan token CSRF valid | Ditolak 403, laporan tetap ada | HTTP 403, item still there: true | Lulus | [isi] |
| 21 | Laporan barang | Update (status) | Menutup laporan sendiri lalu membukanya kembali | Toast "Report closed..." lalu "Report reopened..." | Report closed. It no longer shows on the board. / Report reopened. It is back on the board. | Lulus | [isi] |
| 22 | Foto | Create (input tidak valid) | Mengunggah berkas teks yang diganti namanya menjadi .png | Ditolak: bukan gambar JPG, PNG, atau WebP | notes.png is not a JPG, PNG or WebP image. | Lulus | [isi] |
| 23 | Foto | Create (input tidak valid) | Mengunggah gambar berukuran 1,2 MB | Ditolak: lebih dari 1 MB, tidak ada yang tersimpan | One of the photos is larger than 1 MB, so nothing was saved. Resize it and try again. | Lulus | [isi] |
| 24 | Foto | Create | Mengunggah 3 foto PNG valid pada halaman ubah | Tersimpan, galeri menampilkan 3 thumbnail | 3 thumbnails | Lulus | [isi] |
| 25 | Foto | Create (input tidak valid) | Mencoba menambah foto keempat | Ditolak: "This item already has 3 photos" | This item already has 3 photos. Remove one before adding another. | Lulus | [isi] |
| 26 | Foto | Read | GET /photo/{id}, lalu diulang dengan If-None-Match | 200 dengan tipe gambar, ETag, dan Cache-Control, lalu 304 | 200 image/png, ETag "p1-1795080879", public, max-age=86400, revalidate 304 | Lulus | [isi] |
| 27 | Foto | Update | Menjadikan foto kedua sebagai sampul | Toast "Cover photo changed." | Cover photo changed. | Lulus | [isi] |
| 28 | Foto | Update | Mengganti foto pertama dengan berkas lain | Toast "Photo replaced." | Photo replaced. | Lulus | [isi] |
| 29 | Foto | Delete | Menghapus satu foto (konfirmasi pada dialog) | Toast "Photo removed.", tersisa 2 foto | Photo removed. 2 left | Lulus | [isi] |
| 30 | Klaim | Create (input tidak valid) | Mengajukan klaim pada K-0409 dengan bukti "short" | Pesan kesalahan meminta bukti lebih rinci | Add a little more detail so the finder can be sure it is yours. | Lulus | [isi] |
| 31 | Klaim | Create | Mengajukan klaim pada K-0409 (temuan, terbuka, bukan milik sendiri) dengan bukti dan pos serah terima | Halaman tiket, toast "Claim filed. Your ticket is K-0409-01." | Claim filed. Your ticket is K-0409-01. | Lulus | [isi] |
| 32 | Klaim | Create (aturan bisnis) | Bima mengirim klaim pada barangnya sendiri (K-0403) | Ditolak 403 | HTTP 403 | Lulus | [isi] |
| 33 | Klaim | Create (aturan bisnis) | Mengirim klaim pada barang hilang (K-0405) | Ditolak dengan pesan, tidak ada klaim dibuat | HTTP 302 redirect back with message "Only found items can be claimed" | Lulus | [isi] |
| 34 | Klaim | Read | Membuka My claims | Potongan tiket klaim milik Bima | 2 tickets | Lulus | [isi] |
| 35 | Klaim | Read (otorisasi) | Ayu membuka tiket klaim milik Bima | Ditolak 403 | HTTP 403 | Lulus | [isi] |
| 36 | Klaim | Update (pengaju) | Ayu mengubah bukti pada klaim pending K-0403-01 | Toast "Claim updated." | Claim updated. | Lulus | [isi] |
| 37 | Klaim | Create | Ayu mengajukan klaim kedua pada K-0409 | Tiket K-0409-02 dibuat | Claim filed. Your ticket is K-0409-02. | Lulus | [isi] |
| 38 | Klaim | Update (penemu menolak) | Bima (penemu K-0403) menolak klaim Ayu | Toast "Claim rejected.", Ayu menerima notifikasi | Claim rejected. | Lulus | [isi] |
| 39 | Klaim | Update (penemu menyetujui) | Rani (penemu K-0409) menyetujui klaim Bima | Barang menjadi Claimed, klaim pending Ayu otomatis ditolak | claims: Approved, Not approved; tag: Claimed | Lulus | [isi] |
| 40 | Klaim | Update (tandai dikembalikan) | Rani menekan Mark returned | Stempel Returned dengan animasi tekan, toast "Marked returned." | Marked returned. stamp: Returned, press animation: true | Lulus | [isi] |
| 41 | Klaim | Delete (pembatalan) | Ayu mengajukan klaim pada K-0408 lalu membatalkannya | Toast "Claim cancelled.", status Cancelled | Claim cancelled. | Lulus | [isi] |
| 42 | Komentar | Create (input tidak valid) | Mengirim komentar kosong | Pesan kesalahan "Write something first" | Write something first, like where and when you saw it. | Lulus | [isi] |
| 43 | Komentar | Create | Menulis komentar penampakan pada K-0402 | Toast "Comment posted.", komentar tampil di akhir utas | Comment posted. | Lulus | [isi] |
| 44 | Komentar | Update | Mengubah komentar sendiri | Toast "Comment saved.", teks berubah dan diberi penanda (edited) | Comment saved. | Lulus | [isi] |
| 45 | Komentar | Update (otorisasi) | Ayu mengirim perubahan untuk komentar milik Bima (id 4) | Ditolak 403 | HTTP 403 | Lulus | [isi] |
| 46 | Komentar | Delete | Menghapus komentar sendiri (konfirmasi pada dialog) | Toast "Comment deleted." | Comment deleted. | Lulus | [isi] |
| 47 | Daftar pantau | Create | Menyimpan K-0410 ke daftar pantau | Toast "Saved to watchlist..." | Saved to watchlist. We'll tell you when K-0410 changes. | Lulus | [isi] |
| 48 | Daftar pantau | Read | Membuka /watchlist | K-0410 tercantum beserta tagnya | 2 saved items, K-0410 present | Lulus | [isi] |
| 49 | Daftar pantau | Update (input tidak valid) | Menyimpan catatan lebih dari 140 karakter (dikirim langsung tanpa formulir) | Ditolak: "Keep the note under 140 characters" | Keep the note under 140 characters. It was not saved. | Lulus | [isi] |
| 50 | Daftar pantau | Update | Menambah catatan pribadi pada K-0410 | Toast "Note saved.", catatan tampil | Note saved. | Lulus | [isi] |
| 51 | Daftar pantau | Delete | Menghapus K-0410 dari daftar pantau | Toast "Removed from watchlist." | Removed from watchlist. | Lulus | [isi] |
| 52 | Kategori | Read (otorisasi) | Mahasiswa membuka /admin/categories | Halaman 403 | HTTP 403 | Lulus | [isi] |
| 53 | Kategori | Read | Admin membuka /admin/categories | Tabel 8 kategori contoh beserta jumlah laporan | 8 rows | Lulus | [isi] |
| 54 | Kategori | Create (input tidak valid) | Menambah kategori bernama "Electronics" (sudah ada) | Pesan kesalahan nama kategori sudah ada | There is already a category called Electronics. | Lulus | [isi] |
| 55 | Kategori | Create | Menambah kategori "Sports gear" dengan ikon tas | Toast "Category added." | Category added. | Lulus | [isi] |
| 56 | Kategori | Update | Mengubah nama dan ikon kategori tersebut | Toast "Category saved." | Category saved. | Lulus | [isi] |
| 57 | Kategori | Delete (aturan bisnis) | Menghapus "Electronics" yang masih dipakai laporan | Diblokir dengan pesan untuk memindahkan laporan terlebih dahulu | 3 items use Electronics. Move them to another category first. | Lulus | [isi] |
| 58 | Kategori | Delete | Menghapus kategori baru yang tidak dipakai | Toast "Category deleted." | Category deleted. | Lulus | [isi] |
| 59 | Lokasi | Read | Admin membuka /admin/locations | Tabel 10 lokasi contoh, 3 di antaranya pos SKK | 10 rows, 3 SKK posts | Lulus | [isi] |
| 60 | Lokasi | Create | Menambah "Gedung Pascasarjana" sebagai pos SKK | Toast "Location added." | Location added. | Lulus | [isi] |
| 61 | Lokasi | Update | Mengubah lokasi tersebut dan mematikan penanda pos SKK | Toast "Location saved.", kolom pos SKK menjadi No | Location saved. SKK post: No | Lulus | [isi] |
| 62 | Lokasi | Delete (aturan bisnis) | Menghapus "Kantin Pusat" yang masih dipakai laporan | Diblokir dengan pesan untuk memindahkan laporan terlebih dahulu | 3 items were reported at Kantin Pusat. Move them to another location first. | Lulus | [isi] |
| 63 | Lokasi | Delete | Menghapus lokasi baru yang tidak dipakai | Toast "Location deleted." | Location deleted. | Lulus | [isi] |
| 64 | Notifikasi | Read | Bima membuka /notifications setelah klaimnya disetujui | Notifikasi terkait klaim dengan penanda belum dibaca | 4 notifications, newest: "Unread: K-0409 Grey umbrella, wooden handle was marked returned. Glad it found its way back." | Lulus | [isi] |
| 65 | Notifikasi | Update / Delete | Menandai semua sudah dibaca, lalu membersihkan semua | Toast "Marked all as read." dan "Notifications cleared." | Marked all as read. / Notifications cleared. | Lulus | [isi] |
| 66 | Pengguna (admin) | Update | Admin mengubah Ayu menjadi admin lalu kembali menjadi mahasiswa | Dua toast "Role changed..." | Role changed. Ayu Lestari is now a desk admin. / Role changed. Ayu Lestari is now a student. | Lulus | [isi] |
| 67 | Keamanan | CSRF | POST /profile/edit tanpa token _csrf | Ditolak 403 "This form expired" | HTTP 403 | Lulus | [isi] |
| 68 | Keamanan | XSS | Membuat laporan berjudul &lt;script&gt;alert(1)&lt;/script&gt; | Ditampilkan sebagai teks, tidak ada elemen script yang dijalankan | title rendered as text: "&lt;script&gt;alert(1)&lt;/script&gt; xss test", alert fired: false | Lulus | [isi] |
| 69 | Keamanan | SQL injection | Mencari ' OR 1=1 -- di papan | Diperlakukan sebagai teks biasa: hasil kosong, tidak ada error | HTTP 200, 0 tags | Lulus | [isi] |
| 70 | Keamanan | Autentikasi | Tamu membuka /claims | Diarahkan ke /login?next=/claims | /login?next=%2Fclaims | Lulus | [isi] |
| 71 | Keamanan | Otorisasi | Mahasiswa membuka /admin | Halaman 403 "That is behind the desk." | HTTP 403 | Lulus | [isi] |
| 72 | Laporan barang | Delete | Menghapus laporan sendiri (konfirmasi pada dialog) | Toast "Report deleted. K-xxxx is off the board." | Report deleted. K-0412 is off the board. | Lulus | [isi] |
| 73 | Akun | Delete (input tidak valid) | Menghapus akun dengan kata sandi yang salah | Ditolak: "That is not your password. Your account was not deleted." | That is not your password. Your account was not deleted. | Lulus | [isi] |
| 74 | Akun | Delete | Menghapus akun dengan kata sandi yang benar | Akun dan laporannya terhapus, toast "Account deleted..." | Account deleted. Your items, claims and comments were removed with it. | Lulus | [isi] |
## 5.4 Kasus negatif wajib sesuai ketentuan tugas

| Ketentuan | Nomor uji | Perilaku sistem |
|---|---|---|
| Masukan tidak valid | 1, 2, 3, 6, 9, 11, 13, 22, 23, 25, 30, 42, 49, 54, 73 | Server menolak dan menampilkan pesan di bawah kolom terkait, data yang diketik dipertahankan, tidak ada data yang tersimpan |
| Mengubah atau menghapus laporan orang lain | 19, 20, 45 | Server mengembalikan 403. Uji 20 memastikan laporan benar-benar masih ada setelah percobaan penghapusan dengan token CSRF yang sah |
| Mengklaim barang sendiri | 32 | Ditolak dengan 403 oleh `ClaimService.whyCannotClaim` |
| Mengklaim barang hilang (bukan temuan) | 33 | Ditolak dengan pesan "Only found items can be claimed", tidak ada klaim dibuat |
| Menghapus kategori atau lokasi yang masih dipakai | 57, 62 | Diblokir dengan pesan yang menyebutkan jumlah laporan dan cara menyelesaikannya |
| Membuka `/admin` sebagai mahasiswa | 52, 71 | Halaman 403 "That is behind the desk." dari `AdminFilter` |
| Membuka tiket klaim orang lain | 35 | Ditolak dengan 403 |
| Mengakses halaman terlindungi tanpa masuk | 70 | Diarahkan ke halaman masuk dengan parameter `next` agar kembali ke tujuan setelah masuk |

## 5.5 Kegagalan yang ditemukan selama pengembangan

Tidak semua pengujian langsung lulus. Berikut kegagalan nyata yang ditemukan selama pengembangan dan pengujian, beserta penyebab dan perbaikannya. Catatan ini disertakan agar evaluasi mencerminkan proses yang sebenarnya.

| No | Gejala | Penyebab | Perbaikan |
|---|---|---|---|
| 1 | Halaman papan utama menghasilkan *error* 500 `PropertyNotFoundException` saat filter dipakai | Expression Language (EL) pada JSP tidak dapat membaca *field* publik suatu objek, hanya *getter* | Menambahkan metode *getter* pada kelas `ItemDao.Filter` |
| 2 | Respons foto memiliki tipe `image/png;charset=UTF-8` | `EncodingFilter` menetapkan *charset* untuk semua respons, termasuk data biner | `PhotoServlet` menghapus *charset* sebelum mengirim isi gambar |
| 3 | Kompilasi gagal pada model `Category` dan `Location` | Nama paket `id.ketemu` bentrok dengan *field* `id` saat memanggil utilitas dengan nama lengkap | Memakai `import` biasa untuk kelas `Fmt` |
| 4 | Pada lebar 390 px, halaman admin dapat digulir ke samping | *Caption* tabel yang disembunyikan secara visual keluar dari kotak tabel | Menambahkan `position: relative` pada pembungkus tabel |
| 5 | *Toast* tampil di tengah dan tidak selebar konten | *Margin* otomatis pada tata letak *flex* | Menetapkan lebar penuh pada wadah *toast* |
| 6 | Tombol yang seharusnya tersembunyi tetap terlihat | Gaya `.btn` menimpa atribut HTML `hidden` | Menambahkan aturan `[hidden] { display: none !important; }` |
| 7 | Uji 49 (catatan daftar pantau lebih dari 140 karakter) gagal pada putaran pertama, 73 dari 74 lulus | Kesalahan pada skrip uji, bukan pada aplikasi: permintaan langsung mengikuti pengalihan sehingga pesan *flash* sudah terbaca sebelum halaman dibuka | Permintaan dikirim tanpa mengikuti pengalihan (`maxRedirects: 0`). Pada putaran berikutnya 74 dari 74 lulus |
