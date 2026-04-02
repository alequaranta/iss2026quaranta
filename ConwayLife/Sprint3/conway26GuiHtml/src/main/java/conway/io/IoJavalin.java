package conway.io;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.msg.ApplMessage;

public class IoJavalin {
	
	private static AtomicInteger pageCounter = new AtomicInteger(0);
	private WsMessageContext  lifeCtrlCtx;
	private String name;
	private String firstCaller        = null;
	private WsContext ownerCtx = null;
	protected Vector<WsContext> allConns = new Vector<>();

	public IoJavalin(String name) {
		this.name = name;
        var app = Javalin.create(config -> {
        	// Configurazione globale del timeout per le connessioni (dalla versione 6.x in avanti)
            //config.http.asyncTimeout = 300000L; // 5 minuti in millisecondi
        	config.jetty.modifyWebSocketServletFactory(factory -> {
                // Imposta il timeout (ad esempio 5 minuti)
                factory.setIdleTimeout(Duration.ofMinutes(30));
            });
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
    		  
        	var inputStream = getClass().getResourceAsStream("/page/LifeIInOutCanvas.html");     
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
        app.ws("/eval", ws -> {
            

        	ws.onConnect(ctx -> {
        		int id = pageCounter.incrementAndGet();
        		String callerName = "caller" + id;

        		sendsafe(ctx, "ID:" + callerName);
        		allConns.add(ctx);

        		CommUtils.outcyan(name + " | nuova connesione: " + callerName);

        		// ASSEGNAZIONE OWNER
        		if (ownerCtx == null) {
        			ownerCtx = ctx;
        			firstCaller = callerName;
        			CommUtils.outmagenta(name + " | owner assegnato a: " + callerName);
        		}
        	});

             
            ws.onMessage(ctx -> {
            	
                String message = ctx.message();     
                
                try {
                	IApplMessage m = new ApplMessage(message);
                    
                	
                	/* --------------------------------------------------
                	 * A) GOF -> PAGES
                	 * Invio griglia alle pagine connesse
                	 *  
                	 */
                	
                	if( m.msgContent().startsWith("[[")) {
                		CommUtils.outyellow(name + " | inoltro griglia di gioco");
                		for (WsContext conn : allConns) {
                		    sendsafe(conn, m.msgContent());
                		}
                		return;
                	}
                    
                    
                    /* --------------------------------------------------
                	 * B) PAGES -> SERVER
                	 * Canvas pronto, salva CTX pagina
                	 *  
                	 */
                    
                    if(  m.msgReceiver().equals(name) && m.msgContent().contains("canvasready") ) { 
                    	allConns.add(ctx);
                    	CommUtils.outgray(name + " |  memorizzo page Ctx: " + ctx);
                    
                    /* --------------------------------------------------
                     * C) GOF -> SERVER
                     * Set del controller, salva CTX controller
                     *  
                     */
                    
                    }else if( m.msgReceiver().equals(name) && m.msgSender().equals("lifectrl") && m.msgId().contains("setcontroller")) { 
                    	lifeCtrlCtx = ctx; //memorizzo connessione controller
                    	CommUtils.outgray(name + " |  memorizzo lifeCtrl Ctx: " + lifeCtrlCtx );
                    	//server ordina di ri-inizializzare la griglia di gioco
            			sendsafe( lifeCtrlCtx, "msg( eval, dispatch, guiserver, lifectrl, clear, 0 )" );
           
            		/* --------------------------------------------------
                     * D) PAGES -> GOF
                     * Server funge da ponte verso il controller
                     *  
                     */	
                    	
                    }else if( m.msgReceiver().equals("lifectrl") ) {  
                     	if( m.msgSender().equals("unknown") ){
                    		//Nuova paginna collegata
                    		return;
                    	}
                    	if( ctx.equals(ownerCtx) ){
                       		CommUtils.outblue(name + " | owner invia al controller: " + m);
                    		if( lifeCtrlCtx != null ) 
                    			sendsafe( lifeCtrlCtx, m.toString() );
                     	}else {
                     		CommUtils.outgray(name +" | viewer tenta di interagire - " + m.msgSender());
                    		                 		
                     	}
                    }
                    
                }catch(Exception e) {
                	CommUtils.outred(name + " |  not a IApplMessage: " + message);

                }               
            });
            

            ws.onClose(ctx -> {
                allConns.remove(ctx);

                //se non era l’owner non fare nulla
                if (ctx != ownerCtx)
                    return;

                CommUtils.outmagenta(name + " | owner della partita disconnesso");

                //scegli un nuovo owner se disponibile
                if (!allConns.isEmpty()) {
                    ownerCtx = allConns.get(0);
                    CommUtils.outmagenta(name + " | nuovo owner assegnato: " + ownerCtx);
                } else {
                    ownerCtx = null;
                    CommUtils.outgray(name + " | in attesa di nuove connesioni");
                }
            });

        });        
	}
	
    protected void sendsafe(WsContext ctx, String msg) {
    	synchronized (ctx.session) {  
            if (ctx.session.isOpen()) {
                ctx.send(msg);
            }
        }
    }
	

	
	public static void main(String[] args) {
		var resource = IoJavalin.class.getResource("/pages");
		CommUtils.outgreen("DEBUG: La cartella /page si trova in: " + resource);
		new IoJavalin("guiserver");
	}

}
