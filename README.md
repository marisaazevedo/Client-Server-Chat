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
   
## Utilização, estados e transições:

<div>
<table style="border: 1px solid black;">
   
   <thead>
      <tr>
         <th style="border: 1px solid black;">Estado actual</th>
         <th style="border: 1px solid black;">Evento</th>
         <th style="border: 1px solid black;">Acção</th>
         <th style="border: 1px solid black;">Próximo estado</th>
         <th style="border: 1px solid black;">Notas</th>
      </tr>
   </thead>
   <tbody>
      <tr>
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code><i>nome</i></code> fica indisponível para outros utilizadores</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/join <i>sala</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>JOINED <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">entrou na sala <code><i>sala</i></code>; começa a receber mensagens dessa sala</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;">mantém o nome antigo</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>mensagem</i></code></td>
         <td style="border: 1px solid black;"><code>MESSAGE <i>nome mensagem</i></code> para todos os utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">necessário escape de / inicial, i.e., / passa a //, // passa a ///, etc.</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">mantém o nome antigo</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>NEWNICK <i>nome_antigo nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/join <i>sala</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala antiga<br><code>JOINED <i>nome</i></code> para os outros utilizadores na sala nova</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">entrou na sala <code><i>sala</i></code>; começa a receber mensagens dessa sala; deixa de receber mensagens da sala antiga</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/leave</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;">deixa de receber mensagens</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/bye</i></code></td>
         <td style="border: 1px solid black;"><code>BYE</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">utilizador fechou a conexão</td>
         <td style="border: 1px solid black;"><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/bye</i></code></td>
         <td style="border: 1px solid black;"><code>BYE</code> para o utilizador</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;">utilizador fechou a conexão</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>mensagem</i></code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;">mantém o estado</td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer</td>
         <td style="border: 1px solid black;">comando não suportado nesse estado</td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;">mantém o estado</td>
         <td style="border: 1px solid black;"></td>
      </tr>
   </tbody>
</table>
</div>



## Realizado por:

|Nome|Identificação|
|----|-------------|
| [Beatriz Marques de Sá](https://github.com/beatrizmsa) | up202105831 |
| [Francisco Rafael Lima Ribeiro](https://github.com/franciscoribeiro2003) | up202104797 |
| [Marisa Peniche Salvador Azevedo](https://github.com/marisaazevedo) | up202108624 |
