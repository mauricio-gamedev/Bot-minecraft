# Teste no dispositivo — MC Companion 0.2.0

Este roteiro valida o comportamento dentro do Minecraft Bedrock Android. A validação do GitHub Actions confirma estrutura/JSON, sintaxe do script e empacotamento; não substitui o teste do runtime no jogo.

## Preparação

1. Faça uma cópia do mundo em que pretende testar. Não use seu mundo principal na primeira execução.
2. Importe MC-Companion-Bedrock-0.2.0.mcaddon.
3. Nas configurações da cópia do mundo, confira que Behavior Pack e Resource Pack do MC Companion 0.2.0 estão ativos.
4. Entre no mundo, invoque a entidade com o ovo ou com /summon mccompanion:buddy.
5. Domestique com maçã, pão, biscoito ou cenoura dourada.

## Menu de ordens

Depois de domesticar, deixe a mão vazia e interaja com a companheira. No controle por toque, deve aparecer a ação Ordens do MC Companion. O menu é restrito ao dono.

| Ordem | Teste | Resultado esperado |
| --- | --- | --- |
| Seguir você | Afaste-se por alguns blocos | A companheira retoma o acompanhamento |
| Explorar por perto | Fique parado por um tempo | A companheira passeia nas proximidades |
| Proteger esta área | Coloque um zumbi/skeleton nas proximidades | A companheira procura e ataca ameaças próximas |
| Ficar aqui | Afaste-se e observe; depois deixe um mob atacá-la ou ataque você | Ela não sai procurando monstros, mas reage a agressões contra ela ou o dono |
| Coletar itens | Jogue alguns blocos/itens no chão a poucos blocos de distância | Ela tenta recolher os itens e levá-los ao dono |

## App Android

O APK 0.2.0 acrescenta memória local cifrada das últimas 16 mensagens. Faça uma conversa, recolha/reabra a janela flutuante e confirme que o contexto anterior permanece. O botão Apagar memória da conversa deve limpar esse histórico.

Observação de assinatura: este APK é um build de depuração do GitHub Actions. Builds de depuração de execuções distintas podem usar certificados diferentes; por isso, a atualização por cima de uma instalação anterior pode ser recusada pelo Android. Não desinstale a versão que já funciona se precisar preservar a chave armazenada e o histórico. Primeiro vamos configurar uma assinatura persistente para atualizações normais.

## Quando algo falhar

- Se a ação de ordens não aparecer, confirme mão vazia e domesticação; envie captura de tela e a versão exata do Minecraft.
- Se o menu aparecer mas uma ordem não fizer efeito, teste cada ordem separadamente e envie a ordem selecionada e o que a entidade fez.
- Se a coleta falhar, teste com itens comuns no chão e mantenha o jogador próximo.
- Ative o Content Log no mundo de teste e envie a mensagem de erro do MC Companion, se houver.

## Limites conhecidos

- A coleta atual cobre itens derrubados; não minera, quebra blocos nem fabrica itens.
- O chat do app não lê o estado do mundo e não controla diretamente a entidade.
- A entidade continua executando sua IA enquanto seu mundo/chunk está carregado; o app auxiliar não mantém o Minecraft ativo depois de descarregado/fechado.