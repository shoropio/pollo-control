# PolloControl

Aplicacion Android para administrar la operacion de una granja de pollos: lotes, inventario, alimentacion, gastos, sanidad, pesajes, sacrificios, ventas, clientes, reportes y respaldos.

## Tecnologias

- Kotlin
- Android Jetpack Compose
- Material 3
- Room
- Gradle Kotlin DSL

## Requisitos

- Android Studio reciente
- JDK 17
- Android SDK con `compileSdk` 36

## Configuracion local

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Sincroniza Gradle.
4. Ejecuta la app desde el modulo `app`.

Los archivos locales o sensibles no se versionan:

- `local.properties`
- `google-services.json`

Si activas Firebase mas adelante, agrega tu propio `google-services.json` localmente y descomenta las dependencias indicadas en los archivos Gradle.

## Comandos utiles

En Windows:

```powershell
.\gradlew.bat app:assembleDebug
```

En macOS/Linux:

```bash
./gradlew app:assembleDebug
```

## Estado

Proyecto en desarrollo inicial.
