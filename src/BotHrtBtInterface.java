import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BotHrtBtInterface extends Remote{
	
	//Function that'll be called by other bots
	//to check on the current bot
	public boolean isBotAlive() throws RemoteException;
}
