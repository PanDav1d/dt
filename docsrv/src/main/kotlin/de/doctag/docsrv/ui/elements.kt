package de.doctag.docsrv.ui

import com.github.salomonbrys.kotson.fromJson
import de.doctag.docsrv.*
import de.doctag.docsrv.model.FileData
import de.doctag.lib.toSha1HexString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.util.gson
import kweb.util.random
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

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
            fps: 5    // Optional frame per seconds for qr code scanning
            //qrbox: 400  // Optional if you want bounded box UI
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
        //val resp : QRCodeStopScanResult = gson.fromJson(it.toString())
    }
}

fun ElementCreator<*>.scanQrCode(onScanSuccessful:(String)->Unit){

    val cameras = KVar<GetCamerasResponse?>(null)
    val activeCamera = KVar(0)
    var isScanning = false

    listCameras { cameraResponse ->
        cameras.value = cameraResponse
        logger.info("Detected cameras ${cameraResponse.cameras?.map { it.label }?.joinToString(",")}")
    }

    render(cameras){ cameraResponse ->
        div(fomantic.ui.menu).new {
            cameraResponse?.cameras?.forEachIndexed {idx, camera ->
                a(fomantic.item.active(idx==activeCamera.value)).text(camera.label).on.click {
                    logger.info("Setting active camera to ${idx}")
                    activeCamera.value = idx
                }
            }
        }

        div(mapOf("id" to "reader", "width" to "600px"))

        render(activeCamera){rActiveCamera ->

            logger.info("Active Camera did change")

            if(isScanning){
                stopScanning()
                isScanning = false
            }

            cameraResponse?.cameras?.get(rActiveCamera)?.id?.let {cameraId ->
                runBlocking { delay(1000) }
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
    }

    onCleanup(true){
        if(isScanning) {
            stopScanning()
            logger.info("Abort scanning for QR Codes")
        }
    }
}


data class SignaturePadData(val base64Content: String)
data class SignaturePadEmptyResult(val isEmpty: Boolean)

class SignatureElement(
        private val canvasElement: CanvasElement
) {
    fun onBeginDraw(callback:()->Unit) {
        val callbackId = Random.nextInt()
        canvasElement.browser.executeWithCallback("""
            window.signaturePad.onBegin = function(){
                callbackWs($callbackId,{});
            }
        """.trimIndent(), callbackId){inputData ->
            callback()
        }
    }

    fun onEndDraw(callback:()->Unit) {
        val callbackId = Random.nextInt()
        GlobalScope.launch {
            canvasElement.browser.executeWithCallback("""
                function setupSignaturePadOnEnd() {
                    if(!window.signaturePad) {
                        setTimeout(setupSignaturePadOnEnd, 50)
                    } else {
                        window.signaturePad.onEnd = function(){
                            callbackWs($callbackId,{});
                        }
                    }
                }
                setupSignaturePadOnEnd()
            
        """.trimIndent(), callbackId){inputData ->
                callback()
            }
        }
    }

    fun clear(){
        canvasElement.browser.execute("""
            window.signaturePad.clear()
        """.trimIndent())
    }

    fun fetchContent(onFetchCompleted: (pad:SignaturePadData)->Unit){
        val callbackId = Random.nextInt()
        //GlobalScope.launch {
            canvasElement.browser.executeWithCallback("callbackWs($callbackId,{base64Content:window.signaturePad.toDataURL()});", callbackId) { inputData ->
                val pos: SignaturePadData = gson.fromJson(inputData.toString())
                onFetchCompleted(pos)
            }
        //}
    }

    fun isEmpty(): CompletableFuture<Boolean>{
        val callbackId = Random.nextInt()
        logger.info("SignaturePad->isEmpty() :: called (cb = $callbackId)")

        val response = CompletableFuture<Boolean>()

        //GlobalScope.launch {
            canvasElement.browser.executeWithCallback("""
                callbackWs($callbackId,{"isEmpty": window.signaturePad.isEmpty()});
            """.trimIndent(), callbackId){inputData ->
                logger.info("SignaturePad->isEmpty() :: Received result from client")
                val result: SignaturePadEmptyResult = gson.fromJson(inputData.toString())

                response.complete(result.isEmpty)
            }
        //}

        return response
    }
}

fun ElementCreator<Element>.canvas(
    attributes: Map<String, Any> = emptyMap(),
    new: (ElementCreator<CanvasElement>.(CanvasElement) -> Unit)? = null
): CanvasElement {
    return CanvasElement(
        element(
            "canvas",
            attributes
        )
    )
}

fun ElementCreator<*>.inputSignatureElement(backgroundImage: String? = null) : SignatureElement {
    element("script", mapOf("src" to "/ressources/signature_pad.min.js"))

    val canvas = canvas(/*420,300*/).apply {
        this.setAttributeRaw("style", "border: 1px solid black;width: 100%; height: 300px;")
    }//.focus()

    val pad  = SignatureElement(canvas)


    val callbackId = Math.abs(random.nextInt())
    browser.executeWithCallback("""
    canvas = null;
    
    function drawBackground()
    {
      context = canvas.getContext('2d');
      base_image = new Image();
      base_image.src = '${backgroundImage}';
      base_image.onload = function(){
        context.drawImage(base_image, 0,0, 420, 300);
      }  
    }
    
    function initCanvas(){
        try{
            canvas = document.getElementById("${canvas.id}");
            
            if(!canvas){
                console.log("Canvas element not ready, retry later")
                setTimeout(initCanvas, 50)
            }
            
            console.log("width", canvas.offsetWidth)
            console.log("height", canvas.offsetHeight)
            canvas.width = canvas.offsetWidth
            canvas.height = canvas.offsetHeight
            
            window.signaturePad = new SignaturePad(canvas,{width:canvas.offsetWidth, height: canvas.offsetHeight });
            
            if(${backgroundImage != null}){
                drawBackground()
            }
    
            canvas.focus();
            callbackWs($callbackId, {"setup":"ok"});
        }
        catch(ex){
            console.log("SignaturePad js not ready, retry later")
            setTimeout(initCanvas, 50)
        }
    }
    initCanvas()
    """.trimIndent(), callbackId){

    }


    return pad
}