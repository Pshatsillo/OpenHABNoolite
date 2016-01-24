# [OpenHAB](http://www.openhab.org/) binding for [nooLite](http://www.noo.com.by/sistema-noolite.html)

# Based on [Noolite4j](https://github.com/Neuronix2/Noolite4J) library

---

# INSTALLATION

# Mac os X

Plug and play :)

# Linux 

You need write permissions on the device file of the USB device you want to communicate with. Check if the devices are accessible when running your program as root. If this works then it is recommended to configure udev to give your user write permissions when the device is attached. You can do this by creating a file like `/etc/udev/rules.d/99-userusbdevices.rules` with content like this:

 

-  **FOR RX2164:** `SUBSYSTEM=="usb",ATTR{idVendor}=="16c0",ATTR{idProduct}=="05dc",MODE="0660",GROUP="wheel"`
    
- **FOR PC118, PC1116, PC1132:** `SUBSYSTEM=="usb",ATTR{idVendor}=="16c0",ATTR{idProduct}=="05df",MODE="0660",GROUP="wheel"`
  
	**GROUP** = username or other existing with user membership
  
# Windows 

Use the most recent version of [Zadig](http://zadig.akeo.ie), an Automated Driver Installer GUI application for WinUSB, libusb-win32 and libusbK...

Change driver to WinUSB or libusb-win32 - they both work.


----------


# Using with OpenHAB #

- **In openhab.cfg:** 
```
############################## Noolite Binding ####################################
    	#
    	# IP address or hostname for the module
    	noolite:refresh=100000
    	noolite:RX=On
		# 8, 16 or 32 (depends on USB module you choose)
    	noolite:PC=32
```

- ## For [RX2164](http://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html):

	 **GENERAL:** 
	
     **{noolite="Receive:bind"}**  
     **{noolite="Receive:unbind"}**  
     **{noolite="Receive:test"}**  
     **{noolite="Receive:channelNumber:Type"}**
     
- ## Binding:
  - **items:**
       ```java
		Number Read_channel_setpoint "Receiver channel: [%d]" 
		Switch Read_bind {noolite="Receive:bindflag"}  //when bind successful - switch is off
		Number Read_bind_channel {noolite="Receive:bind"}

		//unbinding:
		Switch Read_unbind
		Number Read_ubind_channel {noolite="Receive:unbind"}
       ```
  - **sitemap:**
  
       ```java
	    Frame {
		  Setpoint item=Read_channel_setpoint minValue=1 maxValue=64
		  Switch item=read_bind label="Receiver bind"
		  }
       ```
   - **rules:**
    
        ```java
		rule "Receive bind channel"
		when 
		  Item Read_channel_setpoint received command
		then 
		  postUpdate(Read_channel_setpoint, Read_channel_setpoint.state)
		end

		rule "Binding receiver"
		when
		  Item Read_bind changed to ON
		then
		  sendCommand(Read_bind_channel, Read_channel_setpoint.state as DecimalType)
		end

		
		rule "Unbinding receiver"
		when
		  Item Read_unbind changed to ON
		then
		  sendCommand(Read_ubind_channel, Read_channel_setpoint.state as DecimalType)
		end
       ```

- ## Test item:  
    Used for discover the channel number

  ```java
   String ChannelNumber "You receive values from channel: [%s]" {noolite="Receive:test"} 
	```
- ## Sensors and switches: ##
	
	[PT111](http://www.noo.com.by/pt111.html)(temperature and humidity) and [PT112](http://www.noo.com.by/pt112.html)(temperature only) 

  - **items:**
    ```
    //sensors
	String Temperature {noolite="Receive:1:PT111_t"}
	String Humidity {noolite="Receive:1:PT111_h"}
	String BatteryState {noolite="Receive:1:PT111_batt"}
    //switches
    String Radiopult313_button1 {noolite="Receive:2:PU313"}
    String Radiopult313_button2 {noolite="Receive:3:PU313"}
    String Radiopult313_button3 {noolite="Receive:4:PU313"}
	```
    
  - **rules:**
    ```java
    rule "using Button"
    when 
      Item Radiopult313_button1 received update
    then 
      if (Radiopult313_button1.state.equals("RUN_SCENE")){
           postUpdate(Buttonresult, "Button short pressed")	
      } else if(Radiopult313_button1.state.equals("RECORD_SCENE")){
	             postUpdate(Buttonresult, "Button long pressed")	
      }
    end
    ```
 - ## For [РС1ххх](http://www.noo.com.by/adapter-noolite-pc.html):
    **GENERAL:** 
    
     **{noolite="Send:bind"}**  
     **{noolite="Send:channelNumber"}**  
     **{noolite="Send:channelNumber:RGB"}**

- ## Binding:
  - **items:**

     ```java
     Number Send_Channel_Setpoint "Sender channel: [%d]"
     Switch Send_Bind
     Number Send_Bind_Channel {noolite="Send:bind"}
     ```
  
   - **sitemap:**
        ```java
	    Frame {
		  Setpoint item=Send_Channel_Setpoint minValue=1 maxValue=32
		  Switch item=Send_Bind label="Bind sender"
    	}
       ```
       
       - **rules:**
    
        ```java
		rule "Send bind"
        when 
          Item Send_Bind changed to ON
        then
          sendCommand(Send_Bind_Channel, Send_Channel_Setpoint.state as DecimalType)
          postUpdate(Read_Bind, OFF)
        end

        rule "Setting bind send channel"
        when 
          Item Send_Channel_Setpoint received command
        then
          postUpdate(Send_Channel_Setpoint, Send_Channel_Setpoint.state)
        end
       ```
       
- ## Power modules and dimmers:

   - **items:**
   
     ```java
     Switch Send_on_off {noolite="Send:1"}
     Dimmer Dimm_light {noolite="Send:2"}
     ```
     
   - **sitemap:**
   
     ```java
     Switch item=Send_on_off label="On/Off switch"
	 Slider item=Dimm_light label="Dimmer [%d %%]"
     ```
     
- ## RGB LED Controller
	- **items:**
	
	  ```java
       String RGB {noolite="Send:3:RGB"}
       Color RGB_COLOR "rgbLight" <slider>
      ```
      
    - **sitemap:**
  
      ```java
      Colorpicker item=RGB_COLOR
      ```
      
   - **rules:**
   
     ```java
     import org.openhab.core.library.types.*
     rule "RGB LED"
     when
       Item RGB_COLOR changed
     then
       var HSBType hsbValue
       var String red
       var String green
       var String blue

       hsbValue = RGB_COLOR.state as HSBType

       red = hsbValue.red.intValue.toString
       green = hsbValue.green.intValue.toString
       blue = hsbValue.blue.intValue.toString

       var String rgbResult =  red + "," + green +","+ blue

       sendCommand(RGB, rgbResult)
     end
     ```
