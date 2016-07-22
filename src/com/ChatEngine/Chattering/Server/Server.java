package com.ChatEngine.Chattering.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable{

	
	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();
	
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	
	private Thread run,manage,send,receive;
	private boolean row = false;
	private final int MAX_ATTEMPTS = 5;
	
	public Server(int port){
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		run.start();
	}

	@Override
	public void run() {//the run method for the Thread called "run"
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		Scanner scanner = new Scanner(System.in);
		while(running){
			String text = scanner.nextLine();
			if(!text.startsWith("/")){
				sendToAll("/m/Server: " + text + "/e/");
				continue;
			}
			text = text.substring(1);//it will get rid of the "/" at the beginning
			if(text.equals("row")){
				if (row) System.out.println("Row mode off.");
				else System.out.println("Row mode on.");
				row = !row;/*if we're in row mode,we see the actual packets of anything that gets in-->the actual message,with all its postfixes/prefixes*/
			}
			else if(text.equals("clients")){
				System.out.println("Clients:");
				System.out.println("========");
				for(int i = 0;i < clients.size(); i++){
					ServerClient c = clients.get(i);
					System.out.println(c.getName().trim() + "(" + c.getID() + ")" + c.getAddress().toString() + ":" + c.getPort());
				}
				System.out.println("=========");
			}else if( text.startsWith("kick")){
				String name = text.split(" ")[1];
				int id = -1;
				boolean number = true;//is it a number(is it the persons ID) or is it a word(the persons name)
				//the easiest way to find out weather a string is a number or not is trying and coverting it into a number and seeing if that works
				try{
					id = Integer.parseInt(name);
				}catch(NumberFormatException e){
					number = false;
				}if (number){/*the user has entered an ID,we wanna see if that ID exists or not,if so then we delete it*/
					boolean exists = false;
					for( int i = 0;i < clients.size();i++){
						if(clients.get(i).getID() == id){
							exists = true;
							break;
						}
					}
					if(exists) disconnect(id , true);
					else System.out.println("Client " + id + "doesnt exist! Check ID Number.");
				}else{//we receive a username not an ID
					for(int i = 0; i < clients.size();i++){
						ServerClient c = clients.get(i);
						if(name.equals(c.getName())){
							disconnect(c.getID(),true);
							break;
						}
					}
				}
			}else if(text.equals("help")){
				printHelp();
			}else if(text.equals("quit")){
				quit();
			}else{
				System.out.println("Unknown command.");
				printHelp();
			}
		}
		scanner.close();
	}
	
	private void printHelp(){
		System.out.println("Here is a list of all available commands:");
		System.out.println("=========================================");
		System.out.println("/row - enables row mode.");
		System.out.println("/clients - shows all connected clients.");
		System.out.println("/kick [users ID or username] - kick a user.");
		System.out.println("/help - shows help.");
		System.out.println("/quit - shuts down the server.");
	}
	
	private void manageClients(){/*this method is going to be responsible for managing the clients,so its not gonna receive data(thats what the receive method is for)but what it will be doing is essentially sending out ping's every now and then(once every 2 or 5 seconds for example)
	and then it will see if the client actually replies and if the client does'nt we'll send a few more ping's just incase its a lost packet or something but after a while we can assume that the users pretty much timed out or the user has improperly closed their application-->because
	when the client closes their little chat window we wanna actually send a disconnect packet to the server just to make sure we actually remove that client from the list of clients but sometimes people will just end process or for example just unplug their computer from the powerpoint
	and kill it that way(like just holding the power button and shutting it off that way) and if u do that obviously the computer has no chance to send that disconnecting packet and thats why we need to send check*/
		/*this method will make sure the clients are still there and disconnects them if their not still there and stuff like that*/
		manage = new Thread("Manage"){
			public void run(){
				while(running){
					sendToAll("/i/Server");//this is for checking if the clients are still connected or not..sort of like a ping message
					sendStatus();
					try{
						Thread.sleep(2000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					for(int i = 0; i < clients.size();i++){
						ServerClient c = clients.get(i);
						if(!clientResponse.contains(c.getID())){
							if(c.attempt >= MAX_ATTEMPTS ){
								disconnect(c.getID(),false);
							}else{
								c.attempt++;
							}
						}else{
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}
	
	private void sendStatus(){
		if(clients.size() <= 0 ) return;
		String users = "/u/";
		for(int i = 0;i < clients.size() - 1;i++){
			users += clients.get(i).getName() + "/n/";//the "/n/" is for differentiating between each username in our usernamelist 
		}
		users += clients.get(clients.size() - 1).getName() + "/e/";//for the last user,we wont be putting a "/n/" on the end but a "/e/"
		sendToAll(users);
	}
	
	private void receive(){
		/*this method will receive anything that it gets and its actually very very vital how we handle this receive process and the reason its vital is because we're not just receiving a packet from one client we could be receiving packets every millisecond pretty much, so we're receiving 
		 * a ton of packets we might have 50 clients we might have stuff like disconnecting/reconnecting/connecting/sending messages to each other/sending messages to a specific client...we'll have all mayhem going on here so what we'll do is actually create 2 Threads*/
		receive = new Thread("Receive"){
			public void run(){
				while(running){
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					}catch (SocketException e) {//if theres a socket exception do nothing
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}
	
	private void send(String message, InetAddress address, int port){
		message += "/e/";//the End Character
		send(message.getBytes(),address,port);
		
	}
	
	
	
	
	private void process(DatagramPacket packet){
		String string = new String(packet.getData());
		if (row) System.out.println(string.trim());
		if ( string.startsWith("/c/")){//we have a connection packet-->we need to connect the person to the server --> add the client to the client list
			int id = UniqueIdentifier.getIdentifier();
			String name = string.split("/c/|/e/")[1].trim();
			System.out.println(name + "(" + id + ") Connected!");
			clients.add(new ServerClient(name , packet.getAddress(), packet.getPort() , id));
			String ID = "/c/" + id;
			send(ID,packet.getAddress(),packet.getPort());//sending shit back to the client
		}else if(string.startsWith("/m/")){
			sendToAll(string);
		}else if(string.startsWith("/d/")){
			String id = string.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		}else if(string.startsWith("/i/")){/*as long as the server has replied to us,I.E we receive a message starting with "/i/" we'll go ahead and we'll add it 
		to the clientResponse*/
			clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
			
		}else{
			System.out.println(string);
		}
	}

	private void quit(){
		for( int i = 0; i < clients.size(); i++){
			disconnect(clients.get(i).getID(), true);
		}
		running = false;
		socket.close();
	}
	
	
	private void disconnect(int id,boolean status){/*we wanna disconnect in two cases:1.of natural causes meaning the user hits the close button or uses the Alt+F4 method
	2.of unnatural causes e.g the power going out,holding the power button to shut down the computer,using task manager to terminate the process,in which cases we could use
	a status boolean that is the status being:if they closed it via natural method or it has been closed via unnatural methods meaning if the Client has Timed-Out,so 
	Timed-Out is something that happens if essentially the server tries to ping the client but it can't get a response and it does that for like 5 times or whoever many times
	but the status boolean is for seeing if we're dealing with case 1 or 2*/
		ServerClient c = null;
		boolean existed = false;
		for(int i = 0;i <clients.size();i++){
			if(clients.get(i).getID() == id){ 
				c = clients.get(i);
				clients.remove(i);
				existed = true;
				break;
			}
		}
		if (!existed) return;
		String message = "";
		if (status){
			message = "Client " + c.getName().trim() + " (" + c.getID() + ") @ " + c.getAddress().toString() + ":" + c.getPort() + " disconnected.";
		}else{
			message = "Client " + c.getName().trim() + " (" + c.getID() + ") @ " + c.getAddress().toString() + ":" + c.getPort() + " timed out.";
		}
		System.out.println(message);
	}
	
	private void sendToAll(String message){
		if (message.startsWith("/m/")){
			String text = message.substring(3).trim();
			text = text.split("/e/")[0];
			System.out.println(text);
		}
		if(row) System.out.println(message);
		for(int i = 0; i < clients.size(); i++){
			ServerClient client = clients.get(i);
			send(message.getBytes(),client.getAddress() , client.getPort());
		}
	}
	
	private void send(final byte[] data, final InetAddress address,final int port){/*since we're the Server,clients connect to us,in other words if 
	we want to send a packet of data we actually have to specify for each packet of data where we want to send it so make sure that u also
	have the IP(InetAddress) and port number :D*/
		send = new Thread("Send"){
			public void run(){
				DatagramPacket packet = new DatagramPacket(data,data.length,address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
		
	}
	
	
}
