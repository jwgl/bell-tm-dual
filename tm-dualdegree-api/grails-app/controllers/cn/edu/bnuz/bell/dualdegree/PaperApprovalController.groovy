package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.utils.ZipTools
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_PAPER_APPROVE")')
class PaperApprovalController {
    PaperApprovalService paperApprovalService
    @Value('${bell.student.filesPath}')
    String filesPath

    def index(String mentorId, ListCommand cmd) {
        renderJson(paperApprovalService.list(mentorId, cmd))
    }

    def show(String mentorId, Long paperApprovalId, String id, String type) {
        ListType listType = ListType.valueOf(type ?: 'todo')
        if (id == 'undefined') {
            renderJson paperApprovalService.getFormForReview(mentorId, paperApprovalId, listType)
        } else {
            renderJson paperApprovalService.getFormForReview(mentorId, paperApprovalId, listType, UUID.fromString(id))
        }
    }

    def patch(String mentorId, Long id, String op) {
        Long formId = id
        if (!formId) {
            formId = params.getLong("paperApprovalId")
        }
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.FINISH:
                paperApprovalService.finish(mentorId, id)
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = formId
                paperApprovalService.reject(mentorId, cmd)
                break
            default:
                throw new BadRequestException()
        }

        renderJson paperApprovalService.getFormForReview(mentorId, formId, ListType.TODO)
    }

    /**
     * 上传文件
     */
    def upload(String mentorId, Long paperApprovalId) {
        DegreeApplication form = DegreeApplication.get(paperApprovalId)
        if (!form) {
            throw new NotFoundException()
        }
        if (form.approver.id != mentorId && form.paperApprover.id != mentorId) {
            throw new ForbiddenException()
        }
        def prefix = params.prefix
        def file = request.getFile('file')
        if (prefix && !file.empty) {
            def filename=file.originalFilename
            def type=filename.substring(filename.lastIndexOf(".") + 1).toLowerCase()
            def filePath = "${filesPath}/${form.awardId}/${form.student.id}"
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            } else {
                for (File f: dir.listFiles()) {
                    if (f.name.indexOf("${prefix}_${form.student.id}") != -1) {
                        // 备份原上传文件
                        def names = dir.list().join(";")
                        def frequence = (names=~"bak_${prefix}")
                        def name = "${filePath}/bak_${prefix}_${frequence.size() + 1}" +
                                ".${f.name.substring(f.name.lastIndexOf(".") + 1).toLowerCase()}"
                        f.renameTo(name)
                    }
                }
            }

            file.transferTo( new File("${filePath}/${prefix}_${form.student.id}.${type}") )

            renderOk()
        } else {
            renderBadRequest()
        }

    }

    def attachments(String mentorId, Long awardId) {
        def students = paperApprovalService.findUsers(mentorId, awardId)
        def basePath = "${filesPath}/${awardId}/"
        def zipTools = new ZipTools()
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("待审论文.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << zipTools.zip(students, basePath, 'paper')
        response.outputStream.flush()
    }
}
