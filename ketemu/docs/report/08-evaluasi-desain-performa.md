# 8. Evaluasi Desain, Responsivitas, Performa, dan Keterbatasan

## 8.1 Evaluasi terhadap konsep desain

Konsep visual Ketemu adalah **meja barang temuan di pos satpam kampus**: papan tempat tag-tag barang digantung, stempel karet untuk menandai status, formulir yang diisi dengan pena, dan tiket penitipan yang disobek. Tabel berikut menilai sejauh mana konsep tersebut terwujud dan menunjukkan buktinya.

| Unsur konsep | Wujud di aplikasi | Bukti |
|---|---|---|
| **Tag manila** | Setiap laporan ditampilkan sebagai tag berwarna manila (`#DDB46E`) dengan lubang tali, kode `K-NNNN`, ikon kategori atau foto sampul, dan judul bergaya tulisan tangan (Shantell Sans). Tag di papan sedikit miring dengan sudut yang berbeda-beda, dihitung dari `id` agar konsisten setiap kali dimuat. Komponen ditulis sekali di `WEB-INF/tags/tag.tag`. | `01-home-board.png`, `02-item-detail.png` |
| **Stempel karet** | Status ditampilkan sebagai stempel berbingkai dengan rotasi kecil: Lost berwarna merah stempel (`#B8152F`), Found berwarna biru tinta, Claimed berbingkai ganda berwarna abu, dan Returned berupa stempel penuh berwarna grafit. Status klaim memakai bahasa yang sama (Pending, Approved, Not approved, Cancelled). Komponen `stamp.tag`. | `01-home-board.png`, `28-marked-returned.png` |
| **Kolom isian bergaya pena** | Kolom formulir berupa garis bawah dengan teks berwarna tinta biru (`#2335C9`), menyerupai menulis di formulir kertas. Komponen `penfield.tag`. | `04-report-form.png`, `13-register.png` |
| **Tiket klaim** | Klaim ditampilkan sebagai tiket dengan nomor besar, garis sobekan, dan potongan tiket pada daftar klaim. Komponen `ticket.tag`. | `08-my-claims.png`, `09-claim-ticket.png` |
| **Gerak yang hemat** | Hanya ada dua animasi: tag pada bagian pembuka berayun pelan satu kali (2,2 detik) saat halaman dimuat, dan stempel "Returned" dicapkan dengan animasi tekan saat barang ditandai kembali. | `28-marked-returned.png`, uji 40 |
| **Bahasa antarmuka** | Pesan ditulis sebagai kalimat lengkap yang menjelaskan apa yang terjadi dan apa yang harus dilakukan, misalnya "3 items use Electronics. Move them to another category first." | `22-delete-blocked-message.png`, kolom "Hasil aktual" di `05-pengujian.md` |

Palet warna dibatasi pada token di `assets/css/tokens.css`: dinding `#EEF1EC`, manila `#DDB46E`, tinta `#2335C9`, stempel `#B8152F`, grafit `#22252A`, baja `#9AA3A8`, dan abu teks `#565C63`. Tipografi memakai dua keluarga huruf: Archivo untuk judul dan teks antarmuka, Shantell Sans untuk tulisan tangan pada tag dan deskripsi. Pembatasan ini membuat setiap halaman terasa berasal dari sistem yang sama.

## 8.2 Responsivitas

Tangkapan layar diambil pada tiga lebar: 1440 px (desktop), 768 px (tablet), dan 390 px (ponsel). Lihat `04-screenshot-halaman.md` bagian 4.26.

- Tata letak memakai CSS Grid dengan titik henti 900, 760, 640, 520, dan 370 px.
- Halaman dua kolom (pembuka, detail barang, formulir laporan, halaman masuk) berubah menjadi satu kolom di bawah 900 px.
- Baris formulir dengan dua kolom isian berubah menjadi satu kolom di bawah 640 px.
- Tabel admin dibungkus kotak yang dapat digulir horizontal, sehingga halaman secara keseluruhan tidak pernah bergeser ke samping. Masalah guliran horizontal pada 390 px sempat ditemukan dan diperbaiki (lihat `05-pengujian.md` bagian 5.5 nomor 4).

## 8.3 Aksesibilitas

| Aspek | Penerapan |
|---|---|
| **Fokus keyboard** | Semua elemen interaktif memiliki garis fokus tebal 3 px berwarna tinta (`:focus-visible` di `base.css`). Untuk tag di papan, garis fokus digambar pada bingkai tag agar tidak terpotong oleh rotasi. Tersedia tautan "lewati ke konten" (`.skip`) yang muncul saat ditekan Tab pertama kali. |
| **Kontras warna** | Rasio kontras dihitung dengan rumus WCAG 2.1: teks utama grafit di atas dinding 13,49 : 1; teks abu di atas dinding 5,93 : 1; tautan tinta di atas dinding 7,60 : 1; teks putih di tombol tinta 8,66 : 1; teks grafit di atas tag manila 7,93 : 1. Semuanya memenuhi AA (minimal 4,5 : 1). Stempel Returned (teks terang di atas grafit) mencapai 13,49 : 1. Stempel Lost merah di atas manila (3,40 : 1) dan stempel Found biru di atas manila (4,47 : 1) hanya memenuhi ambang teks besar (3 : 1); hal ini dapat diterima karena stempel ditulis tebal dan berukuran besar, dan statusnya juga tercantum dalam teks di halaman detail. |
| **Gerak yang dikurangi** | Kedua animasi dimatikan bila pengguna mengaktifkan pengaturan `prefers-reduced-motion: reduce` di sistem operasinya (`base.css`, `components.css`, `pages.css`). |
| **Struktur semantik** | Setiap halaman memiliki satu `<h1>`, formulir memakai `<label>` yang terhubung dengan kolomnya, pesan kesalahan dihubungkan ke kolom dengan `aria-describedby`, tabel admin memiliki `<caption>`, dan ikon dekoratif disembunyikan dari pembaca layar. |
| **Konfirmasi tindakan berbahaya** | Setiap tindakan hapus memakai dialog konfirmasi (`21-confirm-dialog.png`), dan penghapusan akun mewajibkan pengetikan ulang kata sandi. |

