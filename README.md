networker
=========

org.sneer.networker (we can move this stuff into sneerteam later)


MAJOR INTERNAL FIXME: making the protocol be fire-and-forget was dumb. you need ping acks from the router so that it's not that either you register on the network on boot or you have to wait 10 minutes to try again.



sample use (general idea):



1. deploy to dynamic.sneer.me node, just run org.sneer.networker.dumb.DumbNetworkerRouter without arguments on some terminal/screen (it blocks forever and prints stats every 30 minutes or so)

2. to create an echo server (draft skeleton):

 public class MyEchoServer implements NetworkerListener {

     DumbNetworker n;

     receive(NetId sender, byte[] data) {
        // it's echo, so:
        n.send(sender, data);
     }

 };

// somewhere in the MyEchoServer init or constructor:

 n = new DumbNetworker("dynamic.sneer.me", 65235, myEchoServer); // FIXME: default port, should not be required arg ...
 NetId myAddr = ... <--- you have to put a hardcoded 256-bit value here, it is the echo server overlay network address.
 n.setId(myAddr); // FIXME: the first ping will go with the wrong ID ... server unreachable for 10 minutes

3. the echo client:

 public class MyEchoClient implements NetworkerListener {

   receive(NetId sender, byte[] data) {
      if (sender == echo server address) {
         System.out.println("DUDE! I think I just said this: " + data);
      }
   }
   
   // somewhere you have a loop reading the keyboard   
   String str = read the keyboard
   NetId echoservernetid = .... // 256-bit number which is the echo server's overlay netowrk address
   n.send(echoservernetid, str.to-byte-array-UTF8() or something);


 }

 // somewhere in the MyEchoClient init or constructor: (you don't have to set an ID)

 n = new DumbNetworker("dynamic.sneer.me", 65235, myEchoClient); // FIXME: default port, should not be required arg ...








