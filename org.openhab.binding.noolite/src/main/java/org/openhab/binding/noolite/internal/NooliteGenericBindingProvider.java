/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.noolite.internal;

import org.openhab.binding.noolite.NooliteBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Petros
 * @since 1.0.0
 */
public class NooliteGenericBindingProvider extends AbstractGenericBindingProvider implements NooliteBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "noolite";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		//if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
		//	throw new BindingConfigParseException("item '" + item.getName()
		//			+ "' is of type '" + item.getClass().getSimpleName()
		//			+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		//}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		NooliteBindingConfig config = new NooliteBindingConfig();
		
		//parse bindingconfig here ...
		
		addBindingConfig(item, config);		
	}
	
	
	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author Petros
	 * @since 1.0.0
	 */
	class NooliteBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
	}
	
	
}
