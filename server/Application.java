import java.awt.EventQueue;


public class Application {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//ChatView frame = new ChatView();
					//frame.setVisible(true);
					Server s = new Server();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
