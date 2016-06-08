import java.rmi.RemoteException;
import java.util.ArrayList;

public class LocateTargetTask implements Runnable{
	
	Bot leaderBot;
	
	LocateTargetTask(Bot leaderBotRef){
		leaderBot =  leaderBotRef;
	}
	
	@Override
	public void run(){
		
		
		//For each bot
		//Assign section to the bot
		//Start a thread to communicate with the bot 
			//and check it's heart beat and piggyback section
			//information
	
			//Invoke the scanSection() on the bot to begin scanning
	
		//Create a list to hold the list of bots that have been allocated sections
		try{
			
			
			//assign sections only after initilization thread has finished
			leaderBot.initThread.join();
			
			//Check from the board if the target is hidden or not
			int [] tgtPos = leaderBot.objBoard.getObjectPosition();
			
			//If the value returned is -1 then the object position is not known
			if(tgtPos[0] == -1){
				
				    leaderBot.listBotsWithSections = new ArrayList<Integer>();
					System.out.println("Number of bots on board: " + leaderBot.botsOnBoard.size());
					
					int [] completeSection =  {0, 0, leaderBot.l-1, leaderBot.b-1 };
					
					for(int botIndex : leaderBot.botsOnBoard.keySet()){
						
						//Assign Sections only if the bots are friendly
						if(leaderBot.botsOnBoard.get(botIndex).getKeyStr().equalsIgnoreCase(
						   leaderBot.getKeyStr())){
							
							System.out.println("Beginning to assign section to bot " + botIndex);
							assignSectionForBot(leaderBot.listBotsWithSections, botIndex);
						}
						else{
							
							System.out.println("Bot with index: "+ botIndex + " is malicious. "
											  +" Not assigning a section to this bot.");
							
							//Assign the complete section
							leaderBot.botsOnBoard.get(botIndex).setSectionOfBot(completeSection);
						}
						
					}
						
					//Issue the scan section command for each bot other than the leader
					for(int botIndex : leaderBot.botsOnBoard.keySet()){
						
						if(botIndex != leaderBot.ownindex)
							leaderBot.botsOnBoard.get(botIndex).scanSectionOfBoard(botIndex);
					}	
					
					//Leader bot will scan its own section
					leaderBot.scanSectionOfBoard(leaderBot.ownindex);
					
					//Wait till the target found flag is set
					while(!leaderBot.isTargetFound){
						
						try{
							System.out.println("Leader thread sleeping for 3 secs since target was not found");
							Thread.sleep(3000);
						}
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
						
					}
				
			}
			else{
				
					 System.out.println("Target is not hidden. Not assigning sections as"
					 				   +" the target is already found.");
					 
					 //Set the flag as the target it found
//					 leaderBot.isTargetFound = true;
					 
					 //Notify all the other bots
					 leaderBot.targetFound(tgtPos[0], tgtPos[1], leaderBot.currXCoord, 
							 			   leaderBot.currYCoord, leaderBot.ownindex);
				 
			}
			
			//Surround target
			leaderBot.arrageTargets();
			leaderBot.surroundTarget();
			
			//Ask the board to display latest status 
			leaderBot.objBoard.display();
		}
		catch(RemoteException | InterruptedException e){
			
			e.printStackTrace();
		}
			
	}
	
	
	
	//Function that return the low and high coordinates of a section for the bot
	public void assignSectionForBot(ArrayList<Integer> listOfBotsWithSections, 
			                      int botIndex ) throws RemoteException{
		
		int [] retArr = new int[4];
		//If the list is empty
			//Return coordinates of the enite board
		//Else
			//Calculate the index that is the closest to the specified index 
			//Get the section from the bot 
			//Partition the section

		
		if(listOfBotsWithSections.isEmpty()){
			retArr[0] = 0; retArr[1] = 0;
			retArr[2] = leaderBot.l-1; retArr[3] = leaderBot.b-1;
			
			System.out.println("Section Assigned to bot: " + botIndex);
			//Call the setter to set the section
			leaderBot.botsOnBoard.get(botIndex).setSectionOfBot(retArr);
			
		}
		else{
			
			int closetBotIndex = returnClosestBot(listOfBotsWithSections, botIndex);
			System.out.println("Beginning to partition bot " + closetBotIndex + " section");
			this.partitionSectionOfSpecifiedBot(closetBotIndex, botIndex);
			
		}
			
		//Add bot to the list that have been assigned sections
		listOfBotsWithSections.add(botIndex);
		
	}
	
	
	
