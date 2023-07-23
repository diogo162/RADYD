import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class ListNetworkInterfaces {
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            System.out.println("Interface: " + networkInterface.getName());
            System.out.println("Display Name: " + networkInterface.getDisplayName());
            System.out.println("Status: " + (networkInterface.isUp() ? "Up" : "Down"));
            System.out.println("Loopback: " + (networkInterface.isLoopback() ? "Yes" : "No"));
            System.out.println("MTU: " + networkInterface.getMTU());

            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            for (InterfaceAddress address : addresses) {
                System.out.println("  Address: " + address.getAddress());
                System.out.println("  Broadcast: " + address.getBroadcast());
                System.out.println("  Prefix Length: " + address.getNetworkPrefixLength());
            }

            System.out.println();
        }
    }
}
