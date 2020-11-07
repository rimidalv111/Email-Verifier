package rimidalv111.varifier.smtpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import rimidalv111.varifier.server.SMTPChecker;

public class ServerMultithreaded implements Runnable
{
	protected ServerSocket serverSocket = null;
	protected boolean hasStopped = false;
	protected Thread movingThread = null;
	protected SMTPChecker instance;

	public ServerMultithreaded(SMTPChecker i)
	{
		instance = i;
	}

	public void run()
	{
		synchronized (this)
		{
			this.movingThread = Thread.currentThread();
		}
		opnSvrSocket();
		while (!hasStopped())
		{
			Socket clntSocket = null;
			try
			{
				clntSocket = this.serverSocket.accept();
			} catch(IOException e)
			{
				if(hasStopped())
				{
					instance.serverLog("[status] Server has stopped...");
					return;
				}
				throw new RuntimeException("[error] Client cannot be connected", e);
			}
			//start a new controller thread
			new Thread(new ControllerThread(this, clntSocket)).start();
		}
		instance.serverLog("[status] Server has shutdown...");
	}

	private synchronized boolean hasStopped()
	{
		return this.hasStopped;
	}

	public synchronized void stop()
	{
		//call shutdown stuff
		instance.onServerShutdown();

		this.hasStopped = true;
		try
		{
			this.serverSocket.close();
		} catch(IOException e)
		{
			throw new RuntimeException("[error] Server can not be closed", e);
		}
	}

	private void opnSvrSocket()
	{
		try
		{
			this.serverSocket = new ServerSocket(0, 1, InetAddress.getLocalHost());

			instance.serverLog("[started] Email Verifier Server: " + "\r\nHost=" + this.serverSocket.getInetAddress().getHostAddress() + " \r\nPort=" + this.serverSocket.getLocalPort());

			//set the server port
			instance.setServerPort(this.serverSocket.getLocalPort());

			//call startup stuff
			instance.onServerStartup();

		} catch(IOException e)
		{
			throw new RuntimeException("Not able to open the port 8080", e);
		}
	}

	protected SMTPChecker getSMTPChecker()
	{
		return instance;
	}
}
