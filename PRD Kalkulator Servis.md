# **Product Requirements Document (PRD)**

**Nama Produk:** Ketantech Store - Sistem Kasir & Kalkulator Servis

**Versi:** 1.0 (MVP)

**Tanggal:** 21 Juli 2026

**Target Pengguna:** Teknisi Servis HP (Internal), Pelanggan (Eksternal)

---

## **1. Ringkasan Eksekutif (Executive Summary)**

Aplikasi ini dirancang untuk memecahkan masalah penetapan harga servis HP yang seringkali tidak konsisten dan tidak transparan. Aplikasi ini berfungsi sebagai alat internal bagi teknisi untuk menghitung biaya servis (berdasarkan harga sparepart, risiko, operasional, dan tingkat kesulitan), sekaligus sebagai antarmuka (struk digital) yang ramah pelanggan untuk menjelaskan rincian biaya tersebut secara transparan.

### **1.1. Problem Statement**
- Teknisi sering "menebak" harga servis, menyebabkan inkonsistensi antar transaksi
- Pelanggan tidak memahami komponen biaya servis, menimbulkan ketidakpercayaan
- Toko kesulitan mengamankan margin risiko kegagalan pemasangan secara sistematis

### **1.2. Solusi**
Kalkulator harga servis dua-mode (input teknisi & nota pelanggan) dengan logika perhitungan terstandarisasi dan tampilan transparan yang mengedukasi pelanggan.

---

## **2. Tujuan Produk (Objectives)**

* **Standarisasi Harga:** Mencegah teknisi "menebak" harga servis yang dapat merugikan toko atau pelanggan.
* **Transparansi Pelanggan:** Mengedukasi pelanggan dengan memecah total biaya menjadi komponen yang masuk akal (Harga Komponen, QC/Garansi, Bahan Penunjang, Jasa).
* **Manajemen Risiko:** Mengamankan margin toko (terutama untuk risiko kegagalan pemasangan sparepart) secara sistematis tanpa terlihat memberatkan pelanggan.

### **2.1. Success Metrics (Indikator Keberhasilan)**

| Metrik | Target | Cara Ukur |
|--------|--------|-----------|
| Konsistensi harga | 100% transaksi menggunakan kalkulator | Cek manual / log penggunaan |
| Waktu penetapan harga | < 2 menit per transaksi | Stopwatch saat testing |
| Keluhan harga pelanggan | Turun 50% dalam 3 bulan | Catatan komplain |
| Adopsi teknisi | Semua teknisi aktif menggunakan dalam 1 minggu | Observasi harian |

---

## **3. Ruang Lingkup (Scope)**

### **3.1. Dalam Ruang Lingkup (In Scope - V1)**
- Kalkulator harga servis dengan 3 level jasa
- Mode nota/kasir untuk tampilan pelanggan
- Mode pengaturan (settings) dengan penyimpanan lokal
- Responsive design (desktop split-view, mobile stacked)
- Perhitungan otomatis real-time
- Format mata uang Rupiah (IDR)

### **3.2. Di Luar Ruang Lingkup (Out of Scope - V1)**
- Multi-user / autentikasi
- Penyimpanan riwayat transaksi ke cloud
- Integrasi printer thermal / export PDF
- Manajemen inventaris sparepart
- Multi-bahasa (hanya Bahasa Indonesia)
- Offline sync antar perangkat

---

## **4. Tech Stack & Platform**

| Komponen | Spesifikasi | Alasan |
|----------|-------------|--------|
| **Platform** | **Android Native App** (min SDK 24 / Android 7.0, target SDK 34) | Penggunaan pribadi di HP teknisi, performa optimal |
| **Bahasa** | Kotlin | Standar resmi Android, ringkas dan aman |
| **UI** | XML Layouts + Material Components 3 | Native feel, mature, APK kecil |
| **Arsitektur** | Single-Module, Activity + ViewBinding sederhana | MVP tidak butuh arsitektur kompleks |
| **Penyimpanan** | SharedPreferences (gantikan localStorage) | Persistence standar Android |
| **Build** | Gradle (Kotlin DSL) + Gradle Wrapper | Reproducible build |
| **CI/CD** | **GitHub Actions** — build, test, dan rilis APK otomatis via GitHub Releases | Automasi penuh dari push ke rilis |
| **Format Angka** | NumberFormat.getCurrencyInstance(Locale("in","ID")) | Format Rupiah standar Indonesia |

