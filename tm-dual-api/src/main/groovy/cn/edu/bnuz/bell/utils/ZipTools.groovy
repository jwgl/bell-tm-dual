package cn.edu.bnuz.bell.utils

import java.io.File;

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipTools {

    byte[] zip(List users, String baseDir, String pre){
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)
        users.each { item ->
            def base = "${baseDir}/${item.id}"
            File dir = new File (base)
            if (dir?.exists()) {
                File[] files = dir.listFiles()
                for (File file: files) {
                    if (("*" == pre && file.name.indexOf("bak_") == -1 && file.name.indexOf("review") == -1)
                            || file.name.indexOf(pre)==0) {
                        zipFile.putNextEntry(new ZipEntry(item.id + item.name + "/" + preLabel(file.name)))
                        file.withInputStream { input -> zipFile << input }
                        zipFile.closeEntry()
                    }
                }
            }
        }
        zipFile.finish()

        return baos.toByteArray()
    }

    private File getFile(String baseDir,String pre){
        File dir = new File(baseDir)
        if(dir.exists()){
            File[] files = dir.listFiles();
            for (File file:files) {
                if(file.name.indexOf(pre)!=-1){
                    return file
                }
            }
        }
        return null
    }

    def zip(String baseDir) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)
        addEntry(baseDir, zipFile)
        zipFile.finish()
        return baos.toByteArray()
    }

    private addEntry(String base,ZipOutputStream zipFile) {
        File dir = new File(base)
        if (dir?.exists()) {
            File[] files = dir.listFiles()
            for (File file:files) {
                if (file.isFile() && file.name.indexOf("bak_") == -1) {
                    zipFile.putNextEntry(new ZipEntry(file.name))
                    file.withInputStream { input -> zipFile << input }
                    zipFile.closeEntry()
                }
            }

        }
    }

    def preLabel(String filename) {
        def labelMap = [certi: '证书',
        paper: '论文',
        photo: '照片',
        trans: '成绩']
        def pre = filename.substring(0,5)
        return filename.replaceFirst(pre, labelMap[pre])
    }
}
