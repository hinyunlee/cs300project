import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JTextField;


public class PrivateChatView extends JFrame {

	private JPanel contentPane;
	private JTextField messageInput;
	private JTextPane messages;
	private JScrollPane messagesScroll;
	
	private String name;

	/**
	 * Create the frame.
	 */
	public PrivateChatView() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		messages = new JTextPane();
		messages.setEditable(false);

		messagesScroll = new JScrollPane(messages);
		contentPane.add(messagesScroll, BorderLayout.CENTER);

		messageInput = new JTextField();
		contentPane.add(messageInput, BorderLayout.SOUTH);
		messageInput.setColumns(10);
	}

	public Client presenter = null;
	
	public void initialize() {
		this.addWindowListener(presenter);
		messageInput.setName("privateChatInput");
		messageInput.addActionListener(presenter);
		messageInput.addKeyListener(presenter);
		
		messages.setEditable(false);
		
		this.setTitle("Private chat with: " + name);
	}
	
	public void setUsername(String n) {
		name = n;
	}
	
	public String getUsername() {
		return name;
	}

	public String getMessageInput() {
		return messageInput.getText();
	}
	
	public void clearMessageInput() {
		messageInput.setText(new String());
	}
	
	public String getMessages() {
		return messages.getText();
	}
	
	public void setMessages(String messageList) {
		messages.setText(messageList);
	}
}