> **Catatan:** Tidak perlu backend/server. Aplikasi 100% offline, semua data tersimpan lokal di perangkat. Target ukuran APK < 5 MB.

---

## **5. Fitur Utama (Key Features)**

### **5.1. Mode Kalkulator (Input Teknisi)**

| Fitur | Deskripsi | Validasi |
|-------|-----------|----------|
| **Input Nama/Tipe Perangkat** | Field teks untuk mencatat perangkat yang diservis (misal: "Samsung A51") | Opsional, maks 100 karakter, tampil di nota |
| **Input Nama Pelanggan** | Field teks untuk nama pelanggan | Opsional, maks 100 karakter |
| **Input Modal Sparepart** | Field angka untuk harga beli komponen + ongkos kirim | Wajib jika ada sparepart; hanya angka ≥ 0; default 0 |
| **Pemilihan Level Jasa (Tiering)** | Radio button / dropdown 3 level | Wajib pilih salah satu; default Level 1 |
| **Tombol Reset** | Menghapus semua input dan kembali ke default | Konfirmasi tidak diperlukan |

**Detail Level Jasa:**
- **Level 1 (Ringan/Software):** Pengerjaan tanpa bongkar mesin (flashing, bypass, reset).
- **Level 2 (Menengah/Hardware Eksterior):** Pengerjaan bongkar standar (ganti LCD, konektor, baterai, kamera).
- **Level 3 (Berat/Hardware Mesin):** Pengerjaan berisiko tinggi (angkat IC, mati total, solder PCB, water damage).

### **5.2. Mode Nota/Kasir (Tampilan Pelanggan)**

* **Rincian Biaya (Itemized Billing):**
  * **Suku Cadang / Komponen:** Menampilkan angka dari Input Modal Sparepart.
  * **Garansi & Quality Control:** Menampilkan hasil perhitungan persentase "Dana Risiko" dari modal sparepart. **(Hanya muncul jika modal sparepart > 0)**.
  * **Bahan & Alat Penunjang:** Menampilkan biaya operasional flat.
  * **Jasa Teknisi:** Menampilkan tarif jasa sesuai Level yang dipilih + label level (misal: "Jasa Teknisi - Level 2").
* **Total Estimasi:** Penjumlahan otomatis seluruh komponen biaya, ditampilkan lebih besar/menonjol.
* **Info Perangkat:** Menampilkan nama perangkat dan nama pelanggan (jika diisi).
* **Pesan Transparansi/Edukasi:** Teks dinamis di bawah nota (durasi garansi menyesuaikan level jasa yang dipilih):
  > *"Harga di atas adalah estimasi transparan. Biaya Garansi & QC dialokasikan untuk jaminan pemasangan dan penggantian jika terjadi kegagalan komponen selama masa garansi. Garansi berlaku **{X} hari** untuk jasa pemasangan dan komponen yang diganti."*
* **Logo & Tanggal & Nomor Nota:** Logo toko di bagian atas nota, tanggal otomatis (hari ini) dan nomor nota sederhana (auto-increment berbasis SharedPreferences, format: KTS-YYYYMMDD-XXX).

### **5.3. Mode Pengaturan (Settings)**

* **Form Konfigurasi:**

