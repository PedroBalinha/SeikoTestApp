package com.seikoinstruments.sdk.thermalprinter.sample;

public class DeviceListItem {

    /** device name */
    private String mDeviceName;

    /** device mac address */
    private String mDeviceMacAddress;

    /** device ip address */
    private String mDeviceIpAddress;


    /**
     * constructor
     *
     * @param deviceName
     * @param deviceAddress
     */
    public DeviceListItem(
            final String deviceName,
            final String macAddress
            ) {

        mDeviceName = deviceName;
        mDeviceMacAddress = macAddress;
        mDeviceIpAddress = "";
    }


    /**
     * constructor
     *
     * @param deviceName
     * @param deviceAddress
     */
    public DeviceListItem(
            final String deviceName,
            final String macAddress,
            final String ipAddress
            ) {

        mDeviceName = deviceName;
        mDeviceMacAddress = macAddress;
        mDeviceIpAddress = ipAddress;
    }


    public void setName(final String name) {
        mDeviceName = name;
    }


    public String getName() {
        return mDeviceName;
    }


    public void setMacAddress(final String address) {
        mDeviceMacAddress = address;
    }


    public String getMacAddress() {
        return mDeviceMacAddress;
    }


    public void setIpAddress(final String address) {
        mDeviceIpAddress = address;
    }


    public String getIpAddress() {
        return mDeviceIpAddress;
    }

}
