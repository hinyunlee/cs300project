import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Component;

import javax.swing.ListSelectionModel;

import java.awt.FlowLayout;

import javax.swing.SpringLayout;
import javax.swing.AbstractListModel;


import javax.swing.ListModel;
import javax.swing.DefaultListModel;

public class ChatView extends JFrame {

	private JPanel contentPane;
	private JTextField messageInput;
	private JButton btnGlobal;
	private JPanel usersPanel;
	private JButton btnRecords;
	private JLabel lblMessageType;
	private JLabel lblInputType;
	private JPanel messagesPanel;
	private JPanel inputPanel;
	private JPanel controlsPanel;
	private JLabel lblUsers;
	private JTextPane messages;
	private JList userList;
	private JScrollPane messagesScroll;
	private JScrollPane userListScroll;
	
	/**
	 * Create the frame.
	 */
	public ChatView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		controlsPanel = new JPanel();
		contentPane.add(controlsPanel, BorderLayout.NORTH);
		
		//btnGlobal = new JButton("Global");
		//controlsPanel.add(btnGlobal);
		
		//btnRecords = new JButton("Records");
		//controlsPanel.add(btnRecords);
		
		messagesPanel = new JPanel();
		contentPane.add(messagesPanel, BorderLayout.CENTER);
		SpringLayout sl_messagesPanel = new SpringLayout();
		messagesPanel.setLayout(sl_messagesPanel);
		
		lblMessageType = new JLabel("Global:");
		sl_messagesPanel.putConstraint(SpringLayout.NORTH, lblMessageType, 0, SpringLayout.NORTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.WEST, lblMessageType, 0, SpringLayout.WEST, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.SOUTH, lblMessageType, 10, SpringLayout.NORTH, messagesPanel);
		messagesPanel.add(lblMessageType);
		
		lblUsers = new JLabel("Users:");
		sl_messagesPanel.putConstraint(SpringLayout.EAST, lblMessageType, 0, SpringLayout.WEST, lblUsers);
		sl_messagesPanel.putConstraint(SpringLayout.WEST, lblUsers, 300, SpringLayout.WEST, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.NORTH, lblUsers, 0, SpringLayout.NORTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.SOUTH, lblUsers, 10, SpringLayout.NORTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.EAST, lblUsers, 0, SpringLayout.EAST, messagesPanel);
		messagesPanel.add(lblUsers);
		lblUsers.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		messages = new JTextPane();
		messages.setEditable(false);
		
		messagesScroll = new JScrollPane(messages);
		sl_messagesPanel.putConstraint(SpringLayout.NORTH, messagesScroll, 10, SpringLayout.NORTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.WEST, messagesScroll, 0, SpringLayout.WEST, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.SOUTH, messagesScroll, 0, SpringLayout.SOUTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.EAST, messagesScroll, 300, SpringLayout.WEST, messagesPanel);
		messagesPanel.add(messagesScroll);
		
		userList = new JList();
		userList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		userListScroll = new JScrollPane(userList);
		sl_messagesPanel.putConstraint(SpringLayout.NORTH, userListScroll, 10, SpringLayout.NORTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.WEST, userListScroll, 0, SpringLayout.EAST, messagesScroll);
		sl_messagesPanel.putConstraint(SpringLayout.SOUTH, userListScroll, 0, SpringLayout.SOUTH, messagesPanel);
		sl_messagesPanel.putConstraint(SpringLayout.EAST, userListScroll, 0, SpringLayout.EAST, messagesPanel);
		messagesPanel.add(userListScroll);
		
		usersPanel = new JPanel();
		contentPane.add(usersPanel, BorderLayout.EAST);
		usersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		inputPanel = new JPanel();
		contentPane.add(inputPanel, BorderLayout.SOUTH);
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		
		//lblInputType = new JLabel("Global:");
		//inputPanel.add(lblInputType);
		
		//messageInput = new JTextField();
		//inputPanel.add(messageInput);
		//messageInput.setColumns(10);
	}

	public Server presenter = null;
	
	public void initialize() {
		this.addWindowListener(presenter);
		//messageInput.setName("mainChatInput");
		//messageInput.addActionListener(presenter);
		//messageInput.addKeyListener(presenter);
		
		messages.setEditable(false);
		
		userList.setName("userList");
		userList.setModel(new DefaultListModel());
		userList.addMouseListener(presenter);
		
		// buttons
		//btnRecords.addActionListener(presenter);
		//btnGlobal.addActionListener(presenter);
		
		this.setTitle("ChatApp Server");
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
	
	public ListModel getUserList() {
		return userList.getModel();
	}
	
	public void setUserList(ListModel list) {
		userList.setModel(list);
	}
	
	public String getSelectedUser() {
		return (String)userList.getSelectedValue();
	}
	
	public void deselectUser() {
		userList.clearSelection();
	}
}
