# 7. Perbandingan dengan Proyek Midterm

Proyek midterm kelompok berjudul **Campus Lost & Found** dan dibangun dengan framework Laravel (PHP). Kuis 2 melanjutkan gagasan yang sama, yaitu layanan barang hilang dan temuan di kampus, tetapi dibangun ulang dari awal dengan JSP dan Servlet di bawah nama baru **Ketemu**. Tidak ada kode Laravel yang dipindahkan; yang dilanjutkan adalah domain masalah, entitas inti, dan pelajaran dari midterm.

> **Catatan untuk penulis report.** Repositori midterm tidak tersedia saat bahan ini disusun, sehingga poin tentang isi midterm di bawah ini ditulis berdasarkan gambaran umum proyek. Bagian yang ditandai **[verifikasi]** perlu dicocokkan dengan repositori atau report midterm kelompok sebelum dimasukkan ke report akhir. Poin tentang Ketemu semuanya dapat dibuktikan dari kode di repositori ini.

## 7.1 Ringkasan perbedaan

| Aspek | Midterm: Campus Lost & Found (Laravel) | Kuis 2: Ketemu (JSP) |
|---|---|---|
| Bahasa dan platform | PHP dengan framework Laravel | Java 17, Jakarta EE 10 (Servlet 6, JSP 3.1, JSTL 3.0) di Tomcat 10.1 |
| Arsitektur | MVC bawaan Laravel: *route*, *controller*, model Eloquent, *view* Blade | MVC manual: *servlet* sebagai *controller*, kelas DAO dan *service* sebagai model, JSP dengan JSTL sebagai *view* |
| Akses basis data | ORM Eloquent dan *migration* Laravel **[verifikasi]** | JDBC murni dengan `PreparedStatement`, *connection pool* HikariCP, skema di satu berkas `schema.sql` |
| Basis data | **[verifikasi: MySQL atau lainnya]** | PostgreSQL 16 (lokal di Docker, *production* di Neon) |
| Autentikasi | Fitur autentikasi bawaan atau paket Laravel **[verifikasi]** | Ditulis sendiri: bcrypt, filter sesi, penggantian ID sesi setelah masuk |
| CSRF dan *escaping* | Otomatis dari Laravel (`@csrf`, `{{ }}` di Blade) | Ditulis sendiri: `CsrfFilter` dan tag `<k:csrf/>`, `<c:out>` di setiap keluaran, *scriptlet* dilarang |
| Alur klaim | **[verifikasi: apakah midterm sudah memiliki klaim, dan sejauh apa]** | Alur lengkap: pengajuan klaim dengan bukti, tiket bernomor, persetujuan atau penolakan oleh penemu, penolakan otomatis klaim lain, stempel "Returned" saat serah terima |
| Penyimpanan foto | **[verifikasi: berkas di disk / storage Laravel atau tanpa foto]** | Disimpan sebagai `BYTEA` di PostgreSQL, maksimal 3 foto per laporan, dilayani dengan `ETag` dan *cache* |
| Peran admin | **[verifikasi]** | Petugas meja SKK: buku besar meja, kategori, lokasi (dengan penanda pos SKK), manajemen pengguna, moderasi |
| Fitur tambahan | **[verifikasi]** | Komentar penampakan, daftar pantau dengan catatan pribadi, notifikasi di dalam aplikasi |
| Desain | **[verifikasi]** | Konsep visual "meja barang temuan": tag manila, stempel karet, kolom isian bergaya tulisan tangan, tiket klaim |
| *Hosting* | **[verifikasi]** | Render (kontainer Docker) dan Neon (PostgreSQL *serverless*) |
| Pengujian | **[verifikasi]** | 74 kasus uji *end-to-end* otomatis dengan Playwright, termasuk 30 kasus negatif |

## 7.2 Apa yang berubah dan mengapa

