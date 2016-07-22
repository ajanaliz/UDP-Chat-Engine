package com.ChatEngine.Chattering;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client{

	private static final long serialVersionUID = 1L;
	
	private String name, address;
	private int port;
	private Thread send;
	
	/*a quick Explanation about UDP and TCP protocols:*/
	/*for connecting a computer to another computer via the networking hardware we need to access the socket class-->so we need to create a socket and this is were we decide what protocol we would want to use,there are two main protocols that we can actually use for transferring data or sending packets in between two  clients(devices on a network)
	 * now the first protocol is called TCP(the Transmission Control Protocol),the benefits of it is that TCP actually garuntee's delivery of packets(a packet btw is just a bunch of data-->just a bunch of bytes that we can then use),because what happens is once a packet is received on the receiving end it actually sends a reply saying that "i've got it" and this way u 
	 * can guarantee that the packet was delivered and was'nt lost on the way,and the other thing it garuantee's is actual sequential deliveries of packets -->does'nt actually receive them sequentially but after it has received them,it sorts them into the actual order they were sent in-->so as u can see TCP is very good for places were actually data is very important and 
	 * don't want to lose it the other thing worth mentioning is that TCP has to actually conform a connection before sending packets and it can't just send packets to an address--- it has to establish a connection with that address(or that device) first 
	 * the other protocol is called UDP(User Datagram Protocol) basically this protocol is the opposite of the TCP,it does'nt care if the packets get there or not and it does'nt care if it has established a connection with the address given to it e.g u can simply send a packet to 192.168.1.100 whilst not having a device connected to that IP address and while using UDP
	 * the protocol would'nt care if it actually gets there or not it would actually send it but if we were using TCP for example u could'nt just send packets to that IP address and u would need to establish a connection first (there's something called the threeway handshake) --> u can imagine straight away now that UDP is useful for things like games because sometimes u 
	 * temporarily lose connection with the server and thats not a big deal,the other problem is that TCP will resends packets that do not get there e.g if your network times out for 2 seconds TCP will actually resends those packets that were not received by you as a result of that network time-out,because of that you'll be 2 seconds behind in the game and thats a bit of an issue
	 * see in UDP u time out for 2 seconds u don't receive any data at all and when u reconnect u receive the most up to date data thats why UDP is good for games
	 * all u need to know is TCP requires a connection at all times and will acknowledge were the packets were received or not and that UDP does'nt care*/
	/*now the question is should we use the UDP protocol for this ChatEngine? is that the best idea?probably not because if we actually do lose the connection we would actually never get that message that was sent but because this is a leed up to gameprogramming we are going to be using UDP*/
	
	private DatagramSocket socket;
	private InetAddress ip;//an IP address -- > we use this address for descening our location of packets meaning if we want to send a packet to a particular address we use this InetAddress format thing instead of the String version of it...simply because socket accepts InetAddress and not a String 
	
	
	private int ID = -1;/*this is for receiving the unique identifier that gets assigned to us,the -1 means it has'nt been set*/
	
	public Client(String name, String address, int port){
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	
	public boolean openConnection(String address){
		/*we've got our IP address as a string here,what we need to do is convert it to the InetAddress form*/
		try {
			socket = new DatagramSocket();//we're constructing a datagram socket 
			ip = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String receive(){
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);//this statement will hang the program and will sit here until it has received something --> we need threads
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		return message;
	}
	
	public void send(final byte[] data){
		send = new Thread("Send"){
			public void run(){
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	public void close(){
		new Thread(){//this will make a new thread and close the socket on an entirely different process
			public void run(){
				synchronized(socket){//this makes sure the socket is'nt being used when its going close it --> we don't wanna close it if its still in use
					socket.close();//closes the connection to the Client
				}
			}
		}.start();
	}
	
	public String getName() {
		return name;
	}
	public String getAddress() {
		return address;
	}
	public int getPort(){
		return port;
	}
	public void setID(int ID){
		this.ID = ID;
	}
	public int getID(){
		return ID;
	}
}
