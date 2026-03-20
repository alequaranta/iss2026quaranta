package conway.io;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import main.java.conway.domain.Life;
import main.java.conway.domain.LifeController;
import main.java.conway.domain.LifeInterface;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.msg.ApplMessage;

public class IoJavalin {
	
	private WsConnectContext ownerCtx;
    private final List<WsMessageContext> allCtx = new CopyOnWriteArrayList<>();

	private WebOutDev outdev;
	private LifeController controller;
	
	public IoJavalin() {
		

		LifeInterface game = new Life(20, 20);
		//viene passato un riferimento a allCtx | ogni modifica è vista da outdev
        outdev = new WebOutDev(allCtx);
        controller = new LifeController(game, outdev);
	
        
		
/*
 * --------------------------------------------
 * Creazione Server      
 * --------------------------------------------
 */
		
        var app = Javalin.create(config -> {
			config.staticFiles.add(staticFiles -> {
				staticFiles.directory = "/page";
				staticFiles.location = Location.CLASSPATH; // Cerca dentro il JAR/Classpath
				/*
				 * i file sono "impacchettati" con il codice, non cercati sul disco rigido esterno.
				 */
		    });
		}).start(8080);
 
/*
 * --------------------------------------------
 * Parte HTTP        
 * --------------------------------------------
 */
        app.get("/", ctx -> {
        	
    		//Path path = Path.of("./src/main/resources/page/ConwayInOutPage.html");    		    
        	/*
        	 * Java cercherà il file all'interno del Classpath 
        	 * (dentro il JAR o nelle cartelle dei sorgenti di Eclipse), 
        	 * rendendo il codice universale
         	 */
        	
        	var inputStream = getClass().getResourceAsStream("/page/ConwayInOutPage.html");       	
        	if (inputStream != null) {
        		// Trasformiamo l'inputStream in stringa (o lo mandiamo come stream)
        	    String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        	    ctx.html(content);
        	} else {
		        ctx.status(404).result("File non trovato nel file system");
		    }
        }); 
       
/*
 * --------------------------------------------
 * Parte Websocket
 * --------------------------------------------
 */
        
        app.ws("/chat", ws -> {
            ws.onConnect(ctx -> CommUtils.outgreen("Client connected chat!"));
            ws.onMessage(ctx -> {
                String message = ctx.message();
                CommUtils.outcyan("IoJavalin |  riceve:" + message);
                ctx.send("Echo: " + message);
            });
        });   
        
        app.ws("/eval", ws -> {
            
        	ws.onConnect(ctx -> {
        		CommUtils.outgreen("IoJavalin | Client connected eval");

               if(ownerCtx == null){
                    ownerCtx = ctx;
                    CommUtils.outcyan("IoJavalin | New OWNER connected");
                } else {
                    CommUtils.outcyan("IoJavalin | New VIEWER connected");
                }

        		});
            
            ws.onMessage(ctx -> {
                String message = ctx.message();     
                CommUtils.outblue("IoJavalin |  eval receives:" + message );
                
                try {
                	IApplMessage m = new ApplMessage(message);
                    CommUtils.outblue("IoJavalin |  eval:" + m.msgContent() );
                    
                    // CMD: ready
                    if( m.msgContent().equals("ready")) allCtx.add(ctx);
                 
                    	
                    // BLOCCO DEI VIEWER
                    if(!ctx.equals(ownerCtx)) return;

                    
                    // CMD: start
                    if(m.msgContent().equals("start")){
                    	CommUtils.outgreen("IoJavalin | start game");
                    	controller.onStart();
             
                    // CMD: stop
                    }else if(m.msgContent().equals("stop")){
                    	CommUtils.outgray("IoJavalin | stop");
                    	controller.onStop();
                    
                    // CMD: clear
                    }else if(m.msgContent().equals("clear")){
                    	CommUtils.outgray("IoJavalin | clear");
                    	controller.onClear();
                    	
                    // CMD: change cell status
                    }else if( m.msgContent().contains("cell(")) { 

                                String data = m.msgContent().replace("cell(", "").replace(")", "");
                                String[] xy = data.split(",");
                                int x = Integer.parseInt(xy[0]);
                                int y = Integer.parseInt(xy[1]);
                                controller.switchCellState(x, y);

                    }else ctx.send(m.msgContent());
                }catch(Exception e) {
                	CommUtils.outred("IoJavalin |  error:" + e.getMessage());
                } 
                
            });
            

            ws.onClose(ctx -> {
                allCtx.remove(ctx);
                if(ctx.equals(ownerCtx)){
                    ownerCtx = null;
                    CommUtils.outcyan("IoJavalin | OWNER disconnected");
                }
            });

        });        
	}
	
 
	

	
	public static void main(String[] args) {
		var resource = IoJavalin.class.getResource("/pages");
		CommUtils.outgreen("DEBUG: La cartella /page si trova in: " + resource);
		new IoJavalin();
	}

}
