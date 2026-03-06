package main.java.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.conway.domain.ICell;
import main.java.conway.domain.IGrid;
import main.java.conway.domain.Life;
import main.java.conway.domain.LifeInterface;
 

public class LifeTest {
private LifeInterface lifeModel;

	@Before
	public void setup() {
		System.out.println("LifeTest | setup");	
		lifeModel = Life.CreateLife(5, 5);
	}
	@After
	public void down() {
		System.out.println("LifeTest | down");
	}
	
	@Test
    void testIsAliveAfterSet() {
		System.out.println("LifeTest | cell is alive");
        lifeModel.setCell(3, 3, true);
        boolean state = lifeModel.isAlive(3, 3);

        assertTrue(state);
    }
	
	@Test
    void testSetCellDead() {
		System.out.println("LifeTest | cell is dead");
        lifeModel.setCell(2, 2, false);
        assertFalse(lifeModel.isAlive(2, 2));
    }
	
	@Test
    void testGetCell() {
		System.out.println("LifeTest | get cell");
        ICell cell = lifeModel.getCell(0, 0);

        assertNotNull(cell);
    }
	
	 @Test
	    void testGetGrid() {
		 System.out.println("LifeTest | get grid");
	        IGrid grid = lifeModel.getGrid();

	        assertNotNull(grid);
	    }

}
