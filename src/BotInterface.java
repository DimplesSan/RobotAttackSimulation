import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BotInterface extends Remote {
	
	public void start() throws RemoteException;
	
	public void scanSectionOfBoard(int currIndex) throws RemoteException;
//	public int[] move(int destX, int destY) throws RemoteException;
	public int[] move(int destX, int destY) throws RemoteException;
	public boolean checkNeighborCellsForTarget() throws RemoteException;
	public void targetFound(int tgtXCoord, int tgtYCoord, int botCurrX, int botCurrY, int botIndex) throws RemoteException;
	public int[] getCurrPosOfBot() throws RemoteException;
	public void stopBotMovement() throws RemoteException;
	public void startBotMovement() throws RemoteException;
	public void setTargetCoordinates(int tgtX, int tgtY) throws RemoteException;
	
	public Section getScanSectionOfBot() throws RemoteException;
	public void setSectionOfBot(int [] arrCoord) throws RemoteException;
	
	public boolean isTargetFound() throws RemoteException;
	public void surroundTarget() throws RemoteException;
	public void setAllBotsJoinedFlag() throws RemoteException;
	
	public void setIsTargetFoundFlag() throws RemoteException;
	
	//--------------------------------------------------------------------
	//Added 12/18/15 - Sid
	public String getKeyStr() throws RemoteException;
	//--------------------------------------------------------------------
}
