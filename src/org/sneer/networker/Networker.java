package org.sneer.networker;

import java.util.ArrayList;

/*
 * The Networker interface is implemented by classes that know how to try to send datagrams
 *   to another Networker.
 *   
 * A Networker does not have NetIds. It has zero or more NetworkerDevices in it, and each one
 *   of these stores/keeps its own NetId (they can all copy each other so it appears that 
 *   "the Networker has a NetId").
 *   
 * If you call one of the "send" methods on a Networker, you don't really know which 
 *   NetworkerDevice will be selected to send it (well, if you just have one NetworkerDevice
 *   then you know, or if you retrieve the device you want and send through it, that works too).
 */
public interface Networker extends Sender {

	/*
	 * Inspect the virtual networker device list.
	 */
	public ArrayList<NetworkerDevice> getDevices();
}