| Pengaturan | Label di UI | Tipe | Default | Validasi |
|------------|-------------|------|---------|----------|
| Persentase Dana Risiko | Persentase Garansi & QC (%) | Angka 0-100 | 10 | Tidak boleh negatif atau > 100 |
| Biaya Operasional | Biaya Bahan & Alat Penunjang (Rp) | Angka ≥ 0 | 20.000 | Tidak boleh negatif |
| Tarif Jasa Level 1 | Tarif Jasa Level 1 - Ringan (Rp) | Angka ≥ 0 | 75.000 | Tidak boleh negatif |
| Tarif Jasa Level 2 | Tarif Jasa Level 2 - Menengah (Rp) | Angka ≥ 0 | 150.000 | Tidak boleh negatif |
| Tarif Jasa Level 3 | Tarif Jasa Level 3 - Berat (Rp) | Angka ≥ 0 | 300.000 | Tidak boleh negatif |
| Garansi Level 1 | Durasi Garansi Level 1 (hari) | Angka 0-365 | 7 | Placeholder, bisa diubah pemilik |
| Garansi Level 2 | Durasi Garansi Level 2 (hari) | Angka 0-365 | 30 | Placeholder, bisa diubah pemilik |
| Garansi Level 3 | Durasi Garansi Level 3 (hari) | Angka 0-365 | 14 | Placeholder, bisa diubah pemilik |

* **Penyimpanan Lokal:** Nilai pengaturan disimpan di `SharedPreferences` agar tidak reset saat aplikasi ditutup (persistence).
* **Tombol Reset ke Default:** Mengembalikan semua pengaturan ke nilai default dengan dialog konfirmasi.
* **Akses Settings:** Ikon gear di pojok kanan atas layar utama; **tanpa password/PIN** (penggunaan pribadi).
* **Logo:** Logo toko ditampilkan di header nota dan sebagai ikon aplikasi. File: `res/drawable/ic_logo.xml` (vektor placeholder, dapat diganti logo asli kapan saja tanpa mengubah kode).
* **Durasi garansi dinamis:** Teks garansi di nota menampilkan durasi sesuai level jasa yang dipilih (misal: "Garansi berlaku 30 hari untuk jasa pemasangan dan komponen yang diganti.").

---

## **6. Spesifikasi UX/UI (User Experience/Interface)**

### **6.1. Layout**
* **Desktop/Tablet (≥ 768px):** *Split-View* — Kiri: Kolom Input Teknisi, Kanan: Rincian Nota Pelanggan.
* **Mobile (< 768px):** Susunan atas-bawah — Input di atas, Nota di bawah.
* **Nota pelanggan:** Menyerupai struk/kwitansi digital dengan border, background putih, dan font monospace untuk angka agar rapi.

### **6.2. Psikologi Bahasa (Dual Terminology)**

| Konteks | Istilah yang Digunakan |
|---------|------------------------|
| *Backend/Settings (Teknisi)* | "Dana Risiko", "Biaya Operasional", "Modal Sparepart" |
| *Frontend/Nota (Pelanggan)* | "Garansi & QC", "Bahan Penunjang", "Suku Cadang" |

