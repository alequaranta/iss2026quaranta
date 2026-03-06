package main.java.conway.domain;

public interface ICell {
	/* La cella ha la capacita' di modificare il proprio stato, rappresentato da un booleano */
	void setStatus(boolean v);
	
	/* La cella ha la capacita' di restituire il proprio stato */
	boolean isAlive();
	
	/* La cella ha la capacita' di permutare il proprio stato, da viva a morta e viceversa */
	void switchCellState();
}
