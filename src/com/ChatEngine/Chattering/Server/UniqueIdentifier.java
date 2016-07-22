package com.ChatEngine.Chattering.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueIdentifier {
	private static List<Integer> ids = new ArrayList<Integer>();
	private static final int RANGE = 10000;
	
	private static int index = 0;
	
	static{/*because this is a static class this static{} will run without it being called--> in other words instead of us creating a method like: public static void init(){} and instead of calling it whenever our program starts,we can just use this technique :D*/
		for( int i = 0; i < RANGE; i++){
			ids.add(i);
		}
		Collections.shuffle(ids);
	}
	
	public UniqueIdentifier(){
		
	}
	
	public static int getIdentifier(){
		if ( index > ids.size() - 1 ) index = 0;
		return ids.get(index++);
	}

}
