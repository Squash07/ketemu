# 6. Catatan Keamanan

Bagian ini menjelaskan mekanisme keamanan yang diterapkan di Ketemu, menunjukkan kelas atau berkas yang mengimplementasikannya, dan merujuk pada kasus uji yang membuktikan bahwa mekanisme tersebut bekerja (nomor uji merujuk pada `05-pengujian.md`). Semua jalur kode berada di bawah `src/main/java/id/ketemu/`.

## 6.1 Ringkasan

| Ancaman | Mekanisme | Lokasi implementasi | Bukti uji |
|---|---|---|---|
| Kebocoran kata sandi | Hash bcrypt dengan *salt* acak, cost 10 | `web/AuthServlet.java`, `web/ProfileServlet.java`, `config/AppInit.java` | 4, 6, 7, 11, 12 |
| SQL injection | Seluruh query memakai `PreparedStatement` dengan parameter terikat | `dao/BaseDao.java` dan semua kelas DAO | 69 |
| Cross-Site Request Forgery (CSRF) | Token sinkronisasi per sesi pada setiap formulir `POST` | `filter/CsrfFilter.java`, `util/Csrf.java`, `WEB-INF/tags/csrf.tag` | 67 |
| Cross-Site Scripting (XSS) | Semua keluaran data pengguna di-*escape*; *scriptlet* dilarang | Semua JSP (`<c:out>`, `fn:escapeXml`), `WEB-INF/web.xml` | 68 |
| Session fixation | ID sesi diganti setelah masuk dan setelah ganti kata sandi | `web/BaseServlet.java` (`signIn`), `web/ProfileServlet.java` | 7 |
| Akses tanpa hak | Filter autentikasi, filter admin, dan pemeriksaan kepemilikan di lapisan *service* | `filter/AuthFilter.java`, `filter/AdminFilter.java`, `service/ItemService.java`, `service/ClaimService.java`, `web/CommentServlet.java` | 19, 20, 32, 35, 45, 52, 70, 71 |
| Unggahan berbahaya | Jenis berkas ditentukan dari isi (*magic bytes*), batas ukuran dan jumlah | `util/Uploads.java` | 22, 23, 25 |
| *Open redirect* | Parameter `next` hanya menerima jalur relatif di dalam situs | `web/BaseServlet.java` (`safeNext`) | 70 |

## 6.2 Hash kata sandi dengan bcrypt

Kata sandi tidak pernah disimpan dalam bentuk asli. Saat mendaftar (`AuthServlet.register`) dan mengganti kata sandi (`ProfileServlet.password`), kata sandi di-*hash* dengan `BCrypt.hashpw(password, BCrypt.gensalt(10))` dari pustaka jBCrypt. Setiap *hash* memiliki *salt* acak yang tersimpan di dalam *hash* itu sendiri, sehingga dua pengguna dengan kata sandi yang sama tetap menghasilkan *hash* yang berbeda. Faktor cost 10 berarti 2^10 putaran, cukup lambat untuk menghambat serangan tebak massal tetapi tetap cepat untuk satu kali login.

Saat masuk, `AuthServlet.login` memverifikasi dengan `BCrypt.checkpw`. Pesan kesalahan sengaja dibuat sama untuk email yang tidak terdaftar maupun kata sandi yang salah ("That email and password do not match..."), sehingga penyerang tidak dapat mengetahui email mana yang terdaftar. Panjang kata sandi dibatasi 8–72 karakter karena bcrypt hanya memproses 72 *byte* pertama.

Akun contoh pada `seed.sql` tidak menyimpan *hash* yang dapat ditebak. Kolom kata sandinya berisi penanda `PENDING`, lalu `AppInit` menggantinya dengan *hash* bcrypt dari variabel lingkungan `DEMO_PASSWORD` saat aplikasi pertama kali dinyalakan. Selama masih berisi penanda tersebut, akun tidak dapat dipakai masuk.

## 6.3 PreparedStatement untuk mencegah SQL injection

