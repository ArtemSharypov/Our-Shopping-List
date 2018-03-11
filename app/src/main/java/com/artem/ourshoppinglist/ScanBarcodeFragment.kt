package com.artem.ourshoppinglist

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_scan_barcode.view.*
import java.io.IOException

class ScanBarcodeFragment : Fragment() {

    private var activityBarcodeCallback: PassBarcodeDataInterface? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_scan_barcode, null)
        var barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.UPC_E or Barcode.UPC_A)
                .build()

        var cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(1080, 1920)
                .build()

        var callback = object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    //Checks for the Camera being enabled
                    if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), 200)
                    } else if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(view.fragment_scan_barcode_sv_camera.holder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                 cameraSource.stop()
            }
        }

        view.fragment_scan_barcode_sv_camera.holder.addCallback(callback)

        var processor = object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detecetions: Detector.Detections<Barcode>?) {
                val barcodes = detecetions?.detectedItems

                //If any barcodes were scanned, pass the first one back to the activity, then into the edit item fragment
                if(barcodes?.size() != 0) {
                    activityBarcodeCallback?.passBarcodeToActivity(barcodes?.valueAt(0)?.displayValue!!)
                    activity.onBackPressed()
                }
            }
        }

        barcodeDetector.setProcessor(processor)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityBarcodeCallback = context as PassBarcodeDataInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.toString() + " must implement PassBarcodeDataInterface")
        }
    }
}