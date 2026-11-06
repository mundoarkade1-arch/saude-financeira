# Saúde Financeira — Estado do projeto

App Android offline para controle financeiro pessoal. O código-fonte deste documento é a referência do que já está implementado; os arquivos de interface e este registro devem ser mantidos alinhados.

## O que já funciona

- **Início:** renda, compromissos, sobra prevista, projeção de parcelas para seis meses, maiores gastos, composição mensal e evolução histórica.
- **Guia de uso:** tour interativo na primeira abertura, com progresso, opção de pular e acesso para rever em Ajustes.
- **Contas:** cadastro, edição e exclusão de contas fixas e variáveis; vencimentos; lançamento mensal de variáveis; média e tendência.
- **Histórico de contas:** consulta e correção de meses anteriores; inclusão e exclusão de lançamentos sem apagar o valor do mês atual.
- **Relatórios:** intervalo selecionável, totais por mês e exportação CSV com separador `;` e decimais em vírgula.
- **Compras:** cadastro à vista, cartão, crediário, VR/VM e parcelas; acompanhamento de cartão/loja, progresso, saldo, mês final e faturas.
- **Dicas, metas e dívidas:** orientações locais, contas em alta, metas/reserva e dívidas com prestações incluídas no orçamento, juros, vencimento, negativação e parcela negociada.
- **Lembretes:** notificações configuráveis de vencimento, 5 e 2 dias antes e no dia do vencimento, com seleção dos avisos desejados.
- **Ajustes:** exportação e restauração de backup JSON, lembretes e atualização pelo GitHub Releases com verificação SHA-256.
- **Atualização visível:** consulta ao abrir e ao voltar ao app (com intervalo mínimo de cinco minutos), estado e falhas visíveis na tela inicial; em Ajustes, explica que só Releases publicadas contam como atualização.
- **Privacidade:** os dados financeiros permanecem no aparelho. A conexão é usada para buscar atualizações.

## Verificações automatizadas

- Testes unitários cobrem exportação CSV, validação e compatibilidade de backups antigos e atuais.
- Testes instrumentados cobrem persistência Room e restauração transacional do backup; precisam de aparelho ou emulador Android.
- O workflow atual executa testes unitários e build debug, mas ainda não inicializa emulador para executar os testes instrumentados.

## Persistência e compatibilidade

- Kotlin 1.9.24, Jetpack Compose, Room 2.6.1, Navigation Compose e WorkManager.
- Valores financeiros são armazenados em centavos (`Long`).
- Banco Room v7, com migrações explícitas e sem descarte silencioso de dados. As prestações de acordos são incluídas no orçamento e no histórico.
- Histórico de contas variáveis começa quando o recurso é usado; não há dados retroativos. Ao abrir versões antigas, o valor atual das contas variáveis é registrado no mês corrente.
- Histórico de evolução também não é retroativo: os meses começam a ser registrados a partir da versão que introduziu o recurso.
- Backups antigos sem metas, evolução ou dívidas continuam compatíveis. O JSON não é criptografado.

## Próximos aprimoramentos

1. Habilitar um emulador Android no CI para executar os testes instrumentados e ampliar a cobertura das migrações.
2. Avaliar categorias personalizáveis e proteção opcional do backup com senha.
3. Considerar sincronização entre aparelhos como opção futura, mantendo o modo local como padrão. Dívidas e negativação são informadas pela pessoa; não há consulta automática a credores ou birôs.
4. Confirmar o build e a publicação de release assinada no GitHub Actions com os secrets de assinatura configurados.

Os itens acima são melhorias futuras, não telas que o app anuncia como prontas.
