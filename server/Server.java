import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Server implements ActionListener, KeyListener, MouseListener, WindowListener {
	ChatView chat = null;
	
	ServerSocket serverSocket = null;
	Timer timer = null;
	
	int pingTime = 0;
	boolean pingUsers = false;
	
	//HashMap<String, User> users = new HashMap<String, User>();
	HashMap<String, User> onlineUsers = new HashMap<String, User>();
	ArrayList<User> disconnectedUsers = new ArrayList<User>();
	
	String record = new String();
	
	public Server() {
		chat = new ChatView();
		chat.setVisible(true);
		chat.presenter = this;
		chat.initialize();
		
		try {
			serverSocket = new ServerSocket(1300);
			serverSocket.setSoTimeout(10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		timer = new Timer(100, this);
		timer.start();
	}
	
	// Update loop
	public synchronized void update() {
		// Clear disconnected users
		while (disconnectedUsers.size() > 0) {
			User u = onlineUsers.remove(disconnectedUsers.remove(0).name);
			((DefaultListModel)chat.getUserList()).removeElement(u.name);
			saveUser(u);
		}

		try {
			// Wait for incoming new connections
			Socket s = serverSocket.accept();
			s.setSoTimeout(10000);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
			String m = reader.readLine();
			
			String[] tokens = m.split(",", 3);
			
			if (tokens[0].equals("login")) {
				String username = tokens[1];
				String password = tokens[2];

				// WIP
				User user = loadUser(username);
				
				if (user == null) {
					writer.println("usernameNotFound");
					return;
				}
				
				String pass = user.password;
				
				// Unregistered name
//				if (!users.containsKey(username)) {
//					writer.println("usernameNotFound");
//					return;
//				}
//				
//				String pass = users.get(username).password;
				
				// Check password
				if (!password.equals(pass)) {
					// return invalid password message
					writer.println("invalidPassword");
				}
				else {
					// Username and password valid
					if (onlineUsers.containsKey(username)) {
						// error
						writer.println("alreadyConnected");
					}
					else {
						// success
						user.input = reader;
						user.output = writer;
						user.socket = s;
						
						onlineUsers.put(username, user);
						writer.println("loginSuccess");
						
						broadcast("userConnected," + username);
						
						s.setSoTimeout(10);
						
						// Add user to list
						DefaultListModel lm = (DefaultListModel)chat.getUserList();
						
						if (!lm.contains(user.name)) {
							lm.addElement(user.name);
						}
					}
				}
			}
			else if (tokens[0].equals("register")) {
				String username = tokens[1];
				String password = tokens[2];
				
				// WIP
				User user = loadUser(username);
				
				if (user != null) {
					writer.println("usernameExists");
					return;
				}
				else {
					// success
					User u = new User();
					u.name = username;
					u.password = password;
					u.input = reader;
					u.output = writer;
					u.socket = s;
					
					//users.put(username, u);
					saveUser(u);

					onlineUsers.put(username, u);
					writer.println("registered");
					
					broadcast("userConnected," + username);
					
					s.setSoTimeout(10);
					
					// Add user to list
					DefaultListModel lm = (DefaultListModel)chat.getUserList();
					
					if (!lm.contains(u.name)) {
						lm.addElement(u.name);
					}
				}
			}
		} catch (SocketTimeoutException e) {
			++pingTime;

			// Check for commands from users
			Iterator i = onlineUsers.entrySet().iterator();
			
			// Check for user inputs
			while (i.hasNext()) {
				Map.Entry p = (Map.Entry)i.next();
				User u = (User)p.getValue();
				
				updateUser(u);

				// Handle ping timeout
				if (pingTime >= 100) {
					// Find timeout users
					if (u.timeouts >= 10) {
						disconnectedUsers.add(u);
					}
					else {
						u.output.println("ping");
					}
					
					pingTime = 0;
				}
			}
		} catch (IOException e) {
		}
	}
	
	// User commands
	public void updateUser(User user) {
		if (user == null) {
			return;
		}
		
		try {
			for (int n = 0; n < 10; ++n) {
			String m = user.input.readLine();
			
			user.timeouts = 0;
			
			if (m.isEmpty()) {
				return;
			}
			
			String[] tokens = m.split(",", 2);
			String command = tokens[0];
			
			if (command.equals("ping")) {
				user.output.println("pong");
			}
			if (command.equals("pong")) {
				user.timeouts = 0;
			}
			else if (command.equals("globalMessage")) {
				//System.out.println("globalMessage");
				String from = user.name;
				String message = tokens[1];
				
				broadcast("globalMessage," + from + "," + message);
				
				record += "\n" + from + "> " + message;
				
				chat.setMessages(chat.getMessages() + "\n" + from + "> " + message);
			}
			else if (command.equals("privateMessage")) {
				String from = user.name;
				tokens = tokens[1].split(",", 2);
				String to = tokens[0];
				String message = tokens[1];
				
				if (onlineUsers.containsKey(to)) {
					onlineUsers.get(to).output.println("privateMessage," + from + "," + message);
					
					User other = onlineUsers.get(to);
					
					if (!user.record.containsKey(other.name)) {
						user.record.put(other.name, new String());
					}
					
					if (!other.record.containsKey(user.name)) {
						other.record.put(user.name, new String());
					}
				
					user.record.put(other.name, user.record.get(other.name) + "\n" + from + "> " + message);
					other.record.put(user.name, other.record.get(user.name) + "\n" + from + "> " + message);
				}
				else {
					user.output.println("userOffline," + to);
				}
			}
			else if (command.equals("retrieveRecords")) {
				user.output.println("globalRecords," + loadGlobalRecord() + record);
				user.output.println("globalRecordsEnd");
			}
			else if (command.equals("retrievePrivateRecords")) {
				String precord = user.record.get(tokens[1]);
				
				if (precord != null) {
					user.output.println("privateRecords," + tokens[1] + "," + precord);
					user.output.println("privateRecordsEnd");
				}
			}
			else if (command.equals("retrieveUsers")) {
				Iterator i = onlineUsers.entrySet().iterator();
				
				while (i.hasNext()) {
					Map.Entry p = (Map.Entry)i.next();
					User u = (User)p.getValue();
					
					user.output.println("userConnected," + u.name);
				}
			}
			else if (command.equals("disconnect")) {
				broadcast("userDisconnected," + user.name);
				
				user.input.close();
				user.output.close();
				user.socket.close();
				user.input = null;
				user.output = null;
				user.socket = null;
				
				disconnectedUsers.add(user);
				
				return;
			} // for loop
			}
		} catch (SocketTimeoutException e) {
			if (pingTime >= 100) {
				++user.timeouts;
			}
		} catch (IOException e) {
			disconnectedUsers.add(user);
		}
	}
	
	// Broadcast command to all online clients
	// msg: The command to send
	private void broadcast(String msg) {
		Iterator i = onlineUsers.entrySet().iterator();
		
		while (i.hasNext()) {
			Map.Entry p = (Map.Entry)i.next();
			User u = (User)p.getValue();
			
			u.output.println(msg);
		}
	}
	
	public String loadGlobalRecord() {
		File f = new File("global.txt");
		
		if (!f.exists() || f.isDirectory()) {
			return "";
		}
		
		String r = new String();
		
		try {
			Scanner s = new Scanner(f);
			
			while (s.hasNextLine()) {
				r += s.nextLine();
				r += '\n';
			}
			
			s.close();
			
			return r;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public void saveGlobalRecord() {
		try {
			File f = new File("global.txt");
			PrintWriter writer;
			
			if (!f.exists() || f.isDirectory()) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			}
			else {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(f, true))); // append
			}
			
			if (!record.isEmpty()) {
				writer.println(record);
				record = new String();
			}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public User loadUser(String name) {
		File f = new File("users/" + name + ".xml");
		
		if (!f.exists() || f.isDirectory()) {
			return null;
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(f);
			
			Element root = document.getDocumentElement();
			
			User u = new User();
			u.name = new String(name);
			u.password = new String(root.getAttribute("password"));
			
			//System.out.println("Load: " + u.password);
			
			NodeList l = root.getElementsByTagName("Record");
			
			for (int i = 0; i < l.getLength(); ++i) {
				Element record = (Element)l.item(i);
				u.record.put(record.getAttribute("username"), record.getTextContent());
			}
			
			return u;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void saveUser(User user) {
		//File f = new File("users/" + user.name + ".xml");
		
		// make sure directory exists
		File d = new File("users");
		
		if (!d.exists() || !d.isDirectory()) {
			d.mkdir();
		}
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			//Document document = builder.parse(f);
			
			Document document = builder.newDocument();
			
			Element root = document.createElement("User");
			document.appendChild(root);
			
			root.setAttribute("username", user.name);
			root.setAttribute("password", user.password);
			
			//System.out.println("Save: " + user.password);
			
			Iterator i = user.record.entrySet().iterator();
			
			while (i.hasNext()) {
				Map.Entry u = (Map.Entry)i.next();
				String name = (String)u.getKey();
				String record = (String)u.getValue();

				Element precord = document.createElement("Record");
				
				precord.setAttribute("username", name);
				precord.setTextContent(record);
				root.appendChild(precord);
			}
			
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer transformer = tfactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File("users/" + user.name + ".xml"));
			transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	



	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getWindow() == chat) {
			broadcast("serverDisconnected");
			saveGlobalRecord();
			
			// save all users
			Iterator i = onlineUsers.entrySet().iterator();
			
			while (i.hasNext()) {
				Map.Entry p = (Map.Entry)i.next();
				User u = (User)p.getValue();
				
				saveUser(u);
			}
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

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
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
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			update();
		}
	}
}
