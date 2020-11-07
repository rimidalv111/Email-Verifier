package rimidalv111.varifier.smtpserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import rimidalv111.varifier.server.SMTPChecker;

public class ControllerThread extends Thread
{
	protected ServerMultithreaded instance;
	protected Socket client = null;
	protected String data = null;

	public ControllerThread(ServerMultithreaded i, Socket clntSocket)
	{
		instance = i;
		this.client = clntSocket;
	}

	public void run()
	{
		try
		{
			String clientAddress = client.getInetAddress().getHostAddress();
			instance.getSMTPChecker().serverLog("\r\n[new connection] " + clientAddress);

			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			//send reply stream
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			
			while ((data = in.readLine()) != null)
			{
				instance.getSMTPChecker().serverLog("\r\n[message] " + clientAddress + ": " + data);
				if(data.contains("[V]"))
				{
					String email = data.replace("[V] ", "");

					if(email.contains(",")) //is this a bulk check?
					{
						out.println(runBulkChecker(email));
					} else
					{
						//start up our smtp check & reply our results for email
						MailSMTPVerify smtpCheckThread = new MailSMTPVerify();
						smtpCheckThread.start(); //start the verification
						
						out.println(encode(email,smtpCheckThread.checkEmail(email)));

						smtpCheckThread.interrupt(); //destroy completed thread
					}

					out.flush();
				}
			}
			
			out.close();
			in.close();
			
		} catch(Exception io)
		{
			instance.getSMTPChecker().serverLog("[error] Looks like the server is no longer listening");
			io.printStackTrace();
		}
	}
	
	public String encode(String status, String log)
	{
		return status + ":|:" + log;
	}
	
	private String runBulkChecker(String e) //bulk validation [V] email@do45main.com,test@dmfde.org,goodwork@washing.ton
	{
		String[] testData = e.split(",");
		
		String return_results = "";
		
		int count = 0; //make sure we return the right amount of results : preventive measure
		
		for(int i = 0; i < testData.length; i++)
		{
			String splitEncode = (i == (testData.length - 1)) ? "" : "%#%$"; //ignore last email encode
			String email = testData[i]; //grab email
			
			MailSMTPVerify smtpCheckThread = new MailSMTPVerify();
			smtpCheckThread.start(); //start the verification
			
			
			return_results = return_results + (encode(email,smtpCheckThread.checkEmail(email))) + splitEncode;
			
			smtpCheckThread.interrupt();
			
			count++;
		}

		if(testData.length != count)
		{
			instance.getSMTPChecker().serverLog("[count error] We wanted " + testData.length + " but we only counted " + count);
			return "Something went wrong, it looks like we lost a few results...";
		}
		
		return return_results;
	}
}
