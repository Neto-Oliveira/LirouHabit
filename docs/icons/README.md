# Pacote de Ícones — LirouHabit

Pacote de 21 ícones em **SVG** (viewBox 24×24, `fill="currentColor"` — herdam a cor do texto/contexto).
Os mesmos ícones existem como **Android Vector Drawables** em `android/app/src/main/res/drawable/ic_*.xml`.

| Arquivo | Uso |
|---|---|
| `ic_flame.svg` | Marca / agressividade crescente |
| `ic_trophy.svg` | Sonhos / card "SEU SONHO" |
| `ic_chain.svg` | Vícios & maus hábitos |
| `ic_phone.svg` | Apps vilões |
| `ic_chart.svg` | Estatísticas / permissão "acesso ao uso" |
| `ic_gear.svg` | Configurações |
| `ic_alert.svg` | Avisos (permissões pendentes, MIUI) |
| `ic_siren.svg` | Intervenções / eventos TRIGGERED |
| `ic_check_circle.svg` | Vitórias / permissões concedidas |
| `ic_x_circle.svg` | Derrotas / permissões pendentes |
| `ic_eye.svg` | Monitor ativo / lista vazia |
| `ic_download.svg` | Botões de download (landing) |
| `ic_lock.svg` | Privacidade (landing) |
| `ic_store.svg` | Play Store "em breve" (landing) |
| `ic_battery.svg` | Permissão de bateria |
| `ic_bell.svg` | Permissão de notificações |
| `ic_layers.svg` | Permissão de overlay |
| `ic_bolt.svg` | Badge "anti-procrastinação agressiva" |
| `ic_dumbbell.svg` | Onboarding de permissões |
| `ic_target.svg` | Estado vazio de sonhos |
| `ic_star.svg` | Prioridade dos sonhos |

## Uso na web
```html
<svg class="ic" viewBox="0 0 24 24"><path d="..."/></svg>
<!-- CSS: .ic { width: 1.05em; height: 1.05em; fill: currentColor; } -->
```

## Uso no Android (Compose)
```kotlin
Icon(
    painter = painterResource(R.drawable.ic_flame),
    contentDescription = null,
    tint = BrandRed,
    modifier = Modifier.size(24.dp)
)
```
