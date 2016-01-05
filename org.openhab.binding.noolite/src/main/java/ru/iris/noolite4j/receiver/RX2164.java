/*
 * Copyright 2014 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.noolite4j.receiver;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import ru.iris.noolite4j.watchers.BatteryState;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.DataFormat;
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.SensorType;
import ru.iris.noolite4j.watchers.Watcher;

/**
 * �������� ������� RX2164
 * 
 * @see <a href="http://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html">http
 *      ://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html</a>
 */

public class RX2164 {

	private static final long READ_UPDATE_DELAY_MS = 200L;
	private static final short VENDOR_ID = 5824; // 0x16c0;
	private static final short PRODUCT_ID = 1500; // 0x05dc;
	private final Logger LOGGER = LoggerFactory.getLogger(RX2164.class.getName());
	private final Context context = new Context();
	private Watcher watcher = null;
	private byte availableChannels = 64;
	private boolean shutdown = false;
	private DeviceHandle handle;
	private boolean pause = false;

	/**
	 * ��� �������� �����-callback
	 * 
	 * @param watcher
	 *            ���������� ��� �����
	 */
	public void addWatcher(Watcher watcher) {
		this.watcher = watcher;
	}

	/**
	 * ����� ������ ������ � ����������
	 * 
	 * @throws LibUsbException
	 *             ������ LibUSB
	 */
	public boolean open() throws LibUsbException {

		LOGGER.debug("Opening device RX2164");

		// �������������� ��������
		int result = LibUsb.init(context);
		if (result != LibUsb.SUCCESS) {
			try {
				throw new LibUsbException("LibUSB init error", result);
			} catch (LibUsbException e) {
				LOGGER.error("LibUSB init error: ", result);
				e.printStackTrace();
			}
		}

		handle = LibUsb.openDeviceWithVidPid(context, VENDOR_ID, PRODUCT_ID);

		if (handle == null) {
			LOGGER.error("Device RX2164 not found!");
			return false;
		}

		if (LibUsb.kernelDriverActive(handle, 0) == 1) {
			LibUsb.detachKernelDriver(handle, 0);
		}

		int ret = LibUsb.setConfiguration(handle, 1);

		if (ret != LibUsb.SUCCESS) {
			LOGGER.error("Device RX2164 configuration error");
			LibUsb.close(handle);
			if (ret == LibUsb.ERROR_BUSY) {
				LOGGER.error("Device RX2164 bisy");
			}
			return false;
		}

		LibUsb.claimInterface(handle, 0);
		return true;
	}

	/**
	 * ���������� ������
	 */
	public void close() {
		LOGGER.debug("����������� ���������� RX2164");
		shutdown = true;

		if (handle != null)
			LibUsb.close(handle);

		LibUsb.exit(context);
	}

