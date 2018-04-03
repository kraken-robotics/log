/*
 * Copyright (C) 2013-2018% Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import pfg.config.Config;

/**
 * Service de log
 * 
 * @author pf
 *
 */

public class Log
{
	private boolean logClosed = false;
	private BufferedWriter writer = null;
	private String file;
	private boolean save;

	// Ecriture plus rapide sans appel à la pile d'exécution
	private boolean fastLog = false;
	private boolean stdoutLog;
	private Severity defaultSeverity;

	/**
	 * date du démarrage
	 */
	private static final long dateInitiale = System.currentTimeMillis();
	
	public Log(Severity defaultSeverity, String configFilename, String... configprofile)
	{
		this.defaultSeverity = defaultSeverity;
		
		Config config = new Config(ConfigInfoLog.values(), false, configFilename, configprofile);
		fastLog = config.getBoolean(ConfigInfoLog.FAST_LOG);
		stdoutLog = config.getBoolean(ConfigInfoLog.STDOUT_LOG);
		file = config.getString(ConfigInfoLog.SAVE_FILE);

		if(!file.isEmpty())
		{
			Runtime.getRuntime().addShutdownHook(new ThreadCloseOnShutdown(this));
			try
			{
				writer = new BufferedWriter(new FileWriter(file));
			}
			catch(IOException e)
			{
				System.err.println("Erreur lors de la création du fichier : " + e);
			}
		}
	}
	
	public synchronized void write(String message, LogCategory categorie)
	{
		write_(message, defaultSeverity, categorie);
	}

	public synchronized void write(Object message, LogCategory categorie)
	{
		write_(message == null ? "null" : message.toString(), defaultSeverity, categorie);
	}

	public synchronized void write(Object message, Severity niveau, LogCategory categorie)
	{
		write_(message == null ? "null" : message.toString(), niveau, categorie);
	}
	
	public synchronized void write(String message, Severity niveau, LogCategory categorie)
	{
		write_(message, niveau, categorie);
	}
	
	/**
	 * Ce synchronized peut ralentir le programme, mais s'assure que les logs ne
	 * se chevauchent pas.
	 * 
	 * @param niveau
	 * @param message
	 * @param couleur
	 * @param ou
	 */
	private synchronized void write_(String message, Severity niveau, LogCategory categorie)
	{
		if(!logClosed)
		{
			long date = System.currentTimeMillis() - dateInitiale;

			String affichage;
			if(fastLog)
				affichage = date + " > " + message;
			else
			{
				StackTraceElement elem = Thread.currentThread().getStackTrace()[3];
				affichage = date + " "+ niveau + " " + elem.getClassName().substring(elem.getClassName().lastIndexOf(".") + 1) + ":" + elem.getLineNumber() + " (" + Thread.currentThread().getName() + ") > " + message;
			}

			if(stdoutLog && categorie.shouldPrint())
				System.out.println(affichage);
			if(writer != null)
			{
				try
				{
					writer.write(categorie.getMask() + " " + affichage + "\n");
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Log closing ; save the log file if necessray.
	 */
	private void close()
	{
		if(!logClosed && save)
		{
			try
			{
				if(writer != null)
				{
					writer.flush();
					writer.close();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		logClosed = true;
	}
	
	public PrintWriter getPrintWriter()
	{
		return new PrintWriter(writer);
	}

	public class ThreadCloseOnShutdown extends Thread
	{
		private Log log;

		public ThreadCloseOnShutdown(Log log)
		{
			this.log = log;
		}

		@Override
		public void run()
		{
			Thread.currentThread().setName(getClass().getSimpleName());
			log.close();
		}
	}
}
