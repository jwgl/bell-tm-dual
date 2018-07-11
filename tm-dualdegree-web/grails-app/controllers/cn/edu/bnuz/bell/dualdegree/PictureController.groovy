package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.utils.ImageUtil
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus

class PictureController {
    SecurityService securityService

    @Value('${bell.student.filesPath}')
    String filesPath

    def show(String awardId, String studentId, String fileName) {
        def picturePath = filesPath
        def desFileName = fileName
        def userId = studentId
        if (securityService.hasRole('ROLE_STUDENT')) {
            userId = securityService.userId
        }

        if (fileName) {
            def fileType = fileName.substring(fileName.lastIndexOf('.'))
            if (fileType == ".pdf") {
                desFileName = "pdf.jpg"
            } else if (awardId) {
                picturePath = "${filesPath}/${awardId}/${userId}"
            }
        } else {
            desFileName = 'none.jpg'
        }
        File file = new File(picturePath, "${desFileName}")
        Boolean thumbnail = (desFileName != 'none.jpg') && (desFileName != 'pdf.jpg')
        output(file, thumbnail)
    }

    def fileView(String awardId, String studentId, String fileName) { }

    def fileSource(String awardId, String studentId, String fileName) {
        def userId = studentId
        if (securityService.hasRole('ROLE_STUDENT')) {
            userId = securityService.userId
        }
        def picturePath = "${filesPath}/${awardId}/${userId}"
        File file = new File(picturePath, fileName)
        output(file, false)
    }

    private output(File file, Boolean thumbnail) {
        if (!file.exists()) {
            render status: HttpStatus.NOT_FOUND
        } else {
            byte[] bytes = null
            if (thumbnail) {
                bytes = ImageUtil.thumbnailImage(file, 253, 192)
            } else {
                bytes = file.bytes
            }
            response.contentType = URLConnection.guessContentTypeFromName(file.getName())
            response.outputStream << bytes
            response.outputStream.flush()
        }
    }

    def download(String awardId, String studentId, String fileName) {
        def userId = studentId
        if (securityService.hasRole('ROLE_STUDENT')) {
            userId = securityService.userId
        }
        def picturePath = "${filesPath}/${awardId}/${userId}"
        File file = new File(picturePath, fileName)
        response.contentType = URLConnection.guessContentTypeFromName(file.getName())
        response.setHeader("Content-disposition","attachment;filename='${file.name}'")
        response.outputStream << file.bytes
        response.outputStream.flush()
    }
}
