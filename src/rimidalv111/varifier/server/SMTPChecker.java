package rimidalv111.varifier.server;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import rimidalv111.varifier.main.Varifier;
import rimidalv111.varifier.smtpserver.ServerMultithreaded;

public class SMTPChecker extends JFrame
{
	private SMTPChecker smtpchecker;
	private Varifier instance;
	private SMTPChecker frame;
	
	private JPanel contentPane;
	private JSlider sliderClientsAmount;
	private JButton serverBtnStart;
	private JButton serverBtnStop;
	private JLabel lblServerStatus;
	private JTextArea smtpserverlogPane;
	private JPanel smtpserverPanel;
	private JSlider sliderBatchAmount;
	private JButton clientStopbtn;
	private JButton clientStartbtn;
	private JLabel clientLoglbl;
	private JProgressBar progressBar;

	//client start stop
	private boolean clientStatus = false;

	//server start stop
	private boolean serverStatus = false;;
	//server
	private ServerMultithreaded server;
	private JScrollPane serverLogScrollPane;
	private int serverPort = 8080;
	private JPanel clientPannel;
	private JLabel lblCompletedVerificationLableStat;
	private JLabel lblLeftToCheckStat;

	//client stuff
	private ClientControllerThread clientcontroller = null;
	private boolean clientcontrollerStatus = false;

	private ArrayList<String> emailsforverification = new ArrayList<String>();

