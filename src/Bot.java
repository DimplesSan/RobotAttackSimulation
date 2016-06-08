import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.TreeMap;


//Class that'll interact with the board
public class Bot extends UnicastRemoteObject implements BotInterface {
	
	
	//Board specific parameters
	public TreeMap<Integer, BotInterface> botsOnBoard;	//List of others bots on the board
	public ArrayList<Board.botInfo> botInfoList;
	public ArrayList<Integer> listBotsWithSections;
	
	public int initXCoord, initYCoord, //Bot's initial Coordinates
				currXCoord, currYCoord, //Bot's current coordinates
				tgtXCoord, tgtYCoord;	//Coordinates of the target, when found
	
	public Section sectionTOScan;
	
	public boolean isTargetFound;
	public boolean canStopMovement;
	public boolean haveAllBotsJoined;
	public boolean ifNotFound = true;
	public int[] botCoordinate = {-1 , -1};
	public int l, b;
	
	public int ownindex, leaderIndex;	//Identifiers in the list of bots
	
	public Registry boardRegistry;
	public BoardInterface objBoard;
	
	Thread initThread;
	public String keyStr;
	
	//Network specific parameters
	public String ipAddrOfBoard, currBotIP; //IP Address of the board and current Bot  
	public int portOfBoard, currBotPort, //Port numbers of the Board, 
	            hrtBtPort ;  //current Bot and the heart beat port  
	
	/**
	 * Serialization ID  
	 */
	public static final long serialVersionUID = 5988762427168790819L;
	
	
	//Bot Constructor
	public Bot(String _ipAddrBrd, int _portBoard, int _portOfBot, 
			   int _botHrtBtPort, String botKey, int[] botCoord) throws 
																RemoteException, 
														   UnknownHostException, 
														      NotBoundException{
		isTargetFound = false;
		canStopMovement = false;
		haveAllBotsJoined = false;
		ipAddrOfBoard = _ipAddrBrd;
		portOfBoard = _portBoard; 
		
		keyStr = botKey;
			
		//currBotIP = "129.21.64.18";	// Need to hardcode IP for linux
		currBotIP = InetAddress.getLocalHost().toString().split("/")[1];
		currBotPort = _portOfBot;
		hrtBtPort = _botHrtBtPort;
		botsOnBoard = new TreeMap<Integer, BotInterface>();
		botCoordinate = new int[]{botCoord[0],botCoord[1]};
		System.out.println("Current IP: " +currBotIP + "Current Port: " + currBotPort);
	}	
	
	
	
	//Getter for scan section of the current bot
	public Section getScanSectionOfBot() throws RemoteException{
		
		return this.sectionTOScan;
	}
	
	
	
