package rimidalv111.varifier.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class VerifyClientConnect extends Thread
{
	private ClientControllerThread instance;
	private Socket socket;
	private String emails;
	private boolean finished = false;
	private String threadID;
	
	public VerifyClientConnect(ClientControllerThread cct, String em, String id) throws Exception
	{
		instance = cct;
		emails = em;
		threadID = id;
		
		this.socket = new Socket(InetAddress.getLocalHost(), instance.getSmtpChecker().getServerPort());
		
		instance.getSmtpChecker().serverLog("\r\n[client] Connected to: " + this.socket.getInetAddress());
	}

	@Override
    public void run()
    {
	    try
	    {
			//listen for input
			String input;
			
			PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            InputStream is = this.socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
			while (!finished)
			{
				input = "[V] " + emails; //send batch input here email@domain.com,emadf@test.com,thisis@format.way,forbatch@checking.bulk
				
				out.println(input);
				out.flush();

		        //Get the return message from the server
	            String message = br.readLine();
	            
	            if(message.contains("%#%$")) //lets read the bulk results
	            {
	            	String stripEncoding = message.replace("%#%$", System.getProperty("line.separator"));
	                
//testing #######  	
//	            	Thread.sleep((1000 + (new Random()).nextInt(25999)));
	                
	            	instance.logClientResult(stripEncoding, threadID);
	                
	            	System.out.println("REPLY: " + stripEncoding);
	            	
	            } else
	            {
	            	//System.out.println("[R] " + message);
	            	instance.logClientResult(message, threadID);
	            	
	            	System.out.println("REPLY: " + message);
	            }
	            finished = true;
	            
			}
            
	    } catch (Exception io)
	    {
	    	//System.out.println("This thread was interupted, goodbye...");
	    	instance.clientErrorReQue(emails, threadID, io.toString()); //make sure we note that a thread was interrupted
	    	
	    }
	    
    }

	public String getEmails()
    {
    	return emails;
    }

	public void setEmails(String emails)
    {
    	this.emails = emails;
    }
}
