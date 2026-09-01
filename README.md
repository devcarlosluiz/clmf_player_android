# CLMF Player

Player IPTV nativo para Android (smartphones, tablets e Android TV/Google TV),
compatível com **Xtream Codes API**, construído em Kotlin + Jetpack Compose +
Media3/ExoPlayer, seguindo Clean Architecture + MVVM.

## Descrição

CLMF Player organiza canais ao vivo, filmes e séries de um provedor IPTV,
com cache offline (Room), configurações persistentes (DataStore), favoritos,
histórico com retomada de reprodução, busca global e um player profissional
com reconexão automática. Nenhum dado do usuário sai do dispositivo além das
chamadas ao servidor IPTV configurado pelo próprio usuário.

## Tecnologias

- Kotlin, Jetpack Compose, Material 3
- Media3 / ExoPlayer (HLS, DASH, MP4/TS)
- Hilt (injeção de dependência)
- Retrofit + OkHttp + kotlinx.serialization
- Room (cache offline) + DataStore (preferências) + Jetpack Security (senhas)
- Navigation Compose, Coroutines/Flow, WorkManager (preparado), androidx.tv

## Arquitetura

```
com.clmf.player
├── data          # DTOs, API Xtream, Room, DataStore, repositórios
├── domain        # Modelos e contratos de repositório (IPTVProvider, ContentRepository, ...)
├── presentation  # Navigation Compose + telas/ViewModels (splash, login, home, live, movies, series, player, favorites, search, settings)
├── player        # PlayerManager/PlayerState (Media3) desacoplado da UI, PlaybackService (MediaSessionService)
├── di            # Módulos Hilt (Network, Database, Repository)
└── utils         # AppLogger (sanitiza credenciais), AppError/AppResult, ErrorMapper
```

## Como executar

1. Abra a pasta `CLMFPlayer` no Android Studio (Ladybug ou mais recente).
2. O Android Studio detecta o `gradle/wrapper/gradle-wrapper.properties` e
   baixa/configura o Gradle automaticamente na primeira sincronização.
3. Rode a configuração `app` em um emulador, dispositivo físico ou
   Android TV/Google TV.

> **Nota de ambiente:** este projeto foi gerado em um ambiente sandboxed sem
> acesso a sockets de loopback, o que impede rodar `./gradlew` a partir deste
> assistente. O código não foi compilado automaticamente — abra no Android
> Studio e rode **Build > Make Project** para validar e corrigir eventuais
> erros de sincronização (eles tendem a ser de versão de dependência, não de
> lógica).

## Como gerar APK

```bash
./gradlew assembleDebug
```

O APK de debug fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Como gerar Release

Configure as variáveis de ambiente abaixo (nunca commitadas) antes de rodar
`./gradlew assembleRelease`:

```bash
export CLMF_KEYSTORE_PATH=/caminho/para/keystore.jks
export CLMF_KEYSTORE_PASSWORD=...
export CLMF_KEY_ALIAS=...
export CLMF_KEY_PASSWORD=...
```

## Configuração — Xtream Codes

Na tela de login, informe nome da conexão, servidor (`http://host:porta`),
usuário e senha. O app testa a conexão (`player_api.php`) antes de salvar; a
senha é criptografada com Jetpack Security (Android Keystore) e nunca é
gravada em texto puro nem aparece em logs.

## Configuração — M3U

A arquitetura já expõe `IPTVProvider` como abstração e inclui um
`M3uParser` (`data/remote/m3u/M3uParser.kt`) compatível com `#EXTINF`,
`tvg-id`, `tvg-name`, `tvg-logo` e `group-title`. Um `M3uProvider` pode
implementar `IPTVProvider` reaproveitando todo o restante do app (cache,
player, favoritos, histórico) sem alterações.

## Android TV

O manifesto inclui `LEANBACK_LAUNCHER` e `banner`; os componentes de UI usam
`onFocusChanged` para destacar foco de D-Pad. Recomenda-se testar em um
emulador Android TV para refinar a ordem de foco em telas com muitos itens.

## Estado atual / limitações conhecidas

Implementado: autenticação Xtream, Home offline-first, Live TV com
categorias/busca/favoritos, Filmes e Séries (com temporadas/episódios),
player Media3 com retry (1s/2s/5s) e troca de canal sem sair do player,
favoritos, histórico com retomada, busca global com debounce, configurações,
splash, tema dark com paleta azul/grafite, logs sanitizados, senha
criptografada.

Ainda não implementado (próximos passos sugeridos): EPG/XMLTV, importação
de playlist M3U via UI (parser já existe), múltiplos perfis simultâneos na
UI (o modelo de dados já suporta múltiplas conexões), WorkManager para sync
periódico em background, testes de UI (Compose/Espresso) e ícones de
launcher definitivos (placeholders vetoriais estão em uso).
