import java.rmi.RemoteException;

public class ScanningTask implements Runnable{
	
	Bot currBotRef;
	 
	public ScanningTask(Bot _currBotRef) {
		
		currBotRef = _currBotRef;
	}
	
	
	
	@Override
	public void run(){
		

		
		//Define a 2-D array to keep track of the locations
		//the bot has been visited by the bot 
//		int [][] scannedSection = new int [objSect.x2][objSect.y2];
//		
//		//Initialize scannedSection
//		for(int i = objSect.x1; i< objSect.x2; ++i){
//			
//			for(int j = objSect.y1; j<objSect.y2; ++j){
//				scannedSection[i][j] = 0;
//			}
//		}
		try{
			
			//Start scanning only after initialization thread has finished
			currBotRef.initThread.join();
			
			System.out.println("\nBeginning to scan section of board");
			
			//Get the section assigned by the leader
			Section objSect = currBotRef.sectionTOScan;
			System.out.println("Section to be scanned " + objSect +"\n");
			
			System.out.println("Beginning to move up.");
			//Move Up till the top of the section
			for(int j=currBotRef.currYCoord -1; j >=objSect.y1; --j){
				
				if(!currBotRef.canStopMovement)
					currBotRef.move(currBotRef.currXCoord,j);
				else
					break;
					
//				scannedSection[this.currXCoord][j] = 1;
			}
			

			System.out.println("\nBeginning to move left.");
			//Move left till the beginning of the section
			for(int i = currBotRef.currXCoord -1; i >=objSect.x1; --i){
				
//				scannedSection[i][this.currYCoord] = 1;
				if(!currBotRef.canStopMovement)
					currBotRef.move(i, currBotRef.currYCoord);
				else
					break;
					
			}	
			
		
			//Scan complete rectangular area
			for(int i = objSect.x1 + 1; i<objSect.x2; ++i ){
				
				if(!currBotRef.canStopMovement){
					
					for(int j = objSect.y1; j<objSect.y2; ++j){
						
						if(!currBotRef.canStopMovement)
							currBotRef.move(i,j);
						else
							break;
					}
				}
				else
					break;
			}
			
			if(currBotRef.canStopMovement)
				System.out.println("Movement stopped as Target was found");
			else
				System.out.println("Movement stopped as Complete section was scanned, but target was not found");
				
		}
		catch(RemoteException | InterruptedException e){
			
			e.printStackTrace();
		}
		
	}

	
}