**1. Dari framework ke Servlet dan JSP murni.** Ketentuan kuis mewajibkan JSP. Perpindahan ini berarti banyak hal yang di Laravel tersedia otomatis harus dibangun sendiri: *routing* (dilakukan dengan anotasi `@WebServlet` dan pemecahan `pathInfo` di `BaseServlet`), validasi (kelas `Validator`), pesan *flash* setelah pengalihan (kelas `Flash`), proteksi CSRF, paginasi (kelas `Paging`), transaksi (kelas `Tx`), dan pemuatan skema serta data contoh (`AppInit`). Hasilnya kode menjadi lebih panjang, tetapi setiap mekanisme terlihat jelas dan dapat dijelaskan di report.

**2. Alur klaim dan serah terima yang lebih lengkap.** Ketemu memperlakukan klaim sebagai proses dengan status yang jelas (*pending*, *approved*, *rejected*, *cancelled*) dan aturan bisnis yang ditegakkan di server: hanya barang temuan yang dapat diklaim, pelapor tidak dapat mengklaim barangnya sendiri, satu klaim aktif per pengguna per barang, dan penyetujuan satu klaim otomatis menolak klaim lain dalam satu transaksi. Pos SKK dijadikan titik serah terima yang nyata melalui atribut `is_skk_post` pada lokasi.

**3. Foto disimpan di basis data.** Karena Render tidak menyimpan berkas di disk secara permanen, foto dipindahkan ke kolom `BYTEA`. Keputusan ini dan konsekuensinya dibahas di `02-model-fisik.md` bagian 2.12 dan `08-evaluasi-desain-performa.md`.

**4. Report berbasis bukti.** Midterm kehilangan nilai pada bagian report **[verifikasi: catatan penilaian midterm]**. Untuk kuis ini, setiap klaim di bahan report didukung bukti: kasus uji bernomor dengan hasil aktual, tangkapan layar yang diambil otomatis, dan rujukan ke kelas atau berkas yang mengimplementasikan setiap mekanisme.

## 7.3 Apa yang lebih sulit

- **Menulis ulang mekanisme yang biasanya disediakan framework.** CSRF, validasi dengan pesan per kolom, *flash message*, dan paginasi memerlukan kelas tersendiri dan pengujian tersendiri.
- **Keterbatasan Expression Language.** EL di JSP tidak dapat membaca *field* publik maupun Java *record*, dan tidak dapat memformat tipe `java.time`. Setiap data yang ditampilkan harus memiliki *getter*, dan format tanggal dibuat melalui kelas utilitas `Fmt`.
- **Unggahan *multipart*.** Servlet menyediakan `getParts()`, tetapi validasi ukuran, jenis berkas, batas jumlah, dan penanganan berkas yang melebihi batas Tomcat harus diatur sendiri.
- **Konfigurasi *deployment*.** Render tidak memiliki *runtime* Java bawaan, sehingga aplikasi perlu dikemas sebagai kontainer Docker dengan *build* bertahap (Maven, lalu Tomcat) dan skrip yang menyesuaikan port Tomcat dengan variabel `PORT` dari Render.

## 7.4 Apa yang lebih mudah

- **Kontrol penuh atas SQL.** Query seperti penghitungan nomor tiket klaim atau penolakan massal klaim dengan `UPDATE ... RETURNING` lebih mudah ditulis langsung dalam SQL dibanding melalui ORM.
- **Domain sudah dipahami.** Entitas inti (pengguna, laporan, kategori, lokasi) sudah dirancang pada midterm, sehingga waktu dapat difokuskan pada alur klaim, desain, dan pengujian.
- **Tag file JSP.** Komponen visual yang berulang (tag barang, stempel, kolom isian, tiket, ikon, paginasi) cukup ditulis sekali sebagai *tag file* di `WEB-INF/tags/` dan dipakai di semua halaman, mirip komponen Blade.
- **Docker untuk lingkungan yang sama.** Dengan `docker compose up`, setiap anggota kelompok mendapatkan Tomcat dan PostgreSQL dengan versi yang sama tanpa memasang keduanya secara manual.
