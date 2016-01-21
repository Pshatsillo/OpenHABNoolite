# OpenHABNoolite
Binding for nooLite

Для мака - ничего не нужно делать

On Linux you need write permissions on the device file of the USB device you want to communicate with. Check if the devices are accessible when running your program as root. If this works then it is recommended to configure udev to give your user write permissions when the device is attached. You can do this by creating a file like /etc/udev/rules.d/99-userusbdevices.rules with content like this:

  SUBSYSTEM=="usb",ATTR{idVendor}=="89ab",ATTR{idProduct}=="4567",MODE="0660",GROUP="wheel"
  
  GROUP = username or other existing with user membership
  
This means that whenever a USB device with vendor id 0x89ab and product id 0x4567 is attached then the group wheel is permitted to write to the device. So make sure your user is in that group (or use a different group).

If your device uses a shared vendor/product id then you might want to filter for the manufacturer and product name. This can be done by checking the ATTR properties manufacturer and product.

To activate this new configuration you may need to re-attach the USB device or run the command udevadm trigger.


Windows - Use the most recent version of Zadig, an Automated Driver Installer GUI application for WinUSB, libusb-win32 and libusbK...

http://zadig.akeo.ie

Смена драйвера на WinUSB или libusb-win32 - оба работают.

