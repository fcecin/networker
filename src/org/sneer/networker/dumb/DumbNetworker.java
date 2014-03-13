package org.sneer.networker.dumb;

import java.util.ArrayList;
import org.sneer.networker.*;

public class DumbNetworker implements Networker, NetworkerDeviceListener {
	
	NetworkerListener listener;
	DumbNetworkerDevice device;
	ArrayList<NetworkerDevice> devices = new ArrayList(1);
	
	/*
	 * Each DON (Dumb Overlay Network) instance has a single UDP/IP router 
	 *   process somewhere, e.g. "dynamic.sneer.me" addr and "65235" port.
	 */
	
	public DumbNetworker(String dumbRouterAddr, int dumbRouterPort, NetworkerListener listener) {
		this.listener = listener;
		
		device = new DumbNetworkerDevice(this);
		device.connect(dumbRouterAddr, dumbRouterPort);
		
		devices.add(device);
	}
	
	/*
	 * This is not mandated by the Networker interface.
	 * 
	 * The setId/getId are essential because they allow you to create a 
	 *   logical (overlay) server such as a chat server that always has the
	 *   same address.
	 */
	
	public void setId(NetId newId) {
		device.setId(newId);
	}
	
	public NetId getId() {
		return device.getId();
	}
	
	/*
	 * Networker
	 */
	
	@Override
	public ArrayList<NetworkerDevice> getDevices() {
		return devices;
	}

	@Override
	public void send(NetId receiver, byte[] data) {
		device.send(receiver, data);
	}

	/*
	 * NetworkerDeviceListener.
	 *
	 * All NetworkDevices call the same listener, which is the Networker
	 *   that owns them.
	 * A Networker ideally doesn't want to know what device received something.
	 * Devices are transparent to the user and hopefully to the networker as well.
	 * In case we find out they aren't, we're probably going to have to redesign 
	 *   the whole networker package.	
	 */

	@Override
	public void receive(NetId sender, byte[] data) {
		listener.receive(sender, data);
	}
}
