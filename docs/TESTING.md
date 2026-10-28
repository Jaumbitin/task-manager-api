# Validação da versão

## Automatizada

- Contexto Spring inicializa.
- Cadastro não retorna senha; senha persistida é um hash.
- Login, criação, listagem, edição, conclusão e exclusão.
- Uma conta não acessa nem altera tarefas de outra.
- E-mail duplicado, senha errada, campos vazios e tamanhos excedidos.
- Token ausente, inválido, expirado, sem claims e assinado por outra chave.
- Expiração independente do fuso local.
- CORS para origens configuradas.
- Contrato HTTP, FXML, busca e filtros no repositório desktop.

## Manual: aplicação JavaFX

1. Inicie a API e execute Launcher no desktop.
2. Cadastre uma conta e tente repetir o mesmo e-mail.
3. Entre com senha incorreta e depois com a correta.
4. Crie uma tarefa sem descrição e outra com descrição.
5. Edite título/descrição e altere status.
6. Conclua, reabra, filtre e busque por palavras.
7. Cancele uma exclusão; depois confirme a exclusão de uma tarefa de teste.
8. Saia e entre com outra conta: a lista deve ser independente.
9. Interrompa a API, clique em Atualizar e confira a mensagem de indisponibilidade.
10. Reinicie a API; atualize novamente.
11. Navegue com Tab, Enter e Escape. Redimensione a janela.

Os testes automatizados não substituem uma avaliação manual de usabilidade.
Evite usar contas ou tarefas importantes ao testar exclusão.
