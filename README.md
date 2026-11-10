# Saúde Financeira

App Android nativo (Kotlin + Jetpack Compose) para organizar a vida financeira.

## Recursos
- **Dia a dia:** quanto você pode gastar hoje, lançamento rápido de gastos, prioridade das contas (o que pagar primeiro) e divisão do salário com reserva mínima.
- **Início:** sobra prevista da renda em dinheiro, percentual comprometido, projeção de parcelas e dica em destaque. VR e VM são acompanhados separadamente, sem inflar a sobra em dinheiro disponível para contas.
- **Evolução:** histórico mensal da sobra, com comparação com o mês anterior (começa a partir da versão que trouxe o recurso).
- **Para onde vai o dinheiro:** composição do mês (fixas, variáveis, parcelas e sobra) e os 5 maiores gastos, com % da renda.
- **Contas:** fixas (por tempo indeterminado) e variáveis (luz, água, gasolina, mercado) com histórico mensal, média e tendência.
- **Histórico de contas:** consulte, corrija ou inclua lançamentos de meses anteriores.
- **Relatórios:** escolha um intervalo, compare renda, gastos e sobra, e exporte CSV compatível com planilhas em português.
- **Faturas por cartão:** total de parcelas de cada cartão no mês atual e nos próximos, com a data da última parcela.
- **Guia de uso:** apresentação interativa na primeira abertura, com opção de rever em Ajustes.
- **Compras:** registro à vista, cartão, crediário, VR e VM; informe valor total ou por parcela, quantidade, parcela atual e cartão/loja. Acompanhe progresso, saldo, mês final e faturas por cartão.
- **Planejar:** dicas locais, metas, reserva de emergência e registro de dívidas, com saldo, juros informados, vencimento, negativação e parcela negociada. As orientações ajudam a comparar prioridades, sem consultar birôs ou credores.
- **Lembretes:** notificação diária (por volta das 9h) das contas com dia de vencimento, 5 e 2 dias antes e no dia do vencimento, conforme as opções selecionadas.
- **Backup:** exportar e restaurar em arquivo JSON (sem criptografia).
- **Atualização:** verifica a última Release do GitHub, baixa, confere o SHA-256 e abre o instalador do Android.
- **Ícone:** identidade adaptativa com moeda e gráfico de crescimento, em degradê verde-azul.

## Privacidade
Os dados ficam somente no aparelho. A internet é usada apenas para consultar e baixar atualizações neste repositório.

O acompanhamento de dívidas e a indicação de negativação dependem das informações preenchidas por você; o app não consulta credores nem birôs. VR/VM são acompanhados à parte e não são somados à sobra em dinheiro; prestações de dívidas entram no orçamento e no histórico. O histórico não é retroativo: contas variáveis e evolução mensal passam a ser registradas conforme o app é usado. Backups JSON não são criptografados; mantenha o arquivo em local seguro.

O atualizador consulta somente Releases publicadas no GitHub. APKs de teste da aba Actions não são tratados como atualizações automáticas.

## Instalação
Baixe o APK mais recente em [Releases](../../releases) e instale. Nas próximas versões, atualize pelo próprio app (Ajustes).

## Desenvolvimento
Kotlin 1.9.24, Compose BOM 2024.06.00, Room 2.6.1. O build, os testes e a publicação (APK assinado) rodam no GitHub Actions. Veja [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) para o que já está pronto e os próximos aprimoramentos.
