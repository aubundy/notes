# Man in the Middle Attack

Not as important as the other material, but still very good to know. This attack targets ARP requests and replies within a local network. When communicating within a network, a computer will send out an ARP request for a specific IP address, and the correct IP will respond with its MAC address. IP addresses and MAC addresses help routers and computers within the same network communicate with each other. But if we manipulate our response, and say we have the router's IP address, the other computer will start sending us its packets instead of the router. We would forward these requests to the router that way computers still get their requests fulfilled, but we will be able to manipulate the data transfer.

