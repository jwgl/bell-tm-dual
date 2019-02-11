package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

import java.time.LocalDate

@PreAuthorize('hasRole("ROLE_DUALDEGREE_STUDENT")')
class ApplicationFormController {
    ApplicationFormService applicationFormService
    AwardPublicService awardPublicService
    ApplicationReviewerService applicationReviewerService
    PaperFormService paperFormService

    @Value('${bell.student.filesPath}')
    String filesPath

    /**
     * @param studentId 学号
     * @return 可申请授予和已申请单
     */
    def index(String studentId) {
        renderJson applicationFormService.list(studentId)
    }
    /**
     * 保存数据
     * @param studentId 学号
     * @return id
     */
    def save(String studentId) {
        def cmd = new ApplicationFormCommand()
        bindData(cmd, request.JSON)
        def form = applicationFormService.create(studentId, cmd)
        renderJson([id: form.id])
    }

    /**
     * 编辑数据
     * @param studentId 学号
     * @param id 申请单id
     */
    def edit(String studentId, Long id) {
        renderJson applicationFormService.getFormForEdit(studentId, id)
    }

    /**
     * 显示数据
     * @param studentId 学号
     * @param awardPublicId 学位授予批次id
     * @param id 申请单id
     * @return
     */
    def show(String studentId, Long id) {
        def form = applicationFormService.getFormForShow(studentId, id)
        renderJson ([
                form: form,
                award: awardPublicService.getAwardInfo((Long)form.awardId),
                fileNames: applicationFormService.findFiles(studentId, form.awardId),
                paperForm: paperFormService.getPaperForm(studentId, id),
                latestAnswer: applicationFormService.getLatestAnswer(id)
        ])
    }

    /**
     * 更新数据
     */
    def update(String studentId, Long id) {
        def cmd = new ApplicationFormCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        applicationFormService.update(studentId, cmd)
        renderOk()
    }

    /**
     * 创建
     * @param studentId 学号
     * @param awardId 授予Id
     * @return
     */
    def create(String studentId, Long awardId) {
        renderJson applicationFormService.getFormForCreate(studentId, awardId)
    }

    def patch(String studentId, Long id, String op) {
        // 如果预期，不做任何操作
        def application = DegreeApplication.load(id)
        if (isExpire(application)) {
            throw new ForbiddenException()
        }
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                applicationFormService.submit(studentId, cmd)
                break
        }
        renderOk()
    }

    /**
     * 获取审核人
     * @param applicationFormId 申请Id
     * @return 审核人列表
     */
    def checkers(Long applicationFormId) {
        def form = DegreeApplication.load(applicationFormId)
        if (!form) {
            renderBadRequest()
        } else {
            renderJson applicationReviewerService.getCheckers(applicationFormId)
        }
    }

    /**
     * 上传文件
     */
    def upload(String studentId, Long awardId) {
        def prefix = params.prefix
        def file = request.getFile('file')
        if (prefix && !file.empty) {
            def filename=file.originalFilename
            def type=filename.substring(filename.lastIndexOf(".") + 1).toLowerCase()
            def filePath = "${filesPath}/${awardId}/${studentId}"
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            } else {
                Date date = new Date()
                for (File f: dir.listFiles()) {
                    if (f.name.indexOf("${prefix}_${studentId}") != -1) {
                        // 备份原上传文件
                        def names = dir.list().join(";")
                        def frequence = (names=~"bak_${prefix}")
                        def name = "${filePath}/bak_${prefix}_${frequence.size() + 1}" +
                                ".${f.name.substring(f.name.lastIndexOf(".") + 1).toLowerCase()}"
                        f.renameTo(name)
                    }
                }
            }

            file.transferTo( new File("${filePath}/${prefix}_${studentId}.${type}") )

            renderOk()
        } else {
            renderBadRequest()
        }

    }

    private static Boolean isExpire(DegreeApplication application) {
        def now = LocalDate.now()
        if (application.status == State.CREATED && now.isAfter(application.award.requestEnd)) {
            return true
        }
    }
}
