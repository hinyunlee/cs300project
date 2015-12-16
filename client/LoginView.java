import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.JPasswordField;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.Box;


public class LoginView extends JFrame {

	private JPanel contentPane;
	private JPasswordField passwordField;
	private JTextField usernameField;
	private JButton btnLogin;
	private JButton btnRegister;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JPanel panel;
	private JPanel panel_2;
	private JPanel panel_3;
	private Component verticalGlue;

	/**
	 * Create the frame.
	 */
	public LoginView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(5, 1, 0, 0));
		
		verticalGlue = Box.createVerticalGlue();
		contentPane.add(verticalGlue);
		
		panel = new JPanel();
		contentPane.add(panel);
		
		lblUsername = new JLabel("Username:");
		panel.add(lblUsername);
		
		usernameField = new JTextField();
		panel.add(usernameField);
		usernameField.setColumns(10);
		
		panel_2 = new JPanel();
		contentPane.add(panel_2);
		
		lblPassword = new JLabel("Password:");
		panel_2.add(lblPassword);
		
		passwordField = new JPasswordField();
		panel_2.add(passwordField);
		passwordField.setColumns(10);
		
		lblStatus = new JLabel("");
		contentPane.add(lblStatus);
		
		panel_3 = new JPanel();
		contentPane.add(panel_3);
		
		btnLogin = new JButton("Login");
		panel_3.add(btnLogin);
		
		btnRegister = new JButton("Register");
		panel_3.add(btnRegister);
	}

	public Client presenter = null;
	private JLabel lblStatus;
	
	public void initialize() {
		btnLogin.addActionListener(presenter);
		passwordField.addKeyListener(presenter);
		btnRegister.addActionListener(presenter);
		
		this.setTitle("ChatApp");
		
		btnLogin.setName("loginButton");
		passwordField.setName("passwordField");
	}
	
	public String getUsername() {
		return usernameField.getText();
	}
	
	public String getPassword() {
		return String.copyValueOf(passwordField.getPassword());
	}
	
	public void setErrorStatus(String m) {
		lblStatus.setText(m);
	}
}
