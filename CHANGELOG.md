# Changelog

Todas las versiones notables de PayBio. El formato sigue [Keep a Changelog](https://keepachangelog.com/es/1.1.0/)
y el proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.7.0] - 2026-06-29

### Cambiado
- **Modo Kiosco (TV):** el texto e ícono de cada recuadro **se reducen automáticamente** cuando hay más tarjetas (p. ej. 6) para que el nombre, titular y cuenta se vean completos sin recortes.

### Añadido
- **Ingesta inteligente:** el botón "Elegir captura" ahora ofrece "App externa" o "**Explorador interno**" para buscar la imagen (útil en TV sin galería).

### Corregido
- **Navegación con control remoto en el inicio:** al volver desde el final de la lista, el foco vuelve a la lista de tarjetas (antes saltaba al botón de IA y no regresaba). Se usa `focusRestorer` + `focusGroup`.

## [1.6.0] - 2026-06-29

### Cambiado
- **Modo Kiosco (TV) aprovecha mejor el espacio:** cada recuadro ahora se divide en dos cuando la celda es ancha — **información de pago a un lado y el QR grande al otro** — para que el QR sea mucho mayor. En celdas altas mantiene el formato vertical. El tamaño se adapta solo a cada celda.

### Añadido
- **Realce de foco para control remoto / TV:** al navegar las tarjetas del inicio con el D-pad, la tarjeta enfocada se resalta (borde de color, leve aumento y elevación), para no perder de vista cuál está seleccionada al editar.

## [1.5.0] - 2026-06-29

### Corregido
- **Modo Kiosco (grilla TV):** el número de cuenta ya no se corta. El QR ahora ocupa todo el espacio disponible de cada celda (layout por peso), quedando **más grande**, mientras el nombre y la cuenta siempre se ven completos.
- **Borde blanco del QR** mucho más delgado y compacto (más QR, menos marco).

### Añadido
- **Logo / QR personalizado en TV:** al elegir imagen ahora se ofrece "App externa" o "**Explorador interno**" (antes daba "ninguna aplicación" en TV sin galería).
- **Exportar respaldo:** además de "Compartir", se puede "**Explorador interno**" para elegir una carpeta y guardar el `.zip` directamente.
- El explorador integrado ahora también navega/selecciona imágenes y carpetas (no solo `.zip`).

### Permisos
- Se añade WRITE_EXTERNAL_STORAGE (API ≤29) para guardar el respaldo en una carpeta con el explorador interno; en API ≥30 se cubre con el acceso a todos los archivos. Sigue **sin Internet**.

## [1.4.0] - 2026-06-29

### Añadido
- **Explorador de archivos integrado** (inspirado en Fossify File Manager) para importar el respaldo `.zip` en dispositivos sin selector del sistema (p. ej. algunas Android TV). Al importar se elige "Selector del sistema" o "Explorador integrado".

### Cambiado
- **Modo Kiosco en pantallas grandes / TV:** ahora **todos** los medios de pago se ajustan y se muestran a la vez en una grilla que se ordena sola según la cantidad (antes solo se veían 2). El tamaño del QR se escala para que quepan sin desplazamiento.
- **Selección de País/Plataforma navegable con control remoto:** los desplegables se reemplazaron por un selector en diálogo con lista enfocable (antes requería mouse en TV).

### Permisos
- Se añade acceso a almacenamiento **opcional** (READ_EXTERNAL_STORAGE en API ≤29; acceso a todos los archivos en API ≥30), usado **solo** si abres el explorador integrado. La app sigue **sin permiso de Internet**.

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

[1.7.0]: ../../releases/tag/v1.7.0
[1.6.0]: ../../releases/tag/v1.6.0
[1.5.0]: ../../releases/tag/v1.5.0
[1.4.0]: ../../releases/tag/v1.4.0
[1.3.0]: ../../releases/tag/v1.3.0
[1.2.0]: ../../releases/tag/v1.2.0
[1.1.0]: ../../releases/tag/v1.1.0
[1.0.0]: ../../releases/tag/v1.0.0
