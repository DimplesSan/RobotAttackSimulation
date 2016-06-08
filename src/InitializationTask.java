import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitializationTask implements Runnable{
	
	Bot currBotRef;
	
	public InitializationTask(Bot objBot) {
		currBotRef = objBot;
	}
	
	@Override
	public void run(){
		
		currBotRef.haveAllBotsJoined = true;
		System.out.println("Board notification: All bots joined");
		
		try{
			
	        //Get leader index
			currBotRef.leaderIndex = currBotRef.objBoard.getLeader();
	        System.out.println("Leader is :"+ currBotRef.leaderIndex);
	        
			//get list of botInfo objects (their IP addresses)
	        currBotRef.botInfoList = currBotRef.objBoard.getAllBots();
	        
	        //Create channels for communication with every other bot
	        getBotInterfaces();
	        
			System.out.println("Bot "+currBotRef.ownindex +" initialized.\n");
		}
		catch(NotBoundException | UnknownHostException | RemoteException e){
			e.printStackTrace();
		}
	}
	
	
	
	//Function to retrieve the interfaces for each bot and store it in the 
	//a Map against their IDs
	public void getBotInterfaces() throws RemoteException, UnknownHostException, NotBoundException{
		
		Registry objTempReg;
		BotInterface tempBotInterface;
		
		//Locate the registry for each bot on the board
		//and get its interface
		System.out.println("Size of BotInfoList "+ currBotRef.botInfoList.size());
		for(Board.botInfo b : currBotRef.botInfoList){
			
			objTempReg = LocateRegistry.getRegistry(b.ipAddr, b.portNo);
//			if(ownindex != b.uniqueID){
				tempBotInterface = (BotInterface)objTempReg.lookup("Bot");
				currBotRef.botsOnBoard.put(b.uniqueID, tempBotInterface);
//			}
		}
		
		
	}
	
	
	

}
