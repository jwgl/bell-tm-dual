package cn.edu.bnuz.bell.utils

import cn.edu.bnuz.bell.http.ForbiddenException

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage

class ImageUtil {
    /**
     * <p>Title: thumbnailImage</p>
     * <p>Description: 依据图片路径生成缩略图 </p>
     * @param imgFile      原图片
     * @param w            缩略图宽
     * @param h            缩略图高
     */
    static byte[] thumbnailImage(File imgFile, int w, int h){
        if(imgFile.exists()){
            // ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
            String types = Arrays.toString(ImageIO.getReaderFormatNames())
            String suffix = null
            if(imgFile.getName().indexOf(".") > -1) {
                suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1)
            }
            if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()) < 0){
                throw new ForbiddenException()
            }
            Image img = ImageIO.read(imgFile)
            // 依据原图与要求的缩略图比例，找到最合适的缩略图比例
            int width = img.getWidth(null)
            int height = img.getHeight(null)
            if((width*1.0)/w < (height*1.0)/h){
                if(width > w){
                    h = Integer.parseInt(new java.text.DecimalFormat("0").format(height * w/(width*1.0)))
                }
            } else {
                if(height > h){
                    w = Integer.parseInt(new java.text.DecimalFormat("0").format(width * h/(height*1.0)))
                }
            }
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            Graphics g = bi.getGraphics()
            g.drawImage(img, 0, 0, w, h, Color.LIGHT_GRAY, null)
            g.dispose()
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", out);
            byte[] bytes = out.toByteArray();
            return bytes
        }
        throw new ForbiddenException()
    }
}