## 8.4 Performa dan hosting

### Pengukuran lokal

Pengukuran berikut diambil pada 19 November 2026 dengan `curl` terhadap kontainer lokal (Tomcat 10.1 dan PostgreSQL 16 di Docker, data contoh 14 laporan). Angka ini menggambarkan biaya pemrosesan di server tanpa latensi jaringan internet.

| Pengukuran | Hasil |
|---|---|
| Waktu *startup* Tomcat hingga siap melayani | sekitar 1,0 detik (log Tomcat: "Server startup in [1004] milliseconds"), ditambah migrasi skema dan *seed* sekitar 1 detik pada basis data kosong |
| Papan utama `/` (18 KB HTML) | 5,7–13,4 ms per permintaan (5 kali percobaan) |
| Pencarian `/?q=umbrella` | 5,8–10,7 ms |
| Detail barang `/items/409` | median 3,8 ms, maksimum 6,1 ms (10 kali percobaan) |
| Halaman masuk `/login` | 3,6 ms |
| Foto `/photo/{id}` dengan `If-None-Match` | dijawab `304 Not Modified` tanpa isi gambar (uji 26) |
| Ukuran `ROOT.war` | sekitar 5,0 MB |
| Ukuran *image* Docker | sekitar 527 MB (Tomcat 10.1 + JRE 17) |

### Pengukuran di Render (diisi setelah *deploy*)

Layanan gratis Render mematikan aplikasi setelah sekitar 15 menit tanpa permintaan. Permintaan pertama setelahnya harus menunggu kontainer dinyalakan ulang (*cold start*). Cara mengukur: biarkan situs tidak diakses selama lebih dari 15 menit, lalu buka DevTools peramban pada tab Network dan muat ulang halaman utama.

| Pengukuran | Hasil |
|---|---|
| Muat pertama setelah tertidur (*cold start*) | [isi setelah deploy] detik |
| Muat normal halaman utama | [isi setelah deploy] ms |
| Muat normal halaman detail barang | [isi setelah deploy] ms |

### Pertimbangan penyimpanan foto sebagai BYTEA

| Kelebihan | Kekurangan | Mitigasi |
|---|---|---|
| Foto tidak hilang saat Render melakukan *deploy* ulang atau menyalakan ulang kontainer, karena sistem berkas Render bersifat sementara | Ukuran basis data bertambah dan memakan kuota penyimpanan gratis Neon | Batas 1 MB per foto dan 3 foto per laporan |
| Pencadangan dan pemulihan basis data otomatis mencakup foto | Setiap foto yang tidak ada di *cache* harus dibaca dari basis data | Header `ETag` dan `Cache-Control: public, max-age=86400` sehingga peramban memakai salinan lokal |
| Penghapusan laporan otomatis menghapus fotonya (`ON DELETE CASCADE`), tidak ada berkas yatim | Query yang tidak hati-hati dapat ikut memuat isi foto | Query daftar hanya mengambil `id` foto sampul; kolom `content` hanya dibaca oleh `PhotoServlet` |
| Tidak memerlukan layanan penyimpanan objek tambahan (misalnya S3) | Foto tidak dikompresi ulang di server | Disebutkan sebagai keterbatasan |

## 8.5 Keterbatasan

Berikut hal-hal yang belum dikerjakan atau sengaja disederhanakan:

1. **Tidak ada notifikasi email.** Notifikasi hanya muncul di dalam aplikasi, sehingga pengguna perlu membuka Ketemu untuk mengetahui bahwa klaimnya disetujui.
2. **Tidak ada kompresi atau pengubahan ukuran gambar.** Foto disimpan sesuai ukuran aslinya (maksimal 1 MB), dan metadata EXIF tidak dihapus.
3. **Layanan gratis Render tertidur.** Pengunjung pertama setelah periode tidak aktif mengalami waktu muat yang jauh lebih lama.
4. **Tidak ada verifikasi identitas.** Email dan NRP tidak diverifikasi, sehingga keaslian pengguna bergantung pada kejujuran saat mendaftar. Serah terima fisik di pos SKK menjadi titik verifikasi yang sebenarnya.
5. **Tidak ada pembatasan percobaan masuk.** Lihat `06-keamanan.md` bagian 6.10.
6. **Pencarian sederhana.** Pencarian memakai `ILIKE` pada judul, deskripsi, kode, dan detail tempat. Belum ada pencarian teks penuh, toleransi salah ketik, atau pencocokan otomatis antara laporan hilang dan temuan yang mirip.
7. **Antarmuka berbahasa Inggris saja.** Belum ada pilihan Bahasa Indonesia.
8. **Pengujian otomatis berupa uji *end-to-end*.** Belum ada uji unit terpisah untuk kelas DAO dan *service*.
