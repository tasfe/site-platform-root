package com.netfinworks.site.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.Test;

public class MacUtilTest {

    @Test
    public void getMacTest() {
        String address = "";
        String host = "10.4.9.159";

        MacUtilTest test = new MacUtilTest();

        address = test.getMacAddress();
        System.out.println("Physical Address is : " + address);

        address = test.getMacAddress(host);
        System.out.println("Physical Address is : " + address);
    }

    @Deprecated
    /**
     * 中文环境下获取不到
     * @return
     */
    public String getMacAddress() {
        String mac = "";
        String line = "";

        String os = System.getProperty("os.name");

        if (os != null && os.startsWith("Windows")) {
            try {
                String command = "cmd.exe /c ipconfig /all";
                Process p = Runtime.getRuntime().exec(command);

                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                while ((line = br.readLine()) != null) {
                    if (line.indexOf("Physical Address") > 0) {
                        int index = line.indexOf(":") + 2;

                        mac = line.substring(index);

                        break;
                    }
                }

                br.close();

            } catch (IOException e) {
            }
        }

        return mac;
    }

    public String getMacAddress(String host) {
        String mac = null;
        StringBuffer sb = new StringBuffer();

        try {
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(host));

            byte[] macs = ni.getHardwareAddress();

            for (int i = 0; i < macs.length; i++) {
                mac = Integer.toHexString(macs[i] & 0xFF);

                if (mac.length() == 1) {
                    mac = '0' + mac;
                }

                sb.append(mac + "-");
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        mac = sb.toString();
        if(mac.length() > 0) {
            mac = mac.substring(0, mac.length() - 1);
        }
        return mac;
    }
}
