package usingSupport;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import demoStrange.AutoMsgMqtt;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.mqtt.MqttSupport;
import unibo.basicomm23.msg.ApplMessage;
import unibo.basicomm23.utils.CommUtils;

/*
 * SUB-TEST MQTT per conway26GuiAlonePactorTcp
 * IOJavalin scrive su TOPIC: lifegameIn
 * */


public class SubTest {
	
	private final String MqttBroker = "tcp://localhost:1883";
	private MqttSupport mqttSupport = new MqttSupport();
 	private String inputTopic = "lifegameIn"; //Topic per conway
	private String name;
	
	public void doJob() throws Exception {
		this.name = "Sub Test";
		mqttSupport.connectToBroker(name,MqttBroker);
		mqttSupport.subscribe ( inputTopic, (topic, mqttmsg) -> {
			//Lambda is of type org.eclipse.paho.client.mqttv3.IMqttMessageListener
			String msg            = new String( mqttmsg.getPayload() );
			IApplMessage applMesg = new ApplMessage(msg);
			CommUtils.outmagenta(name + " | Riceve via listener: " + msg );
			//Se capisco che il messaggio è una richiesta (ovvero nel caso in cui ci si aspetta una risposta):
			if( applMesg.isRequest() ) {
				CommUtils.outred(name + " | WARNING: unable to handle requests " + applMesg);
				System.exit(0);
			}
		});
		CommUtils.outmagenta(name + " | CREATED"  );
	
	}
	
	public static void main(String[] args) throws Exception { 		 
		SubTest appl = new SubTest( );   
		appl.doJob();
    
 	}

}
