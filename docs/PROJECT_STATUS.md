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

## Etapa 3 (entregue, aguardando build)
- UI modernizada (cards, degradê, barras de progresso, tema completo claro/escuro).
- Tela Contas (fixas/variáveis, adicionar/editar/excluir com confirmação).
- Tela Parcelas (valor total ou da parcela, parcela atual, progresso, término).
- Início: comprometimento da renda, projeção de 6 meses, dica em destaque.

## Próximas tarefas
1. Confirmar que o workflow `Build` fica verde.
2. Telas Contas (fixas/variáveis com lançamento mensal e média) e Parcelas (item/valor/nº de parcelas/mês inicial, barra de progresso).
3. Assinatura release + publicação em Releases.
4. Exportar/importar backup.
5. Ajustes (tema, categorias).

## Problemas conhecidos
- Tela Ajustes é provisória.
- Parcelas ainda não têm edição (excluir e recriar).
- Contas variáveis guardam só o valor do mês atual (sem histórico/média ainda).
