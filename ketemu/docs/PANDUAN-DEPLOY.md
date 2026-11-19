# Panduan deploy Ketemu ke Render + Neon

Panduan ini membawa Ketemu dari repository GitHub ke internet dengan dua layanan gratis:

- **Neon** untuk database PostgreSQL (gratis, tidak kedaluwarsa).
- **Render** untuk menjalankan aplikasi (Web Service gratis berbasis Docker).

Perkiraan waktu: 20 sampai 30 menit. Tidak perlu kartu kredit untuk paket gratis keduanya.

> Password database hanya dimasukkan ke dashboard Render. Jangan menuliskannya di kode, di README, di chat, atau di commit.

---

## 0. Persiapan

1. Ekstrak ZIP proyek, lalu upload isinya ke repository GitHub baru (misalnya `ketemu`). Pastikan file `Dockerfile`, `pom.xml`, dan folder `src/` ada di root repository.
2. Pastikan file `.env` **tidak** ikut ter-upload (sudah diabaikan oleh `.gitignore`).

## 1. Buat database di Neon

1. Buka https://neon.tech lalu daftar (bisa memakai akun GitHub).
2. Klik **New Project**.
   - Project name: `ketemu`
   - Postgres version: **16**
   - Region: **AWS Asia Pacific (Singapore)**, paling dekat dengan Indonesia.
3. Setelah project jadi, buka **Dashboard** lalu tombol **Connect**.
4. Pada dialog Connect:
   - Database: `neondb` (bawaan) boleh dipakai.
   - Matikan toggle **Connection pooling** supaya yang tampil adalah host langsung (tanpa `-pooler`). Aplikasi sudah punya connection pool sendiri (HikariCP).
5. Connection string yang muncul bentuknya seperti ini:

   ```
   postgresql://neondb_owner:PASSWORD@ep-xxxx-xxxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
   ```

   Dari situ catat tiga bagian (simpan sendiri, jangan dibagikan):

   | Bagian | Contoh | Dipakai untuk |
   |---|---|---|
   | Host dan nama database | `ep-xxxx-xxxx.ap-southeast-1.aws.neon.tech/neondb` | `DB_URL` |
   | User | `neondb_owner` | `DB_USER` |
   | Password | `PASSWORD` | `DB_PASSWORD` |

6. Susun `DB_URL` dalam format JDBC:

   ```
   jdbc:postgresql://ep-xxxx-xxxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
   ```

Tabel dan data contoh tidak perlu dibuat manual. Saat aplikasi pertama kali menyala, Ketemu membuat semua tabel dan mengisi data contoh secara otomatis.

## 2. Buat Web Service di Render

1. Buka https://render.com lalu daftar dengan akun GitHub.
2. Klik **New** lalu **Web Service**.
3. Pilih **Git Provider: GitHub**, beri izin akses ke repository `ketemu`, lalu pilih repository tersebut.
4. Isi pengaturan:
   - Name: `ketemu` (alamatnya nanti menjadi `https://ketemu-xxxx.onrender.com`)
   - Language / Runtime: **Docker** (Render mendeteksi `Dockerfile` secara otomatis)
   - Branch: `main`
   - Region: **Singapore**
   - Instance Type: **Free**
5. Buka bagian **Environment Variables** dan tambahkan:

   | Key | Value |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://...neon.tech/neondb?sslmode=require` (dari langkah 1.6) |
   | `DB_USER` | user dari Neon |
   | `DB_PASSWORD` | password dari Neon |
   | `DEMO_PASSWORD` | password untuk akun demo, misalnya `ketemu123` |

   `PORT` tidak perlu diisi. Render mengisinya sendiri dan aplikasi menyesuaikan port Tomcat secara otomatis.
6. (Opsional) Di **Advanced**, isi **Health Check Path** dengan `/`.
7. Klik **Deploy Web Service**.

Build pertama memakan waktu sekitar 5 sampai 8 menit (Maven mengunduh dependency, lalu image Tomcat dibuat). Di tab **Logs**, tanda berhasil adalah baris berikut:

```
AppInit.migrate Empty database, loading seed data
AppInit.hashPendingPasswords Set the demo password for 4 seeded accounts
Starting ProtocolHandler ["http-nio-10000"]
```

## 3. Uji situs yang sudah live

1. Buka URL dari Render. Halaman pertama setelah deploy atau setelah situs tidur bisa butuh sekitar satu menit.
2. Masuk dengan akun demo (password sesuai `DEMO_PASSWORD`):
   - Admin: `admin@ketemu.test`
   - Mahasiswa: `rani@ketemu.test`, `ayu@ketemu.test`, `bima@ketemu.test`
3. Jalankan setiap alur CRUD sekali (daftar tabel uji ada di `docs/report/05-pengujian.md`) dan isi kolom hasil live di sana.
4. Tulis URL live di bagian atas `README.md` dan di report.

## 4. Hal yang perlu diketahui tentang paket gratis

- **Render tidur setelah 15 menit tanpa pengunjung.** Kunjungan berikutnya menunggu sekitar satu menit. Buka situs 2 menit sebelum demo atau presentasi.
- **Filesystem Render tidak permanen.** Karena itu foto barang disimpan di database (kolom `BYTEA`), bukan di disk, sehingga tidak hilang saat redeploy.
- **Neon gratis** memberi 0,5 GB penyimpanan. Foto dibatasi 1 MB dan 3 per barang, jadi cukup untuk ratusan laporan.
- Database Neon juga bisa tidur saat tidak dipakai. Koneksi pertama mungkin butuh beberapa detik, dan aplikasi sudah mencoba ulang otomatis saat start.

## 5. Jika ada masalah

| Gejala | Penyebab umum | Solusi |
|---|---|---|
| Log berisi `Database not ready` berulang | `DB_URL`, user, atau password salah | Cek ulang environment variable, pastikan ada `?sslmode=require` |
| Log berisi `password authentication failed` | Password salah atau sudah di-reset | Reset password di Neon (Roles), perbarui `DB_PASSWORD`, lalu **Manual Deploy** |
| Halaman 500 setelah login | Tabel belum terbentuk karena start pertama gagal | Perbaiki env var, lalu **Manual Deploy > Clear build cache & deploy** |
| Build gagal di langkah Maven | Gangguan sementara saat mengunduh dependency | Klik **Manual Deploy** lagi |
| Ingin data contoh dari awal | Database sudah berisi data lama | Di Neon, buka SQL Editor, jalankan `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` lalu deploy ulang |

## Alternatif jika Render tidak bisa dipakai

**Railway** (https://railway.app) juga bisa menjalankan `Dockerfile` yang sama. Buat project dari repository GitHub, lalu isi environment variable yang sama seperti di atas. Database tetap memakai Neon.
