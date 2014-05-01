/*
 * SensorSampler.java
 *
 * Copyright (c) Bhargavi, Chinmary and Karthik
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IIOPin;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application will receive the signals sent from the sun spot and will send enable the respective pins
 * to enable the motors on the toy car
 *
 * @author: Bhargavi, Chinmay and Karthik
 * Date: 29-April-2014
 */
public class SensorSampler extends MIDlet {

    //This is the port used to communicate between the sun spots
    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 10 * 100;  // in milliseconds
    
    /**
     * This function is used to start the application on the sun spot
     * @throws MIDletStateChangeException 
     */
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        String ourAddress = System.getProperty("IEEE_ADDRESS");
        //LEDs 1, 2, 3 and 4 are used on this sun spot
        ITriColorLED led1 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED1");
        ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED2");
        ITriColorLED led3 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED3");
        ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");        
        
        //IO pins D0 - D3 are used for controlling the L293D IC for motor controller
        IIOPin pinD0 = (IIOPin)Resources.lookup(IIOPin.class,"D0");
        IIOPin pinD1 = (IIOPin)Resources.lookup(IIOPin.class,"D1");
        IIOPin pinD2 = (IIOPin)Resources.lookup(IIOPin.class,"D2");
        IIOPin pinD3 = (IIOPin)Resources.lookup(IIOPin.class,"D3");
        
        int ret = 0;
        long now = 0;
        
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port            
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        while (true) {
            try {
                try {
                        now = System.currentTimeMillis();
                        dg.reset();
                        rCon.receive(dg);
                        ret = dg.readInt();
                        System.out.println("Received: " + ret + " from " + dg.getAddress());
                        
                        pinD0.setLow();
                        pinD1.setLow();
                        pinD2.setLow();
                        pinD3.setLow();
                        
                        //Forward
                        if(ret == 100){
                            pinD0.setHigh();
                            led2.setRGB(0, 0, 255);
                            led2.setOn();
                            Utils.sleep(50);
                            led2.setOff();
                            
                            pinD3.setHigh();
                            led4.setRGB(0, 0, 255);
                            led4.setOn();
                            Utils.sleep(50);
                            led4.setOff();
                            
                            pinD1.setLow();
                            pinD2.setLow();
                        }
                        
                        //Left
                        if(ret == 101){
                            pinD3.setHigh();
                            led1.setColor(LEDColor.ORANGE);
                            led1.setOn();
                            Utils.sleep(50);
                            led1.setOff();
                            
                            pinD0.setLow();
                            pinD1.setLow();
                            pinD2.setLow();
                        }
                        
                        //Right
                        if(ret == 110){
                            pinD0.setHigh();
                            led2.setColor(LEDColor.ORANGE);
                            led2.setOn();
                            Utils.sleep(50);
                            led2.setOff();
                            
                            pinD1.setLow();
                            pinD2.setLow();
                            pinD3.setLow();
                        }
                        
                        //Reverse
                        if(ret == 1000){
                            pinD1.setHigh();
                            led2.setColor(LEDColor.RED);
                            led2.setOn();
                            Utils.sleep(50);
                            led2.setOff();
                            
                            pinD2.setHigh();
                            led3.setColor(LEDColor.RED);
                            led3.setOn();
                            Utils.sleep(50);
                            led3.setOff();
                            
                            pinD0.setLow();
                            pinD3.setLow();
                        }
                        
                        //Stop
                        if(ret == 0){
                            pinD0.setLow();
                            pinD1.setLow();
                            pinD2.setLow();
                            pinD3.setLow();
                            
                            led1.setColor(LEDColor.ORANGE);
                            led1.setOn();
                            Utils.sleep(50);
                            led1.setOff();
                            
                            led2.setColor(LEDColor.ORANGE);
                            led2.setOn();
                            Utils.sleep(50);
                            led2.setOff();
                            
                        }
                    } catch (IOException e) {
                        System.out.println("Nothing received");
                    }
                
                // Go to sleep to conserve battery
                Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}