	/**
	 * ������ �������� ������
	 */
	public void start() {
		LOGGER.debug("����������� ������� ��������� ������ �� ���������� RX2164");

		new Thread(new Runnable() {
			@Override
			public void run() {

				int togl;

				// ��� ������ ����� ����� �����, ������ ����� �� ����� ���� �
				// ������ ������ � TOGL
				int tmpTogl = -10000;

				ByteBuffer buf = ByteBuffer.allocateDirect(8);

				/**
				 * ������� ���� ��������� ������
				 */
				while (!shutdown) {

					/**
					 * ����� ����� ��� ���� ����� ����� ����������� ��������
					 * ������ � ���������� (��������, �������) TODO ��������,
					 * ���� ������� �����
					 */
					if (!pause) {
						LibUsb.controlTransfer(handle,
								(byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN),
								(byte) 0x9, (short) 0x300, (short) 0, buf, 100L);
					}

					/**
					 * ���������� �������� TOGL, ����� ������, ��� ������ �����
					 * �������
					 */
					togl = buf.get(0) & 63;

					/**
					 * �������� ����� ������� TOGL ����� ���� 0
					 */
					if (togl != tmpTogl) {

						Notification notification = new Notification();

						notification.setBuffer(buf);

						byte channel = (byte) (buf.get(1) + 1);
						byte action = buf.get(2);
						byte dataFormat = buf.get(3);

						LOGGER.debug("�������� ����� ������� ��� RX2164");
						LOGGER.debug("�������� TOGL: " + togl);
						LOGGER.debug("�������: " + CommandType.getValue(action).name());
						LOGGER.debug("�����: " + channel);

						if (dataFormat == DataFormat.NO_DATA.ordinal()) {
							LOGGER.debug("���������� ������ � �������: ���");
							notification.setDataFormat(DataFormat.NO_DATA);
						} else if (dataFormat == DataFormat.ONE_BYTE.ordinal()) {
							LOGGER.debug("���������� ������ � �������: 1 ����");
							notification.setDataFormat(DataFormat.ONE_BYTE);
						} else if (dataFormat == DataFormat.TWO_BYTE.ordinal()) {
							LOGGER.debug("���������� ������ � �������: 2 �����");
							notification.setDataFormat(DataFormat.TWO_BYTE);
						} else if (dataFormat == DataFormat.FOUR_BYTE.ordinal()) {
							LOGGER.debug("���������� ������ � �������: 4 �����");
							notification.setDataFormat(DataFormat.FOUR_BYTE);
						} else if (dataFormat == DataFormat.LED.ordinal()) {
							LOGGER.debug("���������� ������ � �������: ������ LED");
							notification.setDataFormat(DataFormat.LED);
						} else {
							LOGGER.debug("���������� ������ � �������: ����������� ���");
							notification.setDataFormat(DataFormat.NO_DATA);
						}

						notification.setChannel(channel);

						switch (CommandType.getValue(action)) {
						case TURN_ON:
							notification.setType(CommandType.TURN_ON);
							watcher.onNotification(notification);
							break;
						case TURN_OFF:
							notification.setType(CommandType.TURN_OFF);
							watcher.onNotification(notification);
							break;
						case SET_LEVEL:
							notification.setType(CommandType.SET_LEVEL);
							notification.addData("level", buf.get(4));
							LOGGER.debug("������� ����������: " + buf.get(4));
							watcher.onNotification(notification);
							break;
						case SWITCH:
							notification.setType(CommandType.SWITCH);
							watcher.onNotification(notification);
							break;
						case SLOW_TURN_ON:
							notification.setType(CommandType.SLOW_TURN_ON);
							watcher.onNotification(notification);
							break;
						case SLOW_TURN_OFF:
							notification.setType(CommandType.SLOW_TURN_OFF);
							watcher.onNotification(notification);
							break;
						case STOP_DIM_BRIGHT:
							notification.setType(CommandType.STOP_DIM_BRIGHT);
							watcher.onNotification(notification);
							break;
						case REVERT_SLOW_TURN:
							notification.setType(CommandType.REVERT_SLOW_TURN);
							watcher.onNotification(notification);
							break;
						case RUN_SCENE:
							notification.setType(CommandType.RUN_SCENE);
							watcher.onNotification(notification);
							break;
						case RECORD_SCENE:
							notification.setType(CommandType.RECORD_SCENE);
							watcher.onNotification(notification);
							break;
						case BIND:
							notification.setType(CommandType.BIND);

							/**
							 * ���� ������������� ������, �� �� ��������
							 * ������������� ���� ���
							 */
							if (notification.getDataFormat() == DataFormat.ONE_BYTE) {
								// ��� �������
								notification.addData("sensortype", SensorType.values()[(buf.get(4) & 0xff)]);
							}

							watcher.onNotification(notification);
							break;
						case UNBIND:
							notification.setType(CommandType.UNBIND);
							watcher.onNotification(notification);
							break;
						case SLOW_RGB_CHANGE:
							notification.setType(CommandType.SLOW_RGB_CHANGE);
							watcher.onNotification(notification);
							break;
						case SWITCH_COLOR:
							notification.setType(CommandType.SWITCH_COLOR);
							watcher.onNotification(notification);
							break;
						case SWITCH_MODE:
							notification.setType(CommandType.SWITCH_MODE);
							watcher.onNotification(notification);
							break;
						case SWITCH_SPEED_MODE:
							notification.setType(CommandType.SWITCH_SPEED_MODE);
							watcher.onNotification(notification);
							break;
						case BATTERY_LOW:
							notification.setType(CommandType.BATTERY_LOW);
							watcher.onNotification(notification);
							break;
						case TEMP_HUMI:
							notification.setType(CommandType.TEMP_HUMI);

							/**
							 * ���������� � �����������, ���� ������� �
							 * ��������� ������� ��������� �� 2 ������
							 */

							int intTemp = ((buf.get(5) & 0x0f) << 8) + (buf.get(4) & 0xff);

							if (intTemp >= 0x800) {
								intTemp = intTemp - 0x1000;
							}

							// �������� � �������� �������
							double temp = (double) intTemp / 10;

							// �������� �������
							notification.addData("battery", BatteryState.values()[(buf.get(5) >> 7) & 1]);

							// �����������
							notification.addData("temp", temp);

							// ��� �������
							notification.addData("sensortype", SensorType.values()[((buf.get(5) >> 4) & 7)]);

							/**
							 * � ������� ����� ������ �������� ���������
							 */
							notification.addData("humi", buf.get(6) & 0xff);

							/**
							 * � ��������� ����� ������ �������� ������ �
							 * ��������� ����������� ������� �� ��������� -
							 * unsigned byte (255)
							 */
							notification.addData("analog", buf.get(7) & 0xff);

							watcher.onNotification(notification);
							break;

						default:
							LOGGER.error("����������� �������: " + action);
						}
					}

					/**
					 * ����
					 */
					try {
						Thread.sleep(READ_UPDATE_DELAY_MS);
					} catch (InterruptedException e) {
						LOGGER.error("������: " + e.getMessage());
						e.printStackTrace();
					}

					tmpTogl = togl;
					buf.clear();
				}
			}
		}).start();
	}

