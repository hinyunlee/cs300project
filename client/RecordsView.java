import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;


public class RecordsView extends JFrame {

	private JPanel contentPane;
	private JTextPane messages;
	private JScrollPane messagesScroll;

	/**
	 * Create the frame.
	 */
	public RecordsView() {
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
	}

	public Client presenter = null;
	public String name = null;
	
	public void initialize() {
		//this.addWindowListener(presenter);
		if (name != null) {
			this.setTitle("Chat record for: " + name);
		}
		else {
			this.setTitle("Chat record for global");
		}
	}
	
	public void setMessages(String m) {
		messages.setText(m);
	}
}
