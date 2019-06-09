package com.example.bootloadertest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.example.bootloadertest.IBluetoothHeadset;

public class MainActivity extends Activity {

	private IBluetoothHeadset ibth = null;

	private Context ctx;

	protected static final String TAG = "TAG";
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	Button mScan;

	private UUID applicationUUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ProgressDialog mBluetoothConnectProgressDialog;
	private BluetoothSocket mBluetoothSocket;

	private TextToSpeech tts;

	// spp
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;

	private boolean flag = false;

	private boolean _ACK = false;

	private int acks = 0;

	public int temp_int = 0;

	public int countpacket = 1;

	private int j = 0;

	private int image_code = 1;

	private long send_data_hash = 0;

	private int hash_counter = 0;

	private int _command = 1;

	private String recent_command_sent = "";
	private static final String ACK = "<ACK>";
	private static final String ACKD = "<ACKD>";
	
	private static byte[] dataBytes;
	
	private static String FILENAME="rtos_test_program.bin";
	private static int packet_size=5000;
	private static int FILESIZE=0;
	
	private boolean isEnd_of_datachunk=false;
	private boolean isEnd_of_BulkData=false;

	HeaderImage hm;
	Commands com;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ctx = getApplication();
		Button sendButton = (Button) findViewById(R.id.button1);
		Button connectButton = (Button) findViewById(R.id.button2);
		
		final EditText edsize=(EditText) findViewById(R.id.editText1);
		final EditText edfile=(EditText) findViewById(R.id.editText2);
		
		

		hm = new HeaderImage();
		com = new Commands();
		
		
	//	dataBytes=hm.getDataArray(10000);

		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				countpacket = 0;
				temp_int = 0;
				_ACK = false;
				acks = 0;
				j = 0;
				image_code = 1;

				FILENAME=edfile.getText().toString();
				packet_size=Integer.parseInt(edsize.getText().toString());
				
				dataBytes=getFromSdcard(FILENAME);
				
