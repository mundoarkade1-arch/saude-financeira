# Saúde Financeira

App Android nativo (Kotlin + Jetpack Compose) para organizar a vida financeira.

## Recursos
- **Início:** sobra prevista do mês, % da renda comprometida, projeção das parcelas dos próximos 6 meses e dica em destaque.
- **Evolução:** histórico mensal da sobra, com comparação com o mês anterior (começa a partir da versão que trouxe o recurso).
- **Para onde vai o dinheiro:** composição do mês (fixas, variáveis, parcelas e sobra) e os 5 maiores gastos, com % da renda.
- **Contas:** fixas (por tempo indeterminado) e variáveis (luz, água, gasolina, mercado) com histórico mensal, média e tendência.
- **Histórico de contas:** consulte, corrija ou inclua lançamentos de meses anteriores.
- **Relatórios:** escolha um intervalo, compare renda, gastos e sobra, e exporte CSV compatível com planilhas em português.
- **Faturas por cartão:** total de parcelas de cada cartão no mês atual e nos próximos, com a data da última parcela.
- **Guia de uso:** apresentação interativa na primeira abertura, com opção de rever em Ajustes.
- **Parcelas:** compras parceladas com parcela atual calculada automaticamente, progresso, valor restante e mês final.
- **Dicas:** regra 50/30/20, alerta de compromissos, reserva de emergência e contas em alta (calculadas no aparelho).
- **Metas:** metas de economia (como a reserva de emergência sugerida a partir dos seus gastos), com progresso e mês estimado para concluir.
- **Lembretes:** notificação diária (por volta das 9h) das contas com dia de vencimento, no dia ou com 1 ou 3 dias de antecedência.
- **Backup:** exportar e restaurar em arquivo JSON (sem criptografia).
- **Atualização:** verifica a última Release do GitHub, baixa, confere o SHA-256 e abre o instalador do Android.
- **Ícone:** identidade adaptativa com moeda e gráfico de crescimento, em degradê verde-azul.

## Privacidade
Os dados ficam somente no aparelho. A internet é usada apenas para consultar e baixar atualizações neste repositório.

O histórico não é retroativo: contas variáveis e evolução mensal passam a ser registradas conforme o app é usado. Backups JSON não são criptografados; mantenha o arquivo em local seguro.

O atualizador consulta somente Releases publicadas no GitHub. APKs de teste da aba Actions não são tratados como atualizações automáticas.

## Instalação
Baixe o APK mais recente em [Releases](../../releases) e instale. Nas próximas versões, atualize pelo próprio app (Ajustes).

## Desenvolvimento
Kotlin 1.9.24, Compose BOM 2024.06.00, Room 2.6.1. O build, os testes e a publicação (APK assinado) rodam no GitHub Actions. Veja [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) para o que já está pronto e os próximos aprimoramentos.
