# Scanning

Goal is to find open ports (machines usually have 65,535!) to find more info on technologies being used by target. If at least one port is open, the target is vulnerable. Devices like personal computers usually don't have any ports open since they're not being used as servers.

Common Ports - 
* 80/443 - HTTP/S
* 22 - SSH
* 53 - DNS
* 25 - SMTP
* 21 - FTP

## TCP/UDP

Most widely used protocols for sending data over the internet

### TCP 3way handshake

1. Syn - establish connection to server (synchronized sequence number)
2. Syn/Ack - server responds with response received and the sequence number
3. Ack - client responds with response received, and now the connection is formed

### User Datagram Protocol

Faster than TCP since it does not handle errors

## Downloading Vulnerable VMs

We will download vulnerable virtual machines to practice scanning.

[Metasploitable](https://information.rapid7.com/download-metasploitable-2017.html?utm_source=r7&utm_medium=cta&utm_content=metasploitable&utm_campaign=wbw)

## Looking for Vulnerable Machines on a Network

`sudo netdiscover` - finds devices on the network

`netstat -nr` - info on router

## Scanning Devices on a Network

Nmap is a network mapper that's free and open source. Sends packets to devices on a network and analyzes the responses.

`man nmap` - see detailed write up of nmap

`nmap IPADDRESS` - can sometimes take hours to finish depending on target location, ports open, firewalls, etc. By default, it scans the 1000 most common ports. There are settings to scan all 65000 ports.

`nmap IPADDRESS/24` - scans a range of ip addresses

### Different Scan Types

`sudo nmap -sS IPADDRESS` - TCP Synscan. Probably the most used scan in nmap. It never really opens a full TCP connection, allowing it to be really fast at scanning ports. You only handle the first step of the TCP handshake. If Syn/Ack is received in response, then we know the port is open. An RSD response means the port is closed. No response returns a 'filtered port' which means we don't know if it is open, closed, or behind a firewall.

`nmap -sT IPADDRESS` - TCP Connect Scan. Doesn't need root priveledges because it makes a full TCP handshake. This is easily detected

`nmap -sU IPADDRESS` - UDP Scan. Not as popular since most ports over the interet use TCP procotol since it is faster. But because TCP is more popular, that means UDP security can become an afterthought.

Other scans are found in the manual (`-sY`, `-sN`, `-sF`, `-sX`, etc.)

## Discovering Target OS

nmap has a huge database where they store known OS fingerprints they compare with the host we scan. The target needs at least one open and one closed port.

`sudo nmap -O IPADDRESS` - This can even tell us if we're targeting a Virtual OS, which sometimes are in place to lure hackers, since they tend to focus on the most vulnerable machines first. This will also tell us OS, MAC Address, and the network distance

## Detecting the Version of a Service running on an Open Port

Version discovery is very helpful because there will often be published vulnerabilities listed on the web.

`sudo nmap -sV IPADDRESS` - this procudes a version column with the scan output. You can adjust the intensity of the scan by adding `--version-intensity (0-9)` before the ip address. The default is 7 and should be good in most cases.

There are more options for version discovery in the nmap manual.

`sudo nmap -A IPADDRESS` - nmap's aggressive scan option. Enables OS detection, version detection, and nmap script scanning by default.

## Filtering Port Range and Scan Results Output

`nmap -sn IPADDRESS` - only detects open hosts. Similar to netdiscover, but not as clean output.

`nmap -p PORT IPADDRESS` - only scans the provided port number. Separate port numbers with commas, or a range with '-'. Using this, you can scan all 65,535 ports by inputting 1-65535.

`nmap -F IPADDRESS` - scans top 100 ports

`sudo nmap -sS IPADDRESS >> TEXTFILENAME.txt` - outputs results into a .txt file

`sudo nmap -oN FILENAME -sS IPADDRESS` - outputs results into a file named FILENAME

## What is firewall/IDS?

Firewalls monitor network traffic based on predetermined security rules. There are network and host based firewalls. Network firewalls filter traffic between two or more networks. Host based firewalls only filter traffic that is going in and out of the host machine.

IDS stands for Intrusion Detection System. It is usually a software application that monitors a network for malicious activity. Some of the previous nmap scan techniques would get picked up by an IDS.

## Using Decoys and Packet Fragmentation

We don't know what security rules will be in place when we run into a firewall. They could be using MAC Address filtering (only allow certain devices to connect to a specific port), block different types of packets, block specific ports, etc.

When nmap returns 'filtered', that port is behind a firewall. It returns 'filtered' whenever packets are dropped, so nmap does not know if the port is open or closed.

`sudo nmap -f IPADDRESS` - nmap uses fragmented IP packets. This makes it more difficult for packet filters to stop. `-f` splits the packets into 8 or less bytes.

`sudo nmap -D DECOYADDRESS,DECOYADDRESS,DECOY...,ME IPADDRESS` - it will scan the target with decoy IP addresses, tricking the firewall into not knowing which one is real. Use IP addresses similar to your local address if scanning your own network. Use `RND:NUMBEROFDECOYS` if scanning an outside network.

## Security Evasion Nmap Options

`sudo nmap -S SPOOFEDADDRESS -Pn -e eth0 -g PORTNUMBER` - spoofs your IP address when scanning, but you will not get the scan results, since they will be sent to the spoofed address. `-Pn` specifies that we're assuming all host targets are online. `-e` specifies the network interface that we find in `ifconfig`. Since some firewall settings may only allow traffic from specified ports, we can specify a port number with `-g`.

`sudo nmap -sF IPADDRESS` - we can also try different types of scans.

We can use the `-T` flag to specify the timing of our scans (0-5). The slower the number, the more likely to evade IDS detection.