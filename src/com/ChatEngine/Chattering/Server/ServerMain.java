package com.ChatEngine.Chattering.Server;

public class ServerMain {/*this class is the class that actually runs a server,its kind of the top layer,and one of the reasons its cool to create a class like this is because if we put all of our 
server related logical code into this serverclass, what that means is that we can actually run multiple servers from this one servermain class whereas if we were to do something like just have one 
class,that would'nt be possible,we'd actually have to run multiple programs of servers so in other words we'd have 5 server applications running but with this we can have one application running with 
5 servers*/
	
	private int port;
	private Server server;
	
	public ServerMain(int port){
		this.port = port;
		server = new Server(port);
	}
	
	public static void main(String[] args){
		int port;
		if (args.length != 1){
			System.out.println("Usage: java -jar ChatteringServer.jar [port]");
			return;
		}
		port = Integer.parseInt(args[0]);
		new ServerMain(port);
	}

}
