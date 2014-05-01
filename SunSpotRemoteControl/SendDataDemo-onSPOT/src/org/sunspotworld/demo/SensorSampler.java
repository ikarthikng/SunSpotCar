/*
 * SensorSampler.java
 *
 * Copyright 
 * Bhargavi, Chinmay and Karthik
 * The University of Texas at Arlington
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application is used to send integer values to other sunspot based on the accelerometer readings from the device
 *
 * @author Bhargavi, Chinmay and Karthik
 * Date: 29-April-2014
 */
public class SensorSampler extends MIDlet {

	//The port number used to transmitting the radio packets
    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 10 * 100;  // in milliseconds
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        String ourAddress = System.getProperty("IEEE_ADDRESS"); 
        //The LEDs on the sunspot
        ITriColorLED led1 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED1");
        ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
        ITriColorLED led7 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
	//Accelerometer
        IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
        int ret = 0;
        double xTilt = 0;
        double yTilt = 0;
        
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port            
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        while (true) {
            try {
                // Get the current time
                long now = System.currentTimeMillis();

                //Get the accelerometer reading from the device
                xTilt = accel.getTiltX();
                yTilt = accel.getTiltY();
                
		//This will return 110 indicating the device is turned right
                if (xTilt > 0.5 && yTilt < 0.5 && yTilt > -0.5) {
                    led7.setRGB(0, 255, 0);
                    led7.setOn();
                    Utils.sleep(50);
                    led7.setOff();
                    ret = 110;
                }
				
		//This will return 101 indicating the device is turned left
                if (xTilt < -0.5 && yTilt < 0.5 && yTilt > -0.5) {
                    led1.setRGB(0, 255, 255);
                    led1.setOn();
                    Utils.sleep(50);
                    led1.setOff();
                    ret = 101;
                }
				
		//This will return 100 indicating the device is turned front
                if (yTilt > 0.5 && xTilt < 0.5 && xTilt > -0.5) {
                    led4.setRGB(0, 0, 255);
                    led4.setOn();
                    Utils.sleep(50);
                    led4.setOff();
                    ret = 100;
                }
				
		//This will return 1000 indicating the device is turned reverse
                if (yTilt < -0.5 && xTilt < 0.5 && xTilt > -0.5) {
                    led4.setRGB(255, 0, 0);
                    led4.setOn();
                    Utils.sleep(50);
                    led4.setOff();
                    ret = 1000;
                }
				
                //This will return 0 indicating the device is at level and needs to stop
                if(xTilt < -0.07 && xTilt > -0.2) {
                        led4.setRGB(0, 255, 0);
                        led4.setOn();
                        Utils.sleep(50);
                        led4.setOff();
                        ret = 0;					
                }				
                
                System.out.println("RET Value : "+ret);
                System.out.println("XTilt : "+xTilt+"Y Tilt : "+yTilt);

                // Send sensor reading into a radio datagram and send it.
                dg.reset();
                dg.writeInt(ret);
                rCon.send(dg);
				
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