Seluruh akses basis data melewati kelas `BaseDao`, yang menyediakan metode `list`, `one`, `update`, `insert`, dan `count`. Setiap metode menerima teks SQL dengan tanda `?` dan daftar parameter, lalu memanggil `Connection.prepareStatement(sql)` dan mengikat parameter satu per satu (`BaseDao.bind`). Tidak ada satu pun query yang dibentuk dengan menyambung masukan pengguna ke dalam teks SQL.

Contoh dari pencarian papan utama (`ItemDao.search`):

```java
w.append("AND (i.title ILIKE ? OR i.description ILIKE ? OR i.code ILIKE ? OR i.location_detail ILIKE ?) ");
```

Kata kunci pengguna dikirim sebagai nilai parameter, sehingga masukan seperti `' OR 1=1 --` hanya dicari sebagai teks biasa. Uji 69 membuktikan bahwa masukan tersebut menghasilkan halaman normal dengan nol hasil, bukan *error* atau seluruh isi tabel. Bagian dinamis yang memang harus menjadi bagian SQL (misalnya urutan) dipilih dari daftar tetap di dalam kode, tidak pernah dari masukan pengguna.

## 6.4 Token CSRF

Setiap sesi memiliki token acak 32 *byte* yang dibuat dengan `SecureRandom` dan dikodekan Base64 (`Csrf.token`). Tag `<k:csrf/>` menyisipkan token tersebut sebagai kolom tersembunyi `_csrf` pada setiap formulir `POST`.

`CsrfFilter` memeriksa semua permintaan `POST` sebelum mencapai *servlet*. Bila token tidak ada atau tidak cocok, permintaan ditolak dengan halaman 403 "This form expired". Perbandingan token dilakukan dalam waktu konstan (`Csrf.matches`) agar panjang waktu pemrosesan tidak membocorkan informasi tentang token. Lapisan pertahanan kedua adalah atribut `SameSite=Lax` pada cookie sesi (`web.xml`), sehingga peramban tidak mengirim cookie sesi pada permintaan `POST` yang berasal dari situs lain.

Semua operasi yang mengubah data hanya menerima `POST`. Tautan biasa (`GET`) tidak pernah menghapus atau mengubah data, sehingga gambar atau tautan berbahaya di situs lain tidak dapat memicu perubahan.

## 6.5 Escaping keluaran untuk mencegah XSS

Semua data yang berasal dari pengguna (judul, deskripsi, komentar, nama, catatan, bukti klaim) ditampilkan melalui `<c:out value="..."/>` atau `${fn:escapeXml(...)}` dari JSTL, yang mengubah karakter `<`, `>`, `&`, `"`, dan `'` menjadi entitas HTML. Selain itu, `web.xml` menetapkan `<scripting-invalid>true</scripting-invalid>` untuk semua JSP, sehingga kode Java mentah (*scriptlet*) tidak dapat ditulis di halaman dan semua tampilan dipaksa memakai JSTL dan EL.

Uji 68 membuat laporan berjudul `<script>alert(1)</script>`. Judul tersebut tampil sebagai teks apa adanya, tidak ada elemen `<script>` yang terbentuk di halaman, dan dialog *alert* tidak pernah muncul.

Header tambahan yang dipasang `EncodingFilter` pada setiap respons:

- `X-Content-Type-Options: nosniff`, agar peramban tidak menebak tipe berkas (penting untuk foto yang diunggah pengguna).
- `X-Frame-Options: DENY`, agar halaman tidak dapat disematkan di situs lain (mencegah *clickjacking*).
- `Referrer-Policy: same-origin`, agar alamat halaman internal tidak terkirim ke situs luar.

## 6.6 Perlindungan session fixation

*Session fixation* adalah serangan ketika penyerang menanamkan ID sesi yang ia ketahui ke peramban korban, lalu menunggu korban masuk. Ketemu mencegahnya dengan memanggil `HttpServletRequest.changeSessionId()` di `BaseServlet.signIn` setiap kali pengguna berhasil masuk atau mendaftar, serta di `ProfileServlet` setelah kata sandi diganti. ID sesi lama langsung tidak berlaku.

