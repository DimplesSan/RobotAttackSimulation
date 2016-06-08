import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BotHrtBt extends UnicastRemoteObject implements BotHrtBtInterface{

	/**
	 * Generated Serialization id
	 */
	private static final long serialVersionUID = -1602989110525238119L;
//	private Bot objBot;
	private int hrtBtPort;
	
	public BotHrtBt(int _hrtBtPort) throws RemoteException {
		hrtBtPort = _hrtBtPort;
	}


	
	//< -- Can be used to piggyback information between
	//bots -- >
	//Have to decide on information exhcange
	@Override
	public boolean isBotAlive() throws RemoteException{
		return true;
	}
	
	
	
	//Starts the heart beat for the bot
	public void startHrtBt(){
		
		try{
						
			//Enlist the bot's heart beat in the RMI registry
	        Registry objReg = LocateRegistry.createRegistry(this.hrtBtPort);
	        objReg.rebind("HeartBeat",this);
	
			System.out.println("HeartBeat for bot started.");
			
		}catch(Exception e){
			
			
			System.out.println("Exception while starting BotHrtBt: " + e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	
	
	

}
