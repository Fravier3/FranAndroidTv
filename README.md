# FranTV

Aplicación nativa de televisión con navegación por control remoto, búsqueda, favoritos, categorías, reproductor Media3 y recuperación de señal.

## Descargar

[Descargar FranTV.apk](https://raw.githubusercontent.com/Fravier3/FranAndroidTv/main/FranTV.apk)

Pega ese enlace en Downloader o AFTVnews. Android solicitará confirmar la instalación.

## Canales

Al entrar se consulta https://raw.githubusercontent.com/Fravier3/IPTv-Fran/refs/heads/main/channels.json . Los cambios en ese archivo aparecen sin reinstalar. La copia incluida en este repositorio sirve de respaldo inicial. Si falla la conexión se conserva la última lista válida. Los canales y su disponibilidad dependen de sus proveedores.

## Actualizaciones

La app consulta `update.json` al entrar y desde «Buscar actualización». Una versión superior muestra un aviso y solicita confirmación. Antes de abrir el instalador se verifican SHA-256, nombre del paquete, versión y firma. Las futuras versiones deben conservar `com.fran.tv` y la misma firma e incrementar `versionCode`.

La clave de firma se guarda fuera de este repositorio. No publicar claves ni contraseñas.

## Compilar

Java 17, Android SDK 35 y Gradle 8.9. Configurar `ANDROID_HOME`, `FRANTV_KEYSTORE` y `FRANTV_STORE_PASSWORD`; alias de firma `frantv`.

```sh
gradle assembleRelease lintRelease testDebugUnitTest
python scripts/prepare-release.py
```

El script publica una copia estable y otra versionada del APK y prepara el manifiesto de actualización. Publicar ambos APK y el manifiesto juntos. La instalación inicial de FranTV es independiente de la aplicación anterior.

## Controles

- Inicio: flechas para navegar, OK para ver, mantener OK para favoritos.
- Reproductor: arriba/abajo cambia de canal, OK abre menú, doble OK alterna favorito.
- Números seleccionan canal; Atrás cierra menú o vuelve al inicio.

Las pruebas automatizadas cubren catálogo, filtros, favoritos, manifiesto de actualización y renderizado. La reproducción y el mando deben comprobarse también en un dispositivo físico.
