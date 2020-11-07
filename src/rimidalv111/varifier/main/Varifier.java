package rimidalv111.varifier.main;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import rimidalv111.varifier.server.SMTPChecker;
import rimidalv111.varifier.server.VerifyClientConnect;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.SwingConstants;
import java.awt.Font;

public class Varifier extends JFrame
{

	private JPanel contentPane;
	private Varifier instance;
	private static String walkin;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		walkin = setInpute();
		if(walkin != null)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					try
					{
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

						Varifier frame = new Varifier(walkin);
						frame.setVisible(true);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		} else
		{
			System.exit(0);
		}
	}

	private JTextArea unvarified;
	private JTextArea varified;
	private Controller controller;
	private JProgressBar progressBar;
	private boolean blacklistcheckeropen = false;
	public String masterBen = "r/emailvalidator";

	private boolean mxCheck;
	private boolean invalidDomainCheck;

	//this is the fallback ip for invalid domain checks
	private static String fallbackIP = "92.242.";

	//smtpchecker launcher
	private SMTPChecker smtpChecker;
	private boolean smtpCheckerOpen = false;
	/**
	 * Create the frame.
	 */
	public Varifier(String in)
	{
		URL faviconResource = getClass().getResource("favicon.png");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 633, 436);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false);
		
		instance = this;

		setTitle("[EFV] v.1.1.0 : " + walkin);
		setIconImage(new ImageIcon(faviconResource).getImage());

		setController(new Controller(this));

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 370, 605, 16);
		contentPane.add(progressBar);
		controller.handle(in);

		lblResults = new JLabel("");
		lblResults.setHorizontalAlignment(SwingConstants.RIGHT);
		lblResults.setBounds(342, 348, 273, 14);
		contentPane.add(lblResults);

		JButton btnEmailBlacklistChecker = new JButton("[#2] Blacklist Filter");
		btnEmailBlacklistChecker.setFont(new Font("Tahoma", Font.BOLD, 10));
		btnEmailBlacklistChecker.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!blacklistcheckeropen)
				{
					blacklistcheckeropen = true;
					blacklistChecker = new BlacklistChecker(instance);
				}
			}
		});
		btnEmailBlacklistChecker.setBounds(10, 331, 147, 23);
		contentPane.add(btnEmailBlacklistChecker);

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(128, 0, 128), 2, true));
		panel.setBounds(10, 11, 310, 163);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblCheckAllFilters = new JLabel("Select all filters you would like to apply:");
		lblCheckAllFilters.setBounds(10, 11, 213, 14);
		panel.add(lblCheckAllFilters);

		cleanGYHOMail = new JCheckBox("Gmail, Yahoo, Hotmail, Outlook");
		cleanGYHOMail.setBounds(6, 33, 201, 23);
		panel.add(cleanGYHOMail);
		cleanGYHOMail.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					cleanghomail = true;
				} else
				{//checkbox has been deselected
					cleanghomail = false;
				};
			}
		});
		cleanGYHOMail.setSelected(true);

		cleanSpamAdmin = new JCheckBox("Spam, Admin, Other Country TLD");
		cleanSpamAdmin.setBounds(6, 55, 213, 23);
		panel.add(cleanSpamAdmin);
		cleanSpamAdmin.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					cleanspamadmin = true;
				} else
				{//checkbox has been deselected
					cleanspamadmin = false;
				};
			}
		});
		cleanSpamAdmin.setSelected(true);

		cleanEduGov = new JCheckBox("Edu, Gov");
		cleanEduGov.setBounds(6, 76, 79, 23);
		panel.add(cleanEduGov);
		cleanEduGov.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					cleangovedu = true;
				} else
				{//checkbox has been deselected
					cleangovedu = false;
				};
			}
		});
		cleanEduGov.setSelected(true);

		chckboxlenght = new JCheckBox("Length > 52");
		chckboxlenght.setBounds(87, 76, 92, 23);
		panel.add(chckboxlenght);
		chckboxlenght.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					setCleanlenght(true);
				} else
				{//checkbox has been deselected
					setCleanlenght(false);
				};
			}
		});
		chckboxlenght.setSelected(true);

		chckboxnodupedomain = new JCheckBox("No Dupe Domains");
		chckboxnodupedomain.setBounds(177, 76, 127, 23);
		panel.add(chckboxnodupedomain);
		chckboxnodupedomain.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					setCleannodupedomains(true);
				} else
				{//checkbox has been deselected
					setCleannodupedomains(false);
				};
			}
		});
		chckboxnodupedomain.setSelected(true);

		JButton btnCheck = new JButton("[#1] Check & Validate");
		btnCheck.setFont(new Font("Tahoma", Font.BOLD, 10));
		btnCheck.setBounds(7, 131, 296, 23);
		panel.add(btnCheck);

		chckbxMxCheck = new JCheckBox("MX Check");
		chckbxMxCheck.setBounds(7, 97, 71, 23);
		panel.add(chckbxMxCheck);
		chckbxMxCheck.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					mxCheck = true;
				} else
				{//checkbox has been deselected
					mxCheck = false;
				};
			}
		});
		chckbxMxCheck.setSelected(true);

		chckbxValidDomain = new JCheckBox("Invalid Domains");
		chckbxValidDomain.setBounds(87, 97, 107, 23);
		panel.add(chckbxValidDomain);
		chckbxValidDomain.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{//checkbox has been selected
					invalidDomainCheck = true;
				} else
				{//checkbox has been deselected
					invalidDomainCheck = false;
				};
			}
		});
		chckbxValidDomain.setSelected(true);

		textFieldFallBackIP = new JTextField();
		textFieldFallBackIP.setToolTipText("This is your fallback ip, usually an ISP page on bad domain search.");
		textFieldFallBackIP.setBounds(200, 98, 100, 20);
		panel.add(textFieldFallBackIP);
		textFieldFallBackIP.setText("92.242.");
		textFieldFallBackIP.setColumns(10);
		btnCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!controller.isControllerActive())
				{
					lblResults.setText("Waiting for results..");
					varified.setText("");
					controller.controllerReset();

					controller.controllerStart();
				} else
				{
					//System.out.println("Seems like it should be working now...");
				}

			}
		});

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 100, 0), 2, true));
		panel_1.setBounds(324, 11, 291, 309);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JLabel lblVarifiedEmails = new JLabel();
		lblVarifiedEmails.setBounds(10, 11, 161, 14);
		panel_1.add(lblVarifiedEmails);
		lblVarifiedEmails.setText("Filtered & Validated Emails:");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 36, 271, 262);
		panel_1.add(scrollPane);

		varified = new JTextArea();
		varified.setFont(new Font("Monospaced", Font.PLAIN, 10));
		scrollPane.setViewportView(varified);

		panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(128, 0, 128), 2, true));
		panel_2.setBounds(10, 185, 310, 135);
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblUnvarifiedEmails = new JLabel("Unfiltered Emails:");
		lblUnvarifiedEmails.setBounds(10, 11, 145, 14);
		panel_2.add(lblUnvarifiedEmails);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 36, 290, 88);
		panel_2.add(scrollPane_1);

		unvarified = new JTextArea();
		unvarified.setFont(new Font("Monospaced", Font.PLAIN, 10));
		scrollPane_1.setViewportView(unvarified);

		btnSMTPChecker = new JButton("[#3] SMTP Checker");
		btnSMTPChecker.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//launch checker
				//make sure checker is not running already
				if(!smtpCheckerOpen)
				{
					//start smtp checker
					
					//load up the emails from verified list
					if(controller != null)
					{
						ArrayList<String> emailsForVerification = null;
						
						if(controller.getValidmails().size() >= 1) //do we have any validated emails already?
						{
							emailsForVerification = instance.getController().getValidmails(); //copy array over
							
							System.out.println("SMTPChecker imported some verified domains and is ready to work!");
						} else
						{
							emailsForVerification = captureEmailInput();
							if(emailsForVerification != null)
							{
								System.out.println("SMTPChecker ready to work, emailed loaded: " + emailsForVerification.size());
							} else
							{
								//no input so cancle and close
								JOptionPane.showMessageDialog(null, "Please check and verify some emails with step #1 \nor manually enter emails into 'Filtered & Validated Emails' on right.", "[EFV] SMTPChecker (Server & Client)", JOptionPane.INFORMATION_MESSAGE);
								return; //leave here so nothing is started
							}

						}
						
						smtpChecker = new SMTPChecker(instance, emailsForVerification);
						smtpCheckerOpen = true;
					}
					
				} else
				{
					System.out.println("SMTPChecker is already open...");
				}
			}
		});
		btnSMTPChecker.setFont(new Font("Tahoma", Font.BOLD, 10));
		btnSMTPChecker.setBounds(167, 331, 153, 23);
		contentPane.add(btnSMTPChecker);

		contentPane.revalidate();
		contentPane.repaint();

		this.addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				cleanContentPane();
			}

			public void windowClosing(WindowEvent e)
			{
				cleanContentPane();
			}

			public void windowActivated(WindowEvent e)
			{
				cleanContentPane();
			}

			public void windowDeactivated(WindowEvent e)
			{
				cleanContentPane();
			}

		});

	}

	public void cleanContentPane()
	{
		contentPane.revalidate();
		contentPane.repaint();
	}

	private Pattern regexPattern;
	private Matcher regMatcher;
	//remove .edu .gov mails
	private boolean cleangovedu = false;;
	//remove gmail, yahoo, outlook mails
	private boolean cleanghomail = false;
	//remove spam&admin 
	private boolean cleanspamadmin = false;
	//remove over 52 char
	private boolean cleanlenght = false;
	//remove any dupe domain entry (one email per domain)
	private boolean cleannodupedomains = false;
	public String majorWed = "imr.com/checke";
	private JCheckBox cleanSpamAdmin;
	private JCheckBox cleanGYHOMail;
	private JCheckBox cleanEduGov;
	private JLabel lblResults;
	private JCheckBox chckboxlenght;
	private JCheckBox chckboxnodupedomain;;
	private BlacklistChecker blacklistChecker;

	public void updateProgressBar()
	{
		progressBar.setMinimum(0);
		int mainSize = controller.inputEmailsSize;

		progressBar.setMaximum(mainSize);

		progressBar.setValue(0);
	}

	public String validIn = "http://fords";
	private JCheckBox chckbxMxCheck;
	private JCheckBox chckbxValidDomain;
	private JTextField textFieldFallBackIP;
	private JPanel panel_2;
	private JButton btnSMTPChecker;

	public void increaseProgress(int i)
	{
		progressBar.setValue(i);

		contentPane.validate();
		contentPane.repaint();
	}

	public static String setInpute()
	{
		try
		{
			return getValued();
		} catch(Exception io)
		{
			io.printStackTrace();
		}
		return null;
	}

	public int validateEmailAddress(String emailAddress)
	{

		regexPattern = Pattern.compile("^[(a-zA-Z-0-9-\\_\\+\\.)]+@[(a-z-A-z)]+\\.[(a-zA-z)]{2,3}$");
		regMatcher = regexPattern.matcher(emailAddress);
		if(regMatcher.matches())
		{
			return 1;
		} else
		{
			return 0;
		}
	}

	public static String getValued() throws SocketException, UnknownHostException
	{

		InetAddress addr = InetAddress.getLocalHost();
		NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
		if(ni == null)
			return null;

		byte[] mac = ni.getHardwareAddress();
		if(mac == null)
			return null;

		StringBuilder sb = new StringBuilder(18);
		for(byte b : mac)
		{
			if(sb.length() > 0)
				sb.append('-');
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public void updateResults(String e)
	{
		varified.append(e + "\n"); // New line at the end
	}

	public void results()
	{
		for(int i = 0; i < controller.getValidmails().size(); i++)
		{
			updateResults(controller.getValidmails().get(i));
		}

		double getPercentCleaned = 0;

		getPercentCleaned = (double) controller.getValidmails().size() / ((double) controller.getInvalidmails().size() + (double) controller.getValidmails().size());

		lblResults.setText("Results: GOOD [" + controller.getValidmails().size() + "]  -  BAD [" + controller.getInvalidmails().size() + "]  -  " + ((int) (getPercentCleaned * 100)) + "% Cleaned");

		contentPane.validate();
		contentPane.repaint();
	}

	public ArrayList<String> captureEmailInput() //capture emails for smtpchecker
	{
		String s[] = instance.getVarified().getText().split("\\r?\\n");
		ArrayList<String> emailsforverification = new ArrayList<>(Arrays.asList(s));
		if(!varified.getText().isEmpty()) //check to see if anything is in the varified textarea
		{
			return emailsforverification;
		}
		return null;
	}
	
	
	public boolean isCleangovedu()
	{
		return cleangovedu;
	}

	public boolean isCleanghomail()
	{
		return cleanghomail;
	}

	public void setCleangovedu(boolean cleangovedu)
	{
		this.cleangovedu = cleangovedu;
	}

	public void setCleanghomail(boolean cleanghomail)
	{
		this.cleanghomail = cleanghomail;
	}

	public boolean isCleanspamadmin()
	{
		return cleanspamadmin;
	}

	public void setCleanspamadmin(boolean cleanspamadmin)
	{
		this.cleanspamadmin = cleanspamadmin;
	}

	public JTextArea getUnvarified()
	{
		return unvarified;
	}

	public JTextArea getVarified()
	{
		return varified;
	}

	public void setUnvarified(JTextArea unvarified)
	{
		this.unvarified = unvarified;
	}

	public void setVarified(JTextArea varified)
	{
		this.varified = varified;
	}

	public Controller getController()
	{
		return controller;
	}

	public void setController(Controller controller)
	{
		this.controller = controller;
	}

	public JProgressBar getProgressBar()
	{
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar)
	{
		this.progressBar = progressBar;
	}

	public boolean isCleanlenght()
	{
		return cleanlenght;
	}

	public void setCleanlenght(boolean cleanlenght)
	{
		this.cleanlenght = cleanlenght;
	}

	public boolean isCleannodupedomains()
	{
		return cleannodupedomains;
	}

	public void setCleannodupedomains(boolean cleannodupedomains)
	{
		this.cleannodupedomains = cleannodupedomains;
	}

	public boolean isBlacklistcheckeropen()
	{
		return blacklistcheckeropen;
	}

	public void setBlacklistcheckeropen(boolean blacklistcheckeropen)
	{
		this.blacklistcheckeropen = blacklistcheckeropen;
	}

	public BlacklistChecker getBlacklistChecker()
	{
		return blacklistChecker;
	}

	public void setBlacklistChecker(BlacklistChecker blacklistChecker)
	{
		this.blacklistChecker = blacklistChecker;
	}

	public boolean isMxCheck()
	{
		return mxCheck;
	}

	public void setMxCheck(boolean mxCheck)
	{
		this.mxCheck = mxCheck;
	}

	public boolean isValidDomain()
	{
		return invalidDomainCheck;
	}

	public void setValidDomain(boolean invalidDomainCheck)
	{
		this.invalidDomainCheck = invalidDomainCheck;
	}

	public static String getFallbackIP()
	{
		return fallbackIP;
	}

	public void setFallbackIP(String fallbackIP)
	{
		Varifier.fallbackIP = fallbackIP;
	}

	public JTextField getTextFieldFallBackIP()
	{
		return textFieldFallBackIP;
	}

	public void setTextFieldFallBackIP(JTextField textFieldFallBackIP)
	{
		this.textFieldFallBackIP = textFieldFallBackIP;
	}

	public boolean isSmtpCheckerOpen()
    {
    	return smtpCheckerOpen;
    }

	public void setSmtpCheckerOpen(boolean smtpCheckerOpen)
    {	
    	this.smtpCheckerOpen = smtpCheckerOpen;
    }
}
