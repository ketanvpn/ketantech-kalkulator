# 📱 Ketantech Store — Kalkulator Servis

Aplikasi Android untuk menghitung estimasi biaya servis HP secara **konsisten** dan **transparan**.
Teknisi memasukkan data servis, pelanggan melihat rincian biaya yang jujur dan terstruktur dalam bentuk nota digital.

[![Android CI](https://github.com/USERNAME/REPO/actions/workflows/android-ci.yml/badge.svg)](https://github.com/USERNAME/REPO/actions/workflows/android-ci.yml)

---

## ✨ Fitur

| Fitur | Deskripsi |
|-------|-----------|
| 🧮 **Kalkulator Harga** | Modal sparepart + dana risiko (%) + biaya operasional + jasa per level — dihitung otomatis real-time |
| 🧾 **Nota Digital** | Rincian biaya ramah pelanggan dengan istilah "Garansi & QC" dan "Bahan Penunjang" |
| 📊 **3 Level Jasa** | Level 1 (Software), Level 2 (Hardware Eksterior), Level 3 (Mesin/IC) |
| 🛡️ **Garansi per Level** | Durasi garansi berbeda per level jasa, dapat diatur sendiri |
| ⚙️ **Pengaturan** | Semua tarif, persentase, dan durasi garansi dapat diubah — tersimpan permanen di perangkat |
| 🔢 **Nomor Nota Otomatis** | Format `KTS-YYYYMMDD-XXX`, reset harian |
| 🌐 **100% Offline** | Tanpa internet, tanpa akun, tanpa server |

## 📸 Cara Pakai

1. Isi **nama perangkat**, **nama pelanggan** (opsional), dan **modal sparepart**
2. Pilih **level jasa** (1/2/3)
3. Nota pelanggan di bagian bawah langsung ter-update — tunjukkan ke pelanggan
4. Tekan **✓ Nota Baru** setelah transaksi selesai untuk menaikkan nomor nota dan membersihkan form
5. Atur tarif lewat ikon **⚙️** di pojok kanan atas

## 📲 Download & Install

APK terbaru selalu tersedia di halaman **[Releases](../../releases)**:

1. Download file `ketantech-kalkulator-vX.X.X.apk`
2. Buka di HP Android → izinkan *"Install dari sumber tidak dikenal"*
3. Selesai

> **Syarat:** Android 7.0 (Nougat) atau lebih baru.

## 🚀 Rilis Versi Baru (untuk pemilik)

Semua proses build dilakukan otomatis oleh GitHub Actions. Untuk merilis versi baru:

```bash
git tag v1.0.1
git push origin v1.0.1
```

GitHub akan menjalankan unit test → build APK → membuat Release berisi APK siap download.

## 🛠️ Build dari Source (opsional)

Butuh **JDK 17+** dan **Android SDK 34** (paling mudah lewat Android Studio):

```bash
./gradlew assembleDebug     # APK debug di app/build/outputs/apk/debug/
./gradlew testDebugUnitTest # Jalankan unit test
```

## 🏗️ Struktur Proyek

```
app/src/main/java/com/ketantech/kalkulatorservis/
├── MainActivity.kt            → Layar kalkulator + nota (update real-time)
├── SettingsActivity.kt        → Layar pengaturan
├── PriceCalculator.kt         → Logika perhitungan (murni, ter-unit-test)
├── SettingsPrefs.kt           → Penyimpanan pengaturan (SharedPreferences)
└── ReceiptNumberGenerator.kt  → Nomor nota harian
```

## 🎨 Mengganti Logo

Ganti file `app/src/main/res/drawable/ic_logo.xml` dengan logo asli
(PNG/SVG/vektor). Selama nama resource tetap `ic_logo`, tidak perlu mengubah kode apa pun.
Untuk mengganti ikon aplikasi, perbarui juga `ic_launcher_foreground.xml`.

## 📄 Dokumen

- [PRD Kalkulator Servis.md](PRD%20Kalkulator%20Servis.md) — Product Requirements Document lengkap

## 🗺️ Roadmap

- **V1.1** — Export/share nota ke WhatsApp (gambar/PDF)
- **V1.2** — Riwayat transaksi
- **V2.0** — Multi-item per nota, manajemen sparepart, sync cloud
