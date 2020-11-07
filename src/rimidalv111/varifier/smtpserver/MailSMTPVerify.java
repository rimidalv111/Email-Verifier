package rimidalv111.varifier.smtpserver;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

public class MailSMTPVerify extends Thread
{
	//all smtp errors
	// http://www.serversmtp.com/en/smtp-error
//	private static ArrayList<Integer> whitelisted = new ArrayList<Integer>()
//	{
//		private static final long serialVersionUID = 1L;
//
//		{
//			add(211);
//			add(250);
//			add(220);
//			add(221);
//			add(251);
//			add(354);
//			add(214);
//		}
//	};

	
	private static String[] hear(BufferedReader in) throws IOException
	{
		String line = null;
		String[] res = new String[2];

		System.out.println("---  reply  ---");
		while ((line = in.readLine()) != null)
		{
			System.out.println(line);
			String pfx = line.substring(0, 3); //grab the reply code
			
			res[0] = pfx;
			res[1] = line;

			if(line.charAt(3) != '-')
				break;
		}

		return res;
	}
	
	private static void say(BufferedWriter wr, String text) throws IOException
	{
		System.out.println("--> [ASKING] : " + text);
		wr.write(text + "\r\n");
		wr.flush();

		return;
	}

	private static ArrayList getMX(String hostName) throws NamingException
	{
		// Perform a DNS lookup for MX records in the domain
		Hashtable env = new Hashtable();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[]
		{ "MX" });
		Attribute attr = attrs.get("MX");

		// if we don't have an MX record, try the machine itself
		if((attr == null) || (attr.size() == 0))
		{
			attrs = ictx.getAttributes(hostName, new String[]
			{ "A" });
			attr = attrs.get("A");
			if(attr == null)
				throw new NamingException("No match for name '" + hostName + "'");
		}

		// Huzzah! we have machines to try. Return them as an array list
		// NOTE: We SHOULD take the preference into account to be absolutely
		//   correct. This is left as an exercise for anyone who cares.
		ArrayList res = new ArrayList();
		NamingEnumeration en = attr.getAll();

		try
		{
			while (en.hasMore())
			{
				String x = (String) en.next();
				String f[] = x.split(" ");
				if(f[1].endsWith("."))
					f[1] = f[1].substring(0, (f[1].length() - 1));
				res.add(f[1]);
			}
		} catch (Exception io)
		{
			throw new NamingException("No records found '" + hostName + "'");
		}
		return res;
	}

	private static String isAddressValid(String address)
	{
		//##LOG##
		String log = "";
		//#######
		
		
		// Find the separator for the domain name
		int pos = address.indexOf('@');

		// If the address does not contain an '@', it's not valid
		if(pos == -1)
		{
			log = "address does not contain @";
			return encode("bad", log);
		}

		// Isolate the domain/machine name and get a list of mail exchangers
		String domain = address.substring(++pos);
		ArrayList mxList = null;
		try
		{
//#### TESTING
//			Thread.sleep((1000 + (new Random()).nextInt(45999))); //testingsleep
			
			mxList = getMX(domain);
		} catch(Exception ex)
		{
			log = "could not find any mx records";
			return encode("bad", log);
		}

		// Just because we can send mail to the domain, doesn't mean that the
		// address is valid, but if we can't, it's a sure sign that it isn't
		if(mxList.size() == 0)
		{
			log = "no mx records";
			return encode("bad", log);
		}

		// Now, do the SMTP validation, try each mail exchanger until we get
		// a positive acceptance. It *MAY* be possible for one MX to allow
		// a message [store and forwarder for example] and another [like
		// the actual mail server] to reject it. This is why we REALLY ought
		// to take the preference into account.
		for(int mx = 0; mx < mxList.size(); mx++)
		{
			boolean valid = false;
			try
			{
				int res;
				Socket skt = new Socket((String) mxList.get(mx), 25);
				BufferedReader rdr = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				BufferedWriter wtr = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));

				res = Integer.parseInt(hear(rdr)[0]);

				if(res != 220)
				{
					rdr.close();
					wtr.close();
					skt.close();
					log = "deformed smtp header";
					throw new Exception("Invalid header");
				}
				say(wtr, "EHLO vultr.com");

				res = Integer.parseInt(hear(rdr)[0]);
				if(res != 250)
				{
					rdr.close();
					wtr.close();
					skt.close();
					log = "invalid reply to hello";
					throw new Exception("Not ESMTP");
				}

				// validate the sender address  
				say(wtr, "MAIL FROM: <james@vultr.com>");
				res = Integer.parseInt(hear(rdr)[0]);
				
				if(res != 250)
				{
					rdr.close();
					wtr.close();
					skt.close();
					log = "email check@vultr.com was rejected";
					throw new Exception("Sender rejected");
				}

				say(wtr, "RCPT TO: <" + address + ">");
				
				String[] finalReply = hear(rdr);
				res = Integer.parseInt(finalReply[0]);
				log = finalReply[1];

				// be polite
				say(wtr, "RSET");
				hear(rdr);
				say(wtr, "QUIT");
				hear(rdr);
				if(res != 250)
				{
					rdr.close();
					wtr.close();
					skt.close();
					log = "address is not valid";
					throw new Exception("Address is not valid!");
				}

				valid = true;
				rdr.close();
				wtr.close();
				skt.close();
			} catch(Exception ex)
			{
				// Do nothing but try next host
				log = "could not connect to mx servers (exception thrown)";
			} finally
			{
				if(valid)
				{
					return encode("ok", log);
				}
			}
		}
		return encode("bad", log);
	}
	
	private static String encode(String status, String log)
	{
		return status + ":|:[" + log + "]"; 
	}
	
	public String checkEmail(String email)
	{
		//System.out.println("result:  " + email + "  -  status=" + isAddressValid(email));
		//return "false-test [something happened]";//isAddressValid(email) + " [" + log + "]";
		return isAddressValid(email);
	}
}
