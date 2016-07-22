package com.ChatEngine.Chattering;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ClientWindow extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtMessege;
	private JTextArea history;
	private DefaultCaret caret;
	private Thread run,listen;
	private Client client;

	private boolean running;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	
	private OnlineUsers users;
	
	public ClientWindow(String name , String address , int port ) {
		setTitle("Client");
		client = new Client(name, address, port);
		boolean connect = client.openConnection(address);
		if(!connect){
			System.err.println("Connection Failed!");
			console("Connection Failed!");
		}
		createWindow();
		console("Attempting a Connection to " + address + ":" + port + ", user: " + name);
		String connection = "/c/" + name + "/e/";
		client.send(connection.getBytes());
		users = new OnlineUsers(this);
		running = true;
		run = new Thread(this, "Running");
		run.start();
	}
	
	private void createWindow(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880,550);
		setLocationRelativeTo(null);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);
		
		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{28,815,30, 7};//Sum must be equal to 880-->because the size is 880x550
		gbl_contentPane.rowHeights = new int[]{25, 485,40};//Sum must be equal to 550-->because the size is 880x550
//		gbl_contentPane.columnWeights = new double[]{1.0, 1.0};we wanna set this all to 0,which is the default so i commented out this section
//		gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		history = new JTextArea();
		history.setEditable(false);
		caret = (DefaultCaret)history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll = new JScrollPane(history);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;//-->what gridwidth actually does is specify how many cells this component must take and 1 is the default(that it takes up one cell)
		scrollConstraints.gridheight = 2;
		scrollConstraints.weightx = 1;/*whenever we resize our window our components size's change with the window,for example if we expand the window,the space upon 
		which our component sits gets bigger,because the window is getting bigger now this weight basically tells the specified component here(which is scrollConstraints)
		how to resize along with the window,if we set this to 0-->it will not resize..it will always stay the same size because what we'll actually do is just insert
		white space into the area's inbetween,if we set this equal to 1 then the component will actually get larger,so this component will actually grow when we resize
		which is actually good and is actually what we want it to do,however with something like the button for example-->we would'nt want it to resize so we set the
		weight of the components x and y to 0*/
		scrollConstraints.weighty = 1;
		scrollConstraints.insets = new Insets(0, 5, 0, 0);
		contentPane.add(scroll, scrollConstraints);
		
		txtMessege = new JTextField();
		txtMessege.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER){
					send(txtMessege.getText(),true);
				}
			}
		});
		GridBagConstraints gbc_txtMessege = new GridBagConstraints();
		gbc_txtMessege.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessege.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessege.gridx = 0;
		gbc_txtMessege.gridy = 2;
		gbc_txtMessege.gridwidth = 2;
		gbc_txtMessege.weightx = 1;//we want the message box to resize horizontally --->the width wont change 
		gbc_txtMessege.weighty = 0;//but not vertically ---> the height is the same always 
		contentPane.add(txtMessege, gbc_txtMessege);
		txtMessege.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessege.getText(), true);
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;//now that the weight of the component is 0 it wont resize along with the window 
		contentPane.add(btnSend, gbc_btnSend);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){//this method will run when we close the Client window
				String disconnect = "/d/" + client.getID() + "/e/";
				send(disconnect, false);
				running = false;
				client.close();
			}
		});
		setVisible(true);
		txtMessege.requestFocusInWindow();
	}
	
	private void send(String message, boolean text){
		if(message.equals("")) return;
		if (text){
			message = client.getName() + ": " + message;
			message = "/m/" + message + "/e/";
			txtMessege.setText("");
		}
		client.send(message.getBytes());
	}
	
	public void listen(){
		listen = new Thread("Listen"){
			public void run(){
				while(running){
					String message = client.receive();
					if (message.startsWith("/c/")){
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Successfully Connected to Server! ID: " + client.getID());
					}else if(message.startsWith("/m/")){
						String text = message.substring(3);
						text = text.split("/e/")[0];
						console(text);
					}else if(message.startsWith("/i/")){
						String text = "/i/" + client.getID() + "/e/";
						send(text,false);
					}else if(message.startsWith("/u/")){//users packet--->every once in a while the server sends the client a list of all the people who are online
						String[] u = message.split("/u/|/n/|/e/");//---> makes our string into "" + "ourname" + "" -->so we get 3 rows in our OnlineUsers window
						users.update(Arrays.copyOfRange(u , 1 , u.length - 1));//we're trimming out the two "" strings on the sides of "ourname" so we only get 1 row
					}
				}
			}
		};
		listen.start();
	}
	
	public void console(String message){
		history.append(message + "\n\r");//what this will do is append the message we want to the console
		history.setCaretPosition(history.getDocument().getLength());
	}

	@Override
	public void run() {
		listen();
	}

}
