# Servidor e Cliente de Chat em Java

Este projeto consiste no desenvolvimento de um servidor de chat e de um cliente simples em Java, permitindo a comunicação entre utilizadores através de uma interface de linha de comandos.


## Instruções para Execução

Certifique-se de ter o Java Development Kit (JDK) instalado no seu sistema antes de prosseguir.

1. **Compilação dos Ficheiros Java:**
    Execute o seguinte comando no terminal para compilar os ficheiros Java do projeto:
    ```bash
    javac *.java
    ```
2. **Execução do Servidor de Chat:**
    Inicie o servidor de chat utilizando o comando abaixo. O número de porta (neste exemplo, "800") é opcional e pode ser alterado conforme necessário.
    ```bash
    java ChatServer 800
    ```

3. **Execução do Cliente de Chat:**
    Execute o cliente de chat utilizando o comando abaixo. O primeiro argumento é o endereço do servidor (neste exemplo, "localhost"), e o segundo argumento é o número da porta.

    ```bash
    java ChatCliente localhost 800
    ```

    Repita este passo pelo menos duas vezes para simular a comunicação entre dois utilizadores. Adicione mais instâncias do cliente conforme necessário para envolver mais utilizadores no chat.



## Estados e Transições

| Estado Atual | Evento | Ação | Próximo Estado | Notas |
|--------------|--------|------|----------------|-------|
| init         | /nick nome && !disponível(nome) | ERROR | init | - |
| init         | /nick nome && disponível(nome) | OK | outside | Nome fica indisponível para outros utilizadores |
| outside      | /join sala | OK para o utilizador | JOINED nome para os outros utilizadores na sala | Entrou na sala sala; começa a receber mensagens dessa sala |
| outside      | /nick nome && !disponível(nome) | ERROR | outside | Mantém o nome antigo |
| outside      | /nick nome && disponível(nome) | OK | outside | - |
| inside       | mensagem | MESSAGE nome mensagem para todos os utilizadores na sala | inside | Necessário escape de / inicial, i.e., / passa a //, // passa a ///, etc. |
| inside       | /nick nome && !disponível(nome) | ERROR | inside | Mantém o nome antigo |
| inside       | /nick nome && disponível(nome) | OK para o utilizador | NEWNICK nome_antigo nome para os outros utilizadores na sala | - |
| inside       | /join sala | OK para o utilizador | JOINED nome para os outros utilizadores na sala nova | Entrou na sala sala; começa a receber mensagens dessa sala; deixa de receber mensagens da sala antiga |
| inside       | /leave | OK para o utilizador | LEFT nome para os outros utilizadores na sala antiga | - |
| inside       | /bye | BYE para o utilizador | LEFT nome para os outros utilizadores na sala | Servidor fecha a conexão ao cliente |
| inside       | Utilizador fechou a conexão | LEFT nome para os outros utilizadores na sala | - | Servidor fecha a conexão ao cliente |
| Qualquer exceto inside | /bye | BYE para o utilizador | - | Servidor fecha a conexão ao cliente |
| Qualquer exceto inside | Utilizador fechou a conexão | - | - | Servidor fecha a conexão ao cliente |
| Qualquer exceto inside | Mensagem | ERROR | - | Mantém o estado |
| Qualquer exceto inside | Comando não suportado nesse estado | ERROR | - | Mantém o estado |

## Realizado por:

|Nome|Identificação|
|----|-------------|
| [Beatriz Marques de Sá](https://github.com/beatrizmsa) | up202105831 |
| [Francisco Rafael Lima Ribeiro](https://github.com/franciscoribeiro2003) | up202104797 |
| [Marisa Peniche Salvador Azevedo](https://github.com/marisaazevedo) | up202108624 |
