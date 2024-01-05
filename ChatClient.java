import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatClient {
	// Variaveis relacionadas com a interface grafica * NAO MODIFICAR *
	JFrame frame = new JFrame("Chat Client");
	private JTextField chatBox = new JTextField();
	private JTextArea chatArea = new JTextArea();

	private Socket connection;
	private String lastReq[];
	private OutputStreamWriter outputSW;

	// Corrected the variable name here
	ChatClient chatClient;

	// Remove the socket declaration
	// private Socket socket;

	// Construtor
	public ChatClient(String server, int port) throws IOException {
		// Inicializacao da interface grafica * NAO MODIFICAR *
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(chatBox);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.SOUTH);
		frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
		frame.setSize(500, 300);
		frame.setVisible(true);
		chatArea.setEditable(false);
		chatBox.setEditable(true);
		chatBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					newMessage(chatBox.getText());
				} catch (IOException ex) {
				} finally {
					chatBox.setText("");
				}
			}
		});

		// Initialize the chatClient instance variable
		chatClient = this;

		connection = new Socket(server, port);
	}

	// Metodo a usar para acrescentar uma string a caixa de texto * NAO MODIFICAR *
	public void printMessage(final String message) {
		chatArea.append(message);
	}

	// Metodo invocado sempre que o utilizador insere uma mensagem na caixa de entrada
	public void newMessage(String message) throws IOException {
		if (message.split(" ").length == 0) return;
		lastReq = message.split(" ");
		outputSW = new OutputStreamWriter(connection.getOutputStream());
		BufferedWriter buffer = new BufferedWriter(outputSW);
		buffer.write(message + "\n");
		buffer.flush();
	}

	// Metodo principal do objecto
	public void run() throws IOException {
		while (true) {
			try {
				InputStream inputS = connection.getInputStream();
				InputStreamReader inputSR = new InputStreamReader(inputS);
				BufferedReader buffer = new BufferedReader(inputSR);
				String answer = buffer.readLine();
				if (answer != null) {
					processAnswer(answer);
					//chatClient.printMessage(answer);
					//processAnswer(answer);
				}
			}
			catch(IOException e) { continue; }
		}
	}

	private void  processAnswer(String answer) {
		String[] lastReq = this.lastReq;
		String cmd[] = answer.split(" ");
		String messages = "";

		if (cmd[0].equals("OK")) {
			switch (lastReq[0]) {
				case "/priv":
					String msg = Arrays.toString(Arrays.copyOfRange(lastReq, 2, lastReq.length)).replace(", ", " ").replace("[", "").replace("]", "");
					messages = "[Private message Sent] " + lastReq[1] + ": " + msg + "\n";
					break;
				case "/nick":
					messages = "--> Alteraste o teu nick para: [" + lastReq[1] + "].\n";
					break;
				case "/join":
					messages = "--> Entraste na sala: [" + lastReq[1] + "].\n";
					break;
				case "/leave":
					messages ="--> Saíste da sala.\n";
					break;
				case "/bye":
					messages = "--> Foste desconectado.\n";
			}
		} else if (cmd[0].equals("ERROR")) {
			switch (lastReq[0]) {
				case "/priv":
					messages = "--> Ocorreu um erro. Mensagem não enviada.\n";
					break;
				case "/nick":
					if (lastReq.length == 2) messages = "--> O nick [" + lastReq[1] + "] já está a ser usado.\n";
					else messages = "--> Comando inválido.\n";
					break;
				case "/join":
					if (lastReq.length == 2) messages = "--> Deves ter um nick para entrar numa sala.\n";
					else messages = "--> Comando inválido.\n";
					break;
				case "/leave":
					if (lastReq.length == 2) messages = "--> Já não estás em nenhuma sala.\n";
					else messages = "--> Comando inválido.\n";
					break;
				default:
					if (lastReq[0].charAt(0) == '/') messages = "--> Comando inválido.\n";
					else messages = "--> Deves ter um nick e pertencer a uma sala para enviar uma mensagem.\n";
			}
		} else if (cmd[0].equals("MESSAGE")) {
			String msg = Arrays.toString(Arrays.copyOfRange(cmd, 2, cmd.length)).replace(", ", " ").replace("[", "").replace("]", "");
			messages = cmd[1] + ": " + msg + "\n";
		} else if (cmd[0].equals("PRIVATE")) {
			String msg = Arrays.toString(Arrays.copyOfRange(cmd, 2, cmd.length)).replace(", ", " ").replace("[", "").replace("]", "");
			messages = "[PM Rec] " + cmd[1] + ": " + msg + "\n";
		} else if (cmd[0].equals("NEWNICK"))
			messages = "--> O utilizador [" + cmd[1] + "] alterou o seu nick para [" + cmd[2] + "].\n";
		else if (cmd[0].equals("JOINED"))
			messages = "--> O utilizador [" + cmd[1] + "] juntou-se à sala.\n";
		else if (cmd[0].equals("LEFT"))
			messages = "--> O utilizador [" + cmd[1] + "] saiu da sala.\n";
		else if (cmd[0].equals("BYE")) {
			messages = "--> Foste desconectado.\n";
			// Close the frame when BYE is received
			SwingUtilities.invokeLater(() -> ((JFrame) SwingUtilities.getRoot(chatArea)).dispose());
		}
		printMessage(messages);
		//chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	// Instancia o ChatClient e arranca-o invocando o seu método run() * NÃO MODIFICAR *
	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
		client.run();
	}
}
