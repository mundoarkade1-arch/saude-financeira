# Saúde Financeira — Estado do projeto

App Android offline para controle financeiro pessoal. O código-fonte deste documento é a referência do que já está implementado; os arquivos de interface e este registro devem ser mantidos alinhados.

## O que já funciona

- **Início:** renda, compromissos, sobra prevista, projeção de parcelas para seis meses, maiores gastos, composição mensal e evolução histórica.
- **Contas:** cadastro, edição e exclusão de contas fixas e variáveis; vencimentos; lançamento mensal de variáveis; média e tendência.
- **Histórico de contas:** consulta e correção de meses anteriores; inclusão e exclusão de lançamentos sem apagar o valor do mês atual.
- **Relatórios:** intervalo selecionável, totais por mês e exportação CSV com separador `;` e decimais em vírgula.
- **Parcelas:** cadastro e edição de compras parceladas, cartão, progresso, saldo e mês final; visão de faturas por cartão.
- **Dicas e metas:** orientações geradas no aparelho, contas em alta, metas de economia e reserva de emergência sugerida.
- **Lembretes:** notificações configuráveis de vencimento, com antecedência escolhida pela pessoa usuária.
- **Ajustes:** exportação e restauração de backup JSON, lembretes e atualização pelo GitHub Releases com verificação SHA-256.
- **Privacidade:** os dados financeiros permanecem no aparelho. A conexão é usada para buscar atualizações.

## Verificações automatizadas

- Testes unitários cobrem exportação CSV, validação e compatibilidade de backups antigos e atuais.
- Testes instrumentados cobrem persistência Room e restauração transacional do backup; precisam de aparelho ou emulador Android.
- O workflow atual executa testes unitários e build debug, mas ainda não inicializa emulador para executar os testes instrumentados.

## Persistência e compatibilidade

- Kotlin 1.9.24, Jetpack Compose, Room 2.6.1, Navigation Compose e WorkManager.
- Valores financeiros são armazenados em centavos (`Long`).
- Banco Room v4, com migrações explícitas e sem descarte silencioso de dados.
- Histórico de contas variáveis começa quando o recurso é usado; não há dados retroativos. Ao abrir versões antigas, o valor atual das contas variáveis é registrado no mês corrente.
- Histórico de evolução também não é retroativo: os meses começam a ser registrados a partir da versão que introduziu o recurso.
- Backups antigos que não contêm metas ou evolução continuam compatíveis. O JSON não é criptografado.

## Próximos aprimoramentos

1. Habilitar um emulador Android no CI para executar os testes instrumentados e ampliar a cobertura das migrações.
2. Avaliar categorias personalizáveis e proteção opcional do backup com senha.
3. Considerar sincronização entre aparelhos como opção futura, mantendo o modo local como padrão.
4. Confirmar o build e a publicação de release assinada no GitHub Actions com os secrets de assinatura configurados.

Os itens acima são melhorias futuras, não telas que o app anuncia como prontas.
