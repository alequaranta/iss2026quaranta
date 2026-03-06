package main.java.conway.domain;

public interface IGrid {
	/* La griglia ha la capacità di restituire il numero di righe della griglia stessa */
	  public int getRowsNum();
	  
	  /* La griglia ha la capacità di restituire il numero di colonne della griglia stessa */
	  public int getColsNum();
	  
	  /* La griglia ha la capacità di modificare lo stato di una cella tramite le sue coordinate sulla griglia stessa */
	  public void setCellValue(int x, int y, boolean state);
	  
	  /* La griglia ha la capacità di restituire l'entità cella tramite le sue coordinate */
	  public ICell getCell(int x, int y);
	  
	  /* La griglia ha la capacità di restituire lo stato di una cella tramite le sue coordinate*/
	  public boolean getCellValue(int x, int y);
	  
	  /* La griglia è capace di cambiare lo stato di tutte le celle (a morte)*/
	  public void reset();
}
