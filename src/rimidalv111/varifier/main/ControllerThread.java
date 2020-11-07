package rimidalv111.varifier.main;

public class ControllerThread extends Thread
{
	private Controller cthread;
	public int running = 0; //how many threads running
	private int mailSize = 0;
	private int originalmailSize;

	public ControllerThread(Controller c)
	{
		cthread = c;
		mailSize = cthread.inputEmails.size();
		originalmailSize = mailSize;
	}

	public void run()
	{
		if(running <= mailSize) //how many threads do I want to run at once
		{
			System.out.println(running + "  <- running threads | emails size -> " + cthread.inputEmails.size());
			try
			{
				int runTogether = 10; //how many threads to run in for loop
				if(originalmailSize > 100) //our mail size is massive, lets run more at once
				{
					runTogether = 100;
				}

				//run 100 at a time to conserve thread errors

				for(int i = 0; i < runTogether; i++)
				{
					String nextma = cthread.inputEmails.get(0);
					cthread.inputEmails.remove(0);

					(new VerifyEmailThread((cthread.getInstance()), nextma)).start();

					Thread.sleep(100);

					running++;
				}

				System.out.println("runnin next");

				//update progress bar
				cthread.getInstance().increaseProgress(running);

				run();
			} catch(Exception io)
			{
				try
				{
					Thread.sleep(1000); //wait a little for all checks to complete (mx 800 millisecond check) but no need for this thread.sleep really
					cthread.getInstance().increaseProgress(running);
					
					//update progess bar
					cthread.getInstance().updateProgressBar();
					
					System.out.println("Reached end of the list");
					cthread.controllerStop();
				} catch(Exception ee)
				{
					ee.printStackTrace();
				}
				this.interrupt();
			}
		}

	}

}
