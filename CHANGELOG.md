# Changelog

Todas las versiones notables de PayBio. El formato sigue [Keep a Changelog](https://keepachangelog.com/es/1.1.0/)
y el proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.3.0] - 2026-06-29

### Añadido
- **Respaldo con PIN opcional:** al exportar se pregunta si incluir el PIN del Modo Kiosco; al importar se restaura si está presente.
- **Modo Kiosco adaptable:** en pantallas pequeñas, menú "Medios de pago" para elegir qué mostrar; en TV / pantallas no táctiles / grandes, se muestran todos los medios a la vez en grilla (sin menú) con imágenes ajustadas.
- **Logo más grande** y con encuadre completo (Fit) en el Modo Kiosco.
- **Botón de apoyo Ko-fi** al final de Ajustes (abre el navegador; la app sigue sin permiso de Internet).

### Corregido
- **Ajustes ahora se desplaza** hasta el final (antes se cortaba antes de "Privacidad" en algunos teléfonos).

## [1.2.0] - 2026-06-29

### Añadido
- **Plataforma personalizada:** opción "➕ Otra…" para crear una plataforma que no esté en el catálogo.
- **País / red personalizado:** opción "➕ Otro país…" para agregar uno no listado.
- **Nombre personalizado por tarjeta** (alias) que se muestra en lista, detalle y kiosco.
- **Logo propio** por tarjeta y **QR personalizado** (reemplaza al generado); se usan también en Modo Kiosco.
- **Importar respaldo .zip** en Ajustes (restaura tarjetas, imágenes y clave; reinicia la app).
- Colombia: **PSE** y **Llave (Bre-B)** ahora son plataformas separadas.

### Cambiado
- El respaldo .zip ahora incluye los logos y la clave de cifrado, para poder restaurarlo en cualquier dispositivo.
- Base de datos migrada a v2 (columnas `label` y `logoImagePath`) sin pérdida de datos.

## [1.1.0] - 2026-06-29

### Cambiado
- Build de release con **R8/minify** activado (shrink + ofuscación del bytecode) y reglas
  de keep robustas para ML Kit, SQLCipher, Room y ViewModels. APK universal (todas las ABIs).

### Notas
- La reducción de tamaño es modesta: el peso de PayBio proviene del modelo de ML Kit
  embebido y de las librerías nativas multi-ABI, que R8 no comprime. Para 100% offline el
  modelo debe ir embebido. Una futura optimización mayor requeriría ABI splits.

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

[1.3.0]: ../../releases/tag/v1.3.0
[1.2.0]: ../../releases/tag/v1.2.0
[1.1.0]: ../../releases/tag/v1.1.0
[1.0.0]: ../../releases/tag/v1.0.0
