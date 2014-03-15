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
	
	String dumbRouterAddr;
	int dumbRouterPort;
	
	/*
	 * Each DON (Dumb Overlay Network) instance has a single UDP/IP router 
	 *   process somewhere, e.g. "dynamic.sneer.me" addr and "65235" port.
	 */

	public DumbNetworker(NetworkerListener listener, NetId netId, String dumbRouterAddr) {
		this(listener, netId, dumbRouterAddr, DEFAULT_PORT);
	}
	
	public DumbNetworker(NetworkerListener listener, NetId netId, String dumbRouterAddr, int dumbRouterPort) {
		this.netId = netId;
		this.dumbRouterAddr = dumbRouterAddr;
		this.dumbRouterPort = dumbRouterPort;
		this.listener = listener;
		
		device = new DumbNetworkerDevice(this);
		device.connect(dumbRouterAddr, dumbRouterPort);
		
		devices.add(device);
	}
	
	/*
	 * Networker
	 */
	
	public NetId getId() {
		return new NetId(netId);
	}

	public void send(NetId receiver, byte[] data) {
		if (! dead)
			device.send(receiver, data);
	}
	
	public NetworkerListener getListener() {
		return listener;
	}
	
	public ArrayList<Device> getDevices() {
		return devices;
	}

	public void kill() {
		if (! dead) {
			dead = true;
			
			device.disconnect();
			device = null;

			devices.clear();
			
			listener.killed();
		}
	}

	public boolean isDead() {
		return dead;
	}
	
	/*
	 * DeviceListener.
	 */

	public void receive(NetId sender, byte[] data) {
		if (! dead)
			listener.receive(sender, data);
	}
}