Uji 7 membandingkan nilai cookie `JSESSIONID` sebelum dan sesudah masuk dan memastikan nilainya berubah. Cookie sesi juga ditandai `HttpOnly` (tidak dapat dibaca JavaScript) dan sesi berakhir setelah 120 menit tanpa aktivitas. Ketika aplikasi berjalan di Render melalui HTTPS, cookie dikirim hanya melalui koneksi terenkripsi.

## 6.7 Autentikasi dan otorisasi

Kontrol akses diterapkan dalam tiga lapis:

1. **`AuthFilter`** memuat ulang data pengguna dari basis data pada setiap permintaan. Dengan cara ini, perubahan peran atau penghapusan akun berlaku seketika tanpa menunggu sesi berakhir. Untuk jalur yang dilindungi (`/profile`, `/claims`, `/watchlist`, `/notifications`, `/admin`, `/photos`, `/comments`, `/items/new`), tamu diarahkan ke halaman masuk dengan parameter `next` (uji 70).
2. **`AdminFilter`** menolak semua jalur `/admin` dan `/admin/*` dengan 403 bila pengguna bukan admin (uji 52 dan 71).
3. **Pemeriksaan kepemilikan di lapisan *service*.** `ItemService.requireReporter` memastikan hanya pelapor atau admin yang dapat mengubah, menutup, dan menghapus laporan (uji 19 dan 20). `ClaimService` memastikan pelapor tidak dapat mengklaim barangnya sendiri (uji 32), hanya pengaju dan penemu yang dapat membuka tiket klaim (uji 35), dan hanya penemu yang dapat menyetujui atau menolak klaim. `CommentServlet` memastikan hanya penulis atau admin yang dapat mengubah dan menghapus komentar (uji 45).

Pemeriksaan dilakukan di server untuk setiap permintaan, bukan sekadar menyembunyikan tombol di tampilan. Uji 20 dan 45 sengaja mengirim permintaan langsung dengan token CSRF yang sah untuk membuktikan bahwa server tetap menolak walaupun tombolnya tidak pernah ditampilkan.

## 6.8 Keamanan unggahan foto

`Uploads` memeriksa setiap berkas yang diunggah:

- Ukuran maksimal 1 MB per berkas (`MAX_BYTES = 1_048_576`) dan maksimal 3 foto per laporan (`MAX_PER_ITEM = 3`).
- Jenis berkas ditentukan dari *magic bytes* di awal isi berkas (`Uploads.sniff`): `FF D8 FF` untuk JPEG, `89 50 4E 47` untuk PNG, dan `RIFF....WEBP` untuk WebP. Ekstensi nama berkas dan tipe yang dikirim peramban diabaikan. Uji 22 membuktikan berkas teks yang diganti namanya menjadi `.png` ditolak.
- Bila salah satu berkas tidak valid, tidak ada satu pun berkas yang disimpan, sehingga laporan tidak berada dalam keadaan setengah tersimpan.

Foto disimpan di basis data, bukan di disk server, sehingga tidak ada risiko berkas yang diunggah dieksekusi oleh server web.

## 6.9 Pengelolaan rahasia

Kredensial basis data tidak ditulis di kode sumber. `config/Db.java` membacanya dari variabel lingkungan (`DB_URL`, `DB_USER`, `DB_PASSWORD`), dan kata sandi akun demo dibaca `config/AppInit.java` dari `DEMO_PASSWORD`. Berkas `.env` dikecualikan dari Git melalui `.gitignore`, dan repositori hanya berisi `.env.example` tanpa nilai rahasia. Di Render, nilai-nilai tersebut diisi melalui menu *Environment*.

## 6.10 Keterbatasan keamanan yang disadari

- Belum ada pembatasan jumlah percobaan masuk (*rate limiting*), sehingga serangan tebak kata sandi hanya dihambat oleh lambatnya bcrypt.
- Belum ada verifikasi email atau NRP. Siapa pun dapat mendaftar dengan NRP sembarang yang belum dipakai.
- Belum ada *Content-Security-Policy*. Perlindungan XSS saat ini bergantung pada *escaping* keluaran yang konsisten.
- Isi foto tidak dipindai atau dikompresi ulang, sehingga metadata EXIF (misalnya lokasi GPS) pada foto tetap tersimpan.
