package org.lobzik.sh.modules.sms;

import java.sql.Connection;
import java.util.*;

import org.apache.log4j.Logger;
import org.lobzik.sh.CommonData;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;
import org.lobzik.tools.sms.CIncomingMessage;
import org.lobzik.tools.sms.CMessage;
import org.lobzik.tools.sms.COutgoingMessage;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SMSCommandHandler extends Thread 
{ 
	private static final SMSCommandHandler instance = new SMSCommandHandler();
	private static SMSConnector connector = null;
	private static int timeout = 1000;
	private static int pollPeriod = 30;
	private static Logger log = null;
	private static boolean busy = false;	
	private static boolean run = true;
	private static String smscNumber = "";
	private static Connection conn = null;
	public static final int STATUS_NEW = 0;
	public static final int STATUS_SENT = 1;
	public static final int STATUS_READ = 2;
	public static final int STATUS_ERROR = -1;
	private static String lastRecieved = "";
	private static long lastNewCheck = System.currentTimeMillis();
	private SMSCommandHandler() {}
	
	public static SMSCommandHandler getInstance(SMSConnector smsConnector, Logger logger, Connection connSql, HashMap config)
	{
		instance.setName(instance.getClass().getSimpleName()+"-Thread");
		log = logger;
		connector = smsConnector;
		timeout = connector.getCommandTimeout();
		smscNumber = (String)config.get("SmsCenter");
		conn = connSql;
		pollPeriod = CommonTools.parseInt(config.get("PollPeriod"), pollPeriod);
		return instance;
	}

	
	public void run() 
	{

		while(run)
		{
			try 
			{
				//if (conn == null) conn =  DriverManager.getConnection("jdbc:mysql://192.168.4.4:3306/sh?useUnicode=true" +
				//"&characterEncoding=utf8&autoReconnect=true&user=shuser&password=shpass");
				String sSQL = "SELECT * FROM SMS_OUTBOX WHERE STATUS = " + STATUS_NEW;
							
				synchronized(this) 
				{
					try{wait(pollPeriod * 1000);} catch (InterruptedException ie) {}
				}
				List<HashMap> smsToSendList = DBSelect.getRows(sSQL, conn);
				for (HashMap smsToSend: smsToSendList)
				{
					log.info("Sending SMS ID " + smsToSend.get("ID"));
					smsToSend.put("STATUS", STATUS_ERROR);
					DBTools.updateRow("SMS_OUTBOX", smsToSend, conn);//сразу ему ставим статус с ошибкой, чтобы если что не гонялось по кругу
					COutgoingMessage outMsg = new COutgoingMessage();
					
					outMsg.setMessageEncoding(CMessage.MESSAGE_ENCODING_UNICODE);
					outMsg.setRecipient((String)smsToSend.get("RECIPIENT"));
					outMsg.setText((String)smsToSend.get("MESSAGE"));
					String pdu = outMsg.getPDU(smscNumber);
					int j = pdu.length();
					j /= 2;
					if (smscNumber.length() == 0) j--;
					else
					{
						j -= ((smscNumber.length() - 1) / 2);
						j -= 2;
					}
					j--;
					handle("AT+CMGS=" + j + "\r");
					handle(pdu + "\032");
					if (lastRecieved.equalsIgnoreCase("OK"))
					{
						smsToSend.put("STATUS", STATUS_SENT);
						log.info("Successfully sent");
						DBTools.updateRow("SMS_OUTBOX", smsToSend, conn);
					}
					else
					{
						
						log.error("Error sending: " + lastRecieved);
					}					
				}
				if (smsToSendList.size() == 0)
				{
					if (System.currentTimeMillis() - lastNewCheck >= pollPeriod * 1000)
					{
						connector.getSMSReplies().clear();
						handle("AT+CMGL");
						if (connector.getSMSReplies().size() > 2)
						{
							connector.getSMSReplies().clear();
							handle("AT+CMGD=0,4");
						}
						lastNewCheck = System.currentTimeMillis();
					}

				}
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				synchronized(this) 
				{
					try{wait(10000);} catch (InterruptedException ie) {}
				}
			}
		}
		DBTools.closeConnection(conn);
	}
	
	public  int sendMessage(String recipient, String text) throws Exception
	{
		HashMap message = new HashMap();
		message.put("MESSAGE", text);
		message.put("RECIPIENT", recipient); //TODO проверка на формат телефона!
		message.put("DATE", new Date());
		message.put("STATUS", STATUS_NEW);
		int msgId = DBTools.insertRow("SMS_OUTBOX", message, conn);
		synchronized (this) 
		{
			notify();
		}
		return msgId;
	}
	
	public void exit()
	{
		run = false;		
		synchronized (this) 
		{
			notify();
		}
	}
	
	public void lineRecieved(ArrayList<String> recievedLines)
	{
		
		if (recievedLines.get(recievedLines.size()-1).trim().length() > 0)
			lastRecieved = recievedLines.get(recievedLines.size()-1).trim();
		if (lastRecieved.equals("OK") || lastRecieved.contains("ERROR") || lastRecieved.equals(">"))
		{
			parseReplyLines(recievedLines); 
			synchronized (this) 
			{
				notifyAll();
			}
		}
		if (lastRecieved.contains("+CMTI:"))
		{
			lastNewCheck = 0;
			synchronized (this) 
			{
				notify();
			}
		}
	}
	
	public synchronized void handle(String command) throws Exception
	{
		if (connector == null || !connector.isConnected()) return;
		while (busy)
			synchronized(this) 
			{
				try{wait(timeout*10);} catch (InterruptedException ie) {}
			}
		busy = true;
		connector.doCommand(command);
		synchronized(this) 
		{
			try{wait(timeout);} catch (InterruptedException ie) {}
			busy = false;
			notifyAll();
		}
	}

	public void parseReplyLines(ArrayList<String> replyLines)
	{
		boolean incoming = false;
		for (String replyLine: replyLines) 
		{
			if (incoming)
			{
				try
				{
					CIncomingMessage message = new CIncomingMessage(replyLine, 1);
					HashMap dbMessage = new HashMap();
					dbMessage.put("MESSAGE", message.getNativeText());
					dbMessage.put("DATE", message.getDate());
					dbMessage.put("SENDER", message.getOriginator());
					dbMessage.put("STATUS", STATUS_NEW);
					int id = DBTools.insertRow("SMS_INBOX", dbMessage, conn);
					log.info ("Recieved SMS from " + message.getOriginator() + " ID = " + id);
					if (true)//TODO тут проверить, с какого номера сообщение - можно ли ему управлять
					{
						CommonData.parameters.set("SMS_RECIEVED_SENDER", message.getOriginator());
						CommonData.parameters.set("SMS_RECIEVED_TEXT", message.getNativeText());
					}
				
				}
				catch (Exception e)
				{
					log.error("Error while getting SMS: " + e.getMessage());
				}
			}
			incoming = false;
			if (replyLine.contains("+CMGL:") || replyLine.contains("+CMGR:"))
				incoming = true;
		}
	}
	
	// Operator-specific comands
	public void doSpeedRequest() throws Exception
	{
		handle("AT+CMGF=1\r");
		handle("AT+CMGS=\"5340\"\r");
		handle("?\032");
		handle("AT+CMGF=0\r");
	}
}

