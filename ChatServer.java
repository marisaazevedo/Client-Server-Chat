import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class ChatServer {
	// Buffer pré-alocado para a informação recebida
	static private final Charset charset = Charset.forName("UTF8");
	static private final CharsetDecoder decoder = charset.newDecoder();
	static private final ByteBuffer byteB = ByteBuffer.allocate(16384);

	// Descodificador do texto recebido (Assumir UTF-8)
	static private HashMap <SocketChannel, Client> serverClients = new HashMap <SocketChannel, Client> ();
	static private HashMap <String, ArrayList <Client>> serverRooms = new HashMap <String, ArrayList<Client>> ();

	static public void main(String args[]) throws Exception {
		// Obter a porta como argumento da linha de comandos
		int port = Integer.parseInt(args[0]);

		try {
			// Criar ServerSocketChannel em vez de ServerSocket
			ServerSocketChannel ssc = ServerSocketChannel.open();

			// Definir o ServerSocketChannel para non-blocking para se puder usar select
			ssc.configureBlocking(false);

			// Obter a Socket conectada a este canal e redirecioná-la para a listening port
			ServerSocket ss = ssc.socket();
			InetSocketAddress isa = new InetSocketAddress(port);
			ss.bind(isa);

			// Criar um novo Selector
			Selector selector = Selector.open();

			// Registar o ServerSocketChannel, para puder detectar conexões
			ssc.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Listening on port " + port);

			while (true) {
				// Verificar se houve actividade (Incoming Connection, Incoming Data ou Existing Connection)
				// Se não houver actividade, voltar a tentar
				if (selector.select() == 0) continue;

				        // Obter as chaves correspondentes à actividade detectada e processá-las uma a uma
				Set <SelectionKey> keys = selector.selectedKeys();
				Iterator <SelectionKey> it = keys.iterator();

				while (it.hasNext()) {
					// Obter chave que representa um dos bits da actividade I/O
					SelectionKey key = it.next();

					// Verificar tipo de actividade
					if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
						// Incoming Connection -> Registar esta Socket com o Selector para conseguir receber input
						Socket s = ss.accept();
						System.out.println("Got connection from " + s);

						SocketChannel sc = s.getChannel();
						serverClients.put(sc, new Client(null, null, sc));
						sc.configureBlocking(false);

						// Registar com o selector, para leitura
						sc.register(selector, SelectionKey.OP_READ);
					}
					else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
						SocketChannel sc = null;
						try {
							// Incoming Data numa conexão -> Processar
							sc = (SocketChannel)key.channel();
							boolean ok = processInput(sc);

							// Se a conexão não responder, removê-la do selector e fechá-la
							if (!ok) {
								key.cancel();

								Socket s = null;
								try {
									s = sc.socket();
									leaveRoom(serverClients.remove(sc));
									System.out.println("Closing connection to " + s);
									s.close();
								} catch(IOException ie) { System.err.println("Error closing socket " + s + ": " + ie); }
							}
						} catch(IOException ie) {
							// Em caso de exceção, remover este canal do selector
							key.cancel();
							try {
								sc.close();
								serverClients.remove(sc);
							} catch(IOException ie2) { System.out.println(ie2); }
							System.out.println("Closed " + sc);
							ie.printStackTrace();
						}
					}
				} keys.clear();
			}
		} catch(IOException ie) { System.err.println(ie); }
	}

  	// Ler mensagem da socket e imprimir
	static private boolean processInput(SocketChannel sc) throws IOException {
		// Ler mensagem para o buffer
		Client client = serverClients.get(sc);

		byteB.clear();
		sc.read(byteB);
		byteB.flip();

		// Se não houver data fechar a conexão
		if (byteB.limit() == 0) return false;

		// Descodificar e imprimir mensagem
		String msg = decoder.decode(byteB).toString();
		client.getBuffer().add(msg);

		if (msg.endsWith("\n")) {
			String concat = new String();
			for (String str : client.getBuffer()) concat += str;
			client.getBuffer().clear();

			for (String line : concat.split("\n")) {
				line += "\n";
				if (isCommand(line)) sendToOwner(client, cmdHandle(client, line));
				else if (client.getNick() == null) sendToOwner(client, "ERROR");
				else if (client.getRoom() == null) sendToOwner(client, "ERROR");
				else if (isEscape(line)) sendToAll(client, line.substring(0));
				else sendToAll(client, line);
			}
		}
		return true;
	}

	static boolean isCommand(String msg) {
		if (msg.charAt(0) == '/' && msg.charAt(1) != '/') return true;
		return false;
	}

	static boolean isEscape(String msg) {
		if (msg.charAt(0) == '/' && msg.charAt(1) == '/') return true;
		return false;
	}

	static private String cmdHandle(Client client, String cmd) {
		String cmdList[] = cmd.replace("\n", "").split(" ");
		switch (cmdList[0]) {
			case "/priv":
				String msg = Arrays.toString(Arrays.copyOfRange(cmdList, 2, cmdList.length)).replace(", ", " ").replace("[", "").replace("]", "");
				return privateMsg(client.getNick(), cmdList[1], msg);
			case "/nick":  return cmdList.length == 2 ? changeNick(client, cmdList[1]) : "ERROR";
			case "/join":  return cmdList.length == 2 ? joinRoom(client, cmdList[1]) : "ERROR";
			case "/leave": return cmdList.length == 1 ? leaveRoom(client) : "ERROR";
			case "/bye":   return cmdList.length == 1 ? logout(client) : "ERROR";
		}
		return "ERROR";
	}

	static private String privateMsg(String sender, String destin, String msg) {
		if (sender == null || sender.equals(destin)) return "ERROR";
		for (Client client : serverClients.values()) {
			if (client.getNick() != null && client.getNick().equals(destin)) {
				sendMsg(client, "PRIVATE " + sender + " " + msg + "\n");
				return "OK";
			}
		}
		return "ERROR";
	}

	static private String changeNick(Client client, String nick) {
		for (Client c : serverClients.values())
			if (c.getNick() != null && c.getNick().equals(nick)) return "ERROR";
		sendToOthers(client, "NEWNICK " + client.getNick() + " " + nick);
		client.setNick(nick);
		return "OK";
	}

	static private String joinRoom(Client client, String room) {
		if (client.getNick() == null) return "ERROR";
		leaveRoom(client);
		if (!serverRooms.containsKey(room)) serverRooms.put(room, new ArrayList <Client> ());
		serverRooms.get(room).add(client);
		client.setRoom(room);
		sendToOthers(client, "JOINED " + client.getNick());
		return "OK";
	}

	static private String leaveRoom(Client client) {
		if (client.getRoom() == null) return "ERROR";
		sendToOthers(client, "LEFT " + client.getNick());
		serverRooms.get(client.getRoom()).remove(client);
		client.setRoom(null);
		return "OK";
	}

	static private String logout(Client client) {
		leaveRoom(client);
		sendMsg(client, "BYE");
		Socket s = client.getSocket().socket();
		try {
			serverClients.remove(client.getSocket());
			System.out.println("Closing connection to " + s);
			s.close();
		} catch(IOException ie) { System.err.println("Error closing socket " + s + ": " + ie); }
		return "BYE";
	}

	static private void sendToAll(Client sender, String msg) {
		if (sender.getRoom() == null) return;
		for (Client client : serverRooms.get(sender.getRoom()))
			sendMsg(client, "MESSAGE " + sender.getNick() + " " + msg);
	}

	static private void sendToOthers(Client sender, String msg) {
		if (sender.getRoom() == null) return;
		for (Client client : serverRooms.get(sender.getRoom()))
			if (client != sender) sendMsg(client, msg + "\n");
	}

	static private void sendMsg(Client client, String msg) {
		byteB.clear();
		byteB.put(msg.getBytes());
		byteB.flip();
		while (byteB.hasRemaining()) {
			try { client.getSocket().write(byteB); }
			catch (IOException ie) { return; }
		}
	}

	static private void sendToOwner(Client client, String msg) {
		try {
			byteB.clear();
			byteB.put((msg + "\n").getBytes());
			byteB.flip();
			client.getSocket().write(byteB);
		} catch (IOException ie) { return; }
	}
}


class Client {
	private String nick;
	private String room;
	private SocketChannel socket;

	private ArrayList <String> buffer;

	Client(String nick, String room, SocketChannel socket) {
		this.nick = nick;
		this.room = room;
		this.socket = socket;
		this.buffer = new ArrayList <String> ();
	}

	public String getNick() { return nick; }

	public String getRoom() { return room; }

	public SocketChannel getSocket() { return socket; }

	public ArrayList <String> getBuffer() { return buffer; }

	public void setNick(String nick) { this.nick = nick; }

	public void setRoom(String room) { this.room = room; }
}