	//Setter for scanSection
	public void setSectionOfBot(int [] arrCoord) throws RemoteException{
		
		try {
			
			this.initThread.join();
			System.out.println("Section Coordinates: " + arrCoord[0] +","+ arrCoord[1] +","+ 
				    arrCoord[2] +","+arrCoord[3] );
			this.sectionTOScan = new Section(arrCoord[0], arrCoord[1], arrCoord[2], arrCoord[3]);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}
	
	public String getKeyStr() throws RemoteException{
		return this.keyStr;
	}
	
	//Function to return the current position of the bot
	public int[] getCurrPosOfBot() throws RemoteException{
		int[] retArr = {this.currXCoord, this.currYCoord};
		return retArr;
	}
	
	//Function to reset the flag to start movement
	public void startBotMovement() throws RemoteException{
		this.canStopMovement = false;
	}
	
	//Function to set the flag to indicate that movement may be stopped 
	//becuase, the target may have been found by some other bot
	public void stopBotMovement() throws RemoteException{
		System.out.println("Received the halting command by the leader");
		this.canStopMovement = true;
	}
		
	public boolean isTargetFound() throws RemoteException{
		return this.isTargetFound;
		
	}
	
	public void setIsTargetFoundFlag() throws RemoteException{
		this.isTargetFound = true;
	}
	
	
	
	
	//Starts the bot
	public static void main(String [] args){
		
		try{
			
			//Extract Command line args
			int boardPort = Integer.parseInt(args[1]);
			int botPort = Integer.parseInt(args[2]);
			int[] botCoordinate = {-1, -1};
			if(args.length == 6) 
				botCoordinate = new int[]{Integer.parseInt(args[4]),Integer.parseInt(args[5])};
					
			int botHrtBtPort = botPort +1;
			
			//----------------------------------------------------------------------------------------------------------
			//Added 12/18/15 - Sid
			String botKey = args[3] == null ? "Friendly" : args[3];
			//----------------------------------------------------------------------------------------------------------
			
			//Create the bot
			Bot objBot = new Bot(args[0], boardPort, botPort, botHrtBtPort, botKey ,botCoordinate);
			
			//Register the bot with the RMI registry
	        Registry objReg = LocateRegistry.createRegistry(objBot.currBotPort);
	        objReg.rebind("Bot",objBot);

	        //Register the heart beat on the next port for and start the heart beat
	        BotHrtBt objBotHrtBt = new BotHrtBt(botHrtBtPort);
	        objBotHrtBt.startHrtBt();
	        
	        //Get the initial info from board
	        objBot.getInitInfoFrmBoard();
	        
	        System.out.println("Initial info receive from Board");
	        
		}
		catch(Exception e){
			
			System.out.println("Exception from Bot" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	
	//Get the initial info from board
	private void getInitInfoFrmBoard() throws RemoteException, UnknownHostException, NotBoundException{
		
		//Lookup registry for the board
		boardRegistry = LocateRegistry.getRegistry(ipAddrOfBoard, portOfBoard);
        objBoard = (BoardInterface)boardRegistry.lookup("Board");
        
		//join the board and get inital coordinates from the board
        int [] initCoord = objBoard.joinBoard(currBotIP, currBotPort, botCoordinate, keyStr);
        
        //Get size of board
        int [] sizeOfBoard = objBoard.getSizeOfBoard();
        l = sizeOfBoard[0];
        b = sizeOfBoard[1];
        System.out.println("Size of board is: " + "l: "+l + "b: "+ b);
        
		//Set the curr coordinates to the initial coordinates
        initXCoord = initCoord[1];
        initYCoord = initCoord[2];
        System.out.println("Initial Coordinates of the bot are X: " + initXCoord + " and Y: " + initYCoord);
        
        ownindex = initCoord[0];
        currXCoord = initXCoord;
        currYCoord = initYCoord;
	}
	
	
	
	
	//Called by the board to notify bot that all bots have joined
	//the board
	public void setAllBotsJoinedFlag() throws RemoteException{
		
		//Spawn a thread to do the work and return the call from the
		//Board
		this.initThread = new Thread(new InitializationTask(this));
		initThread.start();
	}
	

	
	//Only the leader's start() will be invoked to begin
	//the task of searching for the target.
	public void start() throws RemoteException{ 
		
		//Spawn a thread to begin the task of locating 
		//the target
		 new Thread(new LocateTargetTask(this)).start();
	}
	
	
	
	//Invoked on bot by one of leader's thread
	public void scanSectionOfBoard(int currIndex) throws RemoteException{
		
		ScanningTask objScnTsk = new ScanningTask(this);
		
		if(currIndex == this.leaderIndex){
			
			//Leader bot has to to work without spawning a new thread
			objScnTsk.run();
		}
		else{
			
			//Invoked by the leader, spwan a thread to work
			//and return call
			new Thread(objScnTsk).start();
		}

			
	}
	
	
	
	//Invoked by scan section
	public int[] move(int destX, int destY) throws RemoteException{
		
		//TO DO - Check the heart beat of the leader
		
		int[] retVal = {this.currXCoord, this.currYCoord};
		
		//Repeat till current position is not equal to dest position
		while(this.currXCoord != destX || this.currYCoord != destY){
			
			//Check if the target is present in it's neighbouring cell
			//list of neighboring cells 
			if(checkNeighborCellsForTarget() && ifNotFound){
				
				
				//Notify the leader only if the current bot is a friendly one
				if(this.keyStr.equalsIgnoreCase(botsOnBoard.get(leaderIndex).getKeyStr()) ){
					
					System.out.println("Current location of bot: "+ +this.currXCoord + "," +this.currYCoord);
					System.out.println("Target found by bot with index: "+this.ownindex 
										+ " in neighboring cell: "+
										this.tgtXCoord + "," +this.tgtYCoord);
					
					//Notify the leader
					this.botsOnBoard.get(this.leaderIndex).targetFound(this.tgtXCoord,
																	   this.tgtYCoord,
																	   this.currXCoord, 
																	   this.currYCoord,
																	   this.ownindex);
					System.out.println("Leader Notified");
					ifNotFound = false;
					return retVal;
				}
				else{
					System.out.println("Target found by the malicious one.");
					return retVal;
				}
				

			}
			else{
				
				//Calculate the next cell in the path to the destination
//				int [] arrNextCoords = nextCellInPath(destX, destY);
				int [] arrNextCoords = {destX, destY};
				
				//Checkf if the bot can move onto the next cell
				//if yes
				if(this.objBoard.canMoveToNextCell(arrNextCoords[0], 
						                           arrNextCoords[1])){
					
					//Move to next cell
					//Repeat till board return successful movement
						//-- Ask the board to increment the status of the bot on the board
						if(this.objBoard.moveBotToNewCell(arrNextCoords[0], 
								arrNextCoords[1], this.ownindex)){
							//if successful --> increment current location
					      	  //--> break out of loop
							this.currXCoord = arrNextCoords[0];
							this.currYCoord = arrNextCoords[1];
							System.out.println("New location of bot "+ ownindex + " " +this.currXCoord + "," +this.currYCoord);
						}
				}
				//return current coordinates
//				else {
//					if(this.canStopMovement) {
//						if(this.currXCoord > destX) {
//							if(this.objBoard.canMoveToNextCell(this.currXCoord+1, this.currYCoord+1)) {
//								this.currXCoord += 1;
//								this.currYCoord += 1;
//								System.out.println("if");
//							}
//						}
//						else {
//							if(this.objBoard.canMoveToNextCell(this.currXCoord-1, this.currYCoord+1)) {
//								this.currXCoord -= 1;
//								this.currYCoord += 1;
//								System.out.println("else");
//							}
//						}
//					}
					else return arrNextCoords;
//				}
			}
		}
		return retVal;
	}
	
	
	
	//Invoked on the bot by itself
	//If Successful then the target position is set in the
	//instance variables and true is returned else false
	public boolean checkNeighborCellsForTarget() throws RemoteException{
		
		boolean retVal = false;
		
		//Calculate the neighboring coordinates
		int neighborXCoord[] = {this.currXCoord -1, this.currXCoord, this.currXCoord +1};
		int neighborYCoord[] = {this.currYCoord -1, this.currYCoord, this.currYCoord +1};
		
		//Check for each cell amongst the neighbors 
		for(int x : neighborXCoord){
			
			for(int y : neighborYCoord){
				
				if(x == this.currXCoord &&  this.currYCoord == y)
					break;
				else{
					
					//Ask the board if target is present on the specified cell
					retVal = this.objBoard.isTargetOnCell(x, y);
					if(retVal){
						
						this.tgtXCoord = x;
						this.tgtYCoord = y;
						return retVal;
					}
				}
			}
		}
		
		return retVal;
	}

	
	
	
	public int[] nextCellInPath(int destX, int destY) 
			throws RemoteException{

		int [] retArr = {destX,destY};
	
		//Calculate the direction
	
		//Move vertically
		if(this.currXCoord == destX){
		
			//Move Up
			if(this.currYCoord < destY)
				retArr[1] = this.currYCoord -1 ;
	
			//Move Down
			else
				retArr[1] = this.currYCoord +1 ;
		}
	
	
		//Move horizontally
		else if(this.currYCoord == destY){
	
			//Move Right
			if(this.currXCoord < destX)
				retArr[0] = this.currXCoord + 1;
			
			//Move Left
			else	
				retArr[0] = this.currXCoord - 1;
	
		}
	
		//Move Diagonally
		else
			retArr = nextDiagonalCell(destX, destY);
	
	
		return retArr;
	
	}
	
	
	
	public int[] nextDiagonalCell(int destX, int destY) throws RemoteException{
		
		int [] retArr = {destX,destY};
		
		//Move North
		if(this.currYCoord > destY){
			
			//Move NorthWest direction
			if(this.currXCoord > destX){
				retArr[0] = this.currXCoord -1;
				retArr[1] = this.currYCoord -1;
			}
				
			//Move NorthEast direction
			else{
				retArr[0] = this.currXCoord +1;
				retArr[1] = this.currYCoord -1;				 
			}
				
		}
		//Move South
		else{
			//Move SouthWest direction
			if(this.currXCoord > destX){
				retArr[0] = this.currXCoord -1;
				retArr[1] = this.currYCoord +1;					
			}
			//Move SouthEast direction
			else{
				retArr[0] = this.currXCoord +1;
				retArr[1] = this.currYCoord +1;				
			}
		}
		

		
		return retArr;
	}
	
	
	
	
	
	
	
	
	
	//Leader's method is called by a bot 
	public void targetFound(int tgtXCoord, int tgtYCoord, 
			                int botCurrX, int botCurrY,
			                int helperBotIndex) 
			                throws RemoteException{
		

		if(!this.isTargetFound) { //Check for the first invocation
			
			BotInterface b;
			//For each bot on the board
			for(int botIndex : botsOnBoard.keySet()){
				
				b = botsOnBoard.get(botIndex);
				
//				if(this.ownindex != botIndex && 
//				   botIndex != helperBotIndex){	//Ensure the target's coordinate for the 
												//leader and the bot who found out the target
												// is not set
					
					botsOnBoard.get(leaderIndex).setIsTargetFoundFlag();
					//Issue halting command
					b.stopBotMovement();
					System.out.println("\nStop Command issued for bot with Index: " + botIndex);
					
					//Notification of the target will be sent only the bot is friendly
					if(botsOnBoard.get(botIndex).getKeyStr().equalsIgnoreCase(botsOnBoard.get(leaderIndex).getKeyStr())){
						//Send the coordinates to each of the bots
						b.setTargetCoordinates(tgtXCoord, tgtYCoord);
						System.out.println("Notification of the target sent to bot with Index: " + botIndex);
					}
					
//				}
				
			}
			
		}
		
		
	}
	
	
	
	//Set the target coordinates - inoked on the bot by the leader
	public void setTargetCoordinates(int tgtX, int tgtY) throws RemoteException{
		System.out.println("Target's coordinates received by the leader "+tgtX + "," + tgtY);
		this.tgtXCoord = tgtX;
		this.tgtYCoord = tgtY;
	}
	
	
	//Function to arrange the bots close around the target 
	public void arrageTargets() throws RemoteException {

		int i = this.tgtXCoord;
		int j = this.tgtYCoord;

		int level = 1;
		int[][] locations = {{i-3,j},{i+3,j},{i,j-3},{i,j+3},{i-3,j-3},{i+3,j+3},{i+3,j-3},{i-3,j+3}};
		int counter = 0;
//		for(int botIndex : listBotsWithSections){
//				botsOnBoard.get(botIndex).move(locations[botIndex][0],locations[botIndex][1]);
//		}
		
		for(int botIndex : botsOnBoard.keySet()){
			if(counter == 0) locations = new int[][]{{i-level,j},{i+level,j},{i,j-level},{i,j+level},{i-level,j-level},{i+level,j+level},{i+level,j-level},{i-level,j+level}};
			
			//If Friendly then assign postion near to target
			if(botsOnBoard.get(botIndex).getKeyStr().equalsIgnoreCase(botsOnBoard.get(leaderIndex).getKeyStr())){
				
				System.out.println("Bot: "+ botIndex + " friendly. Assiging a postion close to the target.");
				botsOnBoard.get(botIndex).move(locations[counter][0],locations[counter][1]);
				++counter;
			}
			else
				moveBotAwayFromTargetToHorizontalEdge(botIndex);
			
			if(counter == 8) {
				counter = 0;
				++level;
			}
		}
	}
	
	
	//Function to arrange the bots right in the neigbourhood of the target
	public void surroundTarget() throws RemoteException {

		int i = this.tgtXCoord;
		int j = this.tgtYCoord;

		int level = 1;
		int[][] locations = {{i-1,j},{i+1,j},{i,j-1},{i,j+1},{i-1,j-1},{i+1,j+1},{i+1,j-1},{i-1,j+1}};
		int counter = 0;
		
		for(int botIndex : botsOnBoard.keySet()){
			if(counter == 0) locations = new int[][]{{i-level,j},{i+level,j},{i,j-level},{i,j+level},{i-level,j-level},{i+level,j+level},{i+level,j-level},{i-level,j+level}};
			
			//If Friendly then assign postion near to target
			if(botsOnBoard.get(botIndex).getKeyStr().equalsIgnoreCase(botsOnBoard.get(leaderIndex).getKeyStr())){
				
				System.out.println("Bot: "+ botIndex + " friendly. Assiging a postion close to the target.");
				botsOnBoard.get(botIndex).move(locations[counter][0],locations[counter][1]);
				++counter;
			}
			else
				moveBotAwayFromTargetToVerticalEdge(botIndex);
			
			if(counter == 8) {
				counter = 0;
				++level;
			}
		}
	}
	
	
	
	//Function to set the target position close to one of the Horizontal edges
	public void moveBotAwayFromTargetToHorizontalEdge(int indexOfBot) throws RemoteException{
		
		//Get Current location
		int currPos[] = botsOnBoard.get(indexOfBot).getCurrPosOfBot();
		int misleadingPos[] = new int[2];
		
		if(currPos[1] > b-currPos[1] ){
			//Closer to the bottom edge
			misleadingPos[0] = currPos[0];
			misleadingPos[1] = b;	
		}
		else{
			//Closer to the top edge
			misleadingPos[0] = currPos[0];
			misleadingPos[1] = 0;
		}
		
		System.out.println("Bot: "+ indexOfBot + " asked to move to "+misleadingPos[0]+","+ misleadingPos[1]);
		//Move to closest edge
		botsOnBoard.get(indexOfBot).move(misleadingPos[0],misleadingPos[1]);
	}
	
	
	//Function to set the target position close to one of the vertical edges
	public void moveBotAwayFromTargetToVerticalEdge(int indexOfBot) throws RemoteException{
		
		//Get Current location
		int currPos[] = botsOnBoard.get(indexOfBot).getCurrPosOfBot();
		int misleadingPos[] = new int[2];
		
		if(currPos[0] > l-currPos[0] ){
			//Closer to the right edge
			misleadingPos[0] = l;
			misleadingPos[1] = currPos[1];	
		}
		else{
			//Closer to the left edge
			misleadingPos[0] = 0;
			misleadingPos[1] = currPos[1];
		}
		
		System.out.println("Bot: "+ indexOfBot + " asked to move to "+misleadingPos[0]+","+ misleadingPos[1]);
		//Move to closest edge
		botsOnBoard.get(indexOfBot).move(misleadingPos[0],misleadingPos[1]);
	}
	

	
}
