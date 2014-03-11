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
 *
 * The idea is that you can implement an app that talks to something that
 *   implements Networker, but you can "add more networks" (logical networks)
 *   to the networker and the app (or apps) will have additional ways to find
 *   its (named) peers transparently. 
 *
 * Let's see if that's a good idea... the DumbNetworker sample impl. doesn't
 *   really stress the concept. When we try to do P2P we will see.
 *
 */
public interface Networker extends Sender {

	/*
	 * Inspect the virtual networker device list.
	 */
	public ArrayList<NetworkerDevice> getDevices();
}
