# Bot Minecraft — MC Companion

Projeto de um companheiro original para Minecraft Bedrock Android, acompanhado por um aplicativo Android auxiliar.

## Objetivo

Criar um personagem próprio dentro do mundo que possa acompanhar o jogador, explorar nas proximidades, defender seu dono e recolher itens. O aplicativo auxiliar oferece conversa com IA via OpenRouter e uma janela flutuante durante o jogo.

## Arquitetura

- **Behavior Pack:** lógica nativa da entidade no Bedrock. A autonomia básica deve continuar funcionando sem o aplicativo aberto.
- **Resource Pack:** aparência, geometria, texturas e identidade visual do personagem.
- **Android companion app:** interface flutuante e chat com IA. A conexão é feita diretamente do aparelho ao OpenRouter; não há VPS ou servidor intermediário.

## Limites técnicos

O app Android não recebe automaticamente o estado interno do mundo Bedrock e não controla diretamente a entidade. O chat funciona como camada de conversa; a movimentação, exploração, combate e coleta precisam ser implementados no add-on. A entidade só executa sua IA enquanto o mundo estiver carregado.

## Estado

Repositório inicializado. O trabalho de implementação ocorre em branches de feature; a branch `main` fica reservada para versões revisadas.

## Segurança

Nunca coloque chaves de API neste repositório. A chave OpenRouter deve ser inserida no próprio aplicativo e protegida localmente no Android Keystore.
