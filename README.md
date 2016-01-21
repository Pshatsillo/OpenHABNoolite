# OpenHABNoolite
Binding for nooLite

Для мака - ничего не нужно делать

# On Linux 
you need write permissions on the device file of the USB device you want to communicate with. Check if the devices are accessible when running your program as root. If this works then it is recommended to configure udev to give your user write permissions when the device is attached. You can do this by creating a file like /etc/udev/rules.d/99-userusbdevices.rules with content like this:

#  FOR RX2164:

  SUBSYSTEM=="usb",ATTR{idVendor}=="16c0",ATTR{idProduct}=="05dc",MODE="0660",GROUP="wheel"
  
#  FOR PC118, PC1116, PC1132
  
  SUBSYSTEM=="usb",ATTR{idVendor}=="16c0",ATTR{idProduct}=="05df",MODE="0660",GROUP="wheel"
  
  GROUP = username or other existing with user membership
  
# Windows 

Use the most recent version of [Zadig](http://zadig.akeo.ie), an Automated Driver Installer GUI application for WinUSB, libusb-win32 and libusbK...



Смена драйвера на WinUSB или libusb-win32 - оба работают.

