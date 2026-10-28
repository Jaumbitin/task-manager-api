# Segurança e limites

- Senhas: BCrypt; cadastro exige mínimo de 8 caracteres e máximo de 72 bytes UTF-8.
- Resposta de login inválido uniforme; BCrypt também é executado para e-mail ausente.
- JWT: HMAC256, emissor verificado, expiração obrigatória de duas horas na emissão.
- Segredo: pelo menos 32 bytes; fornecido por ambiente ou arquivo local ignorado.
- Autorização: identidade do token e consultas de tarefas por proprietário.
- Dados inválidos retornam 400 antes da persistência.
- Logs SQL ficam desativados por padrão.
- O cliente mantém o token somente em memória.

## Antes de publicar

Use HTTPS, limitação de tentativas no login, usuário de banco com privilégios
mínimos e migrações de esquema. O modo Hibernate update é para desenvolvimento.
Sair do desktop apaga o token local, mas não revoga um token já copiado: ele
continua válido até expirar. Trocar a chave invalida todos os tokens emitidos.

Nunca versionar senha de banco, JWT_SECRET, tokens, arquivos locais ou logs.
Ignorar um arquivo só impede novos commits: não remove conteúdo do histórico.
Credenciais que já entraram no histórico precisam ser substituídas, mesmo após
a remoção do arquivo atual. Não reescreva histórico compartilhado nem faça
force-push sem coordenar com quem usa o repositório.

Não houve auditoria externa nem varredura completa de vulnerabilidades nas
dependências. Os testes verificam os cenários descritos em TESTING.md.