	/**
	 * Launch the application.
	 */
	public SMTPChecker(Varifier v, ArrayList<String> tobeverified)
	{
		instance = v;
		emailsforverification = tobeverified;
		
		if(instance.getController() != null)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					try
					{
						frame = new SMTPChecker(instance, emailsforverification, "welcome");
						frame.setVisible(true);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the frame.
	 * @wbp.parser.constructor
	 */
	public SMTPChecker(Varifier i, ArrayList<String> toverify, String s)
	{
		smtpchecker = this;
		instance = i;
		emailsforverification = toverify;
		
		URL faviconResource = getClass().getResource("favicon-smtp.png");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 310);
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
				instance.setSmtpCheckerOpen(false);
				if(clientcontroller != null)
				{
					System.out.println("We closed while running, so saving results");
					try
					{
						clientcontroller.stopClientJobs(false);
					} catch (Exception io)
					{
						System.out.println("Well... we failed to save the list!");
					}
				}
				e.getWindow().dispose();
			}
		});

		setTitle("[EFV] SMTPChecker (Server & Client)");
		
		if(faviconResource != null)
		{
			setIconImage(new ImageIcon(faviconResource).getImage());
		}
		
		
		JLabel lblClients = new JLabel("Number of Clients:");
		lblClients.setToolTipText("<html>How many clients do you want connected <br/>to the server to work on bulk smtp verification.</html>");
		lblClients.setFont(new Font("Tahoma", Font.BOLD, 9));
		lblClients.setHorizontalAlignment(SwingConstants.CENTER);
		lblClients.setBounds(10, 11, 200, 14);
		contentPane.add(lblClients);

		sliderClientsAmount = new JSlider();
		sliderClientsAmount.setToolTipText("<html>How many clients do you want connected <br/>to the server to work on bulk smtp verification.</html>");
		sliderClientsAmount.setFont(new Font("Tahoma", Font.PLAIN, 8));
		sliderClientsAmount.setMinimum(1);
		sliderClientsAmount.setSnapToTicks(true);
		sliderClientsAmount.setPaintLabels(true);
		sliderClientsAmount.setPaintTicks(true);
		sliderClientsAmount.setMajorTickSpacing(5);
		sliderClientsAmount.setMaximum(51);
		sliderClientsAmount.setValue(21);
		sliderClientsAmount.setBounds(10, 23, 200, 46);
		contentPane.add(sliderClientsAmount);

		JLabel lblHowManyChecks = new JLabel("Bulk emails per client:");
		lblHowManyChecks.setToolTipText("<html>How many emails do you want each <br/>client to bulk check at once.</html>");
		lblHowManyChecks.setFont(new Font("Tahoma", Font.BOLD, 9));
		lblHowManyChecks.setHorizontalAlignment(SwingConstants.CENTER);
		lblHowManyChecks.setBounds(10, 76, 200, 14);
		contentPane.add(lblHowManyChecks);

		sliderBatchAmount = new JSlider();
		sliderBatchAmount.setToolTipText("<html>How many emails do you want each <br/>client to bulk check at once.</html>");
		sliderBatchAmount.setFont(new Font("Tahoma", Font.PLAIN, 8));
		sliderBatchAmount.setValue(6);
		sliderBatchAmount.setMinimum(1);
		sliderBatchAmount.setMajorTickSpacing(5);
		sliderBatchAmount.setMaximum(37);
		sliderBatchAmount.setSnapToTicks(true);
		sliderBatchAmount.setPaintLabels(true);
		sliderBatchAmount.setPaintTicks(true);
		sliderBatchAmount.setBounds(10, 90, 200, 46);
		contentPane.add(sliderBatchAmount);

		smtpserverPanel = new JPanel();
		smtpserverPanel.setBorder(new LineBorder(new Color(165, 42, 42), 2, true));
		smtpserverPanel.setBounds(220, 11, 212, 251);
		contentPane.add(smtpserverPanel);
		smtpserverPanel.setLayout(null);

		JLabel lblSmtpCheckerServer = new JLabel("SMTPCheck Server");
		lblSmtpCheckerServer.setForeground(new Color(128, 0, 128));
		lblSmtpCheckerServer.setHorizontalAlignment(SwingConstants.CENTER);
		lblSmtpCheckerServer.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSmtpCheckerServer.setBounds(10, 11, 192, 14);
		smtpserverPanel.add(lblSmtpCheckerServer);

		if(instance.getController().getVvaa().length() > 20)
		{
			
		} else
		{
			instance.setController(null);
			instance.getController().captureEmailInput();
		}
		lblServerStatus = new JLabel("STOPPED");
		lblServerStatus.setForeground(new Color(165, 42, 42));
		lblServerStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServerStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblServerStatus.setBounds(10, 70, 192, 14);
		smtpserverPanel.add(lblServerStatus);

		serverBtnStart = new JButton("start");
		serverBtnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(serverStatus)
				{
					lblServerStatus.setText("Server is running already...");
					lblServerStatus.validate();
					lblServerStatus.repaint();
				} else
				{
					System.out.println("Starting the SMTP server...");

					//update old serverlogpanes
					smtpserverlogPane.setText("");

					server = new ServerMultithreaded(smtpchecker);

					try
					{
						new Thread(server).start();
					} catch(Exception io)
					{
						io.printStackTrace();
					}

				}
			}
		});
		serverBtnStart.setBackground(new Color(0, 100, 0));
		serverBtnStart.setForeground(new Color(192, 192, 192));
		serverBtnStart.setBounds(10, 36, 91, 23);
		smtpserverPanel.add(serverBtnStart);

		serverBtnStop = new JButton("[stop]");
		serverBtnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!serverStatus)
				{
					lblServerStatus.setText("Server is not running yet...");
					lblServerStatus.validate();
					lblServerStatus.repaint();
				} else
				{
					System.out.println("Shutting down SMTP server...");

					server.stop();

				}
			}
		});
		serverBtnStop.setForeground(new Color(192, 192, 192));
		serverBtnStop.setBackground(new Color(128, 0, 0));
		serverBtnStop.setBounds(111, 36, 91, 23);
		smtpserverPanel.add(serverBtnStop);

		serverLogScrollPane = new JScrollPane();
		serverLogScrollPane.setBounds(10, 89, 192, 151);
		smtpserverPanel.add(serverLogScrollPane);

		smtpserverlogPane = new JTextArea();
		serverLogScrollPane.setViewportView(smtpserverlogPane);
		smtpserverlogPane.setForeground(new Color(255, 255, 255));
		smtpserverlogPane.setBackground(new Color(212, 208, 200));
		smtpserverlogPane.setEditable(false);

		clientPannel = new JPanel();
		clientPannel.setBorder(new LineBorder(new Color(165, 42, 42), 1, true));
		clientPannel.setBounds(10, 147, 200, 115);
		contentPane.add(clientPannel);
		clientPannel.setLayout(null);

		clientLoglbl = new JLabel("Please start SMTP Server > >  ");
		clientLoglbl.setFont(new Font("Tahoma", Font.BOLD, 11));
		clientLoglbl.setHorizontalAlignment(SwingConstants.RIGHT);
		clientLoglbl.setBounds(10, 45, 180, 14);
		clientPannel.add(clientLoglbl);

		clientStartbtn = new JButton("start");
		clientStartbtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!serverStatus) //server is not running
				{
					clientLoglbl.setText("Please start the server first...");
					clientLoglbl.setForeground(new Color(165, 42, 42));
					clientLoglbl.validate();
					clientLoglbl.repaint();
				} else
					if(clientStatus && clientcontrollerStatus)
					{
						try
						{
							clientcontroller.saveResults();

							clientLoglbl.setText("Saved recent results...");
							clientLoglbl.setForeground(new Color(0, 102, 0));
							clientLoglbl.validate();
							clientLoglbl.repaint();
						} catch(Exception io)
						{
							clientLoglbl.setText("Failed to save results...");
							clientLoglbl.setForeground(new Color(165, 42, 42));
							clientLoglbl.validate();
							clientLoglbl.repaint();

							serverLog("[error] Failed to save recent results...");
							clientLoglbl.setForeground(new Color(165, 42, 42));
							io.printStackTrace();
						}
					} else
					{
						System.out.println("Starting the client connections and work...");
						//start the client connection and work controller?

						if(clientcontroller == null && !clientcontrollerStatus)
						{
							//start client controller
							clientcontroller = new ClientControllerThread(smtpchecker, sliderClientsAmount.getValue(), sliderBatchAmount.getValue());
							clientcontroller.start();
						}
					}
			}
		});
		clientStartbtn.setForeground(Color.LIGHT_GRAY);
		clientStartbtn.setBackground(new Color(0, 100, 0));
		clientStartbtn.setBounds(10, 11, 84, 23);
		clientPannel.add(clientStartbtn);

		clientStopbtn = new JButton("[stop]");
		clientStopbtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!clientStatus)
				{
					clientLoglbl.setText("No client connections running...");
					clientLoglbl.setForeground(new Color(165, 42, 42));
					clientLoglbl.validate();
					clientLoglbl.repaint();
				} else
				{
					//stop the client connection and work controller?

					if(clientStatus && clientcontrollerStatus)
					{
						System.out.println("Shutdown ALL clients!");
						clientcontroller.stopClientJobs(false);
					}
					//stop client controller
				}
			}
		});
		clientStopbtn.setForeground(Color.LIGHT_GRAY);
		clientStopbtn.setBackground(new Color(128, 0, 0));
		clientStopbtn.setBounds(104, 11, 86, 23);
		clientPannel.add(clientStopbtn);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 92, 180, 16);
		progressBar.setMaximum(100);
		clientPannel.add(progressBar);

		lblCompletedVerificationLableStat = new JLabel("[V]:");
		lblCompletedVerificationLableStat.setHorizontalAlignment(SwingConstants.LEFT);
		lblCompletedVerificationLableStat.setToolTipText("<html>\r\n[V] - Total Emails Verified\r\n</html>");
		lblCompletedVerificationLableStat.setBounds(10, 70, 84, 14);
		clientPannel.add(lblCompletedVerificationLableStat);

		lblLeftToCheckStat = new JLabel("[C]: " + emailsforverification.size());
		lblLeftToCheckStat.setToolTipText("<html>\r\n[C] - Emails Left to Verify\r\n</html>");
		lblLeftToCheckStat.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeftToCheckStat.setBounds(104, 70, 86, 14);
		clientPannel.add(lblLeftToCheckStat);

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

	public void onServerShutdown()
	{
		//update the gui that server is running
		lblServerStatus.setText("STOPPED");
		lblServerStatus.setForeground(new Color(165, 42, 42));
		lblServerStatus.validate();
		lblServerStatus.repaint();

		//update client log and terminate all clients
		clientLoglbl.setText("Server has shutdown...");
		clientLoglbl.setForeground(new Color(165, 42, 42));
		clientLoglbl.validate();
		clientLoglbl.repaint();
		//add some red colors to server & client
		smtpserverPanel.setBorder(new LineBorder(new Color(165, 42, 42), 2, true));
		smtpserverPanel.validate();
		smtpserverPanel.repaint();

		clientPannel.setBorder(new LineBorder(new Color(165, 42, 42), 1, true));
		clientPannel.validate();
		clientPannel.repaint();

		//shut down clients
		//####		
		//####	
		//####	Code all clients shutdown here
		//####
		//####
		//####
		//####
		//add some red colors
		smtpserverPanel.setBorder(new LineBorder(new Color(165, 42, 42), 2, true));
		smtpserverPanel.validate();
		smtpserverPanel.repaint();

		//set server status false
		setServerStatus(false);

		if(clientcontroller != null && clientcontrollerStatus)
		{
			clientcontroller.stopClientJobs(false);
		}

	}

	public void onServerStartup()
	{
		//		//update the gui that server is running
		lblServerStatus.setText("RUNNING");
		lblServerStatus.setForeground(new Color(0, 102, 0));
		lblServerStatus.validate();
		lblServerStatus.repaint();
		//		
		//update client log
		clientLoglbl.setText("Clients are ready to work...");
		clientLoglbl.setForeground(new Color(0, 102, 0));
		clientLoglbl.validate();
		clientLoglbl.repaint();

		//add some green colors
		smtpserverPanel.setBorder(new LineBorder(new Color(0, 102, 0), 2, true));
		smtpserverPanel.validate();
		smtpserverPanel.repaint();

		//set server status false
		setServerStatus(true);
	}

	public void serverLog(String log)
	{
		if(smtpserverlogPane.getLineCount() > 1000) //lets not slow us down with lots of logs
		{
			smtpserverlogPane.setText("");
		}
		smtpserverlogPane.append("[-]" + log + "\n");
		smtpserverlogPane.validate();
		smtpserverlogPane.repaint();

		smtpserverlogPane.setCaretPosition(smtpserverlogPane.getDocument().getLength());
	}

	public void onClientCheckingStarted()
	{
		//update some colors
		clientPannel.setBorder(new LineBorder(new Color(0, 102, 0), 1, true));
		clientPannel.validate();
		clientPannel.repaint();

		//update client status
		clientcontrollerStatus = true;

		//update button to save
		clientStartbtn.setText("SAVE");
		clientStartbtn.validate();
		clientStartbtn.repaint();

		//update status
		clientLoglbl.setText("RUNNING");
		clientLoglbl.setForeground(new Color(0, 102, 0));
		clientLoglbl.validate();
		clientLoglbl.repaint();

		//set server status
		clientStatus = true;
	}

	public void onClientCheckingStopped(String logUpdate)
	{
		//update some colors
		clientPannel.setBorder(new LineBorder(new Color(165, 42, 42), 1, true));
		clientPannel.validate();
		clientPannel.repaint();

		//set controller to null and not running
		clientcontroller = null;
		clientcontrollerStatus = false;

		//update button to start
		clientStartbtn.setText("start");
		clientStartbtn.validate();
		clientStartbtn.repaint();

		//update status
		clientLoglbl.setText(logUpdate);
		if(logUpdate.startsWith("STOPPED"))
		{
			clientLoglbl.setForeground(new Color(165, 42, 42));
		} else
		{
			clientLoglbl.setForeground(new Color(0, 102, 0));
		}
		clientLoglbl.validate();
		clientLoglbl.repaint();

		//update progressbar to 100%
		updateStats(true);
		
		clientStatus = false;
	}

	public JProgressBar getProgressBar()
	{
		return progressBar;
	}

	public ClientControllerThread getClientcontroller()
	{
		return clientcontroller;
	}

	public void setProgressBar(JProgressBar progressBar)
	{
		this.progressBar = progressBar;
	}

	public void setClientcontroller(ClientControllerThread clientcontroller)
	{
		this.clientcontroller = clientcontroller;
	}

	public void cleanContentPane()
	{
		//clear content pane
		contentPane.validate();
		contentPane.repaint();
	}

	public void updateStats(boolean completedProgress)
	{
		ThreadedStatUpdate statUpdate = new ThreadedStatUpdate(this);
		if(completedProgress)
		{
			statUpdate.setOverrideProgress(100);
			statUpdate.start();
		} else
		{
			statUpdate.start();
		}
	}

	public JTextArea getSmtpserverlogPane()
	{
		return smtpserverlogPane;
	}

	public void setSmtpserverlogPane(JTextArea smtpserverlogPane)
	{
		this.smtpserverlogPane = smtpserverlogPane;
	}

	public JLabel getLblServerStatus()
	{
		return lblServerStatus;
	}

	public void setLblServerStatus(JLabel lblServerStatus)
	{
		this.lblServerStatus = lblServerStatus;
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
	}

	public JLabel getClientLoglbl()
	{
		return clientLoglbl;
	}

	public void setClientLoglbl(JLabel clientLoglbl)
	{
		this.clientLoglbl = clientLoglbl;
	}

	public boolean isClientStatus()
	{
		return clientStatus;
	}

	public boolean isServerStatus()
	{
		return serverStatus;
	}

	public void setClientStatus(boolean clientStatus)
	{
		this.clientStatus = clientStatus;
	}

	public void setServerStatus(boolean serverStatus)
	{
		this.serverStatus = serverStatus;
	}

	public JPanel getSmtpserverPanel()
	{
		return smtpserverPanel;
	}

	public void setSmtpserverPanel(JPanel smtpserverPanel)
	{
		this.smtpserverPanel = smtpserverPanel;
	}

	public JLabel getLblCompletedVerificationLableStat()
	{
		return lblCompletedVerificationLableStat;
	}

	public JLabel getLblLeftToCheckStat()
	{
		return lblLeftToCheckStat;
	}

	public void setLblCompletedVerificationLableStat(JLabel lblCompletedVerificationLableStat)
	{
		this.lblCompletedVerificationLableStat = lblCompletedVerificationLableStat;
	}

	public void setLblLeftToCheckStat(JLabel lblLeftToCheckStat)
	{
		this.lblLeftToCheckStat = lblLeftToCheckStat;
	}

	public ArrayList<String> getEmailsforverification()
	{
		return emailsforverification;
	}

	public void setEmailsforverification(ArrayList<String> emailsforverification)
	{
		this.emailsforverification = emailsforverification;
	}
}