				FILESIZE=dataBytes.length;
				
				
				try {
				//	 sendDataX();

					//sendActualfile();
					sendCommand(1);
					// mmOutputStream.write(test.getBytes());
					Log.e(TAG, "sent");

					// Thread.sleep(1000);
					beginListenForData();
					// readdata();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// sendImage();
				// writeImage();
			}
		});

		connectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub

				try {
					// if(!flag){

					findBT();
					openBT();
					// flag=!flag;

				} catch (IOException e) {
					Log.e("TAG", "Exception :::" + e.getMessage());
				}

			}
		});

		// IntentFilter filter = new IntentFilter("SOMEACTION");
		// this.registerReceiver(bcr, filter);

		// Intent i = new Intent(IBluetoothHeadset.class.getName());
		//
		// if (bindService(i, HSPConnection, Context.BIND_AUTO_CREATE)) {
		//
		// } else {
		// Log.e("HSP FAILED", "Could not bind to Bluetooth HFP Service");
		// }

		// Test for reading header files

	//	convertHeaderfile();

	}

	public void convertHeaderfile() {

		byte[] bx = getFromAssets("Image1.h");

		byte[] dx = new byte[bx.length - 1];

		for (int i = 0; i < bx.length - 1; i++) {

			if (bx[i] == '{') {
				int j = 0;
				for (int k = i + 1; k < bx.length - 1; k++) {
					dx[j] = bx[k];
					j++;
				}
			}
		}

		for (int i = 0; i < dx.length; i++) {
			//Log.e(TAG, "DX value :" + dx[i]);
		}

		// Log.e(TAG," Vstring : "+v);

		Log.e(TAG, "LEN : .." + bx.length);

	}

	public ServiceConnection HSPConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			Log.e("AA", "OnService Connected");
			ibth = IBluetoothHeadset.Stub.asInterface(service);
			// ibth instance used to connect and disconnect headset afterwards
			Intent intent = new Intent();
			intent.setAction("HEADSET_INTERFACE_CONNECTED");
			// same as the one we register earlier for broadcastreciever
			ctx.sendBroadcast(intent);
			// ctx is Instance of Context
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			ibth = null;
			Log.e("AA", "OnService DisConnected");
		}

	};

	BroadcastReceiver bcr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			Log.e("AA", "OnReceive");

			String action = intent.getAction();
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Device found
			} else if ("HEADSET_INTERFACE_CONNECTED".equals(action)) {
				if (ibth != null) {
					try {
						ibth.connect(mmDevice);
						Log.e("AA", " HFP Device Connected");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
					Log.e("AA", "ibth null");
			}

		}
	};

	void findBT() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.e("SSP", "No bluetooth adapter available");
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, 0);
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals("Arya-Klugtek")) {
					mmDevice = device;
					break;
				}
			}
		}
		Log.e("SPP", "Bluetooth Device Found");
	}

	void openBT() throws IOException {
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
																				// //SerialPortService
																// ID
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
		mmSocket.connect();
		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();
		// beginListenForData();

		Log.e("SPP", "Bluetooth Opened");
	}

	void beginListenForData() {
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
		
		Log.e(TAG,"Listening..");// character
		
		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable() {
										public void run() {
											Log.e(TAG, "rrRecieved data" + data);
											Log.e(TAG,"rrRecieved data length :"+data.length());

											checkrecievedData(data);

											// checkhash(data);

										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}

	public void checkrecievedData(String data) {

		Log.e(TAG, "xxReceived Command  :"+data); 
		
		
		
		try{
			
			
			if(data.contains(com.LOOPBACK_TEST_BYTE)){
				_command=2;
				sendCommand(_command);
				
			}
			
			
		if (data.contains(ACK)) {
			_ACK = true;
			Log.e(TAG, "No Of ACKS :" + acks);

			acks++;

			
		
			
			if(data.contains(com.FIRM_VER)){
				//Check for latest version
				sendCommand(_command);
				
				
			}
			
			
			
			if(recent_command_sent.contains(com.FIRM_UPDATE_REQ))
			{
				
				String s=com.FILE_SIZE+FILESIZE+">";
				sendCommand(s);
				recent_command_sent=com.FILE_SIZE;
				_command++;
				Log.e(TAG,"ccFILESIze   :"+s);
			}
			
			else	if(recent_command_sent.contains(com.FILE_SIZE))
			{
				Log.e(TAG,"ccFILE Address");
				String s=com.FILE_ADDRESS+"51200>";
				sendCommand(s);
				
				recent_command_sent=com.FILE_ADDRESS;
				_command++;
				
			}
			
			else 	if(recent_command_sent.contains(com.FILE_ADDRESS))
			{
				Log.e(TAG,"ccFIRMWARE_VER");
				String s=com.FIRMWARE_VER+"V1.00>";
				sendCommand(s);
				recent_command_sent=com.FIRMWARE_VER;
				_command++;
				
			}
			
			
			
			else	if(recent_command_sent.contains(com.FIRMWARE_VER))
			{
				
				String s=com.DATA_CHUNK_SIZE+packet_size+">";
				sendCommand(s);
				recent_command_sent=com.DATA_CHUNK_SIZE;
				_command++;
				Log.e(TAG,"ccDATACHUNK_SIZE");
			}
			
			else	if(recent_command_sent.contains(com.DATA_CHUNK_SIZE))
			{
				
				String s=com.ERASE_FLASH;
				sendCommand(s);
				recent_command_sent=com.ERASE_FLASH;
				_command++;
			}
			
			else	if(recent_command_sent.contains(com.ERASE_FLASH))
			{
				
				String s=com.RECEIVE_BULK_DATA;
				sendCommand(s);
				recent_command_sent=com.RECEIVE_BULK_DATA;
				_command++;
			}
			
			
			
				
		} else if(data.contains(ACKD)){
			
			
			 if (isEnd_of_datachunk){
				
				String s=com.getCommand(20)+send_data_hash+">";
				
				Thread.sleep(100);
				sendCommand(s);
				recent_command_sent=com.HASH;
				Log.e(TAG,"ccsenddatahash  :"+s);
			}
			if(recent_command_sent.contains(com.RECEIVE_BULK_DATA)){
				Log.e(TAG,"ccsenddatax");
				sendDataX();
			}
			
			
			
			
	
			
			
			
			
		}
		
		if(data.contains(com.FIRMWARE_VER)){
			sendCommand(3);
			_command++;
			Log.e(TAG,"ccFIRM_VER");
		}

		if(data.contains(com.FLASH_PROGRAMMED)){
			sendDataX();
			
			if(isEnd_of_BulkData){
				String s=com.END_OF_BULK_DATA;
				sendCommand(s);
				recent_command_sent=com.END_OF_BULK_DATA;
				
			}
			
		}
		
		if(data.contains(com.FIRMWARE_UPDATE_SUCCESS)){
			
			Log.e(TAG, "Update Success");
				
		}else if(data.contains(com.FIRMWARE_UPDATE_FAILURE)){
			
			Log.e(TAG, "Update failed");
		}
		
		
		
		if(data.contains(com.INCORRECT_DATA)){
			
			temp_int=temp_int-packet_size;
			sendDataX();
			
		}

		
		Log.e(TAG,"xxRecent Command Sent :"+recent_command_sent);
		
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public void sendCommand(int _cmd) {

		Log.e(TAG,"xxcmd :"+_cmd);
		String msg = com.getCommand(_cmd);

		recent_command_sent = msg;

		// Boolean flagx = false;

		try {

			byte[] bytearry = msg.getBytes();

			byte[] bits = new byte[22];

			int count = 0;

			for (int i = 0; i < bytearry.length; i++) {

				// result += (char) b;
				bits[i] = bytearry[i];

			}
			Log.e("TAG", "Commandlength " + msg.length());

			mmOutputStream.write(bits);

			_command++;

			Log.e("SPP", "Command Sent "+recent_command_sent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendCommand(String msg) {

		
		

		recent_command_sent = msg;

		// Boolean flagx = false;

		try {

			byte[] bytearry = msg.getBytes();

			byte[] bits = new byte[22];

			int count = 0;

			for (int i = 0; i < bytearry.length; i++) {

				// result += (char) b;
				bits[i] = bytearry[i];

			}
			Log.e("TAG", "Commandlength " + msg.length());

			mmOutputStream.write(bits);

			_command++;

			Log.e("SPP", "Command Sent "+recent_command_sent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendDataX() {

		Log.e(TAG,"senddatax-->");
	/*	String data = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		// String
		// msg=data+data+data+data+data+data+data+data+"Packet1-abcdefghijklPacket2-abcdefghijklPacket3-abcdefghijklPacket4-abcdefghijklPacket5-abcdefghijk";
		// +data+data+data;
		// +data+data+data+data+data;
		String msg = data + data + data + data + data + data + data + data + data +data;
*/
		
		byte[] value = readdatabybytes();
		
	//	byte[] value=readfilebybytes();
		
	//	byte[] value = msg.getBytes();
		
		byte [] btvalue=new byte[packet_size];
		

		
		
		
		
			if(value.length<packet_size){
				
				for(int i=0;i<value.length;i++){
					btvalue[i]=value[i];
				}
				send_data_hash = doHash(value);
				
			}else{
				for(int i=0;i<btvalue.length;i++){
					btvalue[i]=value[i];
			}
				send_data_hash = doHash(btvalue);
		}
		
		
		
		
		Log.e(TAG,"Value length"+value.length);

		ByteArrayInputStream bis = new ByteArrayInputStream(btvalue);
		int n = 0;
		int index = 0;

		byte[] buffer = new byte[packet_size];
		byte[] bits = new byte[packet_size];

		try {

			while ((n = bis.read(buffer)) > 0) {
				String result = "";

				int i = 0;

				for (byte b : buffer) {

					result += (char) b;
					bits[i] = b;
				
					i++;

				}

				

			
				if(value.length>0)
				mmOutputStream.write(bits);
				
				
				Log.e(TAG,"Result Length :"+bits.length);

				Log.e(TAG, "Send Data Hash" + send_data_hash);

				
				if(value.length>0){
					isEnd_of_datachunk=true;
					
				}
				else{
				    isEnd_of_BulkData=true;
				    isEnd_of_datachunk=false;
				}

			

				countpacket++;
				
				Log.e(TAG,"ccPackets sent :"+countpacket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void readdata() {

		Log.e(TAG, "READ");
		byte[] bytearray = new byte[1024];
		int bytes;
		while (true) {
			try {
				// DataInputStream dinput = new DataInputStream(mmInputStream);

				// dinput.readFully(bytearray, 0, bytearray.length);

				bytes = mmInputStream.read(bytearray);

				// Send the obtained bytes to the UI activity

				// mmInputStream.read(bytearray);

				Log.e(TAG, "Data read length:" + bytes);

				Log.e(TAG, "Data read :" + bytearray.toString());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Exception " + e.getMessage());
			}
		}

	}

	public void writeImage() {

		AssetManager mngr = getApplicationContext().getAssets();
		try {
			InputStream is = mngr.open("img1.png");

			char[] c = new char[1024];
			mmOutputStream.write(c.toString().getBytes());

			mmOutputStream.write(is.toString().getBytes());

			long hash = doHash(is.toString().getBytes());
			Log.e(TAG, "HASH Value is " + hash);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void closeBT() throws IOException {
		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		Log.e("SPP", "Bluetooth Closed");
	}

	private void showMessage(String theMsg) {
		Toast msg = Toast.makeText(getBaseContext(), theMsg,
				(Toast.LENGTH_LONG) / 160);
		msg.show();
	}

	public void speakOut(final String text) {

		Log.e("TTS", "SpeakOut-->>>");

		tts = new TextToSpeech(getApplicationContext(), new OnInitListener() {

			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
				if (status == TextToSpeech.SUCCESS) {

					int result = tts.setLanguage(Locale.US);

					if (result == TextToSpeech.LANG_MISSING_DATA
							|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Log.e("TTS", "This Language is not supported");
					} else {

						tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
					}

				} else {
					Log.e("TTS", "Initilization Failed!");
				}

			}
		});

	}

	public void sendImage() {

		Log.e(TAG, "SendImg");
		File sourceFile = new File("//mnt/sdcard/img1.png");

		Bitmap bit_img = BitmapFactory.decodeFile("ImageD2.jpg");

	}

	public void checkhash(String data) {

		int x = 0;

		Log.e(TAG, " recieved data length" + data.length());

		String _data = data.substring(0, 800);

		long rc_hash = doHash(_data.getBytes());

		Log.e(TAG, " recieved data HASH " + rc_hash);

		if (send_data_hash == rc_hash) {

			Log.e(TAG, "  HASH :" + x);

		} else {

			Log.e(TAG, "  HASH :" + hash_counter++);
		}

		sendDataX();

	}

	private long doHash(byte[] b) {
		int hash = 0;

		for (int i = 0; i < b.length; i++) {
			hash += (int) ((int) b[i] & 0xFF);
			hash = hash + (hash << 10);

			hash ^= (hash >> 6);

			// hash = ((((hash << 5))) + (hash)) + b[i];
		}

		hash = hash + (hash << 3);

		hash ^= (hash >> 11);

		hash = hash + (hash << 15);

		return hash;
	}

	public byte[] headerfile(int img_id) {

		char a[] = hm.getImageArray(img_id);

		int loopcnt = 0;

		char[] b = new char[800];

		if (j == 1600) {

			loopcnt = 220;
		} else {
			loopcnt = 600;
		}

		// Log.e(TAG,"loopcnt"+loopcnt);

		Log.e(TAG, "J" + j);
		Log.e(TAG, "IMAGE Sending  :" + image_code);

		if (j <= a.length) {

			for (int i = 0; i < loopcnt; i++) {

				// Log.e(TAG,"J"+j);

				b[i] = a[i + j];

			}

			j = j + 800;

		}

		byte[] x = null;
		try {
			x = new String(b).getBytes("UTF-8");

			for (int i = 0; i < 20; i++) {
				// Log.e(TAG,i + ": " + barray[i]);
				Log.e(TAG, i + "xx: " + x[i] + "Length :" + x.length);
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return x;
	}

	public byte[] readfilebybytes() {

	

		AssetManager assetManager = getResources().getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}

		byte[] fileBytes = new byte[0];

		if (files != null) {
			String filename = "rtos_test_program.bin";
			InputStream in = null;

			try {

				in = assetManager.open(filename);

				in.skip(temp_int);
				fileBytes = new byte[in.available()];
				in.read(fileBytes);
				in.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		temp_int = temp_int + 1000;

		return fileBytes;
	}

	public byte[] getFromSdcard(String _filename) {
		
		
		byte[] fileBytes = new byte[0];
		
		Log.e("Filename",""+_filename);
		
		String folder = "/mnt/sdcard/boot/".concat(_filename).trim();
		
		//String folder=fo.concat(_filename);
		
		
		File dir=new File(folder);
		
		Log.e("File specs:","Path:"+dir.getAbsolutePath() +"Fi" );
		
		
		
		
		if (!dir.exists()) {
			
			Toast.makeText(getApplicationContext(), "File Does not Exists in Folder bootloader. "+dir, Toast.LENGTH_LONG).show();
			
		}else{
			
			FileInputStream in=null;
			
			try {
				in=new FileInputStream(folder);
				fileBytes=new byte[in.available()];
				in.read();
				in.close();
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Given File does not exists", Toast.LENGTH_LONG).show();
			
			}
			
			
			

			Toast.makeText(getApplicationContext(), "File Exists.", Toast.LENGTH_LONG).show();
			
			
		}
		
		
		return fileBytes;
		
	}
	
	
	public byte[] getFromAssets(String _filename) {

		AssetManager assetManager = getResources().getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}

		byte[] fileBytes = new byte[0];

		if (files != null) {
			String filename = _filename;
			InputStream in = null;

			try {

				in = assetManager.open(filename);

				// in.skip(temp_int);
				fileBytes = new byte[in.available()];
				in.read(fileBytes);
				in.close();

			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Given File does not exists", Toast.LENGTH_LONG).show();
			}

		}else {
			
			Log.e(TAG,"File List is Empty ");
		}

		return fileBytes;

	}
	
	
	public byte[] readdatabybytes(){
		
		byte[] mb=dataBytes;
		byte[] fileBytes = new byte[0];
		
		if(dataBytes.length>0){
		
				try {

					InputStream in = new ByteArrayInputStream(mb);
					in.skip(temp_int);
					fileBytes = new byte[in.available()];
					in.read(fileBytes);
					in.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			
			
			temp_int=temp_int+packet_size;
			
			
			
		}
		Log.e(TAG,"ccFilebytesLength"+fileBytes.length);
		
		return fileBytes;
		
	}

	public class HeaderImage {
		/*
		char image1[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0 };
		char image2[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image3[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image4[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0 };
		char image5[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image6[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image7[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image8[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image9[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image10[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		
	
		char image11[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		
		*/
		
		char image1[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image2[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0}; 
		char image3[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image4[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image5[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image6[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image7[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image8[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image9[]={  0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		char image10[]={ 0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,   31,   0,   31,   0,   31,   0,   31,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0,   0,    0};
		
				
		
		String data = "Packet1-abcdefghijklPacket2-abcdefghijklPacket3-abcdefghijklPacket4-abcdefghijklPacket5-abcdefghijkl";
	
		String data1000=data+data+data+data+data+data+data+data+data+data;
		
		
		
		String data500=data+data+data+data+data;
		
		String data1500=data1000+data500;
		String data2000=data1000+data1000;
		String data2500=data2000+data500;
		String data3000=data1000+data1000+data1000;
		String data3500=data3000+data500;
		String data4000=data2000+data2000;
		String data4500=data4000+data500;
		String data5000=data2000+data3000;
		
		String data10k=data5000+data5000;
		String data20k=data10k+data10k;
		
		
		
		
		
		HashMap<Integer, char[]> map;
		
		HashMap<Integer, String> mapdata;
		
		
		
		
		public HeaderImage(){
			
			setImageArray();
			setdataArray();
		}
		
		
          public void setdataArray(){
			
            mapdata =new HashMap<Integer, String>();
            
            
            mapdata.put(500,data500);
            mapdata.put(1000, data1000);
            mapdata.put(1500, data1500);
            mapdata.put(2000, data2000);
            mapdata.put(2500, data2500);
            mapdata.put(3000, data3000);
            mapdata.put(3500, data3500);
            mapdata.put(4000, data4000);
            mapdata.put(4500, data4500);
            mapdata.put(5000, data5000);
            mapdata.put(10000, data10k);
            mapdata.put(20000, data20k);
            
            
            
			
			
		}
		
		
		
		public void setImageArray(){
			
            map =new HashMap<Integer, char[]>();
			
			map.put(1, image1);
			map.put(2, image2);
			map.put(3, image3);
			map.put(4, image4);
			map.put(5, image5);
			map.put(6, image6);
			map.put(7, image7);
			map.put(8, image8);
			map.put(9, image9);
			map.put(10, image10);
//			map.put(11, image11);
		}
		
		
		
		
		
		public char[] getImageArray(int img_id){
			
			Log.e(TAG,"HASHMAP hh");

			return map.get(img_id);
		}
		
		
		public byte[] getDataArray(int id){
			
			
			if(id>=1000 && id<=20000 && id%500==0){
			//set bytelength
		//	bytex=id;
		//	readbytex=id+20;
			
			}else{
				Toast.makeText(getApplicationContext(), "Data Length must be multiple of 500", Toast.LENGTH_LONG).show();
			
			}
			
			return mapdata.get(id).getBytes();
		}
		
	}
	
	  public void sendActualfile(){



	        boolean isFlag=false;
	        boolean sendflag=false;
	        boolean bufferfull=false;

	   
	        Log.e("Packets","Send Actual file entered");
	         byte[] value;
	        String bucket="";



	        try {


	        	
	    		byte [] btvalue=new byte[packet_size];
	    		

	    		
	    		
	    		
	        	

	            value=readfilebybytes();
	            
	            for(int i=0;i<btvalue.length;i++){
	    			btvalue[i]=value[i];
	    		}

	            long x=doHash(btvalue);

	            Log.e("Hash","hash value of whole file :"+x);
	            Log.e("Packet","Value length is :"+value.length);
	           //lastpacketsize=String.valueOf(value.length);



	            ByteArrayInputStream bis = new ByteArrayInputStream(btvalue);
	            int n = 0;
	            int index=0;
	           int countpacket=1;
	             byte[] buffer = new byte[1000];
	            byte[] bits=new byte[1000];
	            





	            byte[] ackvalue;
	         //   ackvalue=end_of_data_chunk.getBytes("UTF-8");


	            
	            
	            	


	            while ((n = bis.read(buffer)) > 0) {
	                String result = "";

	                int i=0;


	                for (byte b : buffer) {

	                    result += (char) b;
	                    bits[i]=b;
	                 //   Log.e("Bits","bits content"+bits[i]);
	                  //  Log.e("Char b","bits char "+(char)b);
	                    i++;
	                    
	                    
	                }
	                
	               

	            //    Log.e("Bits","bits length"+bits.length);


	              




	                   // temp_storage+=result;

	                    Arrays.fill(buffer, (byte) 0);













	                	
	                	


	                

	                      



	                countpacket++;
	            }




	          

	        } catch (IOException e) {
	            // TODO Auto-generated catch block

	            Log.e("ReadFile","Exception in BTnsend "+e.getMessage());
	            e.printStackTrace();
	        }



	    }

}