	//Function to return the index that is closest to the specified bot
	public int returnClosestBot(ArrayList<Integer> listOfBotsWithSections, 
							    int botIndex ) throws RemoteException{
		
		int retVal = 0;
		double minDist = 0.0, tempDist = 0.0;
		
		System.out.println("Finding Closest bot to bot " +botIndex);
		
		int j = 0;
		for(int i : listOfBotsWithSections){
			
			//Calculate dist between two specified bots
			tempDist = getDistBetweenBots(i, botIndex);
			
			//First Iteration
			if(j == 0){
				
				//Calculate dist between two specified bots
				minDist = tempDist;
				retVal = i;
			}
			else{
				
				if(tempDist <= minDist){
					minDist = tempDist;
					retVal = i;
				}
			}

			++j;
		}
		
		System.out.println("Closest bot to bot " +botIndex +" is "+retVal);
		return retVal;
	}
	
	
	
	//Function to partition the section of the specified bot
	public void partitionSectionOfSpecifiedBot(int closetBotIndex, 
			   int botIndex) throws RemoteException{
		
		Section sectionToBePartiotioned = leaderBot.botsOnBoard.get(closetBotIndex).getScanSectionOfBot();
		System.out.println("Closest bot is "+ closetBotIndex + " and section to be partitioned: " + sectionToBePartiotioned);
		
		double l = Math.abs(sectionToBePartiotioned.x1 - sectionToBePartiotioned.x2);
		double b = Math.abs(sectionToBePartiotioned.y1 - sectionToBePartiotioned.y2);
		
		//Get the current position of both bots
		int [] oBotPos = leaderBot.botsOnBoard.get(closetBotIndex).getCurrPosOfBot();
		int [] nBotPos = leaderBot.botsOnBoard.get(botIndex).getCurrPosOfBot();
		
		//Get midpoint
		int [] mdpt = new int[2];
		mdpt[0] = ( oBotPos[0] + nBotPos[0] ) / 2;
		
		mdpt[1] = ( oBotPos[1] + nBotPos[1] ) / 2;
		
		
		int [] section1 = new int[4];
		int [] section2 = new int[4];
		
		if(l > b ){
		System.out.println("Partitioning Vertically");
		//Partition Vertically as section is longer horizontally
		section1[0] = sectionToBePartiotioned.x1;
		section1[1] = sectionToBePartiotioned.y1;
		section1[2] = mdpt[0];
		section1[3] = sectionToBePartiotioned.y2;
		
		section2[0] = mdpt[0];
		section2[1] = sectionToBePartiotioned.y1;
		section2[2] = sectionToBePartiotioned.x2;
		section2[3] = sectionToBePartiotioned.y2;
		
		}
		else{
		
		System.out.println("Partitioning Horizontally");
		//Partition horizontally
		section1[0] = sectionToBePartiotioned.x1;
		section1[1] = sectionToBePartiotioned.y1;
		section1[2] = sectionToBePartiotioned.x2;
		section1[3] = mdpt[1];
		
		section2[0] = sectionToBePartiotioned.x1;
		section2[1] = mdpt[1];
		section2[2] = sectionToBePartiotioned.x2;
		section2[3] = sectionToBePartiotioned.y2;
		
		}
		
		//Check which section the original bot belongs to
		System.out.println("Setting new section of "+ closetBotIndex);
		
		if(oBotPos[0] > section1[0] && oBotPos[0] < section1[2] &&
		oBotPos[1] > section1[1] && oBotPos[1] < section1[3]){
		
			//Original bot lies in Section 1
			leaderBot.botsOnBoard.get(closetBotIndex).setSectionOfBot(section1);
			System.out.println("Setting New section of "+ botIndex);
			leaderBot.botsOnBoard.get(botIndex).setSectionOfBot(section2);
		}
		else{
		
			//Original bot lies in Section 2
			leaderBot.botsOnBoard.get(closetBotIndex).setSectionOfBot(section2);
			System.out.println("Setting New section of "+ botIndex);
			leaderBot.botsOnBoard.get(botIndex).setSectionOfBot(section1);
		}
	
	}
	
	
	
	//Function to return the distance between two bots
	public double getDistBetweenBots(int i, int j) throws RemoteException{
		
		double retVal = 0.0;
		
		int[] iArr = leaderBot.botsOnBoard.get(i).getCurrPosOfBot();
		System.out.println("getDistBetweenBots: Current Pos of bot: "+ i +" " +iArr[0] + "," + iArr[1]);
		
		int[] jArr = leaderBot.botsOnBoard.get(j).getCurrPosOfBot();
		System.out.println("getDistBetweenBots: Current Pos of bot: "+ j + " "+ jArr[0] + "," + jArr[1]);
		
		//Calculate the Euclidean distance between the bots
		double xDiff = iArr[0] - jArr[0]; 
		double yDiff = iArr[1] - jArr[1];
		
		retVal = Math.sqrt( (xDiff*xDiff) + (yDiff*yDiff) );  		
		
		System.out.println("Distance between " + i + " & " + j +" is " + retVal);
		return retVal;
	}
	
	

}
