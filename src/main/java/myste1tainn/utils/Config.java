package myste1tainn.utils;

import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class Config {

	private static HashMap<String, String> config;
	private static boolean hasInitializeLog4j = false;

	@SuppressWarnings("rawtypes")
	private static Class mainClass;

	public Config() {

	}

	public static void setMainClass(@SuppressWarnings("rawtypes") Class mainClass) {
		Config.mainClass = mainClass;
	}

	private static void initLog4j() {
		if (!hasInitializeLog4j) {
			// Load log4j.properties
			//
			CodeSource src = mainClass.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL url;
				try {
					System.out.println("Resource Directory : "+src.getLocation());
					url = new URL(src.getLocation(), "log4j.properties");

					System.out.println("log4j Directory : "+url);
					PropertyConfigurator.configure(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void initConfig(@SuppressWarnings("rawtypes") Class mainClass, String configFileName) {
		try {
			Config.mainClass = mainClass;
			Config.initLog4j();

			if (config == null) {
				config = new HashMap<String, String>();

				CodeSource src = Config.mainClass.getProtectionDomain().getCodeSource();
				if (src != null) {
					URL url;
					url = new URL(src.getLocation(), configFileName);
					Properties p = new Properties();
					p.load(url.openStream());

					Enumeration<Object> bundleKeys = p.keys();
					while (bundleKeys.hasMoreElements()) {
						String key = (String)bundleKeys.nextElement();
						String value = p.getProperty(key);
						config.put(key, value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void load(String configFileName) {
		try {
//			Config.initLog4j();

			if (config == null) {
				config = new HashMap<String, String>();
			}

			CodeSource src = mainClass.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL url;
				url = new URL(src.getLocation(), configFileName);
				Properties p = new Properties();
				p.load(url.openStream());

				Enumeration<Object> bundleKeys = p.keys();
				while (bundleKeys.hasMoreElements()) {
					String key = (String)bundleKeys.nextElement();
					String value = (String)p.getProperty(key);
					config.put(key, value);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return config.get(key);
	}
}