package com.miiitv.game.upnp;

import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

import com.miiitv.game.server.Logger;

public class BrowseRegistryListener implements RegistryListener {
	
	private final static String TAG = "BrowseRegistryListener";

	@Override
	public void afterShutdown() {
		Logger.i(TAG, "afterShutdown()");
	}

	@Override
	public void beforeShutdown(Registry reg) {
		Logger.i(TAG, "beforeShutdown()");
	}

	@Override
	public void localDeviceAdded(Registry reg, LocalDevice device) {
		Logger.i(TAG, "localDeviceAdded() ", device.getDisplayString());
	}

	@Override
	public void localDeviceRemoved(Registry reg, LocalDevice device) {
		Logger.i(TAG, "localDeviceRemoved() ", device.getDisplayString());
	}

	@Override
	public void remoteDeviceAdded(Registry reg, RemoteDevice device) {
		Logger.i(TAG, "remoteDeviceAdded() ", device.getDisplayString(), " ip: ", device.getIdentity().getDescriptorURL().getHost());
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry reg, RemoteDevice device, Exception arg2) {
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry reg, RemoteDevice device) {
	}

	@Override
	public void remoteDeviceRemoved(Registry reg, RemoteDevice device) {
		Logger.i(TAG, "remoteDeviceRemoved() ", device.getDisplayString());
	}

	@Override
	public void remoteDeviceUpdated(Registry reg, RemoteDevice device) {
		Logger.i(TAG, "remoteDeviceUpdated() ", device.getDisplayString());
	}
}
