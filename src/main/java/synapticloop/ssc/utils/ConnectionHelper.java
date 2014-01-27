package synapticloop.ssc.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConnectionHelper {
	static {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) { }

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) { return true; }
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	public static String getUrl(String urlString) {
		StringBuilder stringBuilder = new StringBuilder();
		try{
			URL url = new URL(urlString);
			HttpURLConnection connection= (HttpURLConnection)url.openConnection();
			connection.setAllowUserInteraction(true);
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);

			BufferedReader reader= new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String str;
			while ((str = reader.readLine()) != null) {
				stringBuilder.append(str);
			}
			connection.disconnect();
		}catch(Exception ex){
			System.out.println("FATAL (" + ConnectionHelper.class.getCanonicalName() + "), message was: " + ex.getMessage());
		}
		return(stringBuilder.toString());
	}
}
