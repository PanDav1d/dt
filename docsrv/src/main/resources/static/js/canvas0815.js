class Canvas0815 {
  // Vars
  aspectRatio = 0;
  backgroundPosX = 0;
  backgroundPosY = 0;
  backgroundSizeX = 0;
  backgroundSizeY = 0;
  canvas = null;
  canvasSizeX = 0;
  canvasSizeY = 0;
  context2d = null;
  isDragging = false;
  isResizing = false;
  lastMouseX = 0;
  lastMouseY = 0;
  objValues = {};
  qrCodeOriginalSizeX = 0;
  qrCodeOriginalSizeY = 0;
  qrCodePosX = 0;
  qrCodePosY = 0;
  qrCodeSizeX = 0;
  qrCodeSizeY = 0;
  qrCodeScaling = 0;
  showBorder = false;
  showResize = false;
  valueListener = [];

  // Constructor
  constructor(parameter = {}) {
    // Standardwerte
    let defaultValues = {
      backgroundImageSrc: 'background.png',
      backgroundFitToCanvas: true,
      borderColor: '#cccccc',
      debug: false,
      dpi: 72,
      canvasElementId: "canvas",
      maxStartSizeX: 50,
      maxStartSizeY: 50,
      maxScalingFactor: 2,
      qrCodeImageSrc: 'qr.png',
      resizeSquareSize: 5,
      resizeSquareColor: '#000000',
      resizeStyle: 'default', // or circle
    };
    // Standardwerte mit den übergebenen Werten überschreiben
    this.objValues = Object.assign(defaultValues, parameter);
    this.canvas = document.getElementById(this.objValues.canvasElementId);
    this.context2d=this.canvas.getContext('2d');
    // Ermittelte Werte ohne Abhängigkeiten
    this.canvasSizeX = parseInt(this.canvas.attributes.width.value);
    this.canvasSizeY = parseInt(this.canvas.attributes.height.value);
    this.aspectRatio = this.objValues.maxStartSizeX / this.objValues.maxStartSizeY;
    let ths=this;
    // init wird getriggert, wenn die Bilder geladen wurden
    this.loadImages(function() { ths.init(); });
  }

  // initialization
  init() {
    this.debug("Both images have been loaded - Starting with initialization!");
    this.qrCodeSizeX=this.qrCodeOriginalSizeX=this.qrCodeImage.width;
    this.qrCodeSizeY=this.qrCodeOriginalSizeY=this.qrCodeImage.height;
    this.backgroundSizeX=this.backgroundImage.width;
    this.backgroundSizeY=this.backgroundImage.height;
    if(this.objValues.backgroundFitToCanvas) {
      this.debug('backgroundFitToCanvas: true, canvasSizeX: ' + this.canvasSizeX);
      // Proportional zu x, also erst y bestimmen
      this.backgroundSizeY=(this.backgroundSizeY * (this.canvasSizeX/this.backgroundSizeX)).toFixed(0);
      this.backgroundSizeX=this.canvasSizeX;
    }

    this.debug('Background Size (x, y) = (' + this.backgroundSizeX + ', ' + this.backgroundSizeY + ')');
    // Berechnete Werte
    this.qrCodePosX = (this.canvasSizeX - this.qrCodeSizeX) / 2;
    this.qrCodePosY = (this.canvasSizeY - this.qrCodeSizeY) / 2;
    this.context2d = this.canvas.getContext('2d');
    this.debug(this);

    // register event handlers
    var ths=this;
    this.canvas.onmousedown = function(e) { ths.mouseDown(e); };
    this.canvas.onmouseup = function(e) { ths.mouseUp(e); };
    this.canvas.onmousemove = function(e) { ths.mouseMove(e); };
    this.canvas.ondblclick = function(e) { ths.mouseDoubleClick(e); };

    // Zeichnen
    this.drawBackground();
    this.draw();
  }

  // Handler... alles für die Maus
  mouseDoubleClick(e) {
    let pdfObj=this.getQrCodePosition();
    this.info('QR-Code (x, y) = (' + this.qrCodePosX + ', ' + this.qrCodePosY +
                    '), Size (x, y) = (' + this.qrCodeSizeX + ', ' + this.qrCodeSizeY +
                    '), Scaling-Factor = ' + this.qrCodeScaling +
                    ', Point-Koords = (' + pdfObj.x + ', ' + pdfObj.y + ')');

  }
  mouseDown(e) {
    this.debug('mouseDown');
    const x = e.clientX - this.canvas.getBoundingClientRect().left;
    const y = e.clientY - this.canvas.getBoundingClientRect().top;

    this.setMouseToMove();

    if (this.isInsideResizeSquare(x, y)) {
        this.isDragging = false;
        this.isResizing = true;
        this.lastMouseX = x;
        this.lastMouseY = y;
    } else if (this.isInsideQrCode(x, y)) {
        this.isDragging = true;
        this.isResizing = false;
        this.lastMouseX = x;
        this.lastMouseY = y;
    }
  }

  mouseUp(e) {
    this.debug('mouseUp');
    this.isResizing = false;
    this.isDragging = false;

    this.setMouseToDefault();
    this.notify();
  }

  mouseMove = function(e) {
    this.debug('mouseMove');
    const x = e.clientX - this.canvas.getBoundingClientRect().left;
    const y = e.clientY - this.canvas.getBoundingClientRect().top;
    // Rand aktivieren...
    this.showBorder = this.showResize = true;
    // Differenzierung der Fälle
    if (this.isResizing) {
      const deltaX = x - this.lastMouseX;
      const deltaY = y - this.lastMouseY;
      // find the biggest...
      const maxDelta = Math.max(deltaX, deltaY);
      // Die Größe proportional ändern
      const newSizeX = this.qrCodeSizeX + maxDelta;
      const newSizeY = this.qrCodeSizeX / this.aspectRatio + maxDelta / this.aspectRatio;
      // Skalierung in die rechte, untere Richtung und Maximale Skalierung beschränken
      this.qrCodeSizeX = Math.min(newSizeX, this.objValues.maxStartSizeX * this.objValues.maxScalingFactor);
      this.qrCodeSizeY = Math.min(newSizeY, this.objValues.maxStartSizeY * this.objValues.maxScalingFactor);
      // Sicherstellen, dass newSizeX und newSizeY nicht die Minimume Skalierung unterschreiten
      this.qrCodeSizeX = Math.max(this.qrCodeSizeX, this.objValues.maxStartSizeX / this.objValues.maxScalingFactor);
      this.qrCodeSizeY = Math.max(this.qrCodeSizeY, this.objValues.maxStartSizeY / this.objValues.maxScalingFactor);
      // Skalierungsfaktor bestimmen und setzen, da proportional, gibt es nur ein Wert, nehmen wir einfach mal X
      this.qrCodeScaling = (this.qrCodeSizeX / this.qrCodeOriginalSizeX).toFixed(2);

      this.redraw();
      this.lastMouseX = x;
      this.lastMouseY = y;
  } else if (this.isDragging && this.isValidDraggingArea(x, y)) {
      const deltaX = x - this.lastMouseX;
      const deltaY = y - this.lastMouseY;
      this.qrCodePosX += deltaX;
      this.qrCodePosY += deltaY;
      this.redraw();
      this.lastMouseX = x;
      this.lastMouseY = y;
    } else if (this.isInsideResizeSquare(x, y)) {
      this.setMouseToResize();
      this.redraw();
    } else if (this.isInsideQrCode(x, y)) {
      this.redraw();
    } else {
      this.setMouseToDefault();
      this.showBorder = this.showResize = false;
      this.redraw();
    }
  }

  // obtionale Listener
  addValueListener(callback) {
    this.debug('addValueListener');
    this.valueListener.push(callback);
  }

  notify() {
    for (let i = 0; i < this.valueListener.length; i++) {
      this.debug('notify listener ' + i);
      this.valueListener[i](this);
    }
  }

  // image creation
  createImage(imgSrc, params = {}) {
    return new Promise((resolve, reject) => {
      let image = new Image();
      for (let key in params) {
        image[key] = params[key];
        this.debug(key);
      }
      image.src = imgSrc;
      this.debug(image);
      image.onload = function () {
        resolve(image);
      };
      image.onerror = function () {
        reject(new Error(`Failed to load image with source ${imgSrc}`));
      };
    });
  }

  // image loader
  loadImages(callback) {
    let ths=this;
    var bgImgParams={};
    var qrImgParams={};
    Promise.all([
      this.createImage(ths.objValues.backgroundImageSrc, bgImgParams),
      this.createImage(ths.objValues.qrCodeImageSrc, qrImgParams)
    ]).then((images) => {
      ths.backgroundImage = images[0];
      ths.qrCodeImage = images[1];
      callback();
    }).catch((error) => {
      console.error("Error loading images:", error);
    });
  }

  // validity checks
  isInsideQrCode(x, y) {
    var result = (x >= this.qrCodePosX && x <= this.qrCodePosX + this.qrCodeSizeX &&
           y >= this.qrCodePosY && y <= this.qrCodePosY + this.qrCodeSizeY);
    this.debug('isInsideQrCode('+x+', '+y+'): ' + result);
    return result;
  }

  isInsideResizeSquare(x, y) {
    var result=(
      x > this.qrCodePosX + this.qrCodeSizeX - this.objValues.resizeSquareSize &&
      x <= this.qrCodePosX + this.qrCodeSizeX &&
      y > this.qrCodePosY + this.qrCodeSizeY - this.objValues.resizeSquareSize &&
      y <= this.qrCodePosY + this.qrCodeSizeY
    );
    this.debug('isInsideResizeSquare('+x+', '+y+'): ' + result);
    return result;
  }

  isValidDraggingArea(x, y) {
    const newImagePosX = this.qrCodePosX + (x - this.lastMouseX);
    const newImagePosY = this.qrCodePosY + (y - this.lastMouseY);

    if (newImagePosX > 0 && newImagePosX + this.qrCodeSizeX < this.canvasSizeX &&
        newImagePosY > 0 && newImagePosY + this.qrCodeSizeY < this.canvasSizeY) {
        this.debug('isValidDraggingArea('+x+', '+y+'): true');
        return true;
    }
    this.debug('isValidDraggingArea('+x+', '+y+'): false');
    return false;
  }

  // drawing methods
  draw() {
    this.drawQrCode();
    this.drawQrCodeBorder();
    this.drawResize();
  }

  redraw() {
      this.context2d.clearRect(0, 0, this.canvasSizeX, this.canvasSizeY);
      this.drawBackground();
      this.draw();
  }

  drawBackground() {
    this.debug('drawBackground (x, y, w, h): (' + this.backgroundPosX + ', ' + this.backgroundPosY + ', ' + this.backgroundSizeX + ', ' + this.backgroundSizeY + ')');
    this.context2d.drawImage(this.backgroundImage, this.backgroundPosX, this.backgroundPosY, this.backgroundSizeX, this.backgroundSizeY);
  }

  drawQrCode() {
    this.context2d.drawImage(this.qrCodeImage,
      this.qrCodePosX, this.qrCodePosY,
      this.qrCodeSizeX, this.qrCodeSizeY);
  }

  drawQrCodeBorder() {
    if(this.showBorder) {
      this.context2d.strokeStyle = this.objValues.borderColor;
      this.context2d.strokeRect(this.qrCodePosX, this.qrCodePosY, this.qrCodeSizeX, this.qrCodeSizeY);
    }
  }

  drawResize() {
    if(!this.showResize)
      return;
    this.context2d.fillStyle = this.objValues.resizeSquareColor;
    if(this.objValues.resizeStyle == 'circle') {
      this.context2d.beginPath();
      this.context2d.arc(this.qrCodePosX + this.qrCodeSizeX - this.objValues.resizeSquareSize/2,
        this.qrCodePosY + this.qrCodeSizeY - this.objValues.resizeSquareSize/2,
        this.objValues.resizeSquareSize/2,
        0, 2 * Math.PI);
      this.context2d.fill();
        this.context2d.stroke();
    } else {
    this.context2d.fillRect(
      this.qrCodePosX + this.qrCodeSizeX - this.objValues.resizeSquareSize,
      this.qrCodePosY + this.qrCodeSizeY - this.objValues.resizeSquareSize,
      this.objValues.resizeSquareSize, this.objValues.resizeSquareSize);
    }
  }

  // cursor styles
  setMouseToMove() { this.canvas.style.cursor = 'move'; }
  setMouseToDefault() { this.canvas.style.cursor = 'auto'; }
  setMouseToResize() { this.canvas.style.cursor = 'se-resize'; }

  // value getter
  getQrCodePosition() {
    let pdfObj=this.canvasToPdf(this.backgroundSizeY, this.qrCodePosX, this.qrCodePosY);
    return {
      x: this.pixelsToPoints(pdfObj.x, this.objValues.dpi),
      y: this.pixelsToPoints(pdfObj.y, this.objValues.dpi),
    }
  }

  // helper
  debug(msg) { if (this.objValues.debug) console.log(msg); }
  info(msg) { console.log(msg); }
  canvasToPdf(height, x, y) { return { x: x, y: height - y }; }
  pdfToCanvas(height, x, y) { return { x: x, y: height - y }; }
  pixelsToPoints(pixelValue, dpi = 72) { return (pixelValue * 72) / dpi; }
  pointsToPixels(pointValue, dpi = 72) { return (pointValue * dpi) / 72; }
}