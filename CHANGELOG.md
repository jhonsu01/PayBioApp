# Changelog

Todas las versiones notables de PayBio. El formato sigue [Keep a Changelog](https://keepachangelog.com/es/1.1.0/)
y el proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.0.0] - 2026-06-29

### Añadido
- **Módulo 1 — Ingesta Inteligente (IA local):** extracción con ML Kit (texto + QR) desde captura o texto pegado, con clasificador por reglas para cripto y cuentas.
- **Módulo 2 — Catálogo por país:** `payment_catalog.json` con bancos, billeteras y redes blockchain (BTC, ETH, BSC, Solana, Tron) y patrones de validación.
- **Módulo 3 — Modo Kiosco:** PIN de 4–6 dígitos, anti-reposo (`FLAG_KEEP_SCREEN_ON`) y fijado en primer plano (`startLockTask`).
- **Módulo 4 — Respaldo Cero-Nube:** exportación de la base cifrada + QR a `.zip` vía `ACTION_SEND`.
- Persistencia con **Room + SQLCipher** (cifrado en reposo).
- UI **Jetpack Compose** con tema oscuro AMOLED (Material 3).
- **Fast-Share** de QR + datos y **widget** de pantalla de inicio.
- Pipeline de **GitHub Actions**: CI en cada push y publicación automática del APK en Releases por tag `v*`.

[1.0.0]: https://github.com/USER/REPO/releases/tag/v1.0.0
