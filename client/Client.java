import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


public class Client implements ActionListener, KeyListener, MouseListener, WindowListener {
	private boolean connected = false;
	private String name = null;
		
	// Views
	private LoginView login = null;
	private ChatView chat = null;
	private Map<String, PrivateChatView> privateChats = new HashMap<String, PrivateChatView>();
	
	// Update timer
	private Timer timer = null;
	
	// Server
	private Socket socket = null;
	private BufferedReader serverInput = null;
	private PrintWriter serverOutput = null;
	private int timeouts = 0;
	private int pingTime = 0;
	
	// Event queues
	public ArrayList<String> usersJoined = new ArrayList<String>();
	public ArrayList<String> usersQuit = new ArrayList<String>();
	public ArrayList<Message> newMessages = new ArrayList<Message>();
	
	public Client() {
		login = new LoginView();
		login.setVisible(true);
		login.presenter = this;
		login.initialize();
		
		timer = new Timer(100, this);
		timer.start();
	}
	
	// Main update loop
	public synchronized void update() {
		//System.out.println("updating");
		onMessageReceived();
		onUserJoined();
		onUserQuit();
		
		// Listen for server commands
		if (socket != null && connected) {
			try {
				for (int n = 0; n < 10; ++n) {
				String m = serverInput.readLine();
				
				pingTime = 0;
				timeouts = 0;
				
				if (m.isEmpty()) {
					return;
				}
				
				String[] tokens = m.split(",", 2);
				String command = tokens[0];
				
				if (command.equals("ping")) {
					serverOutput.println("pong");
				}
				else if (command.equals("pong")) {
					timeouts = 0;
				}
				else if (command.equals("globalMessage")) {
					tokens = tokens[1].split(",", 2);
					String from = tokens[0];
					String message = tokens[1];
					
					Message msg = new Message();
					msg.from = from;
					msg.message = message;
					newMessages.add(msg);
				}
				else if (command.equals("privateMessage")) {
					tokens = tokens[1].split(",", 2);
					String from = tokens[0];
					String message = tokens[1];
					
					Message msg = new Message();
					msg.from = from;
					msg.message = message;

					PrivateChatView p = privateChats.get(from);

					// create a private chat window
					if (p == null) {
						p = new PrivateChatView();
						p.presenter = this;
						
						p.setVisible(true);
						p.setName(from);
						p.setUsername(from);
						
						p.initialize();
						
						privateChats.put(from, p);
					}
					
					p.setMessages(p.getMessages() + "\n" + from + "> " + message);
				}
				else if (command.equals("userOffline")) {
					String user = tokens[1];
					
					PrivateChatView p = privateChats.get(user);
					
					if (p != null) {
						p.setMessages(p.getMessages() + "\n" + "Error: User is offline");
					}
				}
				else if (command.equals("userConnected")) {
					String user = tokens[1];
					usersJoined.add(user);
				}
				else if (command.equals("userDisconnected")) {
					String user = tokens[1];
					usersQuit.add(user);
				}
				else if (command.equals("globalRecords")) {
					RecordsView r = new RecordsView();
					r.presenter = this;
					r.setVisible(true);
					r.initialize();
					
					String msg = tokens[1];
					
					String br = serverInput.readLine();
					
					while (!br.equals("globalRecordsEnd")) {
						msg += "\n" + br;
						
						br = serverInput.readLine();
						//System.out.println(br);
					}
					
					//System.out.println(msg);
					r.setMessages(msg);
				}
				else if (command.equals("privateRecords")) {
					tokens = tokens[1].split(",", 2);
					String other = tokens[0];
					String message = tokens[1];
					
					RecordsView r = new RecordsView();
					r.presenter = this;
					r.setVisible(true);
					r.name = other;
					r.initialize();
					
					String msg = message;
					
					String br = serverInput.readLine();
					
					while (!br.equals("privateRecordsEnd")) {
						msg += "\n" + br;
						
						br = serverInput.readLine();
					}
					
					r.setMessages(msg);
				}
				else if (command.equals("serverDisconnected")) {
					closeServer();
					
					restart();
					
					login.setErrorStatus("Server disconnected");
					return;
				} // for loop
				}
			} catch (SocketTimeoutException e) {
				++pingTime;

				// ping server
				if (pingTime >= 100) {
					if (timeouts >= 10) {
						closeServer();

						restart();
						
						login.setErrorStatus("Server timeout");
					}
					else {
						serverOutput.println("ping");
						++timeouts;
					}
					
					pingTime = 0;
				}
			} catch (IOException e) {
				closeServer();

				restart();
				
				login.setErrorStatus("Server timeout");
			}
		}
	}
	
