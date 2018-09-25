import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.File;
import java.util.Scanner;

/************************************************************************
UDP server using Datagram Sockets. It recieves a file name from the client
and then proceeds to send that file to the client via the sliding window
model.

@author Nathan Wichman
@version September 2018
*************************************************************************/
class udpserver{
    public static void main(String args[]){
	try{
	    /** Creating an object of this class to use private helper methods **/
	    udpserver manager = new udpserver();

	    /** Recieving the port number from the user **/
	    Scanner scnr = new Scanner(System.in);
	    System.out.println("Enter a port number");
	    int portNumber = scnr.nextInt();
	    
	    /**Setting up the datagram channel and selectore **/
	    DatagramChannel c = DatagramChannel.open();
	    Selector s = Selector.open();
	    c.configureBlocking(false);
	    c.register(s,SelectionKey.OP_READ);
	    c.bind(new InetSocketAddress(portNumber));

	    /** Declaring file and Buffer reader variabler **/
	    File file;
	    BufferedReader reader;

	    while(true){
		int n = s.select(5000);
		if(n == 0){
		    System.out.println("got a timeout");
		}else{
		    Iterator i = s.selectedKeys().iterator();
		    while(i.hasNext()){
			SelectionKey k = (SelectionKey)i.next();

			/** Receiving file name from client **/
			ByteBuffer buf = ByteBuffer.allocate(4096);
			SocketAddress clientaddr = c.receive(buf);
			String fileName  = new String(buf.array());
			System.out.println("Received File Name: " + fileName);
			i.remove();
			
			/** Removing null charachters from the revieved string **/
			fileName = fileName.replaceAll("\0+$", "");
			
			/** Searching for file / opeing if exists **/
		        file = new File(fileName);
			if(file.exists() && !file.isDirectory()){
		            	reader = new BufferedReader(new FileReader(fileName));
			
			/** Testing if Acknowledgments can be gotten from the client (for debugging) **/
			ByteBuffer buffer2 = ByteBuffer.allocate(4096);
			c.receive(buffer2);
			String example = new String(buffer2.array());
			System.out.println("Recieved From Client: " + example);

			/** Reading in the file into a byte array **/
			byte[] fileContent = Files.readAllBytes(file.toPath());
			
			/** Testing connection with the Client (For Debugging) **/
			byte[] test = "check".getBytes();
			ByteBuffer buf2 = ByteBuffer.wrap(test);
			c.send(buf2, clientaddr);

			/** Getting File Size **/
			int size = fileContent.length;
			System.out.println("Size of file: " + size);

			/** Finding the number of packets the Server needs to send, with a max size of 1024 bytes **/
			int numPackets = (size / 1024) + 1;
			
			/** Holds the acknoledgments received **/
			ArrayList<Integer> acks = new ArrayList<Integer>();

			/** Determins if the Server can send to the client **/
			boolean canSend = true;

			/** counting variable for the loop **/
			int counter = 0;

			/** Upper and lower limits of the Sliding Window **/
			int upperLim = 4;
			int lowerLim = 0;

			for(int j = 0; j < numPackets;  j++){
				if(canSend == true){
					/** Sending packet **/
					ByteBuffer buffer = ByteBuffer.allocate(4096);
					byte[] packet = Arrays.copyOfRange(fileContent, (j*1024), ((j+1)*1024));
					manager.sendPacket(c, packet, clientaddr); 
				}
				
				/** Receiving Acknowledgment **/
				ByteBuffer ackBuf = ByteBuffer.allocate(4096);
				c.receive(ackBuf);
				String ack = new String(ackBuf.array());

				/** First letter of Acknowledment is always c **/
				if(ack.charAt(0) == 'c'){
					/** Removing the 'c' leaving just an integer in the string **/
					ack = ack.substring(1);
					System.out.println("Acknowledgment Received" + ack);

					/** removing unneccecary null charachters **/
					ack = ack.replaceAll("\0+","");

					/** converting into an integer **/
					acks.add(Integer.parseInt(ack));
					canSend = true;
				}

				/** Checking to see if we have reached the sliding window limit **/
				if(counter >= upperLim){
					if(!acks.contains(lowerLim)){
						canSend = false; //if reached, sending stops
						j--; //repeating this iteration of the for loop
						counter--;
						System.out.println("Sliding Window limit reached, pausing for acknowledment");
					}
					else{
						/** If the lower limit is in the arrayList, we can adjust the window and continue
						 * sending **/
						lowerLim++;
						upperLim++;
						System.out.println("Sliding Window up by 1");
					}
			        }
				counter++;
			}
			
			/** Sending Termination Code to Client so it knows to end its receiving
			 * and compile the file **/
			byte[] termination = "done".getBytes();
			ByteBuffer buf3 = ByteBuffer.wrap(termination);
			c.send(buf3, clientaddr);
		        
			System.out.println("Finished");
			}else{
				System.out.println("File Not Found");
			}
		    }
		}
	    }
	}catch(IOException e){
	    System.out.println("Error");
	}
    }

/************************************************************************************************
Private helper method to send a packet to the client.

@param A datagramChannel, a packet to send, and the clients socket address
@returns void
************************************************************************************************/
 private void sendPacket(DatagramChannel c, byte[] packet, SocketAddress clientaddr) {
	    try{
		ByteBuffer buf = ByteBuffer.wrap(packet);
		c.send(buf, clientaddr);
	    }catch(IOException e){
		    System.out.println("Error Sending Packet");
	    }
    }
}
