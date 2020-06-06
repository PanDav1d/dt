package de.docward.keysrv.ui

import com.github.salomonbrys.kotson.fromJson
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

class GetCamerasResponse(
        val cameras: List<CameraDevice>? = null,
        val error: String? = null
)

class CameraDevice(
        val id: String,
        val label: String
)

class QRCodeScanResponse(
        val qrCode: String?=null,
        val error: String?=null
)

private fun ElementCreator<*>.listCameras(onCameraListResponse:(GetCamerasResponse)->Unit){
    val callbackId = Math.abs(random.nextInt())
    val js = """
        Html5Qrcode.getCameras().then(devices => {
          /**
           * devices would be an array of objects of type:
           * { id: "id", label: "label" }
           */
          callbackWs($callbackId ,{cameras: devices});
        }).catch(err => {
          callbackWs($callbackId, {error: err});
        });
    """.trimIndent()
    this.browser.executeWithCallback(js, callbackId){
        val resp : GetCamerasResponse = gson.fromJson(it.toString())
        onCameraListResponse(resp)
    }
}

private fun ElementCreator<*>.startScanning(cameraId: String, onQRCodeScanned: (QRCodeScanResponse)->Unit){
    val callbackId = Math.abs(random.nextInt())
    val js = """
        window.html5QrCode = new Html5Qrcode(/* element id */ "reader");
        html5QrCode.start(
          "$cameraId", 
          {
            fps: 5,    // Optional frame per seconds for qr code scanning
            qrbox: 400  // Optional if you want bounded box UI
          },
          qrCodeMessage => {
            console.log("Found " + qrCodeMessage )
            callbackWs($callbackId ,{qrCode: qrCodeMessage});
          },
          errorMessage => {
            console.log("Error Message")
            //callbackWs($callbackId ,{error: errorMessage});
          })
        .catch(err => {
          console.log("Exception Message")
          callbackWs($callbackId ,{error: err});
        });
    """.trimIndent()
    this.browser.executeWithCallback(js, callbackId){
        val resp : QRCodeScanResponse = gson.fromJson(it.toString())
        onQRCodeScanned(resp)
    }
}

class QRCodeStopScanResult(
    val result:String?=null,
    val error: String?=null
)

private fun ElementCreator<*>.stopScanning(){
    val callbackId = Math.abs(random.nextInt())
    val js = """
        window.html5QrCode.stop().then(ignore => {
          // QR Code scanning is stopped.
          callbackWs($callbackId ,{"result": "ok"});
        }).catch(err => {
          callbackWs($callbackId ,{"error": err});
        });
    """.trimIndent()
    this.browser.executeWithCallback(js, callbackId){
        val resp : QRCodeStopScanResult = gson.fromJson(it.toString())
    }
}

fun ElementCreator<*>.scanQrCode(onScanSuccessful:(String)->Unit){

    val cameras = KVar<GetCamerasResponse?>(null)
    val activeCamera = KVar<Int>(0)
    var isScanning = false

    listCameras { cameraResponse ->
        cameras.value = cameraResponse
        logger.info("Detected cameras ${cameraResponse.cameras?.map { it.label }?.joinToString(",")}")
    }

    render(cameras){ cameraResponse ->
        div(fomantic.ui.menu).new {
            cameraResponse?.cameras?.forEachIndexed {idx, camera ->
                a(fomantic.item.active(idx==activeCamera.value)).text(camera.label).on.click {
                    activeCamera.value = idx
                }
            }
        }

        div(mapOf("id" to "reader", "width" to "600px"))

        cameraResponse?.cameras?.get(activeCamera.value)?.id?.let {cameraId ->
            startScanning(cameraId) { qrCode ->
                isScanning = true
                qrCode.qrCode?.let {
                    logger.info("Detected qr code ${qrCode.qrCode}")
                    stopScanning()
                    onScanSuccessful(qrCode.qrCode)
                }
            }
        }
    }

    onCleanup(true){
        if(isScanning) {
            stopScanning()
            logger.info("Abort scanning for QR Codes")
        }
    }
}