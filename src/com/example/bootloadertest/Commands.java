package com.example.bootloadertest;

import java.util.HashMap;

public class Commands {

	// Master constant Commands

	public final static String REQUEST_FIRM_VER = "<REQUEST_FIRM_VER>";
	public final static String LOOPBACK_TEST_BYTE = "<LOOPBACK_TEST_BYTE>";
	public final static String FIRM_UPDATE_REQ = "<FIRM_UPDATE_REQ>";
	public final static String JUMP_TO_APP = "<JUMP_TO_APP>";

	public final static String ERASE_FLASH = "<ERASE_FLASH>";
	public final static String RECEIVE_BULK_DATA = "<RECEIVE_BULK_DATA>";
	public final static String START_OF_DATA_CHUNK = "<START_OF_DATA_CHUNK>";
	public final static String END_OF_DATA_CHUNK = "<END_OF_DATA_CHUNK>";
	
	public final static String END_OF_BULK_DATA="<END_OF_BULK_DATA>";
	
	public final static String FIRMWARE_UPDATE_SUCCESS="<FIRMWARE_UPDATE_SUCCESS>";
	public final static String FIRMWARE_UPDATE_FAILURE="<FIRMWARE_UPDATE_FAILURE>";
	

	// Master Variable Commands

	public final static String FILE_SIZE = "<FILE_SIZE=";
	public final static String FILE_ADDRESS = "<FILE_ADDRESS=";
	public final static String FIRMWARE_VER = "<FIRMWARE_VER=";
	public final static String DATA_CHUNK_SIZE = "<CHUNK_SIZE=";
	public final static String HASH = "<HASH=";

	// Slave Constant commands

	public final static String FLASH_PROGRAMMED = "<FLASH_PROGRAMMED>";

	// Slave Variable commands

	public final static String FIRM_VER = "<FIRM_VER=";
	
	public final static String INCORRECT_DATA="<INCORRECT_DATA>";

	public HashMap<Integer, String> map;
	
	
	
	public Commands(){
		
		setCommands();
	}

	public void setCommands() {

		map = new HashMap<Integer, String>();
		map.put(1, LOOPBACK_TEST_BYTE);
		map.put(2, REQUEST_FIRM_VER);
		map.put(3, FIRM_UPDATE_REQ);
		map.put(4, FILE_SIZE);
		map.put(5, FILE_ADDRESS);
		map.put(6, FIRMWARE_VER);
		map.put(7, DATA_CHUNK_SIZE);
		map.put(8, ERASE_FLASH);
		map.put(9, RECEIVE_BULK_DATA);
		map.put(10, START_OF_DATA_CHUNK);
		
		
		map.put(20, HASH);
		map.put(21, END_OF_DATA_CHUNK);
		map.put(22, ERASE_FLASH);
		
		map.put(23, END_OF_BULK_DATA);

	}

	public String getCommand(int _command) {

		return map.get(_command);
	}

}

// ////////////////////////////////////////////////////////

/*
 * 
 * LOOPBACK_TEST_BYTE ----> <--- LOOPBACK_TEST_BYTE
 * 
 * REQUEST_FIRM_VER ----> <--- FIRM_VER=<V1.00>
 * 
 * FIRM_UPDATE_REQ -----> <--- ACKNOWLEDGED
 * 
 * 
 * FILE_SIZE=86 -----> 85 kB size <--- ACKNOWLEDGED
 * 
 * 
 * FILE_ADDRESS=54272 -----> <--- ACKNOWLEDGED 53kB = 53*1024 = 54272
 * 
 * FIRMWARE_VER=V1.10 ----> <--- ACKNOWLEDGED
 * 
 * DATA_CHUNK_SIZE=5000 ----> <--- ACKNOWLEDGED
 * 
 * ERASE_FLASH ----> <--- ACKNOWLEDGED
 * 
 * RECEIVE_BULK_DATA ----> <--- ACKNOWLEDGED
 * 
 * 
 * START_OF_DATA_CHUNK ----> <--- ACKNOWLEDGED
 * 
 * 
 * Master sends 5000 bytes ----> <--- ACKNOWLEDGED
 * 
 * END_OF_DATA_CHUNK ----> <--- ACKNOWLEDGED
 * 
 * HASH=451149681 ---> <--- ACKNOWLEDGED
 * 
 * //Slave will program flash
 * 
 * <--- FLASH_PROGRAMMED ACKNOWLEDGED -->
 * 
 * //Second data transfer START_OF_DATA_CHUNK ----> <--- ACKNOWLEDGED
 * 
 * Master sends 5000 bytes ----> <--- ACKNOWLEDGED
 * 
 * END_OF_DATA_CHUNK ----> <--- ACKNOWLEDGED
 * 
 * HASH=4511446833 ---> <--- ACKNOWLEDGED
 */