# 4. Screenshot dan Penjelasan Halaman

Seluruh tangkapan layar diambil secara otomatis dengan Playwright (Chromium *headless*) pada lebar 1440 px, memakai data contoh dari `seed.sql`, dan masuk sebagai peran yang sesuai. Tangkapan layar responsif diambil pada lebar 768 px (tablet) dan 390 px (ponsel). Berkas gambar tersimpan di folder `docs/screenshots/`.

Akun yang dipakai: **Bima** (`bima@ketemu.test`, penemu barang), **Ayu** (`ayu@ketemu.test`, pengaju klaim), **Rani** (`rani@ketemu.test`), dan **Hadi Prasetyo** (`admin@ketemu.test`, petugas meja SKK).

---

## 4.1 Papan utama (Read laporan barang)

![Papan utama](../screenshots/01-home-board.png)

Papan utama adalah halaman pertama yang dilihat pengunjung. Bagian atasnya berisi judul, dua tombol aksi ("I lost something" dan "I found something"), jumlah barang yang kembali ke pemiliknya bulan ini, serta tag manila berayun berisi kolom pencarian. Di bawahnya, setiap laporan ditampilkan sebagai tag manila yang tergantung di papan, lengkap dengan kode `K-NNNN`, ikon kategori, lokasi, tanggal, dan stempel status (Lost, Found, Claimed, Returned). Pengunjung dapat memfilter berdasarkan jenis laporan, kategori, dan lokasi, serta mencari dengan kata kunci; hasilnya dipaginasi 12 tag per halaman. Halaman ini merupakan operasi *Read* dari fitur Laporan Barang dan dapat diakses tanpa masuk.

## 4.2 Hasil pencarian kosong

![Pencarian tanpa hasil](../screenshots/27-board-search-empty.png)

Ketika kata kunci tidak cocok dengan laporan mana pun, papan menampilkan keadaan kosong yang menjelaskan apa yang terjadi dan menawarkan tindakan lanjutan, yaitu membuat laporan barang hilang. Pencarian memakai parameter terikat sehingga masukan seperti `' OR 1=1 --` diperlakukan sebagai teks biasa.

## 4.3 Halaman detail barang

![Detail barang](../screenshots/02-item-detail.png)

Halaman detail menampilkan tag barang dalam ukuran besar (berisi foto sampul atau ikon kategori bila belum ada foto) dengan stempel statusnya, galeri foto bila ada, deskripsi yang ditulis dengan gaya tulisan tangan, lokasi kejadian, pos penitipan, serta profil ringkas pelapor. Di bawahnya terdapat utas komentar dan, untuk barang temuan, formulir klaim. Tombol yang muncul menyesuaikan peran: pelapor melihat tombol ubah, tutup, dan hapus, sedangkan pengguna lain melihat tombol simpan ke daftar pantau dan ajukan klaim.

## 4.4 Klaim masuk pada barang milik penemu

![Klaim masuk](../screenshots/03-item-incoming-claims.png)

Ketika penemu (Bima) membuka barang temuannya, halaman menampilkan daftar klaim yang masuk beserta bukti kepemilikan dan pos serah terima yang diusulkan. Penemu dapat menyetujui atau menolak setiap klaim; ini adalah operasi *Update* dari fitur Klaim. Menyetujui satu klaim otomatis menolak klaim lain yang masih menunggu.

## 4.5 Formulir laporan baru (Create laporan barang)

![Formulir laporan](../screenshots/04-report-form.png)

Formulir ini dipakai untuk membuat laporan barang hilang maupun temuan. Pengguna memilih jenis laporan, mengisi judul, kategori, lokasi, detail tempat, tanggal, dan deskripsi, lalu dapat mengunggah hingga tiga foto. Gaya kolom isian menyerupai tulisan tangan dengan pena biru di atas kertas, sesuai konsep visual "meja barang temuan".

## 4.6 Validasi sisi server pada formulir laporan

![Kesalahan validasi laporan](../screenshots/05-report-validation-error.png)

Bila formulir dikirim dengan data tidak lengkap, server menolak dan menampilkan pesan kesalahan tepat di bawah kolom yang bermasalah, sementara isian yang sudah diketik tetap dipertahankan. Validasi dilakukan di server oleh kelas `Validator`, sehingga tidak dapat dilewati dengan mematikan JavaScript atau mengirim permintaan langsung.

## 4.7 Pratinjau foto sebelum unggah

![Pratinjau foto](../screenshots/06-photo-preview.png)

