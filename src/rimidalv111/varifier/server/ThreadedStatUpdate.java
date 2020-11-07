package rimidalv111.varifier.server;

public class ThreadedStatUpdate extends Thread
{
	private SMTPChecker instance;
	private int overrideProgress = -1;
	
	public ThreadedStatUpdate(SMTPChecker sc)
	{
		instance = sc;
	}

	//update the number of verified emails already
	public void updateCompletedVerification()
	{
		if(instance.getClientcontroller() == null)
		{
			return;
		}
		
		instance.getLblCompletedVerificationLableStat().setText("[V]: " + instance.getClientcontroller().getVerifiedEmails().size());
	}

	//update the number of emails waiting to be verified
	public void updateWaitingSMTPCheck()
	{
		if(instance.getClientcontroller() == null)
		{
			return;
		}
		
		instance.getLblLeftToCheckStat().setText("[C]: " + instance.getClientcontroller().getEmailsForVerification().size());
	}

	//update the progress bar
	public void updateProgressBar()
	{
		if(overrideProgress > 0)
		{
			instance.getProgressBar().setValue(overrideProgress);
			return;
		}
		
		if(instance.getClientcontroller() == null)
		{
			return;
		}

		double initial = instance.getClientcontroller().getInitialEmailToCheckSize();
		double verified = instance.getClientcontroller().getVerifiedEmails().size();

		double cleanInitial = ((initial == 0) ? 1 : initial); //avoid divide by 0

		double percentdoneratio = (verified / cleanInitial) * 100; //avoid divide by 0
		instance.getProgressBar().setValue((int) percentdoneratio);

		//System.out.println("Updating progress to: " + percentdoneratio + "  -  and initial was: " + cleanInitial + "  -  and verified: " + verified);

	}

	//repaint gui
	public void refreshGui()
	{
		//clear content pane
		instance.getContentPane().validate();
		instance.getContentPane().repaint();
	}

	@Override
	public void run()
	{
		try
		{
			updateCompletedVerification();
			Thread.sleep(200); //yes sleep to ease off resources!
		} catch(Exception io)
		{
			io.printStackTrace();
		}
		try
		{
			updateWaitingSMTPCheck();
			Thread.sleep(200); //yes sleep to ease off resources!
		} catch(Exception io)
		{
			io.printStackTrace();
		}

		try
		{
			updateProgressBar();
			Thread.sleep(200); //yes sleep to ease off resources!
		} catch(Exception io)
		{
			io.printStackTrace();
		}

		try
		{
			refreshGui();
			Thread.sleep(200); //yes sleep to ease off resources!
		} catch(Exception io)
		{
			io.printStackTrace();
		}

		try
		{
			refreshGui();
			Thread.sleep(200); //yes sleep to ease off resources!
		} catch(Exception io)
		{
			io.printStackTrace();
		}

		//gracefully die
		this.interrupt();
	}

	public int getOverrideProgress()
    {
	    return overrideProgress;
    }

	public void setOverrideProgress(int overrideProgress)
    {
	    this.overrideProgress = overrideProgress;
    }
}
