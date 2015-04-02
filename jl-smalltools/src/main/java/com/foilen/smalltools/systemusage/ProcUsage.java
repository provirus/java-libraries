/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.systemusage.results.CpuInfo;
import com.foilen.smalltools.systemusage.results.NetworkInfo;

/**
 * To retrieve the usage by using the files in /proc. (Of course, your system must have this filesystem mounted)
 */
public final class ProcUsage {

    /**
     * Check the main CPU. (If multiple CPUs, this is the sum of all of them)
     * 
     * @param procStatPath
     *            the path to the stat file. (E.g /proc/stat)
     * @return the cpu infos
     */
    public static CpuInfo getMainCpuInfo(String procStatPath) {
        CpuInfo cpuInfo = new CpuInfo();

        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(procStatPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Find the right line
                if (line.startsWith("cpu ")) {
                    found = true;

                    // Parse the line
                    String[] parts = line.split("[ ]+");
                    int i = 1;
                    cpuInfo.setUser(Integer.valueOf(parts[i++]));
                    cpuInfo.setNice(Integer.valueOf(parts[i++]));
                    cpuInfo.setSystem(Integer.valueOf(parts[i++]));
                    cpuInfo.setIdle(Integer.valueOf(parts[i++]));
                    cpuInfo.setIowait(Integer.valueOf(parts[i++]));
                    cpuInfo.setIrq(Integer.valueOf(parts[i++]));
                    cpuInfo.setSoftirq(Integer.valueOf(parts[i++]));
                }
            }

        } catch (Exception e) {
            throw new SmallToolsException("Problem reading the proc stat file", e);
        }

        // Make sure it is the right file
        if (!found) {
            throw new SmallToolsException("The file " + procStatPath + " is in the wrong format");
        }

        return cpuInfo;
    }

    /**
     * Check the network interfaces.
     * 
     * @param procNetDevPath
     *            the path to the netfile. (E.g /proc/net/dev)
     * @return the networks infos
     */
    public static List<NetworkInfo> getNetworkInfos(String procNetDevPath) {

        List<NetworkInfo> networkInfos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(procNetDevPath))) {

            // Skip 2 headers lines
            reader.readLine();
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {

                NetworkInfo networkInfo = new NetworkInfo();
                line = line.trim();

                // Parse the line
                String[] parts = line.split("[ ]+");
                if (parts.length != 17) {
                    continue;
                }
                String interfaceName = parts[0];
                if (interfaceName.endsWith(":")) {
                    interfaceName = interfaceName.substring(0, interfaceName.length() - 1);
                }
                networkInfo.setInterfaceName(interfaceName);
                networkInfo.setInBytes(Long.valueOf(parts[1]));
                networkInfo.setInPackets(Long.valueOf(parts[2]));
                networkInfo.setOutBytes(Long.valueOf(parts[9]));
                networkInfo.setOutPackets(Long.valueOf(parts[10]));

                networkInfos.add(networkInfo);
            }

        } catch (Exception e) {
            throw new SmallToolsException("Problem reading the proc dev net file", e);
        }

        return networkInfos;
    }
}