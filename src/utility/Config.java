package utility;

public class Config {


	/**
	 * Load a configuration and initialized the object with the specified fileName
	 * @param fileName the fileName (path) for the configuration file (.properties)
	 */
	public Config(String fileName) {

	}

	public String get(String nodeName) {
		if (nodeName == "connectionString")
		{
//			return "jdbc:mysql://localhost/shoutout?rewriteBatchedStatement=true";
			return "jdbc:mysql://localhost:3306/mobile_push_sender?rewriteBatchedStatement=true";
		}
		else if (nodeName == "username")
		{
//			return "homestead";
			return "mpush_sender";
		}
		else if (nodeName == "password")
		{
//			return "secret";
			return "SENDERpush@dm1n";
		}

		return "";
	}

}