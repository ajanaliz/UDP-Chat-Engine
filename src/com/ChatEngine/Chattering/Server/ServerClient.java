package com.ChatEngine.Chattering.Server;

import java.net.InetAddress;

public class ServerClient {/*so what kind of things should we store about the client?this class exists to store information about the client
that has connected to the server*/

	private String name;
	private InetAddress address;
	private int port;
	private final int ID;/*the identification number that we give to our client--> the reason we would need this is because if we would just 
	use something like address as the identifier,lets just say someone at ur house is connecting to someone at my house now that might create
	a problem in a way for example u might have a brother or another person in your household with the same external IP address who's actually
	connecting to my server so suddenly we have two clients with the same address,problem->port numbers could be used but there are some cases
	in which for example if i were to connect with the exact same client on the exact same computer to exact same server u can actually
	duplicate the ports the original plan was to take the IP address e.g 192.168.0.10 and port number e.g 25541 and combine them like this:
	192168001025541 and use that as the ID number but it could fail its purpose ----> so what we're gonna do is make it a randomly generated
	number that is unique*/
	public int attempt = 0;/*this is used for alot of things---->let me put it like this:if the clients internet connection drops out for a 
	minute and the server just has to contact the client for some reason it'll send a packet and it might expect a reply but it does'nt so
	then it says OK im gonna increment attempt by 1 so this will be the second time we're gonna try and send the same packet cuz its been 
	a while(its been like 5 seconds) and there has not been an automated reply so lets try again and it does that and we could set attempt to
	lets just say 5 basically when attempt is greater than or equal to 5 then just drop the Client(kick the client)*/
	
	public ServerClient(String name, InetAddress address, int port, final int ID ){
		this.setName(name);
		this.setAddress(address);
		this.setPort(port);
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/*how will the server manage these clients u ask?we'll make a Array list of type ServerClient :D,inside Server ofcourse*/
	
}
