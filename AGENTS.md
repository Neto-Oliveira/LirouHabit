# AGENTS.md — LirouHabit

## O que é este repo
App Android anti-procrastinação agressiva (Kotlin + Jetpack Compose) + landing page de download.
Também contém, na raiz, um protótipo web legado de rotinas/hábitos (HTML/JS puro).

## Estrutura
- `android/` — app Android (package `com.lirouhabit.app`)
  - `data/` — Room (entities, DAOs, AppDatabase) + DataStore (`SettingsRepository`)
  - `monitor/` — coração do app: `MonitorService` (foreground + polling UsageStats 2s;
    marcos por sessão: limite do app → notificação, 15 min → notificação pesada,
    60 min → overlay full-screen), `UsageMonitor`, `OverlayManager` (ComposeView em
    WindowManager), `BlockingOverlay` (15s de fricção p/ continuar), `PhraseBank`
    (frases reflexivas em 3 níveis — nunca clichês), `BootReceiver`
  - `ui/` — telas Compose em `ui/screens/`, `ui/vm/AppViewModel.kt` (VM único), `ui/Nav.kt`
- `docs/` — landing page estática (GitHub Pages)
- `android-sdk/`, `tooling/` — ferramentas de build locais, NÃO versionadas

## Build
```powershell
cd android
..\tooling\gradle-8.9\bin\gradle.bat assembleDebug
..\tooling\gradle-8.9\bin\gradle.bat assembleRelease  # assinado (keystore local gitignored)
```
- SDK local em `android-sdk/` (referenciado por `android/local.properties`)
- JDK 21 em `C:\Program Files\Java\jdk-21`
- APK sai como `lirouhabit.apk` (nome fixo — o link do GitHub Releases depende disso)

## Convenções
- Kotlin + Compose Material 3, tema escuro com vermelho agressivo (#E53935)
- DI manual (sem Hilt/Koin) — proposital, para manter o APK leve
- minSdk 26 / target 34 · zero backend, zero internet
- Frases de intervenção em `monitor/PhraseBank.kt` com placeholders {sonho} {vicio} {app} {tempo}
- Eventos de intervenção: TRIGGERED / VICTORY / DEFEAT (tabela `intervention_events`)

## Distribuição
- APK via GitHub Releases (`releases/latest/download/lirouhabit.apk`)
- Landing page via GitHub Pages a partir de `docs/`
- NÃO commitar: `android-sdk/`, `tooling/`, `android/keystore/`, builds
