import java.awt.EventQueue;


public class Application {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//ChatView frame = new ChatView();
					//frame.setVisible(true);
					Client c = new Client();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
