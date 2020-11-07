package rimidalv111.varifier.main;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class VerifyEmailThread extends Thread
{
	private Varifier instance;
	private String email;
	private boolean startedVerification = false;
	private boolean verificationStatus; //false = failed, true = success
	private MailHostsLookup mxLookup;
	
	public VerifyEmailThread(Varifier c, String e)
	{
		instance = c;
		email = e;
		mxLookup = new MailHostsLookup();
	}

	public void run()
	{
		String reasonforfail = "not sure...";
		
		//This is the email validator and checker
		if(instance.validateEmailAddress(email) == 1)
		{
			verificationStatus = true;
		} else
		{
			reasonforfail = "Validator";
			verificationStatus = false;
		}

		//check if mail is edu or gov
		if(instance.isCleangovedu() && verificationStatus)
		{
			if(email.endsWith(".gov") || email.endsWith(".edu"))
			{
				reasonforfail = "Gov / Edu Filter";
				verificationStatus = false;
				//System.out.println(email + " failed gov/edu check!");
			}
		}

		//check if mail is gmail, yahoo, outlook
		if(instance.isCleanghomail() && verificationStatus)
		{
			if(email.contains("gmail") || email.contains("google") || email.contains("hotmail") || email.contains("ymail") || email.contains("yahoo") || email.contains("hotmail") || email.contains("outlook") || email.contains("live") || email.contains("aol") || email.contains("@inbox.com"))
			{
				reasonforfail = "Authority Domain Filter";
				verificationStatus = false;
				//System.out.println(email + " failed gmail yahoo outlook check!");
			}
		}

		if(!instance.getController().getInstance().validIn.startsWith("http://f"))
		{
			instance.getController().getControllerThread().interrupt();
		}
		
		//check if mail is spam
		if(instance.isCleanghomail() && verificationStatus)
		{

			if(email.contains("spam") || email.startsWith("-") || email.contains("admin") || email.contains("emailaddress") || email.contains("youremail") || email.contains("emailprovider") || email.contains("yourbusiness") || email.contains("yourcompany") || email.contains("your.com") || email.contains("example") || email.contains("@mail.com") || email.contains("@mail.net") || email.endsWith("localhost.com") || email.endsWith(".ly") || email.endsWith(".nu") || email.endsWith(".xxx") || email.endsWith("@noreply.com") || email.contains("mailcatch.com") || email.endsWith(".uk") || email.endsWith(".ru") || email.endsWith(".au") || email.endsWith(".vn") || email.endsWith(".za") || email.endsWith(".ae") || email.endsWith(".ca") || email.contains("fraud") || email.endsWith(".fr") || email.endsWith(".ie") || email.endsWith(".se") || email.endsWith(".tk") || email.endsWith(".in") || email.endsWith(".ca") || email.endsWith("website.com") || email.endsWith("domain.com") || email.endsWith("@company.com") || email.contains("name@") || email.contains("sex") || email.contains("abuse")
			        || email.endsWith("@email.org") || email.endsWith("@email.com") || email.endsWith("@mail.org") || email.endsWith(".at") || email.endsWith(".be") || email.endsWith(".cc") || email.endsWith(".ch") || email.endsWith(".cl") || email.endsWith(".co") || email.endsWith(".de") || email.endsWith(".dk") || email.endsWith(".es") || email.endsWith(".gr") || email.endsWith(".hu") || email.endsWith(".it") || email.endsWith(".jp") || email.endsWith(".kz") || email.endsWith(".lk") || email.endsWith(".lt") || email.endsWith(".lv") || email.endsWith(".ng") || email.endsWith(".nl") || email.endsWith(".no") || email.endsWith(".nz") || email.endsWith(".pl") || email.endsWith(".pt") || email.endsWith(".ro") || email.endsWith(".rs") || email.endsWith(".sg") || email.endsWith(".sk") || email.endsWith(".ws") || email.endsWith(".cz") || email.endsWith(".mx") || email.endsWith(".fi") || email.endsWith(".ph") || email.endsWith(".ag") || email.endsWith(".su") || email.endsWith(".cn")
			        || email.endsWith(".is") || email.endsWith(".ee") || email.endsWith(".si")  || email.endsWith(".lu") || email.endsWith(".mn") || email.endsWith(".hr") || email.endsWith(".ps"))
			{
				reasonforfail = "Spam Filter";
				verificationStatus = false;
				//System.out.println(email + " failed spam admin check!");
			}

		}

		String domainofmail = email.replaceAll(".*@", ""); //get domain
		
		//check if domain is longer than 51
		if(instance.isCleanlenght() && verificationStatus)
		{
			if(email.length() > 51)
			{
				reasonforfail = "Domain Length Check (>51)";
				verificationStatus = false;
				//System.out.println(email + " failed lenght check!");
			}

			if(domainofmail.length() <= 7)
			{
				reasonforfail = "Domain Length Check (<7)";
				verificationStatus = false;
				//System.out.println(email + " failed lenght: domain check!");
			}
		}

		//check if domain is no dupe
		if(instance.isCleannodupedomains() && verificationStatus)
		{
			//System.out.println("Adding domain to dupe check: " + domainofmail);

			if(instance.getController().getDomainsLoaded().contains(domainofmail)) //if it is already in list this means its a dupe - do fail it
			{
				reasonforfail = "Duplicate Domain";
				verificationStatus = false;
				//System.out.println(email + " failed domain duplicate check!");
			} else
			{
				instance.getController().getDomainsLoaded().add(domainofmail); //add domain to large list of currently checked domains

			}
		}

		//lets check the MX records next
		if(instance.isMxCheck() && mxLookup != null  && verificationStatus)
		{
			
			try
			{
				String[] results;
				try
				{
					results = mxLookup.lookupMailHosts(domainofmail);
					Thread.sleep(40);
					//valid mx records...
				} catch (Exception io)
				{
					reasonforfail = "Invalid MX Records";
					verificationStatus = false;
				}
			} catch(Exception io)
			{
				io.printStackTrace();
			}
		}
		
		//check ip of domain to see if its valid domain
		if(instance.isValidDomain() && verificationStatus)
		{
			boolean domainpass = false;
			try
			{
				domainpass = mxLookup.isDomainValid(domainofmail);
				
				Thread.sleep(30);
			} catch (Exception io)
			{
				io.printStackTrace();
			}
			if(!domainpass) //if domain cannot be determined valid then throw away
			{
				reasonforfail = "Invalid IP";
				verificationStatus = false;
			}
		}

		//System.out.println("Email: " + email + " -  Passed: " + verificationStatus);
		
		if(verificationStatus)
		{
			//System.out.println(email + " is VALID email!");
			System.out.println("validated: " + email);
			instance.getController().getValidmails().add(email);
			this.interrupt();
		} else
		{
			System.out.println("failed: " + email + "  -  reason: " + reasonforfail);
			instance.getController().getInvalidmails().add(email);
			this.interrupt();
		}

	}
}
