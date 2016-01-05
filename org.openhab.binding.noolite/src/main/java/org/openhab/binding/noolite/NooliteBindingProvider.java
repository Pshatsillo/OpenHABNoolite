/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.noolite;

import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.Item;

/**
 * @author Petros
 * @since 1.0.0
 */
public interface NooliteBindingProvider extends BindingProvider {
	
	public String getChannel(String itemName);

	public String getType(String itemName);

	Class<? extends Item> getItemType(String itemName);

}