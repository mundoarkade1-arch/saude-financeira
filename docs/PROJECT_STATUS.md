# Saúde Financeira — Registro de continuidade

- **Objetivo:** app Android de controle financeiro pessoal (renda, contas fixas/variáveis, parcelas, dicas). Offline, sem conta, sem permissões.
- **Repositório:** https://github.com/mundoarkade1-arch/saude-financeira (branch `main`)
- **Stack:** Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00), Room 2.6.1 + KSP, MVVM, Navigation Compose. minSdk 26, targetSdk 34. AGP 8.5.2, Gradle 8.7 (instalado pelo workflow; sem wrapper).
- **Tema:** degradê #0FA39A (verde-azulado) → #2F6FDB (azul), claro/escuro.
- **Valores monetários:** sempre em centavos (Long).

## Etapa 2 (validada: Build verde)
- Assinatura release via Secrets + Release automática (tag `v0.1.N`, `SHA256SUMS.txt`).

## Etapa 1 (validada)
- Estrutura Gradle, tema, banco Room (renda, contas, parcelas), cálculo de parcela atual, motor de dicas, tela Início (salário + resumo), tela Dicas, navegação inferior.
- Testes unitários: `InstallmentCalcTest`.
- Workflow `Build`: testes + APK debug como artefato.

## Secrets necessários (somente nomes)
- Nenhum nesta etapa. Etapa de assinatura: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Revisão de qualidade (entregue, aguardando build)
- Correção em `Money.parse`: "3.500" era lido como R$ 3,50; agora é R$ 3.500 (ponto como milhar). Também rejeita "1e5", negativos e valores que estourariam o Long.
- Testes: `MoneyTest`.

## Etapa 10 (entregue, validada): faturas por cartão e primeiros passos
- Cartão "Faturas por cartão" na tela Parcelas (agrupa por nome do cartão, ignora maiúsculas/espaços; mostra mês atual + 3 meses e última parcela).
- Guia "Primeiros passos" no Início enquanto faltarem salário ou contas.
- Sem mudança de banco. Testes: `CardInvoicesTest`.

## Etapa 9 (validada): histórico mensal
- Banco v4 (migração 3->4 cria `month_snapshots` com índice único year+month; dados preservados). Backup inclui o histórico (backups antigos continuam válidos).
- O ViewModel grava o resumo do mês atual a cada mudança dos números (ignora tudo zerado). Histórico começa na instalação desta versão; não há dados retroativos.
- Cartão "Evolução da sua sobra" no Início (últimos 6 meses + comparação com o mês anterior).
- Testes: `HistoryCalcTest`.

## Etapa 8 (validada): "Para onde vai seu dinheiro"
- Cartão na tela Início: barra de composição (fixas/variáveis/parcelas/sobra) e 5 maiores gastos com % da renda. Sem mudança de banco.
- Testes: `SpendingBreakdownTest`.

## Etapa 7 (validada): metas de economia
- Banco v3 (migração 2->3 cria `goals`; dados preservados). Backup inclui metas (backups antigos continuam válidos).
- Aba Dicas ganhou abas Dicas/Metas: criar/editar/excluir meta, guardar/retirar valor, reserva de emergência sugerida (6x gastos essenciais), prazo estimado pela sobra prevista.
- Testes: `GoalCalcTest`.

## Etapa 6 (validada): lembretes de vencimento
- WorkManager (work-runtime-ktx 2.9.0) agenda verificação diária ~9h; notifica contas com dueDay (hoje ou 1/3 dias antes), sem repetir o mesmo vencimento.
- Permissão POST_NOTIFICATIONS pedida só ao ativar. Ajustes: chave, antecedência e botão de teste.
- Parcelas não têm dia de vencimento, então não geram lembrete.
- Lógica de datas coberta por `ReminderLogicTest`; notificação/worker só testáveis no aparelho (botão de teste).

## Etapa 5 (validar build): versão completa
- Banco v2 (migração 1->2 cria `bill_entries`; dados preservados). Histórico mensal, média e tendência das contas variáveis.
- Edição de parcelas. Backup/restauração em JSON (validação antes de alterar o banco; transação).
- Dica "conta em alta" (>=15% acima da média anterior). Tela Ajustes completa.
- Pendência de teste: backup/restore não tem teste automatizado (org.json não roda em teste JVM sem dependência extra).

## Etapa 4 (validada): atualizador dentro do app
- Consulta `releases/latest` (repo público), compara versionCode (último número da tag), baixa o APK, confere SHA-256 do `SHA256SUMS.txt` e abre o instalador (usuário sempre confirma).
- Permissões novas: INTERNET, REQUEST_INSTALL_PACKAGES. FileProvider em cache/updates.
- Verificação automática ao abrir; aviso na tela Início; tela Ajustes com o fluxo.
- A primeira versão com atualizador precisa ser instalada manualmente.

## Etapa 3 (validada)
- UI modernizada (cards, degradê, barras de progresso, tema completo claro/escuro).
- Tela Contas (fixas/variáveis, adicionar/editar/excluir com confirmação).
- Tela Parcelas (valor total ou da parcela, parcela atual, progresso, término).
- Início: comprometimento da renda, projeção de 6 meses, dica em destaque.

## Próximas tarefas
1. Confirmar que o workflow `Build` fica verde.
2. Telas Contas (fixas/variáveis com lançamento mensal e média) e Parcelas (item/valor/nº de parcelas/mês inicial, barra de progresso).
4. Exportar/importar backup.
5. Ajustes (tema, categorias).

## Problemas conhecidos
- Parcelas ainda não têm edição (excluir e recriar).
- Contas variáveis guardam só o valor do mês atual (sem histórico/média ainda).
