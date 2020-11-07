package rimidalv111.varifier.main;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class BlacklistChecker extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Varifier instance;
	private BlacklistChecker frame;
	private JTextArea checked;
	private JTextArea blacklistdomainstextarea;
	private JTextArea verifiedvalidated;
	private JProgressBar progressBar;
	private ArrayList<String> blacklistDomains = new ArrayList<String>();
	private ArrayList<String> toVerifyEmails = new ArrayList<String>();
	private ArrayList<String> CheckedEmails = new ArrayList<String>();
	private ArrayList<String> FailedEmails = new ArrayList<String>();
	private JCheckBox chckboxlenght;
	private boolean addToList = true;
	private JLabel lblResults;
	
	public BlacklistChecker(Varifier i)
	{
		instance = i;
		if(instance.getController() != null)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					try
					{
						frame = new BlacklistChecker(instance, "welcome");
						frame.setVisible(true);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		} else
		{
			//System.out.println("No controller running.... ");
		}
	}

	public BlacklistChecker(Varifier in, String s)
	{
		instance = in;
		URL faviconResource = getClass().getResource("favicon.png");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 633, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				instance.setBlacklistcheckeropen(false);
				e.getWindow().dispose();
			}
		});

		setTitle("[EFV] Email Blacklist Filter");
		setIconImage(new ImageIcon(faviconResource).getImage());

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(324, 44, 283, 262);
		contentPane.add(scrollPane);

		checked = new JTextArea();
		scrollPane.setViewportView(checked);

		JLabel lblVarifiedEmails = new JLabel();
		lblVarifiedEmails.setText("Checked & Cleaned List:");
		lblVarifiedEmails.setBounds(324, 19, 283, 14);
		contentPane.add(lblVarifiedEmails);

		//imported mails (or pasted) verified & validated
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 44, 296, 129);
		contentPane.add(scrollPane_1);

		verifiedvalidated = new JTextArea();
		scrollPane_1.setViewportView(verifiedvalidated);

		JLabel lblverifiedvalidated = new JLabel("Filtered & Validated Emails:");
		lblverifiedvalidated.setBounds(10, 19, 296, 14);
		contentPane.add(lblverifiedvalidated);

		if(instance.getController() != null)
		{
			for(String mail : instance.getController().getValidmails())
			{
				verifiedvalidated.append(mail + "\n");
			}
			verifiedvalidated.setCaretPosition(0);
			lblverifiedvalidated.setText("Filtered & Validated Emails: (imported: " + instance.getController().getValidmails().size() + ")");
		}

		//blacklist domains

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 217, 296, 89);
		contentPane.add(scrollPane_2);

		blacklistdomainstextarea = new JTextArea();
		scrollPane_2.setViewportView(blacklistdomainstextarea);

		if(!instance.getController().getVvaa().startsWith("http://for"))
		{
			instance.setController(new Controller(null));
			instance.getController().getDomainsLoaded();
		}
		
		JLabel blacklistdomainslbl = new JLabel("Blacklist Domains:");
		blacklistdomainslbl.setBounds(10, 192, 296, 14);
		contentPane.add(blacklistdomainslbl);

		JButton btnEmailBlacklistChecker = new JButton("Filter Domains");
		btnEmailBlacklistChecker.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Starting filter");
				
				//reset old settings
				toVerifyEmails = new ArrayList<String>();
				CheckedEmails = new ArrayList<String>();
				FailedEmails = new ArrayList<String>();
				checked.setText("");
				
				//capture mails
				captureEmailInput();
				
				
				if(!toVerifyEmails.isEmpty())
				{
					BlacklistThread blacklistthread = new BlacklistThread(instance);
					blacklistthread.start();
				} else
				{
					System.out.println("Please add some emails...");
				}
				
			}
		});
		btnEmailBlacklistChecker.setBounds(115, 317, 191, 23);
		contentPane.add(btnEmailBlacklistChecker);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 349, 597, 16);
		contentPane.add(progressBar);

		chckboxlenght = new JCheckBox("Add to List");
		chckboxlenght.setBounds(150, 192, 100, 14);
		chckboxlenght.addItemListener(new ItemListener() {
			@Override
            public void itemStateChanged(ItemEvent e)
            {
		        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
		        	addToList = true;
		        } else {//checkbox has been deselected
		        	addToList = false;
		        }          
            }
		});
		chckboxlenght.setSelected(true);
		contentPane.add(chckboxlenght);
		
		lblResults = new JLabel("");
		lblResults.setBounds(324, 317, 283, 14);
		contentPane.add(lblResults);
		
		loadBlacklist();
	}

	public void updateProgressBar()
	{
		progressBar.setMinimum(0);
		progressBar.setMaximum(toVerifyEmails.size());
		progressBar.setValue(0);
	}

	public void increaseProgress(int i)
	{
		progressBar.setValue(i);
	}

	public void loadBlacklist()
	{
		try
		{
			// create token1
			String token1 = "";

			// for-each loop for calculating heat index of May - October

			// create Scanner inFile1
			String filepath = (new File(".")).getAbsolutePath() + "/blacklist.txt";
			File file = new File(filepath);

			if(!file.exists())
			{
				//System.out.println("Building blacklist file... at " + filepath);
				fillDefaultBlacklist();
				saveBlacklist();
				//clean the list to refresh
				blacklistDomains.clear();
				
				//reload the list after we saved a new one
				loadBlacklist();
				return;
			} else
			{
				blacklistDomains.clear(); //clear old list if any
			
				Scanner inFile1 = new Scanner(file).useDelimiter(",\\s*");

				// Original answer used LinkedList, but probably preferable to use ArrayList in most cases
				// List<String> temps = new LinkedList<String>();
				List<String> temps = new ArrayList<String>();

				// while loop
				while (inFile1.hasNext())
				{
					// find next line
					token1 = inFile1.nextLine();
					temps.add(token1);
				}
				inFile1.close();

				String[] tempsArray = temps.toArray(new String[0]);

				for(String s : tempsArray)
				{
					blacklistDomains.add(s);
				}

				//System.out.println("loaded blacklist mails: " + blacklistDomains.size());
				//finish loading and update blacklist
				refreshBlacklistUI();
			}
		} catch(Exception io)
		{
			//System.out.println("Failed to load blacklist... (not bad if you have no blacklist yet)");
		}
		
		this.addWindowListener(new WindowAdapter() {
		      public void windowOpened(WindowEvent e) {
		    	  cleanContentPane();
		      }

		      public void windowClosing(WindowEvent e) {
		    	  cleanContentPane();
		      }

		      public void windowActivated(WindowEvent e) {
		    	  cleanContentPane();
		      }

		      public void windowDeactivated(WindowEvent e) {
		    	  cleanContentPane();
		      }

		});
		
	}

	public void cleanContentPane()
	{
		contentPane.revalidate();
		contentPane.repaint();
	}

	private void refreshBlacklistUI()
	{
		blacklistdomainstextarea.setText("");
		for(String domain : blacklistDomains)
		{
			blacklistdomainstextarea.append(domain + "\n");
		}
		blacklistdomainstextarea.setCaretPosition(0);
	}

	private void fillDefaultBlacklist()
	{
		blacklistDomains.add("@gmail.com");
		blacklistDomains.add("@yahoo.com");
		blacklistDomains.add("@ymail.com");
		blacklistDomains.add("@live.com");
		blacklistDomains.add("@outlook.com");
	}

	public void saveBlacklist()
	{
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter((new File(".")).getAbsolutePath() + "/blacklist.txt"));

			for(String data : blacklistDomains)
			{
				writer.write(data.toString() + "\r\n");
			}

			writer.flush();
			writer.close();
		} catch(IOException e1)
		{
			e1.printStackTrace();
		}
	}

	public void captureEmailInput()
	{
		
		String s[] = verifiedvalidated.getText().toLowerCase().split("\\r?\\n");
		
		toVerifyEmails = new ArrayList<>(Arrays.asList(s));
	
		if(verifiedvalidated.getText().length() <= 1)
		{
			//System.out.println("No emails could be converted to arraylist");
			toVerifyEmails = new ArrayList<String>();
		}
	}
	
	public void checkerFinished()
	{
		for(String mail : CheckedEmails)
		{
			checked.append(mail + "\n");
		}
		checked.setCaretPosition(0);
		
		lblResults.setText("Results: PASSED [" + CheckedEmails.size() + "]  -  FAILED [" + FailedEmails.size() + "]");

		System.out.println("Blacklist Checker Finished  SIZE: " + CheckedEmails.size());
		System.out.println("---------");
		System.out.println("Passed: [" + CheckedEmails.size() + "]");
		System.out.println("---------");
		System.out.println("Failed: [" + FailedEmails.size() + "]");
		
		if(addToList) //if adding domains to current list
		{
			saveBlacklist(); //save new list
			
			//reload blacklist
			loadBlacklist();
		}
		
		getContentPane().validate();
		getContentPane().repaint();
	}
	
	public class BlacklistThread extends Thread
	{
		public int running = 0; //how many threads running
		private int mailSize = 0;
		private Varifier vinstance;
		
		public BlacklistThread(Varifier i)
		{
			mailSize = toVerifyEmails.size();
			vinstance = i;
			updateProgressBar();
		}

		public void run()
		{
			if(running < mailSize) //how many threads do I want to run at once
			{
				System.out.println(running + "  <- running threads | emails size -> " + toVerifyEmails.size());
				try
				{
					//run 100 at a time to conserve thread errors
					for(int i = 0; i <=100; i++)
					{
						String nextma = toVerifyEmails.get(0);
						toVerifyEmails.remove(0);
	
						(new CheckThread(vinstance, nextma)).start();
						running++;
					}

					Thread.sleep(200);
					
					//update progress bar
					increaseProgress(running);
					
					run();
				} catch (Exception io)
				{
					System.out.println("Finished blacklist check");
					//update progress bar
					increaseProgress(running);
					checkerFinished();
					this.interrupt();
				}
				
			}
		}
	}

	public class CheckThread extends Thread
	{
		private Varifier vinstance;
		private String checkemail;
		private boolean verificationStatus;
		
		public CheckThread(Varifier v, String e)
		{
			vinstance = v;
			checkemail = e;
		}
		
		public void run()
		{
				String domainofmail = checkemail.replaceAll(".*@", "@"); //get domain
				
				if(blacklistDomains.contains(domainofmail)) //if domain is in blacklist then fail test
				{
					verificationStatus = false;
				} else //else the domain of email is not in blacklist
				{
					verificationStatus = true;
				}

				//update verification status
				if(verificationStatus) //not in blacklist
				{
					//System.out.println(checkemail + " is NOT in blacklist!");
					CheckedEmails.add(checkemail); //add to checkedmails list
					
					if(isAddToList()) //did we check to add this domain to blacklist
					{
						blacklistDomains.add(domainofmail); //add domain to blacklist
					}
					
					this.interrupt();
				} else
				{
					//domain is in blacklist
					//System.out.println(checkemail + " FOUND in blacklist!!");
		
					FailedEmails.add(checkemail);
					
					this.interrupt();
				}
		}
	}
	
	
	public ArrayList<String> getBlacklistDomains()
    {
    	return blacklistDomains;
    }

	public void setBlacklistDomains(ArrayList<String> blacklistDomains)
    {
    	this.blacklistDomains = blacklistDomains;
    }

	public ArrayList<String> getToVerifyEmails()
    {
    	return toVerifyEmails;
    }

	public void setToVerifyEmails(ArrayList<String> toVerifyEmails)
    {
    	this.toVerifyEmails = toVerifyEmails;
    }

	public ArrayList<String> getCheckedEmails()
    {
    	return CheckedEmails;
    }

	public void setCheckedEmails(ArrayList<String> checkedEmails)
    {
    	CheckedEmails = checkedEmails;
    }

	public boolean isAddToList()
    {
	    return addToList;
    }

	public void setAddToList(boolean addToList)
    {
	    this.addToList = addToList;
    }

	public ArrayList<String> getFailedEmails()
    {
	    return FailedEmails;
    }

	public void setFailedEmails(ArrayList<String> failedEmails)
    {
	    FailedEmails = failedEmails;
    }
	
}
