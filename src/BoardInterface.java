import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface BoardInterface extends Remote{
	
	public boolean contains(int[] temp) throws RemoteException;
	public void setCoordinates(int i, int j) throws RemoteException;
	public String displayValues() throws RemoteException;
	public void display() throws RemoteException;
	void setObjectPosition() throws RemoteException;
	public int[] joinBoard(String ipAddr, int portNo, int[] botCoord, String botKey) throws RemoteException;
	public void setLeader() throws RemoteException;
	public int getLeader() throws RemoteException;
	public ArrayList<Board.botInfo> getAllBots() throws RemoteException;
	public int[] getSizeOfBoard() throws RemoteException;
	public int[] getObjectPosition() throws RemoteException;
	public String getTrueKey() throws RemoteException;
	
	public boolean isTargetOnCell(int x, int y) throws RemoteException; //Board checks for the target on the specified coordinates
	public boolean canMoveToNextCell(int x, int y) throws RemoteException; //Board checks if the bot can move to the cell with the specified coordinates
	public boolean moveBotToNewCell(int nextX, int nextY, int indexOfBot) throws RemoteException; //Board updates the position of the next cell
}