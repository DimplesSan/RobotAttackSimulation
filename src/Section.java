import java.io.Serializable;

public class Section implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2992556673535592585L;
	
	
	public int x1,y1, x2, y2;
	
	public Section(int _lowX,int _lowY, int _highX, int _highY){
		x1 = _lowX;
		y1 = _lowY;
		x2 = _highX;
		y2 = _highY;
	}
	
	public String toString(){
		return x1+","+y1+","+x2+","+y2;
	}
}

