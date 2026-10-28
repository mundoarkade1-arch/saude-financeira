# Saúde Financeira — Registro de continuidade

- **Objetivo:** app Android de controle financeiro pessoal (renda, contas fixas/variáveis, parcelas, dicas). Offline, sem conta, sem permissões.
- **Repositório:** https://github.com/mundoarkade1-arch/saude-financeira (branch `main`)
- **Stack:** Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00), Room 2.6.1 + KSP, MVVM, Navigation Compose. minSdk 26, targetSdk 34. AGP 8.5.2, Gradle 8.7 (instalado pelo workflow; sem wrapper).
- **Tema:** degradê #0FA39A (verde-azulado) → #2F6FDB (azul), claro/escuro.
- **Valores monetários:** sempre em centavos (Long).

## Implementado (etapa 1 — NÃO validado por build ainda)
- Estrutura Gradle, tema, banco Room (renda, contas, parcelas), cálculo de parcela atual, motor de dicas, tela Início (salário + resumo), tela Dicas, navegação inferior.
- Testes unitários: `InstallmentCalcTest`.
- Workflow `Build`: testes + APK debug como artefato.

## Secrets necessários (somente nomes)
- Nenhum nesta etapa. Etapa de assinatura: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Próximas tarefas
1. Confirmar que o workflow `Build` fica verde.
2. Telas Contas (fixas/variáveis com lançamento mensal e média) e Parcelas (item/valor/nº de parcelas/mês inicial, barra de progresso).
3. Assinatura release + publicação em Releases.
4. Exportar/importar backup.
5. Ajustes (tema, categorias).

## Problemas conhecidos
- Telas Contas, Parcelas e Ajustes são provisórias.
