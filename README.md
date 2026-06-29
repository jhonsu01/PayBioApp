<div align="center">

# 💸 PayBio — Smart Offline Ledger

**Tarjetero virtual de cobros para Android. 100% local, centrado en la privacidad y potenciado por IA en el dispositivo.**

[![Android CI](https://github.com/USER/REPO/actions/workflows/android-ci.yml/badge.svg)](../../actions/workflows/android-ci.yml)
[![Release APK](https://github.com/USER/REPO/actions/workflows/release.yml/badge.svg)](../../actions/workflows/release.yml)
[![Latest release](https://img.shields.io/github/v/release/USER/REPO?label=APK)](../../releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-00E676.svg)](LICENSE)

</div>

---

## ✨ Qué es

PayBio guarda tus métodos de cobro (bancos, billeteras virtuales y direcciones cripto) y los muestra como tarjetas con su **código QR** listo para compartir — **sin conexión a internet y sin servidores**. La app ni siquiera solicita el permiso de Internet.

## 🧩 Módulos principales

| Módulo | Descripción |
| ------ | ----------- |
| **1. Ingesta Inteligente (IA local)** | Sube una captura de tu app bancaria o pega texto: **ML Kit** (reconocimiento de texto + escaneo de QR) extrae las cadenas y un clasificador por reglas detecta el formato (Ethereum `0x...`, CLABE, Yape, Pix, Nequi, etc.) para crear la tarjeta automáticamente. |
| **2. Catálogo por País** | `assets/payment_catalog.json` precargado con bancos, billeteras y redes blockchain (BTC, ETH, BSC, Solana, Tron) y sus patrones de validación. |
| **3. Modo Kiosco** | Para mostradores: bloquea ediciones con un **PIN de 4–6 dígitos**, mantiene la pantalla encendida (anti-reposo) y fija la app en primer plano (`startLockTask`). Salir requiere el PIN. |
| **4. Respaldo Cero-Nube** | Empaqueta la base de datos **cifrada** y las imágenes QR en un `.zip` y delega al sistema (`ACTION_SEND`) dónde guardarlo. PayBio nunca toca credenciales de nube. |

Extras: **Fast-Share** (compartir QR + datos formateados con un toque) y **widget** de pantalla de inicio para lanzar el cobro al instante.

## 🏗️ Stack

- **Kotlin** + **Jetpack Compose** (Material 3, tema oscuro AMOLED obligatorio)
- **Room** + **SQLCipher** (cifrado en reposo de datos bancarios y cripto)
- **Google ML Kit** (Text Recognition + Barcode Scanning) — on-device
- **ZXing** para generación de QR · **java.util.zip** para respaldos
- `minSdk 26` · `targetSdk 35` · `compileSdk 35`

## 📦 Instalar el APK

1. Ve a la pestaña **[Releases](../../releases/latest)**.
2. Descarga `PayBio-vX.Y.Z.apk`.
3. En tu Android, habilita *"Instalar apps de orígenes desconocidos"* para tu navegador/gestor de archivos.
4. Abre el `.apk` e instala.

> El APK de Releases se firma con la clave de depuración estándar de Android para permitir la distribución abierta desde GitHub.

## 🛠️ Compilar desde el código

Requisitos: JDK 17 y Android SDK (platform 35, build-tools 35.0.0).

```bash
# APK de depuración
./gradlew :app:assembleDebug

# APK de release (instalable)
./gradlew :app:assembleRelease -PappVersionName=1.0.0 -PappVersionCode=1
```

El APK queda en `app/build/outputs/apk/`.

## 🔁 Versionado y releases automáticos

- El versionado sigue **SemVer** mediante etiquetas git `vX.Y.Z`.
- Al hacer **push de un tag** `v*`, el workflow [`release.yml`](.github/workflows/release.yml) compila el APK de release, lo nombra `PayBio-vX.Y.Z.apk` y publica una **GitHub Release** con el APK adjunto y notas autogeneradas.
- Cada push a `main` ejecuta [`android-ci.yml`](.github/workflows/android-ci.yml), que compila el APK de depuración y lo sube como *artifact*.

```bash
# Publicar una nueva versión
git tag v1.0.0
git push origin v1.0.0   # -> Actions construye el APK y crea la Release
```

## 🔒 Privacidad

Sin permiso de Internet · sin analítica · sin cuentas · sin servidores. Todo vive cifrado en tu dispositivo y solo sale de él si **tú** decides compartirlo.

## 📄 Licencia

[MIT](LICENSE)
