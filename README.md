# Bot Minecraft — MC Companion

Companheiro original para **Minecraft Bedrock Android**, acompanhado por um aplicativo Android auxiliar.

## Objetivo

Criar uma companheira humanoide original que acompanhe o jogador, explore nas proximidades, defenda seu dono e recolha itens derrubados. O app auxiliar fornece conversa por IA em uma janela flutuante.

## Componentes

- packs/behavior_pack: IA nativa da entidade, coleta e menu de ordens.
- packs/resource_pack: geometria, textura pixel art e animações.
- android-app: app Android com janela flutuante, chat OpenRouter e memória local cifrada.
- scripts: validação dos pacotes e empacotamento do add-on.
- docs: arquitetura e checkpoints técnicos.

## Importar o add-on

Baixe o artefato **MC-Companion-Bedrock-0.2.0.mcaddon** do workflow **Validate Bedrock Packs** ou gere localmente:

    python scripts/validate_packs.py
    python scripts/package_mcaddon.py

Abra o arquivo .mcaddon no Minecraft e ative o Behavior Pack e o Resource Pack nas configurações do mundo. Para invocar por comando:

    /summon mccompanion:buddy

Também existe um ovo de invocação. Ofereça maçã, pão, biscoito ou cenoura dourada para domesticar.

## Comportamentos e ordens disponíveis

- **Seguir você:** acompanha o dono e pode se teleportar quando fica muito para trás.
- **Explorar:** passeia por perto sem seguir o dono constantemente.
- **Proteger esta área:** fica na região e procura monstros próximos para atacar.
- **Ficar aqui:** interrompe o acompanhamento e só reage quando o dono ou a própria entidade é atacado.
- **Coletar itens:** explora nas proximidades, recolhe itens derrubados e tenta entregá-los ao dono.

Depois de domesticar, deixe a mão vazia e interaja com a companheira. No controle por toque, o jogo exibe **Ordens do MC Companion**. O menu é restrito ao dono. Os pacotes passam por validação automática no GitHub Actions, mas o menu e os comportamentos da versão 0.2.0 ainda precisam ser confirmados em uma sessão real no dispositivo.

**Limite atual:** coleta itens que já estão no chão. Mineração autônoma, quebra de blocos, crafting, exploração de cavernas com planejamento e navegação completa como um segundo jogador ainda não estão implementados.

## App Android

O app envia solicitações HTTPS diretamente do telefone para https://openrouter.ai/api/v1/chat/completions, usando openrouter/free. Não usa VPS ou servidor intermediário. Digite uma chave OpenRouter nova diretamente no app; ela é cifrada localmente com AES-GCM e Android Keystore.

As últimas 16 mensagens da conversa são persistidas localmente com AES-GCM. Há um controle na tela principal para apagar essa memória.

O app oferece chat flutuante durante o jogo, mas **não mantém o Minecraft aberto**, não lê automaticamente o estado do mundo e não comanda diretamente a entidade. A autonomia básica está no Behavior Pack e funciona enquanto o mundo/chunk está carregado.

Para compilar o APK de depuração, execute o workflow **Build MC Companion AI**. O APK é disponibilizado como artefato do GitHub Actions.

## Segurança

Nunca coloque API keys, tokens, keystores ou dados pessoais no repositório. Uma chave que tenha sido exposta deve ser revogada e substituída.