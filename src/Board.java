/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author chiragsalian
 */
public class Board extends UnicastRemoteObject implements BoardInterface {

    /**
	 * 
	 */
	public static class botInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 571053781172768948L;
		String ipAddr;
		int portNo;
		int[] coord;
		int electionNum;
		int uniqueID;
		String botKey;
		
		public botInfo(String ipAddr, int portNo, int[] coord, int electionNum, int uniqueID, String botKey) {
			this.ipAddr = ipAddr;
			this.portNo = portNo;
			this.coord = coord;
			this.electionNum = electionNum;
			this.uniqueID = uniqueID;
			this.botKey = botKey;
		}
	}
	
	class notifyBots implements Runnable {
		BotInterface bInt;
		
		notifyBots(BotInterface bInt) {
			this.bInt = bInt;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				bInt.setAllBotsJoinedFlag();
				System.out.println("bot set flag");
			}
			catch(RemoteException e){
				System.out.println("RE");
				e.printStackTrace();
			}
		}
	}
	
	class startLeader implements Runnable {
		BotInterface bInt;
		
		startLeader(BotInterface bInt) {
			this.bInt = bInt;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				bInt.start();
			}
			catch(RemoteException e){
				System.out.println("RE");
				e.printStackTrace();
			}
		}
	}
	
	class setLeader implements Runnable {
		Board b;
		
		setLeader(Board b) {
			this.b = b;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			b.setLeader();
		}
	}
	
	
	private static final long serialVersionUID = -8972822707112908407L;
	public ArrayList<int[]> coordinates;
	public static ArrayList<botInfo> bots;
    public int[] objectPosition = {-1,-1};
    public boolean objectPosKnown = false;
    int sizeHeight, sizeWidth;
    private int botCounter = 0;
    private int totalBots = 18;
    private int[] electionBucket = new int[totalBots];
    private int leaderIndex = -1;
    public static HashMap<String, Integer> keyCollection = new HashMap<String, Integer>(); 
    public static String boardIP;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Board b;
		try {
			boardIP = InetAddress.getLocalHost().toString().split("/")[1];	// need to hardcode IP for linux
			b = new Board(50,50);
			b.setObjectPosition();
        
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
    }

    public void test() {
    	Registry objReg;
		try {
			//objReg = LocateRegistry.getRegistry("129.21.64.18", 5000);
			objReg = LocateRegistry.getRegistry(boardIP, 5000);
			BotInterface tempBotInterface = (BotInterface)objReg.lookup("Bot");
			tempBotInterface.setAllBotsJoinedFlag();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void invokeLeader() {
		// TODO Auto-generated method stub
		botInfo Leaderbot = bots.get(leaderIndex);
		Registry objReg;
		BotInterface tempBotInterface;
		
		try {
			System.out.println("Number of bots: "+bots.size());
			for(botInfo bTemp : bots) {
				System.out.println(bTemp.uniqueID+" : "+bTemp.ipAddr+" : "+bTemp.portNo);
				objReg = LocateRegistry.getRegistry(bTemp.ipAddr, bTemp.portNo);
				tempBotInterface = (BotInterface)objReg.lookup("Bot");
//				new Thread(new notifyBots(tempBotInterface)).start();
				//Added By Sid - 131215
				//---------------------------------------------------------------------------------------------------
				
				tempBotInterface.setAllBotsJoinedFlag();  //Threading will be handled by the bot
				
				//---------------------------------------------------------------------------------------------------
			}
			
			System.out.println("Notifying Leader bot.");
			
			objReg = LocateRegistry.getRegistry(Leaderbot.ipAddr, Leaderbot.portNo);
			tempBotInterface = (BotInterface)objReg.lookup("Bot");
			
//			new Thread(new startLeader(tempBotInterface)).start();
			//Added By Sid - 131215
			//---------------------------------------------------------------------------------------------------
			tempBotInterface.start();//Start the bot
			System.out.println("Leader bot notified to start working");
			//---------------------------------------------------------------------------------------------------
			
			
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean contains(int[] temp){
    	for(botInfo b: bots) {
    		if(b.coord[0] == temp[0] && b.coord[1] == temp[1]) return true;
    	}
        return false;
    }



    @Override
    public int hashCode(){
        return 0;
    }
    
    
  
    public Board(int m, int n) throws RemoteException {
        this.coordinates = new ArrayList<int[]>();
        this.bots = new ArrayList<botInfo>();
        if(m<5 || n<5) {
        	System.out.println("Kindly enter greater values for the board");
        	System.exit(0);
        }
        sizeWidth = m;
        sizeHeight = n;
        
        for (int i=0; i< totalBots; i++) {
        	electionBucket[i] = 0;
        }
        
        Registry objReg = LocateRegistry.createRegistry(8002);
        objReg.rebind("Board",this);
    }
    
    public int[] joinBoard(String ipAddr, int portNo, int[] botCoord, String botKey) {
    	int[] temp = {-1,-1};
    	boolean isPosOkay = false;
    	boolean userCoordAttempt = false;
    	
    	while(!isPosOkay) {
    		int i = new Random().nextInt((sizeHeight - 1) + 1) + 0;
    		int j = new Random().nextInt((sizeWidth - 1) + 1) + 0;
    		
    		if(botCoord[0] != -1 && botCoord[1] != -1 && !userCoordAttempt) {
    			i = botCoord[0];
    			j = botCoord[1];
    			userCoordAttempt = true;
    			System.out.println("User Selected coordinates as ["+i+","+j+"]");
    		}
    		else {
    			i = new Random().nextInt((sizeHeight - 1) + 1) + 0;
    			j = new Random().nextInt((sizeWidth - 1) + 1) + 0;
    		}
    		temp = new int[]{i,j};
    		
    		if((temp[0] >= 0 && temp[0] < sizeWidth) && (temp[1] >= 0 && temp[1] < sizeHeight)) {
    			if(contains(temp)) System.out.println("Coordinate ["+i+","+j+"] already exists");
    			else if(temp[0] == objectPosition[0] && temp[1] == objectPosition[1]) System.out.println("Object placed in corrdinate ["+i+","+j+"]");
    			else { 
    				coordinates.add(temp); 
    				isPosOkay = true;
    				int electionNum = new Random().nextInt((totalBots - 1) + 1) + 0;
    				botInfo b = new botInfo(ipAddr, portNo, temp, electionNum, botCounter, botKey);
    				bots.add(b);
    				++electionBucket[electionNum];
    				keyCollection.put(botKey, keyCollection.get(botKey) == null ? 1 : keyCollection.get(botKey)+1);
    			}
    		}
    	}
    	
    	++botCounter;
    	System.out.println(botCounter+" number of bots joined. Current Bot coordinate: ["+temp[0]+","+temp[1]+"]");
    	if(botCounter == totalBots) new Thread(new setLeader(this)).start();
    	return new int[]{botCounter-1, temp[0], temp[1]};
    }
    
    public String getTrueKey() {
		String temp = "";
		int maxCount = 0;
		Set set = keyCollection.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
		   Map.Entry mentry = (Map.Entry)iterator.next();
		   if((int)mentry.getValue() > maxCount) {
			   maxCount = (int) mentry.getValue();
			   temp = (String) mentry.getKey(); 
		   }
		}
    	return temp;
    }
    
    public void setLeader() {
    	int max = -1;
    	int index = 0;
    	for(int value: electionBucket) {
    		if(value > max) {
    			max = value;
    			leaderIndex = index;
    		}
    		++index;
    	}
    	
    	// check if bot is trustworthy
    	botInfo b = bots.get(leaderIndex);
    	if(b.botKey != getTrueKey()) {
    		// get new leader
    		System.out.println("Leader "+leaderIndex+" was malicious. Searching for new leader");
    		ArrayList<Integer> validBots = new ArrayList<Integer>(); 
    		for (botInfo eachBot : bots) {
    			if(eachBot.botKey.equalsIgnoreCase(getTrueKey())) {
    				validBots.add(eachBot.uniqueID);
    			}
    		}
    		int getNum = new Random().nextInt((validBots.size() - 2) + 1) + 0;
    		leaderIndex = validBots.get(getNum);
    	}
    	
    	System.out.println("Leader is Bot with index "+leaderIndex);
    	invokeLeader();
    }
    
    public int getLeader() {
    	return leaderIndex;
    }
    
    public int[] getObjectPosition() {
    	int[] tempCoord = {-1,-1};
    	
    	if(objectPosKnown) tempCoord = objectPosition;
		return tempCoord;
    	
    }
    
    public ArrayList<botInfo> getAllBots() {
    	return bots;
    }
    
    public void setCoordinates(int i, int j) {
        int[] temp = {i,j};
        if((temp[0] >= 0 && temp[0] < sizeWidth) && (temp[1] >= 0 && temp[1] < sizeHeight)) {
            if(contains(temp)) System.out.println("Coordinate ["+i+","+j+"] already exists");
            else coordinates.add(temp);
        }
        else System.out.println("Enter values ["+i+","+j+"] of robot are outside the range of the board");
    }
    
    public String displayValues() {
    	String str = "";
        
    	for (botInfo value : bots) {
        	System.out.println(value.uniqueID+" \t "+value.ipAddr+" \t "+value.portNo+" \t "+value.coord[0]+" \t "+value.coord[1]+" \t "+value.electionNum);
        	str += value.uniqueID+" \t "+value.ipAddr+" \t "+value.portNo+" \t "+value.coord[0]+" \t "+value.coord[1]+" \t "+value.electionNum;
		}
        
        return str;
	}
    
    public void display() {
        boolean evenRow = true;
        for(int i=0; i<sizeHeight; i++) {
            for(int j=0; j<sizeWidth; j++) {
                System.out.print("----------------");
            }
            System.out.println("-");
            for(int j=0; j<sizeWidth; j++) {
                int[] temp = {i,j};
                System.out.print("|\t");
                if(contains(temp)) { System.out.print("R\t");}
                else if(objectPosition[0] == i && objectPosition[1] == j) { System.out.print("o\t");}
                else { System.out.print("\t");}
            }
            System.out.println("|");
        }
        for(int j=0; j<sizeWidth; j++) {
            System.out.print("----------------");
        }
        System.out.println("-");
    }

    public void setObjectPosition() {
    	Scanner in = new Scanner(System.in);
    	int i,j;
    	String input;
    	System.out.println("Would you like to define the position of object. Input y or n");
    	input = in.next();
    	System.out.println(input);
    	if(input.equals("y")) {
    		System.out.println("Enter two values for your objects position");
    		i = in.nextInt();
    		j = in.nextInt();
    	}
    	else {
	    	i = new Random().nextInt((sizeHeight - 4 - 4) + 1) + 4;
			j = new Random().nextInt((sizeWidth - 4 - 4) + 1) + 4;
    	}
        int[] temp = {i,j};
        if((temp[0] >= 0 && temp[0] < sizeWidth) && (temp[1] >= 0 && temp[1] < sizeHeight)) { 
            objectPosition[0] = temp[0]; 
            objectPosition[1] = temp[1];
        }
        else System.out.println("Entered values ["+i+","+j+"] of object are outside the range of the board");
        
        System.out.println("Object set at position ["+i+","+j+"]");
        System.out.println("Is the position of object known for the bots. Input y or n");
        input = in.next();
    	if(input.equals("y")) objectPosKnown = true;
    	in.close();
    	System.out.println("Board ready and objectPosition known flag is set to :"+objectPosKnown);
    }

	@Override
	public boolean isTargetOnCell(int x, int y) throws RemoteException {
		// TODO Auto-generated method stub
		if(x==objectPosition[0] && y==objectPosition[1]) return true;
		else return false;
	}

	@Override
	public boolean canMoveToNextCell(int x, int y) throws RemoteException {
		// TODO Auto-generated method stub
		int[] temp = new int[]{x,y};
		
		if((temp[0]>=0 && temp[0]<sizeHeight) && (temp[1]>=0 && temp[1]<sizeWidth)) {	// if within dimensions
			if(!contains(temp)) {														// if no other bot present in same place	
				if(!(temp[0] == objectPosition[0] && temp[1] == objectPosition[1])) {	// if its not the same place as object
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean moveBotToNewCell(int nextX, int nextY, int indexOfBot) throws RemoteException {
		// TODO Auto-generated method stub
		if(indexOfBot > bots.size()) return false;
		
		botInfo b = bots.get(indexOfBot);
		
		if(b.botKey.equalsIgnoreCase(getTrueKey())) System.out.println("Bot "+b.uniqueID+" moved from location ["+b.coord[0]+","+b.coord[1]+"] to ["+nextX+","+nextY+"]");
		b.coord[0] = nextX;
		b.coord[1] = nextY;
		
		bots.set(indexOfBot, b);
		return true;
	}

	@Override
	public int[] getSizeOfBoard() throws RemoteException {
		// TODO Auto-generated method stub
		int[] temp = new int[]{sizeHeight, sizeWidth};
		return temp;
	}
    
	
}