	/**
	 * �������, ������������ ��� �������� ���������� �� ������������ �����
	 * RX2164
	 * 
	 * @param channel
	 *            �����, �� ������� ����� ��������� ����������
	 */
	public void bindChannel(byte channel) {
		if (channel > availableChannels - 1) {
			LOGGER.error("�������� ����� ������ ������������� ��������!");
			return;
		}

		ByteBuffer buf = ByteBuffer.allocateDirect(8);
		buf.put((byte) 1);
		buf.put(channel);

		pause = true;
		LibUsb.controlTransfer(handle,
				(byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte) 0x9,
				(short) 0x300, (short) 0, buf, 100);
		pause = false;
	}

	/**
	 * �������, ������������ ��� ������� ���������� � ������������� ������
	 * RX2164
	 * 
	 * @param channel
	 *            �����, � �������� ����� �������� ����������
	 */
	public void unbindChannel(byte channel) {
		if (channel > availableChannels - 1) {
			LOGGER.error("�������� ����� ������ ������������� ��������!");
			return;
		}

		ByteBuffer buf = ByteBuffer.allocateDirect(8);
		buf.put((byte) 3);
		buf.put(channel);

		pause = true;
		LibUsb.controlTransfer(handle,
				(byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte) 0x9,
				(short) 0x300, (short) 0, buf, 100);
		pause = false;
	}

	/**
	 * �������, ������������ ��� ������� ���� ��������� RX2164
	 */
	public void unbindAllChannels() {
		ByteBuffer buf = ByteBuffer.allocateDirect(8);
		buf.put((byte) 4);

		pause = true;
		LibUsb.controlTransfer(handle,
				(byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte) 0x9,
				(short) 0x300, (short) 0, buf, 100);
		pause = false;
	}
}