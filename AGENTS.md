# AGENTS.md — LirouHabit

## O que é este repo
App Android anti-procrastinação agressiva (Kotlin + Jetpack Compose) + landing page de download.
Também contém, na raiz, um protótipo web legado de rotinas/hábitos (HTML/JS puro).

## Estrutura
- `android/` — app Android (package `com.lirouhabit.app`)
  - `data/` — Room (entities, DAOs, AppDatabase) + DataStore (`SettingsRepository`)
  - `monitor/` — coração do app: `MonitorService` (foreground + polling UsageStats 2s;
    DOIS cronômetros por app vilão:
      • SESSÃO (contígua, graça de 2 min ao trocar de aba): limite configurável → notificação
      • DIA (acumulativo, independente de entrar/sair, zera à meia-noite):
        15 min → notificação pesada · 60 min → overlay full-screen
        após 1h, NOVA entrada no vilão → overlay agressivo "Vale mesmo a pena?"
        (acumulado diário é reidratado do banco ao reiniciar o serviço)
    loop exige SÓ acesso ao uso, overlay é checada só no marco de 60 min),
    `UsageMonitor` (eventos + fallback por lastTimeUsed), `OverlayManager`
    (ComposeView em WindowManager), `BlockingOverlay` (15s de fricção p/ continuar),
    `PhraseBank` (frases reflexivas em 3 níveis — nunca clichês), `BootReceiver`,
    `InterventionReceiver` (ações da notificação: "Fechar o app" = VICTORY + home,
    "Ignorar"/swipe = DEFEAT + snooze 5 min), `MonitorLog` (log p/ tela secreta)
  - `ui/` — telas Compose em `ui/screens/`, `ui/vm/AppViewModel.kt` (VM único), `ui/Nav.kt`
  - fluxo de 1º uso: `IntroScreen` (apresentação de impacto) → `IntroDreamScreen`
    ("qual seu maior sonho?") → `IntroAppsScreen` (seleção múltipla de vilões) →
    `OnboardingScreen` (permissões) → `HomeScreen`
- `docs/` — landing page estática (GitHub Pages) + `docs/icons/` (pacote de 21 ícones SVG)
- ícones: SVG em `docs/icons/ic_*.svg` + Vector Drawables em `android/.../res/drawable/ic_*.xml`
  (nunca usar emojis na UI — sempre o pacote de ícones)
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
- Frases em `monitor/PhraseBank.kt`: {sonho} {vicio} {app} {tempo} {hobby} {hobby_tempo};
  níveis 1/2/3 + reentrada (4), sorteio sem repetir a última frase do mesmo nível
- Hobbies (banco v3): nome + `lastPracticedAt`; botão "Pratiquei agora" alimenta
  mensagens "seu hobby está há X dias sem sua atenção"
- Eventos de intervenção: TRIGGERED / VICTORY / DEFEAT (tabela `intervention_events`)
- Tracker diário: tabela `daily_usage` (banco v2) alimenta `TrackerScreen` e os gráficos
  de uso por dia / semana em `StatsScreen` (semana = seg→dom, zera na segunda)
- Modo de teste SECRETO: 3 chacoalhadas fortes no celular abrem a `DevScreen`
  (estado das permissões, testes de notificação/overlay e log ao vivo do monitor).
  Nenhum botão visível leva até ela — é de propósito.

## Distribuição
- APK via GitHub Releases (`releases/latest/download/lirouhabit.apk`)
- Landing page via GitHub Pages a partir de `docs/`
- NÃO commitar: `android-sdk/`, `tooling/`, `android/keystore/`, builds