Setelah memilih berkas, pengguna langsung melihat pratinjau foto yang akan diunggah. Pratinjau hanya bantuan di sisi peramban; pemeriksaan sebenarnya (jenis berkas dari isi, ukuran maksimal 1 MB, dan jumlah maksimal tiga foto) tetap dilakukan di server oleh kelas `Uploads`.

## 4.8 Laporan dengan foto setelah diterbitkan

![Laporan dengan foto](../screenshots/07-item-with-photos-posted.png)

Setelah laporan disimpan, pengguna diarahkan ke halaman barang dengan pesan "Posted. Your tag K-xxxx is on the board." Foto pertama menjadi foto sampul yang juga tampil pada kartu di papan utama. Foto dilayani melalui `/photo/{id}` dengan *cache* peramban.

## 4.9 Pengelolaan foto pada halaman ubah laporan (Update dan Delete foto)

![Kelola foto](../screenshots/07b-edit-item-photos.png)

Pada halaman ubah laporan, setiap foto memiliki tombol untuk menjadikannya sampul, mengganti berkasnya, atau menghapusnya. Penghapusan meminta konfirmasi melalui dialog terlebih dahulu. Halaman ini mencakup operasi *Update* dan *Delete* dari fitur Foto Barang sekaligus *Update* dari fitur Laporan Barang.

## 4.10 Daftar klaim saya (Read klaim)

![Daftar klaim](../screenshots/08-my-claims.png)

Halaman "My claims" menampilkan setiap klaim pengguna sebagai potongan tiket dengan nomor klaim, nama barang, dan status (Pending, Approved, Not approved, Cancelled). Setiap potongan tiket dapat dibuka untuk melihat tiket lengkap.

## 4.11 Tiket klaim

![Tiket klaim](../screenshots/09-claim-ticket.png)

Tiket klaim menyerupai tiket penitipan barang fisik: nomor tiket seperti `K-0409-01`, barang yang diklaim, bukti kepemilikan, pos serah terima, dan status keputusan. Pengaju dapat mengubah bukti atau membatalkan klaim selama klaim masih menunggu. Tiket hanya dapat dibuka oleh pengaju dan penemu; pengguna lain menerima halaman 403.

## 4.12 Mengubah bukti klaim (Update klaim)

![Ubah klaim](../screenshots/16-edit-claim.png)

Selama klaim berstatus *pending*, pengaju dapat memperbaiki bukti kepemilikan dan mengganti pos serah terima. Validasi yang sama dengan saat pengajuan diterapkan kembali, yaitu bukti minimal 15 karakter.

## 4.13 Barang ditandai sudah dikembalikan

![Barang dikembalikan](../screenshots/28-marked-returned.png)

Setelah serah terima, penemu menekan "Mark returned". Stempel "Returned" berwarna grafit dicapkan pada tag dengan animasi tekan singkat (350 ms), satu-satunya animasi selain ayunan tag di halaman utama. Pengaju dan pemantau barang menerima notifikasi.

## 4.14 Komentar dan penyuntingan komentar

![Komentar](../screenshots/17-comments-edit.png)

Utas komentar pada halaman barang dipakai untuk berbagi informasi penampakan. Penulis dapat menyunting komentarnya di tempat; komentar yang sudah diubah diberi penanda "(edited)". Komentar dapat dihapus oleh penulisnya atau oleh admin. Halaman ini mencakup seluruh operasi CRUD fitur Komentar.

## 4.15 Daftar pantau

![Daftar pantau](../screenshots/15-watchlist.png)

Daftar pantau berisi barang yang disimpan pengguna, masing-masing dengan kolom catatan pribadi maksimal 140 karakter dan tombol hapus. Pengguna menerima notifikasi setiap kali status barang yang dipantau berubah.

## 4.16 Notifikasi

![Notifikasi](../screenshots/11-notifications.png)

Halaman notifikasi menampilkan pemberitahuan terbaru dengan penanda belum dibaca. Jumlah notifikasi belum dibaca juga tampil di navigasi pada setiap halaman. Pengguna dapat menandai satu atau semua notifikasi sudah dibaca, menghapus satu notifikasi, atau membersihkan semuanya.

## 4.17 Profil

![Profil](../screenshots/10-profile.png)

Halaman profil menampilkan nama, NRP, email, telepon, dan daftar laporan milik pengguna. Dari sini pengguna dapat mengubah data diri, mengganti kata sandi, atau menghapus akun dengan konfirmasi kata sandi.

## 4.18 Masuk

![Masuk](../screenshots/12-login.png)

