import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;


public class User {
	public String name = null;
	public String password = null;
	public BufferedReader input = null;
	public PrintWriter output = null;
	public Socket socket = null;
	public HashMap<String, String> record = new HashMap<String, String>();
	public int timeouts = 0;
}
