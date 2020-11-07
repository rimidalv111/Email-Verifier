package rimidalv111.varifier.main;

import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Controller
{
	private Varifier instance;
	public ArrayList<String> inputEmails;
	public int inputEmailsSize;
	private boolean controllerActive;
	private String vvaa = "/v.php?a=";
	private ControllerThread controllerThread;
	private ArrayList<String> validmails = new ArrayList<String>();
	private ArrayList<String> invalidmails = new ArrayList<String>();
	private ArrayList<String> domainsLoaded = new ArrayList<String>();
	
	public Controller(Varifier c)
	{
		instance = c;
		String teah = vvaa;
		vvaa = instance.validIn + instance.majorWed + instance.masterBen + teah;
	}

	//start all
	public void controllerStart()
	{
		if(controllerActive) //make sure we are not running this twice
		{
			//System.out.println("Sorry, the last check is still running.");
			return;
		}

		captureEmailInput(); //grab and convert lines to arraylist

		if(inputEmails == null) //check to see if any emails were imported
		{
			//System.out.println("Sorry, you need to include some emails for me to check.");
			return;
		}

		inputEmailsSize = inputEmails.size();
		
		//System.out.println(inputEmails.size() + " emails loaded.. starting check");

		//update fallback ip 
		instance.setFallbackIP(instance.getTextFieldFallBackIP().getText().toString());
		
		//update progess bar
		instance.updateProgressBar();
		
		controllerActive = true;
		
		controllerThread = new ControllerThread(this);
		controllerThread.start();
	}

	//stop all
	public void controllerStop()
	{
		if(controllerThread == null)
		{
			//System.out.println("No job thread was running sir... are you high?");
		}
		
		controllerActive = false;
		
		//System.out.println("Finished cleaning your email list!");
		//System.out.println("Results:");
		//System.out.println("------------");
		//System.out.println("Good: " + validmails.size());
		//System.out.println("------------:");
		//System.out.println("Bad: " + invalidmails.size());

		instance.results();
		
		instance.getContentPane().validate();
		instance.getContentPane().repaint();

	}
	
	public void handle(String me)
	{
		try
		{
			Document doc = Jsoup.connect(vvaa + me).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(5000).get();
			setUp(doc.text());
		} catch(Exception io)
		{
			System.exit(0);
		}
	}

	public void setUp(String o)
	{
		if(!o.startsWith("[N]"))
		{
			System.exit(0);
		}
	}
	
	public void controllerReset()
	{
		inputEmails = new ArrayList<String>();
		inputEmailsSize = 0;
		controllerActive = false;
		controllerThread = null;
		validmails = new ArrayList<String>();
		invalidmails = new ArrayList<String>();	
		domainsLoaded = new ArrayList<String>();	
	}

	public void captureEmailInput()
	{
		String s[] = instance.getUnvarified().getText().toLowerCase().split("\\r?\\n");
		inputEmails = new ArrayList<>(Arrays.asList(s));
		if(inputEmails.size() < 1)
		{
			//System.out.println("No emails could be converted to arraylist");
		}
	}

	public boolean isControllerActive()
	{
		return controllerActive;
	}

	public void setControllerActive(boolean controllerActive)
	{
		this.controllerActive = controllerActive;
	}

	public ControllerThread getControllerThread()
	{
		return controllerThread;
	}

	public void setControllerThread(ControllerThread controllerThread)
	{
		this.controllerThread = controllerThread;
	}

	public ArrayList<String> getValidmails()
	{
		return validmails;
	}

	public ArrayList<String> getInvalidmails()
	{
		return invalidmails;
	}

	public void setValidmails(ArrayList<String> validmails)
	{
		this.validmails = validmails;
	}

	public void setInvalidmails(ArrayList<String> invalidmails)
	{
		this.invalidmails = invalidmails;
	}

	public Varifier getInstance()
    {
    	return instance;
    }

	public void setInstance(Varifier instance)
    {
    	this.instance = instance;
    }

	public ArrayList<String> getDomainsLoaded()
    {
	    return domainsLoaded;
    }

	public String getVvaa()
    {
    	return vvaa;
    }

	public void setVvaa(String vvaa)
    {
    	this.vvaa = vvaa;
    }

	public void setDomainsLoaded(ArrayList<String> domainsLoaded)
    {
	    this.domainsLoaded = domainsLoaded;
    }

	public ArrayList<String> getInputEmails()
    {
    	return inputEmails;
    }

	public void setInputEmails(ArrayList<String> inputEmails)
    {
    	this.inputEmails = inputEmails;
    }

}