Halaman masuk meminta email dan kata sandi. Pesan kesalahan tidak membedakan antara email yang tidak terdaftar dan kata sandi yang salah, sehingga tidak membocorkan keberadaan akun. Setelah berhasil masuk, ID sesi diganti untuk mencegah *session fixation*.

## 4.19 Pendaftaran dan validasinya (Create akun)

![Pendaftaran](../screenshots/13-register.png)

![Validasi pendaftaran](../screenshots/14-register-validation-error.png)

Formulir pendaftaran meminta NRP, nama lengkap, email, telepon opsional, dan kata sandi yang diketik dua kali. Gambar kedua menunjukkan validasi sisi server: setiap kolom wajib yang kosong mendapat pesan kesalahan di bawahnya dengan bahasa yang menjelaskan cara memperbaikinya.

## 4.20 Buku besar meja (admin)

![Buku besar meja](../screenshots/18-admin-ledger.png)

Halaman ini adalah ringkasan kerja petugas SKK: jumlah laporan per status, laporan terbuka per jenis, klaim yang menunggu keputusan, dan daftar laporan terbaru dengan stempel statusnya. Hanya admin yang dapat membukanya; mahasiswa menerima halaman 403.

## 4.21 Kategori (admin)

![Daftar kategori](../screenshots/19-admin-categories.png)

![Ubah kategori](../screenshots/20-admin-category-edit.png)

Tabel kategori menampilkan nama, ikon, dan jumlah laporan yang memakai setiap kategori, beserta formulir untuk menambah kategori baru. Halaman ubah memungkinkan admin mengganti nama dan ikon. Nama kategori yang sudah ada akan ditolak dengan pesan yang jelas.

## 4.22 Konfirmasi hapus dan penghapusan yang diblokir

![Dialog konfirmasi](../screenshots/21-confirm-dialog.png)

![Hapus diblokir](../screenshots/22-delete-blocked-message.png)

Setiap tindakan hapus memunculkan dialog konfirmasi. Ketika admin mencoba menghapus kategori yang masih dipakai, sistem menolak dengan pesan "3 items use Electronics. Move them to another category first." Aturan ini dijaga di aplikasi dan juga oleh `ON DELETE RESTRICT` di basis data.

## 4.23 Lokasi kampus (admin)

![Lokasi](../screenshots/23-admin-locations.png)

Tabel lokasi menampilkan nama, zona, penanda pos SKK, dan jumlah laporan. Admin dapat menambah, mengubah, dan menghapus lokasi dengan aturan penghapusan yang sama seperti kategori.

## 4.24 Manajemen pengguna (admin)

![Pengguna](../screenshots/24-admin-users.png)

Admin dapat mencari pengguna, mengubah peran antara mahasiswa dan admin, serta menghapus akun pengguna lain. Admin tidak dapat menurunkan peran atau menghapus akunnya sendiri dari halaman ini agar sistem tidak kehilangan pengelola.

## 4.25 Halaman kesalahan

![403](../screenshots/25-error-403.png)

![404](../screenshots/26-error-404.png)

Halaman 403 ("That is behind the desk.") muncul ketika pengguna membuka halaman yang bukan haknya, misalnya mahasiswa membuka `/admin` atau mengubah laporan orang lain. Halaman 404 muncul untuk alamat atau kode tag yang tidak ada. Keduanya mengikuti gaya visual yang sama dan menyediakan jalan kembali ke papan utama.

## 4.26 Tampilan responsif

| Halaman | 768 px (tablet) | 390 px (ponsel) |
|---|---|---|
| Papan utama | ![](../screenshots/29-home-768.png) | ![](../screenshots/29-home-390.png) |
| Detail barang | ![](../screenshots/30-item-768.png) | ![](../screenshots/30-item-390.png) |
| Tiket klaim | ![](../screenshots/31-ticket-768.png) | ![](../screenshots/31-ticket-390.png) |

Tata letak memakai *grid* yang menyesuaikan diri pada beberapa titik henti (900, 760, 640, dan 520 px). Pada lebar tablet, bagian pembuka dan halaman detail berubah dari dua kolom menjadi satu kolom dan jumlah kolom tag di papan berkurang. Pada lebar ponsel, tag disusun lebih rapat, baris formulir menjadi satu kolom, dan tabel admin dapat digulir secara horizontal di dalam kotaknya tanpa membuat seluruh halaman bergeser. Pengujian pada lebar 390 px memastikan tidak ada guliran horizontal pada tingkat halaman.
