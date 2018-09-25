import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;

class udpclient{
    public static void main(String args[]){
	try{
	    Scanner scnr = new Scanner(System.in);
	    System.out.println("Enter a port number: ");
	    int portNumber = scnr.nextInt();
	    System.out.println("Enter an IP address: ");
	    scnr.nextLine();
	    String ipAddress = scnr.nextLine();
	    System.out.println("IpReceived: " + ipAddress);
	    
	    //Datagram socket. Even though it doesnt have the word socket in it
	    //Remember a DatagramChannel is a Datagram socket
	    DatagramChannel sc = DatagramChannel.open();
	    Console cons = System.console();
	    String fileName  = cons.readLine("Enter a File Name: ");
	    ByteBuffer buf = ByteBuffer.wrap(fileName.getBytes());
	    sc.send(buf, new InetSocketAddress((ipAddress),portNumber));
            
	    String example = "1";
	    ByteBuffer buffer = ByteBuffer.wrap(example.getBytes());
	    sc.send(buffer, new InetSocketAddress((ipAddress),portNumber));


	    ByteBuffer buf3 = ByteBuffer.allocate(5000);
	    sc.receive(buf3);
	    buf3.flip();
	    byte[] b = new byte[buf3.remaining()];
	    String out = new String(buf3.array());
	    System.out.println("Test: " + out);
	    out = out.replaceAll("\0+","");
	    if(out.equals("check")){
		    System.out.println("Test Passes");
	    }else{
		    System.out.println("Test Failed");
	    }
	    

	    File outputFile = new File("output.txt");
	    FileOutputStream outStream = new FileOutputStream("output.txt");
	    

	    byte[] receivedData = {};
	    byte[] temp;
	    int acknowledgment = 0;
	    while(true){
		    ByteBuffer buf2 = ByteBuffer.allocate(5000);
		    sc.receive(buf2);
		    buf2.flip();
		    byte[] a = new byte[buf2.remaining()];
		    String test = new String(buf2.array());
		    test = test.replaceAll("\0+$", "");
		    buf2.get(a);
		    if(test == null){
			    System.out.println("null");
		    }
		    if(test.equals("done")){
			    System.out.println("done");
			    break;
		    }else{
		//	    System.out.println("test = " + test);
		    }

		    byte[] combo = new byte[a.length + receivedData.length];
		    
		    System.arraycopy(receivedData, 0, combo,0, receivedData.length);
		    System.arraycopy(a, 0, combo, receivedData.length, a.length);

		    receivedData = combo;
	//	    outStream.write(receivedData);
		    System.out.println("Bytes Received: " + a);
		    
		    
	  	    String ack = "c" + Integer.toString(acknowledgment);
		    ByteBuffer buf4 = ByteBuffer.wrap(ack.getBytes());
		    sc.send(buf4, new InetSocketAddress((ipAddress), portNumber));
		    acknowledgment++;

	    }

    	    
	    if(receivedData != null){
	  	  outStream.write(receivedData);
	    }else{
		    System.out.println("ReceivedData was empty");
	    } 

/*	    ByteBuffer buf2 = ByteBuffer.allocate(5000);
	    sc.receive(buf2);
	    buf2.flip();
	    byte[] a = new byte[buf2.remaining()];
	    buf2.get(a);
	    if(a != null){
		File outputFile = new File("output.txt");
		FileOutputStream outStream = new FileOutputStream("output.txt");
		outStream.write(a);
	    } */

/*
	   // String mes = "1"; /*
	    String mes = "1";
	    ByteBuffer buf3 = ByteBuffer.wrap(mes.getBytes());
	    sc.send(buf3, new InetSocketAddress((ipAddress), portNumber)); */
	    sc.close();

	}catch(IOException e){
	    System.out.println("Error happened\n");
	}
    }
}
