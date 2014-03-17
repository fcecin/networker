package org.sneer.networker.dumb;

import java.util.ArrayList;
import org.sneer.networker.*;

/**
 * This package provides a sample and default implementation of the 
 *   org.sneer.networker package of interfaces.
 * 
 * The DumbNetworker is a Networker that routes over a centralized routing
 *   server (DumbNetworkerRouter) which is a singleton process on the 
 *   network (there's exactly one DumbNetworkerRouter serving a network of 
 *   DumbNetworker clients).
 * 
 */
public class DumbNetworker implements Networker, DeviceListener {
	
	public static final int DEFAULT_PORT = 65235;

	NetId netId; // overlay address
	boolean dead;
	
	NetworkerListener listener;
	DumbNetworkerDevice device;
	ArrayList<Device> devices = new ArrayList(1);
	
	// You still probably want to call setListener() and then bind()
	//   once after construction.
	public DumbNetworker(NetId netId) {
		this.netId = netId;

		device = new DumbNetworkerDevice(this);
		devices.add(device);
	}
	
	// Bind to a router. You usually will call this only once for any given
	//   DumbNetworker, but for good measure we'll handle multiple calls too.
	public synchronized void bind(String dumbRouterAddr, int dumbRouterPort) {
		
		// Our DumbNetworkerDevice is not that dumb: it already knows how to
		//  handle multiple subsequent connect()s with different router 
		//  addresses on each call.
		device.connect(dumbRouterAddr, dumbRouterPort);
	}
	
	// Use the default port
	public synchronized void bind(String dumbRouterAddr) {
		bind(dumbRouterAddr, DEFAULT_PORT);
	}

	/*
	 * Networker
	 */
	
	public synchronized NetId getId() {
		return new NetId(netId);
	}

	public synchronized void send(NetId receiver, byte[] data) {
		if (! dead)
			device.send(receiver, data);
	}
	
	public synchronized NetworkerListener getListener() {
		return listener;
	}
	
	public synchronized void setListener(NetworkerListener listener) {
		this.listener = listener;
	}
	
	public synchronized ArrayList<Device> getDevices() {
		return devices;
	}

	public synchronized void kill() {
		if (! dead) {
			dead = true; // note that this should totally happen ...
			
			device.disconnect();
			device = null;
			devices.clear();
			
			if (listener != null)
				listener.killed(); // ... before this, so isDead() is already true
					               // and so killed() can join e.g. a network
						           // thread that is polling isDead() by itself.
		}
	}

	public synchronized boolean isDead() {
		return dead;
	}
	
	/*
	 * DeviceListener.
	 */

	public synchronized void receive(NetId sender, byte[] data) {
		if (! dead && listener != null)
			listener.receive(sender, data);
	}
}
