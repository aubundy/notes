# Intro to Ethical Hacking

## What is Ethical Hacking?

Finding vulnerabilities in systems (networks, servers, computers, websites, etc.) and securing them for businesses.

We will use home devices and virtual machines to practice.

## What is a Virtual Machine?

Machine within our computer that uses it's hardware. This creates a safe area to test and learn ethical hacking. The virtual machine can always be deleted. We just need a virtualization software to allow our computer to run multiple OS.

## Why Linux?

Linux is best for ethical hacking since it is open source, free, good for developing, and light. We can inspect and manipulate it's code and there's a lot of documentation. We can also edit the code to become more optimized for our use case.

We will be using [Kali Linux](https://www.kali.org/) - a distro made specially for penetration testing and ethical hacking - and [Virtual Box](https://www.virtualbox.org/) as our virtualization software.

## Network Settings

`ping google.com` - check network settings

`sudo ifconfig` - check ip address. This is given to us by virtual box

Update Network Adapter in Settings from NAT -> Bridged Adapter. Use a USB Network Adapter. [Configuration settings](https://www.nakivo.com/blog/how-to-install-kali-linux-on-virtualbox/)

[Todo after installing Kali Linux](https://www.ceos3c.com/hacking/top-things-after-installing-kali-linux/)

## 5 Stage of Pen Testing

1. Reconnaissance/Information Gathering
2. Scanning
3. Exploitation/Gaining Access
4. Maintaining Access
5. Covering Tracks