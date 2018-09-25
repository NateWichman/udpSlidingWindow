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

class udpserver{
    public static void main(String args[]){
	try{
	    udpserver manager = new udpserver();
	    Scanner scnr = new Scanner(System.in);
	    System.out.println("Enter a port number");
	    int portNumber = scnr.nextInt();
	    
	    DatagramChannel c = DatagramChannel.open();
	    Selector s = Selector.open();
	    c.configureBlocking(false);
	    c.register(s,SelectionKey.OP_READ);
	    c.bind(new InetSocketAddress(portNumber));

	    File file;
	    BufferedReader reader;

	    while(true){
		/*select checks the channels to see
		//if we can do the operations.
		//it returns the numkber or channels
		//that we can do that operation on */
		int n = s.select(5000);
		if(n == 0){
		    System.out.println("got a timeout");
		}else{
		    Iterator i = s.selectedKeys().iterator();
		    while(i.hasNext()){
			SelectionKey k = (SelectionKey)i.next();
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
			}
			else{
				System.out.println("File Not Found");
			}

			ByteBuffer buffer2 = ByteBuffer.allocate(4096);
			c.receive(buffer2);
			String example = new String(buffer2.array());
			System.out.println("Example is: " + example);

			byte[] fileContent = Files.readAllBytes(file.toPath());
			
			byte[] test = "check".getBytes();
			ByteBuffer buf2 = ByteBuffer.wrap(test);
			c.send(buf2, clientaddr);

			int size = fileContent.length;
			System.out.println("Size of file: " + size);

			int numPackets = (size / 1024) + 1;
			
			ArrayList<Integer> acks = new ArrayList<Integer>();
			boolean canSend = true;
			int counter = 0;
			int upperLim = 4;
			int lowerLim = 0;

			for(int j = 0; j < numPackets;  j++){
				if(canSend == true){
					ByteBuffer buffer = ByteBuffer.allocate(4096);
					byte[] packet = Arrays.copyOfRange(fileContent, (j*1024), ((j+1)*1024));
					manager.sendPacket(c, packet, clientaddr); 
				}
				
				ByteBuffer ackBuf = ByteBuffer.allocate(4096);
				c.receive(ackBuf);
				String ack = new String(ackBuf.array());
				if(ack.charAt(0) == 'c'){
					ack = ack.substring(1);
					System.out.println("Acknowledgment Received" + ack);
					ack = ack.replaceAll("\0+","");
					acks.add(Integer.parseInt(ack));
					canSend = true;
				}
				if(counter >= upperLim){
					if(!acks.contains(lowerLim)){
						canSend = false;
						j--;
						counter--;
						System.out.println("Sliding Window limit reached, pausing for acknowledment");
					}
					else{
						lowerLim++;
						upperLim++;
						System.out.println("Sliding Window up by 1");
					}
			        }
				counter++;
				System.out.println(acks);
				/*if(counter >= upperLim){
					if(!acks.contains(lowerLim)){
						canSend = false;
						j--;
						counter--;
						System.out.println("Sliding Window Limit Reached, pausing for Acknowledment");
					}
					else{
						lowerLim++;
						upperLim++;
						System.out.println("Sliding Window up by 1");
					}
				}
				else{
					canSend = true;
				}*/
				
			}
			
		
			byte[] termination = "done".getBytes();
			ByteBuffer buf3 = ByteBuffer.wrap(termination);
			c.send(buf3, clientaddr);
		//	manager.sendPacket(c, termination, clientaddr);

			
			//manager.sendPacket(c, fileContent, clientaddr);
		        
	
			
			System.out.println("Finished");
			c.send(buf3, clientaddr);

		    }
		}
	    }
	}catch(IOException e){
	    System.out.println("Error");
	}
    }

    private void sendPacket(DatagramChannel c, byte[] packet, SocketAddress clientaddr) {
	    try{
		ByteBuffer buf = ByteBuffer.wrap(packet);
		c.send(buf, clientaddr);
	    }catch(IOException e){
		    System.out.println("Error Sending Packet");
	    }
    }

    private int receiveAck(String message){
	    switch(message){
		    case "1":
			    return 1;
			   
		    case "2":
			    return 2;
			   
	            case "3":
			    return 3;
			    
	            case "4":
			    return 4;
			    
	            case "5":
			    return 5;

	            case "6":
			    return 6;
	            
			    
		    default:
			    return 0;
	    }
    }

}
