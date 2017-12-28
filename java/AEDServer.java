/*
 * AEDServer in Java, Server-side
 * 20080322 by wadelau
 * refined by Xenxin@Ufqi
 * 21:29 27 December 2017
 * Channels? @todo
 * Java NIO? @todo
 * http://rox-xmlrpc.sourceforge.net/niotut/
 */

package samepleserver;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat ;

//import sampleserver.MyService;

public class AEDServer{
	
	private final int port=8521;
	private ServerSocket serverSocket;
	private ExecutorService executorService; //--- thread pool
	private final int POOL_SIZE=10; //--- thread pool size per CPU
	private static int loglevel = 1 ; //--- 0, all msg, 2, very less log
	private final int readTimeout = 3 * 60 * 1000 ; //--- in millisencond

	//MyService myservice = null ;
	
	public AEDServer() throws IOException{
		serverSocket=new ServerSocket(port);
		//--- get number of CPUs 
		ThreadFactory threadfactory = Executors.defaultThreadFactory();
		//executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
		executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE,threadfactory);
		//myservice = new MyService();
		Log.save(loglevel, "sampleserver started...");
		threadfactory = null ;	
	}

	public void service(){
		while(true){
			Socket socket=null;
			try{
				//--- handle client request
				socket=serverSocket.accept();
				//socket.setSoTimeout( readTimeout );
				executorService.execute(new Handler(socket, loglevel, myservice));
				//Log.save(loglevel,"requet coming...");
				//-- remedy on 20080816 by wadelau
				socket.setTcpNoDelay(false);
				socket.setSendBufferSize(8192);
				socket.setReceiveBufferSize(8192);
				socket.setKeepAlive( true );
			} 
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException{
		new AEDServer().service();
	}	
}

//--
class Handler implements Runnable{
	private Socket socket;
	private int loglevel ;
	//private MyService myservice;

	public Handler(Socket socket, int loglevel, MyService myservice){
		this.socket=socket;
		this.loglevel = loglevel ;
		this.myservice = myservice ;
	}
	private PrintWriter getWriter(Socket socket) throws IOException{
		OutputStream socketOut=socket.getOutputStream();
		return new PrintWriter(socketOut,true);
	}
	private BufferedReader getReader(Socket socket) throws IOException{
		InputStream socketIn=socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}

	public void run(){
		try {
			//Log.save(loglevel,"New connection accepted "+socket.getInetAddress()+":"+socket.getPort());
			BufferedReader br=getReader(socket);
			PrintWriter pw=getWriter(socket);
			String msg = null;
			String sendout = "" ;
			while( (msg=br.readLine())!=null ){
				//System.out.println("recv: ["+msg+"]");
				sendout = myservice.doSomething( msg );
				//System.out.println("send:["+sendout+"]");
				pw.println( sendout );
				pw.flush();
				//Log.save(loglevel,"cmd:["+cmd+"] params:["+params+"]");
				if(msg.equals("quit") || msg.equals("exit")){
					socket.close(); //- remedy by wadelau, on Sat Feb  8 10:02:58 CST 2014
					break ;
				}
			}
		}
		catch (IOException e){
			Log.save(loglevel,"timeout?");
			e.printStackTrace();
		}
		finally{
			if(socket!=null){
				//-- keep alive?
				//System.out.println("socket is not null and keep ready...");
				//socket.close();
			}
		}
	}
}

//-
final class Log {
	//private static String logfile = "log/session_CURDATE.log";
	public static SimpleDateFormat dformat =  new SimpleDateFormat("yyyyMMdd") ;	
	public static SimpleDateFormat tformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ; 

	int loglevel = 1 ;

	public Log(){
		//--
	}	

	public static void save(int loglevel, String msg){
		String logfile = "log/session_CURDATE.log";
		Date date = new Date();
		logfile = logfile.replace("CURDATE",dformat.format(date))  ;
		if( loglevel>=0 ){
			System.out.println( "["+tformat.format(date)+"] SessionServer: "+msg+"\n");
		}
		if( loglevel >= 1){
			try{	
				FileWriter fw = new FileWriter(logfile, true);
				fw.write( "["+tformat.format(date)+"] SessionServer: "+msg+" \n" );
				fw.flush();
				fw.close();
				fw = null ;
			}
			catch( Exception e){
				e.printStackTrace();
			}
		}
		date = null ;

	}

}

