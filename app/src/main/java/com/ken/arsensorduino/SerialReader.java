package com.ken.arsensorduino;

import static android.content.Context.USB_SERVICE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.util.Optional;
import java.util.stream.IntStream;

public class SerialReader {

  public SerialReader start(Context context) {
    UsbManager usbManager = (UsbManager) context.getSystemService(USB_SERVICE);

    Optional<UsbDevice> device =
        usbManager.getDeviceList().values()
            .stream()
            // Arduinoのベンダー識別子は9025。
            .filter(d -> d.getVendorId() == 9025)
            .findFirst();
    if (!device.isPresent()) {
      return this;
    }

    // 既に接続許可をユーザーから貰っている場合はすぐに接続する。
    if (usbManager.hasPermission(device.get())) {
      setupEndpoint(usbManager, device.get());
      return this;
    }
    // まだ許可がない場合は、接続許可をもらう。
    final String intentAction = "arsensor.USB_PERMISSION";
    context.registerReceiver(new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
          setupEndpoint(usbManager, device.get());
        }
      }
    }, new IntentFilter(intentAction));
    usbManager.requestPermission(device.get(),
        PendingIntent
            .getBroadcast(context, 0, new Intent(intentAction), 0));
    return this;
  }

  private void setupEndpoint(UsbManager usbManager, UsbDevice device) {
    UsbDeviceConnection connection = usbManager.openDevice(device);

    // CDC (Communications Device Class)のインターフェースを探して接続。
    Optional<UsbInterface> cdcInterface =
        IntStream.range(0, device.getInterfaceCount())
            .boxed()
            .map(device::getInterface)
            .filter(i -> i.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
            .findFirst();
    if (!cdcInterface.isPresent() || !connection.claimInterface(cdcInterface.get(), true)) {
      connection.close();
      return;
    }

    // バルク転送のエンドポイントを探す。
    Optional<UsbEndpoint> endPoint =
        IntStream.range(0, cdcInterface.get().getEndpointCount())
            .boxed()
            .map(cdcInterface.get()::getEndpoint)
            .filter(e -> e.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
            .filter(e -> e.getDirection() == UsbConstants.USB_DIR_IN)
            .findFirst();
    if (!endPoint.isPresent()) {
      return;
    }

    // シリアル通信を9600bpsにセットアップ。
    connection.controlTransfer(
        0x21, 0x22, 0, 0, null, 0, 0);
    connection.controlTransfer(0x21, 0x20, 0, 0,
        new byte[]{(byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08}, 7, 0);

    beginRead(connection, endPoint.get());
  }

  private volatile boolean keepRunning = true;
  private volatile Integer latestValue = null;

  private void beginRead(UsbDeviceConnection connection, UsbEndpoint endpoint) {
    new Thread(() -> {
      byte[] buf = new byte[2];
      int upper5Bits = 0;
      boolean waitingHigh = true;
      while (keepRunning) {
        int length = connection.bulkTransfer(endpoint, buf, buf.length, 0);
        for (int i = 0; i < length; i++) {
          byte b = buf[i];
          if ((b & 0b10000000) != 0) {
            upper5Bits = (b & 0b11111) << 5;
          } else {
            // 上位バイトが来ると思っていたのに下位バイトが来たら無視する。
            if (waitingHigh) {
              continue;
            }
            latestValue = (upper5Bits | (b & 0b11111));
          }
          waitingHigh = !waitingHigh;
        }
      }
      connection.close();
    }
    ).start();
  }

  public Integer fetchLatestValue() {
    try {
      return latestValue;
    } finally {
      latestValue = null;
    }
  }
}
