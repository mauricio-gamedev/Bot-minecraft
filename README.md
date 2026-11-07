# Bot Minecraft — MC Companion

Companheiro original para **Minecraft Bedrock Android**, acompanhado por um aplicativo Android auxiliar.

## Objetivo

Criar uma personagem humanoide original que acompanhe o jogador, explore nas proximidades, defenda seu dono e recolha itens derrubados. O app auxiliar fornece conversa por IA em uma janela flutuante.

## Componentes

- packs/behavior_pack: autonomia nativa da entidade (seguir, explorar, defender, combater, coletar e devolver itens).
- packs/resource_pack: geometria, textura pixel art e animações originais.
- android-app: app Android com janela flutuante e chat direto com OpenRouter.
- scripts: validação dos pacotes e empacotamento do add-on.
- docs: arquitetura e checkpoints técnicos.

## Importar o add-on

Baixe o artefato MC-Companion-Bedrock-0.1.0.mcaddon do workflow Validate Bedrock Packs ou gere localmente:

    python scripts/validate_packs.py
    python scripts/package_mcaddon.py

Abra o arquivo .mcaddon no Minecraft, ative o Behavior Pack e o Resource Pack nas configurações do mundo. Para invocar por comando:

    /summon mccompanion:buddy

Também existe um ovo de invocação. Ofereça maçã, pão, biscoito ou cenoura dourada para domesticar.

## O que já faz

- Explora por perto quando não está seguindo/defendendo.
- Segue seu dono após domesticação.
- Reage para defender o dono e ataca monstros (com filtro para não procurar creepers).
- Recolhe itens derrubados em seu inventário e tenta entregá-los ao dono.
- Possui 32 pontos de vida e ataque base de 5.

**Limite atual:** a coleta é de itens que já estão no chão. Mineração autônoma, quebrar blocos, crafting e navegação completa como um segundo jogador ainda não estão implementados.

## App Android

O app envia solicitações HTTPS diretamente do telefone para https://openrouter.ai/api/v1/chat/completions, usando openrouter/free. Não usa VPS ou servidor intermediário. Digite uma chave OpenRouter nova diretamente no app; ela é cifrada localmente com AES-GCM e Android Keystore.

O app consegue manter sua própria interface flutuante por um serviço em primeiro plano, mas **não mantém o Minecraft aberto**, não acessa automaticamente o estado do mundo e não comanda a entidade. A autonomia básica fica no Behavior Pack e roda enquanto o mundo/chunk está carregado.

Para compilar o APK de depuração, execute o workflow Build MC Companion AI. O APK é disponibilizado como artefato do GitHub Actions.

## Segurança

Nunca commit API keys, tokens, keystores ou dados pessoais. Uma chave que tenha sido exposta deve ser revogada e substituída.