package com.nhlstenden.navigationapp.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeUtils {

    public static Bitmap generateQRCode(String data)
    {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    data,
                    BarcodeFormat.QR_CODE,
                    600, 600
            );
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
