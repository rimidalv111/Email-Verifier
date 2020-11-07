package rimidalv111.varifier.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ClientControllerThread extends Thread
{
	private SMTPChecker smtpChecker;
	private int numberOfClients = 1;
	private int bulkSize = 3;
	private int serverPort = 8080;
	private ArrayList<String> emailsForVerification;
	private ArrayList<String> verifiedEmails; //the emails + log output
	private int initialEmailToCheckSize;

	private boolean shutdown = false;
	
	//how many clients currently running
	private int runningClients = 0;
	private HashMap<String,VerifyClientConnect> vClientMap = new HashMap<String,VerifyClientConnect>(); //[0]=email(s) [1]=results [2]=
	private HashMap<String,String> cClientTimeout = new HashMap<String,String>();
	
	
	public ClientControllerThread(SMTPChecker i, int noc, int bs)
	{
		smtpChecker = i;
		serverPort = smtpChecker.getServerPort();
		numberOfClients = noc;
		bulkSize = bs;
		smtpChecker.serverLog("[client] Thread conroller initiated");

		
		emailsForVerification = i.getEmailsforverification();//  ADD THIS WHEN DONE TESTING
		
//#### TESTING STUFF
//		emailsForVerification = new ArrayList<String>();
//		
//		for(int d = 0; d < 4043; d++)
//		{
//			emailsForVerification.add(("test@testme" + i +".com"));
//		}
		
		
		System.out.println("Clients have gotten request to start work on: " + emailsForVerification.size() +" emails");
		initialEmailToCheckSize = emailsForVerification.size();

		//initialize variable
		verifiedEmails = new ArrayList<String>();
	}

	public void startClientJobs()
	{
		smtpChecker.onClientCheckingStarted();
		
		if(emailsForVerification.size() <= 0)
		{
			smtpChecker.serverLog("[client] you need to filter some domains first, or you can paste them into validated box in main window...");
			stopClientJobs(false);
			return;
		}
		
		boolean queFinished = false; //only allow numberOfCLients running at once
		while(!queFinished && !shutdown)
		{

				try
				{
					for(int i = 0; i < numberOfClients; i ++) //only two clients running
					{
						
						if(shutdown) //we dont want this running when we shut this class down
						{
							break;
						}
						
						if(runningClients >= numberOfClients) //we only want certain amount of clients running at once so keep track
						{
							System.out.println("All clients are busy");
							Thread.sleep(3000);
							break; //break if we are running x amount of clients
						}
						
						String nextEmails = grabNextBulkEmails(); //grab our next emails and return none if we are out
						
					
						if(!nextEmails.equalsIgnoreCase("none")) //if we have no more emails so dont run anymore clients
						{
							
							String uniqueID = UUID.randomUUID().toString();
							
							VerifyClientConnect vclient = new VerifyClientConnect(this, nextEmails, uniqueID);
							vclient.start(); //we start check
							
							vClientMap.put(uniqueID, vclient);
							cClientTimeout.put(uniqueID, String.valueOf(System.currentTimeMillis())); //log when we started this client
							
							runningClients++; //keep track of what is running
							
							
							Thread.sleep(400); //rest before new client
							
							
							
						} else
						{
							//System.out.println("Map Size: " + vClientMap.size());
							if(vClientMap.size() <= 0)
							{
						
								System.out.println("Running finish");

								//finish this clients job
								queFinished = true;
								Thread.sleep(2000); //wait for all jobs to finish first
								stopClientJobs(true);
								break;
							}
						}
					}
					
				} catch (Exception io)
				{
					smtpChecker.serverLog("[client] Something went wrong when client was connecting to server..");
					io.printStackTrace();
				}
		}
	}
	
	//threadTimeoutCheck
	public void threadTimeOutCheck()
	{
		ArrayList<String> threadsToKill = new ArrayList<String>();
		
		for(String id : cClientTimeout.keySet())
		{
			String clientId = id;
			String clientStartTime = cClientTimeout.get(clientId);
			long currentTime = System.currentTimeMillis();
			
			if(currentTime >= Long.parseLong(clientStartTime) + ((bulkSize*3)*1000)) //multiply by 1000 to get milliseconds
			{
				//this thread is over due and needs to be shutdown and restarted
				System.out.println("Overdue Thread: " + clientId + " so ReQUEing...");
				
				if(vClientMap.get(clientId) != null)
				{
					VerifyClientConnect vcc = vClientMap.get(clientId);
					String emailsRaw = vcc.getEmails();
							
					vcc.interrupt(); //interrupt the old thread
					
					System.out.println("Interupted Thread: " + clientId + "  - Emails Grabbed: " + emailsRaw);
					
					vClientMap.remove(clientId); //remove the timed out old thread
					
					threadsToKill.add(clientId); //mark for removal
					
					//start new threads
					try
					{
						String newThreadID = UUID.randomUUID().toString(); //generate new id
						
						vcc = new VerifyClientConnect(this, emailsRaw, newThreadID);
						vcc.start(); //we start check
						
						vClientMap.put(newThreadID, vcc);
						cClientTimeout.put(newThreadID, String.valueOf(System.currentTimeMillis())); //log when we started this client
						
					} catch (Exception io)
					{
						runningClients--; //at least finish the clients if we failed
						System.out.println("Failed to restart thread on timeout...");
					}
					
				}
				
				
			}
		}
		
		for(String toKillID : threadsToKill)
		{
			cClientTimeout.remove(toKillID);
		}
	}
	
	
	
	

	private String grabNextBulkEmails()
	{
		if(emailsForVerification.size() <= 0)
		{
			return "none";
		}

		String nextBulk = "";

		int numberOfEmails = bulkSize;


			
			
		if(bulkSize >= emailsForVerification.size()) //we are requesting amount of emails that we do not have
		{
			numberOfEmails = emailsForVerification.size(); //update to emails we do have
		}

		String[] grabbedEmails = new String[numberOfEmails]; //store our emails in array

		for(int i = 0; i < numberOfEmails; i++)
		{

			String singleEmail = emailsForVerification.get(i);
			grabbedEmails[i] = singleEmail;

		}
		
		
		//remove emails from listtobeverified
		for(String e : grabbedEmails)
		{
			emailsForVerification.remove(e);
		}

		//format list
		for(int d = 0; d < grabbedEmails.length; d++)
		{
			if(d == (grabbedEmails.length - 1)) //this is last variable
			{
				nextBulk = nextBulk + grabbedEmails[d]; //dont add comma
			} else
				if(d >= 0) //this is first variable
				{
					nextBulk = nextBulk + grabbedEmails[d] + ","; //split with comma
				}
		}

		//System.out.println("Next BULK emails found: " + grabbedEmails.length + ", emails left: " + emailsForVerification.size());
		return nextBulk;
	}

	public void clientErrorReQue(String emails, String threadID, String reason) //the client has an error (maybe connection interrupted / times out?)
	{
		System.out.println("Looks like we lost thread: " + threadID + "  because " + reason);
		if(reason.contains("interupt")) //we shut down server
		{
			return;
		}
		//that is okay we will just reQue these emails!
		
		//first verify this was an actual thread we started
		if(vClientMap.containsKey(threadID))
		{
			vClientMap.remove(threadID); //remove the failed client job
			cClientTimeout.remove(threadID);
			
			try
			{
				String newThreadID = UUID.randomUUID().toString(); //generate new id
			
				VerifyClientConnect vclient = new VerifyClientConnect(this, emails, newThreadID);
				vclient.start(); //we start check
			
				vClientMap.put(newThreadID, vclient); //log our new client
				cClientTimeout.put(newThreadID, String.valueOf(System.currentTimeMillis())); //log when we started this client
				
				
			} catch (Exception io)
			{
				runningClients--; //at least finish the clients if we failed
				System.out.println("Exception when trying to start new jobs on client error");
			}
		
		} else
		{
			System.out.println("Well I guess we could not start a new thread after all because we never started thread: " + threadID);
		}
		
	}
	
	public ArrayList<String> getEmailsForVerification()
	{
		return emailsForVerification;
	}

	public ArrayList<String> getVerifiedEmails()
	{
		return verifiedEmails;
	}

	public void setEmailsForVerification(ArrayList<String> emailsForVerification)
	{
		this.emailsForVerification = emailsForVerification;
	}

	public void setVerifiedEmails(ArrayList<String> verifiedEmails)
	{
		this.verifiedEmails = verifiedEmails;
	}

	public void stopClientJobs(boolean finished)
	{
		shutdown = true; //shut this thread down
		
		//shutdown all verification clients
		for(String queid : vClientMap.keySet())
		{
			vClientMap.get(queid).interrupt(); //shutdown running clients if any
		}
		
		
		smtpChecker.onClientCheckingStopped(finished ? "Completed SMTP verification.." : "STOPPED");
		
		//save results:
		try
		{
			saveResults();
		} catch (Exception io)
		{
			smtpChecker.serverLog("[error] could not save your verified emails...");
		}
		
		smtpChecker.serverLog("[client] The clients have completed their SMTP verification jobs: Process: [" + verifiedEmails.size() + "] out of [" + initialEmailToCheckSize + "] emails verified");
		smtpChecker.serverLog("[client] Clients " + (finished ? "completed SMTP verification..." : "STOPPED") + " and forced to disconnect");
		System.out.println("[client] The clients have completed their SMTP verification jobs: Process: [" + verifiedEmails.size() + "] out of [" + initialEmailToCheckSize + "] emails verified");
	}


	public void logClientResult(String result, String threadID)
	{
		//result can be bulk or just one
		if(result.contains(System.getProperty("line.separator"))) //lets process the bulk results
		{
			String[] lines = result.split("\\r?\\n"); //split reply by new lines after encoding fix
			
			for(String bresults : lines) //read bulk results and update verified emails
			{
				verifiedEmails.add(decodeResult(bresults));
			}
			
			
			vClientMap.remove(threadID); //remove from tracking
			cClientTimeout.remove(threadID);
			
			runningClients--; //remove client from running because it has finished
			
//## pauses here on large checks
			
			System.out.println("[sat bulk report] currently running clients: " + runningClients + "  vclientcount: " + vClientMap.size());
			
			smtpChecker.updateStats(false); //update all of our stats
			
			//threadTimeOutCheck(); //check if any clients timed out?
			
			return;
		}
		
		verifiedEmails.add(decodeResult(result));

		//System.out.println("Verified Emails Size: " + verifiedEmails.size() + " result: " + decodeResult(result));

		runningClients--; //remove client from running because it has finished

		vClientMap.remove(threadID); //remove from tracking
		cClientTimeout.remove(threadID);
		
		System.out.println("[sat report] Running CLients: " + runningClients + "  vclientcount: " + vClientMap.size());
		
		smtpChecker.updateStats(false); //update all of our stats
	}

	public String decodeResult(String r)
	{
		return r.replace(":|:", ",");
	}

	@Override
	public void run()
	{
		startClientJobs();
	}

	public void saveResults() throws Exception
	{
		if(verifiedEmails == null)
		{
			//nothing to save
			return;
		}
		String buildFilePath = (new File(".")).getAbsolutePath();
		buildFilePath = buildFilePath.substring(0, buildFilePath.length() - 1) + "results-" + currentDateTime(true) + ".csv";
		smtpChecker.serverLog("[client] Saving latest results: " + buildFilePath);

		File file = new File(buildFilePath);
		//System.out.println("Saving: " + file.getAbsolutePath());

		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("Email,Status,Log: " + currentDateTime(false));
		bw.newLine();

		for(String response : verifiedEmails)
		{
			bw.write(response);
			bw.newLine();
		}

		bw.close();
		fw.close();
	}

	public String currentDateTime(boolean small)
	{
		long currentDateTime = System.currentTimeMillis();

		//creating Date from millisecond
		Date currentDate = new Date(currentDateTime);

		DateFormat df = null;
		if(small)
		{
			df = new SimpleDateFormat("MM-dd-HH-mm");
		} else
		{
			df = new SimpleDateFormat("MM-dd-yy@HH:mm");
		}

		return df.format(currentDate);
	}

	public SMTPChecker getSmtpChecker()
	{
		return smtpChecker;
	}

	public void setSmtpChecker(SMTPChecker smtpChecker)
	{
		this.smtpChecker = smtpChecker;
	}

	public int getInitialEmailToCheckSize()
	{
		return initialEmailToCheckSize;
	}

	public void setInitialEmailToCheckSize(int initialEmailToCheckSize)
	{
		this.initialEmailToCheckSize = initialEmailToCheckSize;
	}

	public int getRunningClients()
	{
		return runningClients;
	}

	public void setRunningClients(int runningClients)
	{
		this.runningClients = runningClients;
	}
}
