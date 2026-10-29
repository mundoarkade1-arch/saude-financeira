# Saúde Financeira

App Android nativo (Kotlin + Jetpack Compose) para organizar a vida financeira.

## Recursos
- **Início:** sobra prevista do mês, % da renda comprometida, projeção das parcelas dos próximos 6 meses e dica em destaque.
- **Contas:** fixas (por tempo indeterminado) e variáveis (luz, água, gasolina, mercado) com histórico mensal, média e tendência.
- **Parcelas:** compras parceladas com parcela atual calculada automaticamente, progresso, valor restante e mês final.
- **Dicas:** regra 50/30/20, alerta de compromissos, reserva de emergência e contas em alta (calculadas no aparelho).
- **Metas:** metas de economia (como a reserva de emergência sugerida a partir dos seus gastos), com progresso e mês estimado para concluir.
- **Lembretes:** notificação diária (por volta das 9h) das contas com dia de vencimento, no dia ou com 1 ou 3 dias de antecedência.
- **Backup:** exportar e restaurar em arquivo JSON (sem criptografia).
- **Atualização:** verifica a última Release do GitHub, baixa, confere o SHA-256 e abre o instalador do Android.

## Privacidade
Os dados ficam somente no aparelho. A internet é usada apenas para consultar e baixar atualizações neste repositório.

## Instalação
Baixe o APK mais recente em [Releases](../../releases) e instale. Nas próximas versões, atualize pelo próprio app (Ajustes).

## Desenvolvimento
Kotlin 1.9.24, Compose BOM 2024.06.00, Room 2.6.1. O build, os testes e a publicação (APK assinado) rodam no GitHub Actions. Veja [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md).
