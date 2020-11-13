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

`nmap IPADDRESS` - can sometimes take hours to finish depending on target location, ports open, firewalls, etc. By default, it scans the 1000 most common ports. There are settings to scan all 65000 ports.

`nmap IPADDRESS/24` - scans a range of ip addresses