/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.noolite.internal;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.noolite.NooliteBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.iris.noolite4j.receiver.RX2164;
import ru.iris.noolite4j.sender.PC1116;
import ru.iris.noolite4j.sender.TXChoose;
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.Watcher;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Petros
 * @since 1.0.0
 */
public class NooliteBinding extends AbstractActiveBinding<NooliteBindingProvider> {

	private static final Logger logger = LoggerFactory.getLogger(NooliteBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is
	 * set in the activate() method and must not be accessed anymore once the
	 * deactivate() method was called or before activate() was called.
	 */
	private BundleContext bundleContext;

	/**
	 * the refresh interval which is used to poll values from the Noolite server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;
	private boolean rx, tx;
	TXChoose pc;

	public NooliteBinding() {
	}

	/**
	 * Called by the SCR to activate the component with its configuration read
	 * from CAS
	 * 
	 * @param bundleContext
	 *            BundleContext of the Bundle that defines this component
	 * @param configuration
	 *            Configuration properties for this component obtained from the
	 *            ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component
		// definition has the
		// configuration-policy set to require. If set to 'optional' then the
		// configuration may be null

		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

		String rxS = (String) configuration.get("RX");

		if (StringUtils.isNotBlank(rxS)) {

			if (rxS.equals("On")) {
				rx = true;
			}
		}

		String PC = (String) configuration.get("PC");

		if (StringUtils.isNotBlank(PC)) {

			if (PC.equals("8")) {
				tx = true;
				pc = new TXChoose((byte) 8);
			} else if (PC.equals("16")) {
				tx = true;
				pc = new TXChoose((byte) 16);
			} else if (PC.equals("32")) {
				tx = true;
				pc = new TXChoose((byte) 32);
			}
		}

		setProperlyConfigured(true);

		if (tx) {

			pc.open();
		}

		if (rx) {
			RX2164 rx = new RX2164();
			Watcher watcher = new Watcher() {

				@Override
				public void onNotification(Notification notification) {
					logger.debug("Command accepted");
				}
			};
			if (rx.open()) {
				rx.addWatcher(watcher);
				rx.start();
			}
		}

	}

	/**
	 * Called by the SCR when the configuration of a binding has been changed
	 * through the ConfigAdmin service.
	 * 
	 * @param configuration
	 *            Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
	}

	/**
	 * Called by the SCR to deactivate the component when either the
	 * configuration is removed or mandatory references are no longer satisfied
	 * or the component has simply been stopped.
	 * 
	 * @param reason
	 *            Reason code for the deactivation:<br>
	 *            <ul>
	 *            <li>0 – Unspecified
	 *            <li>1 – The component was disabled
	 *            <li>2 – A reference became unsatisfied
	 *            <li>3 – A configuration was changed
	 *            <li>4 – A configuration was deleted
	 *            <li>5 – The component was disposed
	 *            <li>6 – The bundle was stopped
	 *            </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "Noolite Refresh Service";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		// logger.debug("tx is: " + tx + " rx is: " + rx);

	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand({},{}) is called!", itemName, command);

		for (NooliteBindingProvider provider : providers) {
			for (String itemname : provider.getItemNames()) {

				if (provider.getChannel(itemName).equals("bind")) {
					pc.bindChannel(Byte.parseByte(command.toString()));
					logger.debug("binding " + command.toString() + " channel");
				} else if (provider.getChannel(itemName).equals("unbind")) {
					pc.unbindChannel(Byte.parseByte(command.toString()));
					logger.debug("unbinding " + command.toString() + " channel");
				} else {

					if (provider.getItemType(itemname).toString().contains("SwitchItem")) {
						if (itemname.equals(itemName)) {

							if (command.toString().equals("ON")) {
								pc.turnOn(Byte.parseByte(provider.getChannel(itemName)));
								logger.debug(provider.getChannel(itemName));
							} else if (command.toString().equals("OFF")) {
								pc.turnOff(Byte.parseByte(provider.getChannel(itemName)));
								logger.debug(provider.getChannel(itemName));
							}

						}

					}
				}
			}
		}

	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);
	}

}