	// Connect to the server
	// Returns true if connection if successful
	public boolean connect() {
		try {
			// parse file for ip address			
			File f = new File("ip.txt");
			String ip;
			
			if (!f.exists() || f.isDirectory()) {
				ip = "localhost";
			}
			else {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				ip = reader.readLine();
				reader.close();
				
				if (ip == null || ip.isEmpty()) {
					ip = "localhost";
				}
			}
			
			socket = new Socket(ip, 1300);
			socket.setSoTimeout(10000); // 10 seconds timeout
			
			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			serverOutput = new PrintWriter(socket.getOutputStream(), true);
		} catch (SocketTimeoutException e) {
			return false;
		} catch (UnknownHostException e) {
			socket = null;
			return false;
		} catch (IOException e) {
			socket = null;
			return false;
		}

		return true;
	}
	
	// Disconnect from the server
	public boolean disconnect() {
		connected = false;
		
		try {
			if (serverOutput != null) {
				serverOutput.println("disconnect");
				serverOutput.close();
				serverOutput = null;
			}
			
			if (serverInput != null) {
				serverInput.close();
				serverInput = null;
			}
			
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			serverInput = null;
			serverOutput = null;
			socket = null;
			return false;
		}

		return true;
	}
	
	public void restart() {
		connected = false;
		name = null;
			
		// Views
		login = null;
		
		chat.setVisible(false);
		chat = null;
		
		// close all private chat windows
		Iterator i = privateChats.entrySet().iterator();
		
		while (i.hasNext()) {
			Map.Entry p = (Map.Entry)i.next();
			PrivateChatView u = (PrivateChatView)p.getValue();
			
			u.setVisible(false);
		}
		
		privateChats = new HashMap<String, PrivateChatView>();
		
		// Server
		socket = null;
		serverInput = null;
		serverOutput = null;
		timeouts = 0;
		pingTime = 0;
		
		// Event queues
		usersJoined = new ArrayList<String>();
		usersQuit = new ArrayList<String>();
		newMessages = new ArrayList<Message>();
		
		// Initialize
		login = new LoginView();
		login.setVisible(true);
		login.presenter = this;
		login.initialize();
		
		timer.restart();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			update();
		}
		else if (e.getActionCommand().equals("Login")) {
			onLogin();
		}
		else if (e.getActionCommand().equals("Register")) {
			onRegister();
		}
		else if (e.getActionCommand().equals("Records")) {
			onRecords();
		}
		else if (e.getActionCommand().equals("Global")) {
			onGlobal();
		}
	}

	// login button
	public void onLogin() {
		String username = login.getUsername();
		String password = login.getPassword();
		
		// Username should only contain alphabets, capital alphabets, numbers and underscore
		if (!username.matches("[a-zA-Z_]+")) {
			login.setErrorStatus("Error: Invalid username. Username should only contain alphabets, numbers and/or underscores");
			return;
		}
		
		// check if empty username
		if (username == null || username.isEmpty()) {
			login.setErrorStatus("Error: Empty username");
			return;
		}

		// query server
		if (!connect()) {
			login.setErrorStatus("Connection timeout");
			return;
		}
		
		// message the server and wait for reply
		serverOutput.println("login," + username + "," + password);
		
		try {
			String result = serverInput.readLine();
			
			if (result.equals("alreadyConnected")) {
				login.setErrorStatus("Username already connected");
			}
			else if (result.equals("usernameNotFound")) {
				login.setErrorStatus("Username not found");
			}
			else if (result.equals("invalidPassword")) {
				login.setErrorStatus("Invalid password");
			}
			else if (result.equals("loginSuccess")) {
				// close login window and create chat window
				login.setVisible(false);
				login = null;
				
				chat = new ChatView();
				chat.setVisible(true);
				chat.presenter = this;
				chat.setUsername(username);
				chat.initialize();

				this.name = username;
				socket.setSoTimeout(10);
				connected = true;
				
				// get user list from server
				serverOutput.println("retrieveUsers");
			}
		} catch (IOException e) {
			login.setErrorStatus("Login timeout");
			
			closeServer();
		}
	}
	
	// register button
	public void onRegister() {
		String username = login.getUsername();
		String password = login.getPassword();
		
		//System.out.println("Name: " + username + " Password: " + password);
		
		// Username should only contain alphabets, capital alphabets, numbers and underscore
		if (!username.matches("[a-zA-Z_]+")) {
			login.setErrorStatus("Error: Invalid username. Username should only contain alphabets, numbers and/or underscores");
			return;
		}
		
		// check if empty username
		if (username == null || username.isEmpty()) {
			login.setErrorStatus("Error: Empty username");
			return;
		}
		
		if (!connect()) {
			login.setErrorStatus("Connection timeout");
			return;
		}

		// connection success
		serverOutput.println("register," + username + "," + password);
		
		try {
			String result = serverInput.readLine();
			
			if (result.equals("usernameExists")) {
				login.setErrorStatus("Username already exists");
			}
			else if (result.equals("registered")) {
				// success
				// login to the server
				login.setVisible(false);
				login = null;
				
				chat = new ChatView();
				chat.setVisible(true);
				chat.presenter = this;
				chat.initialize();
				
				this.name = username;
				socket.setSoTimeout(10);
				connected = true;
				
				// get user list from server
				serverOutput.println("retrieveUsers");
			}
		} catch (IOException e) {
			//System.out.println("Timeout");
			login.setErrorStatus("Register timeout");
			
			closeServer();
		}
	}
	
	// sending a message
	public void onMessage() {
		Message m = new Message();
		m.from = name;
		m.message = chat.getMessageInput();
		
		if (m.message == null || m.message.isEmpty()) {
			return;
		}
		
		chat.setMessages(chat.getMessages() + "\n" + m.from + "> " + m.message);
		chat.clearMessageInput();
		
		// message server
		serverOutput.println("globalMessage," + m.message);
	}
	
	// view records button
	public void onRecords() {
		String user = chat.getSelectedUser();
		
		if (user == null) {
			serverOutput.println("retrieveRecords");
			return;
		}
		
		// query server
		serverOutput.println("retrievePrivateRecords," + user);
	}
	
	// global button
	public void onGlobal() {
		//chat.deselectUser();
		serverOutput.println("retrieveRecords");
	}
	
	// new message event
	public void onMessageReceived() {
		while (!newMessages.isEmpty()) {
			//Message m = new Message();
			//m.from = name;
			//m.message = chat.getMessageInput();
			Message m = newMessages.remove(0);
			
			if (!m.from.equals(name)) {
				chat.setMessages(chat.getMessages() + "\n" + m.from + "> " + m.message);
			}
		}
	}
	
	// user joined event
	public void onUserJoined() {
		while (!usersJoined.isEmpty()) {

			String user = usersJoined.remove(0);
			DefaultListModel lm = (DefaultListModel)chat.getUserList();
			
			if (!lm.contains(user)) {
				lm.addElement(user);
			}
		}
	}
	
	// user quit event
	public void onUserQuit() {
		while (!usersQuit.isEmpty()) {
			((DefaultListModel)chat.getUserList()).removeElement(usersQuit.remove(0));
		}
	}
	
	// Close connection to the server
	private void closeServer() {
		try {
			serverInput.close();
			serverInput = null;
		} catch (IOException e) {
			serverInput = null;
		}
		
		serverOutput.close();
		serverOutput = null;

		try {
			socket.close();
			socket = null;
		} catch (IOException e) {
			socket = null;
		}
	}
	
	public void keyPressed(KeyEvent k) {
		if (((Component)k.getSource()).getName() == "passwordField" && login != null) {
			if (k.getKeyCode() == KeyEvent.VK_ENTER) {
				onLogin();
			}
		}
		else if (((Component)k.getSource()).getName() == "mainChatInput") {
			if (k.getKeyCode() == KeyEvent.VK_ENTER) {	
				onMessage();
			}
		}
		else if (((Component)k.getSource()).getName() == "privateChatInput") {
			PrivateChatView p = (PrivateChatView)SwingUtilities.getRoot((JTextField)k.getSource());

			if (k.getKeyCode() == KeyEvent.VK_ENTER) {	
				Message m = new Message();
				m.from = name;
				m.to = p.getUsername();
				m.message = p.getMessageInput();
				
				if (m.message == null || m.message.isEmpty()) {
					return;
				}
				
				p.setMessages(p.getMessages() + "\n" + m.from + "> " + m.message);
				p.clearMessageInput();
				
				// message server
				serverOutput.println("privateMessage," + m.to + "," + m.message);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (((Component)e.getSource()).getName() == "userList") {
			JList list = (JList)e.getSource();
			
			if (e.getClickCount() == 2) {
				PrivateChatView p = privateChats.get((String)list.getSelectedValue());

				if (p == null) {
					PrivateChatView c = new PrivateChatView();
					c.presenter = this;
					
					c.setVisible(true);
					c.setName((String)list.getSelectedValue());
					c.setUsername((String)list.getSelectedValue());
					
					c.initialize();
					
					privateChats.put((String)list.getSelectedValue(), c);
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (privateChats.containsValue(e.getWindow())) {
			privateChats.remove(e.getWindow().getName());
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getWindow() == chat ) {
			disconnect();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
