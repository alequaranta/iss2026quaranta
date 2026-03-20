package conway.io;

import io.javalin.websocket.WsMessageContext;
import main.java.conway.domain.IOutDev;
import main.java.conway.domain.IGrid;

import java.util.List;

public class WebOutDev implements IOutDev {

    private final List<WsMessageContext> connections;

    public WebOutDev(List<WsMessageContext> connections){
        this.connections = connections;
    }

    private void sendAll(String msg){
        for(WsMessageContext ctx : connections){
            try{ ctx.send(msg); } catch(Exception ignored){}
        }
    }

    @Override
    public void display(String msg){
        sendAll("LOG from Life:" + msg);
    }

    @Override
    public void displayCell(IGrid grid, int x, int y){
        int v = grid.getCellValue(x,y) ? 0 : 1;  
        sendAll("cell(" + x + "," + y + "," + v + ")");
    }

    @Override
    public void displayGrid(IGrid grid){
        sendFullGrid(grid);
    }

    public void sendFullGrid(IGrid grid){
        for(int y=0; y<grid.getColsNum(); y++){
            for(int x=0; x<grid.getRowsNum(); x++){
                int v = grid.getCellValue(x,y) ? 0 : 1;
                sendAll("cell(" + x + "," + y + "," + v + ")");
            }
        }
    }

    @Override
    public void close(){}
}