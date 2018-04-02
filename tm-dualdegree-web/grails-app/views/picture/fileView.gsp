<html>
<head>
    <title>文件浏览</title>
    <script src="/static/js/lib/pdf.min.js"></script>
</head>
<body>
    <div id="the-img" style="margin:0 auto;"></div>
    <canvas id="the-canvas"></canvas>
<script>
    var filename = '${params.fileName}';
    if (filename && filename.lastIndexOf('.') != -1) {
        var url = '/web/dualdegree/picture/filesrc?awardId=${params.awardId}&studentId=${params.studentId}&fileName=${params.fileName}';
        var type = filename.slice(filename.lastIndexOf('.'));
        if (type.toLowerCase() == '.pdf') {

            PDFJS.workerSrc = '/static/js/lib/pdf.worker.min.js';

            // Asynchronous download of PDF
            var loadingTask = PDFJS.getDocument(url);
            loadingTask.promise.then(function(pdf) {
                console.log('PDF loaded');

                // Fetch the first page
                var pageNumber = 1;
                pdf.getPage(pageNumber).then(function(page) {
                    console.log('Page loaded');

                    var scale = 1.5;
                    var viewport = page.getViewport(scale);

                    // Prepare canvas using PDF page dimensions
                    var canvas = document.getElementById('the-canvas');
                    var context = canvas.getContext('2d');
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;

                    // Render PDF page into canvas context
                    var renderContext = {
                        canvasContext: context,
                        viewport: viewport
                    };
                    var renderTask = page.render(renderContext);
                    renderTask.then(function () {
                        console.log('Page rendered');
                    });
                });
            }, function (reason) {
                // PDF loading error
                console.error(reason);
            });
        } else {
            var canvas = document.getElementById('the-img');
            canvas.innerHTML = '<img src=' + url + '>'
        }
    }
</script>
<style>
    body {
        text-align:center;
    }
</style>
</body>
</html>