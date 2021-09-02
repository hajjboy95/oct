package com.octopus.node.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class HardwareUtils {

    private final static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final static OperatingSystem operatingSystem = new OperatingSystem(os.getName(), os.getVersion());

    public static class MacAddressException extends Exception {
        MacAddressException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class OperatingSystem {
        final private String name;
        final private String version;

        OperatingSystem(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }

    public static String getMacAddress() throws MacAddressException {
        try {
            final InetAddress ip = InetAddress.getLocalHost();
            final NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            final byte[] macAddress = network.getHardwareAddress();
            final StringBuilder macStrBuilder = new StringBuilder();

            // Build the mac address string
            for (int i = 0; i < macAddress.length; i++) {
                macStrBuilder.append(String.format("%X", macAddress[i]));
                macStrBuilder.append(i < macAddress.length - 1 ? "-" : "");
            }

            return macStrBuilder.toString();

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            throw new MacAddressException("Failed to get mac address", e);
        }
    }

    public static String getComputerIdentifier() {
        SystemInfo systemInfo = new SystemInfo();
        oshi.software.os.OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = computerSystem.getSerialNumber();
        String processorIdentifier = centralProcessor.getProcessorIdentifier().getIdentifier();
        int processors = centralProcessor.getLogicalProcessorCount();

        String delimiter = "-";

        return String.format("%08x", vendor.hashCode()) + delimiter
                + String.format("%08x", processorSerialNumber.hashCode()) + delimiter
                + String.format("%08x", processorIdentifier.hashCode()) + delimiter + processors;
    }

    public static OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public static String getProcessorCount() {
        return os.getVersion();
    }

    public static double getSystemLoadAverage() {
        return os.getSystemLoadAverage();
    }
}
