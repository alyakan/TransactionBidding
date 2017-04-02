package auction;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

import com.zaxxer.hikari.HikariDataSource;

public class Dispatcher {

	protected Hashtable _htblCommands;
	protected ExecutorService _threadPoolCmds;
	protected HikariDataSource _hikariDataSource;

	public Dispatcher() {
	}

	protected void dispatchRequest(ClientHandle clientHandle, ClientRequest clientRequest) throws Exception {

		Command cmd;
		String strAction;
		strAction = clientRequest.getAction();

		Class<?> innerClass = (Class<?>) _htblCommands.get(strAction);
		Class<?> enclosingClass = Class.forName("Dispatcher");
		Object enclosingInstance = enclosingClass.newInstance();
		Constructor<?> ctor = innerClass.getDeclaredConstructor(enclosingClass);
		cmd = (Command) ctor.newInstance(enclosingInstance);
		cmd.init(_hikariDataSource, clientHandle, clientRequest);
		_threadPoolCmds.execute((Runnable) cmd);
	}

	protected void loadHikari(String strAddress, int nPort, String strDBName, String strUserName, String strPassword) {

		_hikariDataSource = new HikariDataSource();
		_hikariDataSource.setJdbcUrl("jdbc:postgresql://" + strAddress + ":" + nPort + "/" + strDBName);
		_hikariDataSource.setUsername(strUserName);
		_hikariDataSource.setPassword(strPassword);
	}

	protected void loadCommands() throws Exception {
		_htblCommands = new Hashtable();
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream("config/commands.properties");
		prop.load(in);
		in.close();
		Enumeration enumKeys = prop.propertyNames();
		String strActionName, strClassName;

		while (enumKeys.hasMoreElements()) {
			strActionName = (String) enumKeys.nextElement();
			strClassName = (String) prop.get(strActionName);
			Class<?> innerClass = Class.forName("Dispatcher$" + strClassName);
			_htblCommands.put(strActionName, innerClass);
		}
	}

	protected void loadThreadPool() {
		_threadPoolCmds = Executors.newFixedThreadPool(20);
	}

	public void init() throws Exception {
		loadHikari("localhost", 5432, "thedatabase", "postgres", "thepassword");
		loadThreadPool();
		loadCommands();
	}
}