### **6.3. Tema Warna**
Profesional, bersih, dan menumbuhkan kepercayaan:
- **Primary:** Biru (#2563EB / blue-600) — kepercayaan
- **Background:** Putih (#FFFFFF) dan Slate/Abu-abu muda (#F1F5F9 / slate-100)
- **Teks:** Slate gelap (#1E293B / slate-800)
- **Aksen Total:** Hijau (#16A34A / green-600) untuk total tagihan
- **Error/Warning:** Merah (#DC2626 / red-600) untuk validasi

### **6.4. States**
- **Empty state:** Sebelum input, nota menampilkan placeholder "Masukkan data servis untuk melihat rincian biaya".
- **Loading state:** Tidak diperlukan untuk V1 (semua kalkulasi instan di client).
- **Error state:** Input tidak valid ditandai border merah + pesan error kecil di bawah field.

---

## **7. Logika Perhitungan (Business Logic)**

### **7.1. Formula**

```
Modal_Sparepart   = Input User (Rp), default 0
Dana_Risiko       = Modal_Sparepart × (Settings_Risk_Margin / 100)
Biaya_Operasional = Settings_Operational_Cost (flat rate)
Biaya_Jasa        = Settings_Level_X_Fee (berdasarkan Level 1, 2, atau 3)

Total_Tagihan     = Modal_Sparepart + Dana_Risiko + Biaya_Operasional + Biaya_Jasa
```

### **7.2. Aturan Tampilan Nota**

| Kondisi | Tampilan di Nota |
|---------|------------------|
| Modal_Sparepart = 0 | Baris "Suku Cadang" dan "Garansi & QC" **disembunyikan** |
| Modal_Sparepart > 0 | Kedua baris ditampilkan |
| Semua kondisi | "Bahan Penunjang" dan "Jasa Teknisi" selalu ditampilkan |
| Nama perangkat kosong | Tampilkan "-" atau sembunyikan baris |
| Nama pelanggan kosong | Tampilkan "Pelanggan" sebagai default |

### **7.3. Contoh Perhitungan**

**Skenario:** Ganti LCD Samsung A51 (Level 2), Modal LCD = Rp 450.000, Settings default.

| Komponen | Perhitungan | Jumlah |
|----------|-------------|--------|
| Suku Cadang / Komponen | Input user | Rp 450.000 |
| Garansi & Quality Control | 450.000 × 10% | Rp 45.000 |
| Bahan & Alat Penunjang | Flat rate | Rp 20.000 |
| Jasa Teknisi - Level 2 | Flat rate | Rp 150.000 |
| **Total Estimasi** | | **Rp 665.000** |

---

## **8. Edge Cases & Error Handling**

| Kasus | Perilaku yang Diharapkan |
|-------|--------------------------|
| Input modal sparepart kosong | Dianggap 0; baris sparepart & garansi disembunyikan di nota |
| Input modal sparepart negatif | Ditolak — EditText `inputType="number"` tidak menerima tanda minus; nilai lama dipertahankan |
| Input non-numeric (huruf/simbol) | Ditolak di level keyboard (inputType number) + parsing aman (toLongOrNull) |
| Angka sangat besar (> 1 miliar) | Diterima, format currency tetap benar (NumberFormat IDR); batas maks Long |
| SharedPreferences corrupt | Nilai default digunakan; aplikasi tidak crash |
| Belum pilih level jasa | Default otomatis Level 1, tidak ada error |
| Desimal pada input Rupiah | Tidak dimungkinkan (inputType number = integer saja) |
| Rotasi layar / proses kill | Input kalkulator dipertahankan via onSaveInstanceState, settings tetap aman |
| Aplikasi di-restart | Input kalkulator reset, settings tetap tersimpan |
| Nomor nota saat kalkulasi berubah-ubah | Nomor nota & counter **hanya naik saat tombol "Nota Baru / Finalisasi" ditekan**, bukan saat angka berubah |

---

## **9. Struktur Data (SharedPreferences)**

### **9.1. File Prefs: `ketantech_settings`**
```
KEY riskMarginPercent : Int    = 10
KEY operationalCost   : Long   = 20000
KEY level1Fee         : Long   = 75000
KEY level2Fee         : Long   = 150000
KEY level3Fee         : Long   = 300000
KEY warrantyDaysL1    : Int    = 7
KEY warrantyDaysL2    : Int    = 30
KEY warrantyDaysL3    : Int    = 14
```

### **9.2. File Prefs: `ketantech_receipt_counter`**
```
KEY lastDate : String = "2026-07-21"   (format ISO, reset harian)
KEY counter  : Int    = 5
```
*(Counter naik hanya saat nota di-"finalkan" (lihat 10.1); nomor nota: `KTS-20260721-005`)*

---

## **10. Asumsi & Keterbatasan (Assumptions & Constraints)**

### **10.1. Asumsi**
- Aplikasi digunakan oleh satu teknisi/toko pada perangkat Android pribadi (single-tenant, **tanpa autentikasi**).
- Harga dalam Rupiah (IDR) saja, tidak perlu multi-currency.
- Tidak ada kebutuhan perhitungan diskon atau pajak (PPN) di V1.
- Satu nota = satu perangkat = satu level jasa (tidak ada multi-item).
- Counter nomor nota naik saat teknisi menekan tombol **"Nota Baru"** (menandakan transaksi selesai & siap input berikutnya).
- Logo menggunakan placeholder vektor yang akan diganti logo asli oleh pemilik.

### **10.2. Keterbatasan**
- Data settings hanya tersimpan di perangkat yang sama (tidak sync antar perangkat).
- Tidak ada riwayat nota yang tersimpan setelah aplikasi di-uninstall / data di-clear.
- Tidak ada autentikasi — siapa pun yang memegang HP bisa mengubah settings.
- Nomor nota tidak unik secara global (hanya per perangkat).

---

## **10.3. CI/CD & Rilis (GitHub)**

* **Repository:** Source code di-push ke GitHub.
* **Build Otomatis:** GitHub Actions menjalankan unit test + build APK pada setiap push ke `main` dan Pull Request.
* **Rilis Otomatis:** Saat tag `v*` dibuat (misal `v1.0.0`), workflow mem-build APK (debug) dan mem-publish **GitHub Release** berisi file `.apk` yang siap di-download & di-install.
* **Signing:** V1 menggunakan debug signing (cukup untuk penggunaan pribadi). Release signing ke Play Store masuk Future Scope.
* **Versioning:** `versionCode` otomatis dari run number GitHub Actions; `versionName` dari nama tag.

---

## **11. Acceptance Criteria (Kriteria Penerimaan)**

Fitur dianggap selesai jika:

1. ✅ Teknisi dapat memasukkan nama perangkat, nama pelanggan, modal sparepart, dan memilih level jasa.
2. ✅ Nota pelanggan menampilkan rincian biaya yang ter-update **real-time** saat input berubah.
3. ✅ Baris "Suku Cadang" dan "Garansi & QC" otomatis hilang jika modal sparepart = 0.
4. ✅ Total tagihan dihitung sesuai formula di Bagian 7 dan ditampilkan dalam format Rupiah yang benar (misal: Rp 665.000).
5. ✅ Pengaturan dapat diubah dan tetap tersimpan setelah browser ditutup dan dibuka kembali.
6. ✅ Tombol "Reset ke Default" di settings mengembalikan nilai awal dengan konfirmasi.
7. ✅ Tampilan responsive: split-view di desktop, stacked di mobile.
8. ✅ Input invalid (negatif, huruf) ditolak dengan pesan error yang jelas.
9. ✅ Nomor nota dan tanggal otomatis ter-generate dengan format yang benar.
10. ✅ Pesan edukasi/transparansi selalu tampil di bagian bawah nota.

---

## **12. Ruang Lingkup Masa Depan (Future Scope - Tidak untuk V1)**

* **V1.1 - Print/Export:** Fitur cetak nota ke printer thermal bluetooth atau export PDF/gambar untuk dikirim ke WhatsApp pelanggan.
* **V1.2 - Riwayat Transaksi:** Menyimpan riwayat nota servis ke Firebase/Supabase dengan fitur pencarian.
* **V1.3 - Multi-item:** Satu nota bisa berisi beberapa jasa/sparepart sekaligus.
* **V2.0 - Inventory Management:** Integrasi ketersediaan dan harga real-time sparepart dari database toko.
* **V2.0 - Autentikasi & Multi-user:** Login teknisi, role admin untuk settings, sync antar perangkat.
* **V2.0 - Diskon & Pajak:** Dukungan diskon promo dan perhitungan PPN.

---

## **13. Open Questions (Pertanyaan Terbuka)**

| No | Pertanyaan | Keputusan |
|----|-----------|-----------|
| 1 | Apakah perlu password/PIN sederhana untuk masuk Mode Pengaturan? | ✅ **Diputuskan:** Tidak perlu — penggunaan pribadi |
| 2 | Apakah nota perlu logo toko (Ketantech Store)? | ✅ **Diputuskan:** Ya — placeholder vektor dulu, diganti logo asli nanti |
| 3 | Apakah ada kebutuhan mencetak langsung (print) dari aplikasi untuk V1? | ✅ **Diputuskan:** Belum perlu |
| 4 | Apakah garansi memiliki durasi berbeda per level jasa? | ✅ **Diputuskan:** Ya — durasi garansi per level dapat diatur di Settings (default sementara: L1=7 hari, L2=30 hari, L3=14 hari; pemilik menentukan angka final nanti) |
