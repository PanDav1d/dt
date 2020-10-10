var canvas, context;
var star_img = new Image();
var background_img = new Image();
var isDraggable = false;

var currentX = 0;
var currentY = 0;

function _Go() {
  _MouseEvents();

  setInterval(function() {
    _ResetCanvas();
    _DrawImage();
  }, 1000/30);
}
function _ResetCanvas() {
  context.fillStyle = '#fff';
  //context.fillRect(0,0, canvas.width, canvas.height);
  context.drawImage(background_img, 0, 0, canvas.width, canvas.height);
}
function _MouseEvents() {
  canvas.onmousedown = function(e) {

    console.log("Offset left " + this.offsetLeft + " Offset top " + this.offsetTop)
    var mouseX = e.offsetX;//e.pageX - this.offsetLeft;
    var mouseY = e.offsetY;//e.pageY - this.offsetTop;

    console.log("Mouse down event @" + mouseX + "/" + mouseY + ". Active region x"
    + (currentX - star_img.width/2) + " till "
    + (currentX + star_img.width/2) + "; y "
    + (currentY - star_img.height/2) + " till "
    + (currentY + star_img.height/2)
    )

    if (mouseX >= (currentX - star_img.width/2) &&
        mouseX <= (currentX + star_img.width/2) &&
        mouseY >= (currentY - star_img.height/2) &&
        mouseY <= (currentY + star_img.height/2)) {
        console.log("Clicked on star_img")
      isDraggable = true;
    }
  };
  canvas.onmousemove = function(e) {


    if (isDraggable) {
        console.log("Mouse move event")
      currentX = e.offsetX;//e.pageX - this.offsetLeft;
      currentY = e.offsetY;//e.pageY - this.offsetTop;
    }
  };
  canvas.onmouseup = function(e) {
    isDraggable = false;
  };
  canvas.onmouseout = function(e) {
    isDraggable = false;
  };
}
function _DrawImage() {
  context.drawImage(star_img, currentX-(star_img.width/2), currentY-(star_img.height/2));
}