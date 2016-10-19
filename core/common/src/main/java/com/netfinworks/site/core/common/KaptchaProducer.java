/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2015年1月20日
 */
package com.netfinworks.site.core.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 验证码生成类
 * </p>
 * 
 * @author 徐威
 * @since jdk6
 * @date 2015年1月20日
 */
public class KaptchaProducer {
    /** 所有随机字符 */
    private String       RANDOM_STRING = "2346789ABCDEFGHJKLMNPQRTUVWXYZabcdefghijkmnpqrtuvwxyz";

    /** 放到session中的key */
    public static String KAPTCHA_KEY   = "SESSION_KAPTCHA_SESSION_KEY";

    /** 随机产生的字符串 */
    private Random       random        = new Random();

    /** 图片宽 */
    private int          width         = 100;

    /** 图片高 */
    private int          height        = 28;

    /** 干扰线数量 */
    private int          lineSize      = 5;

    /** 随机产生字符数量 */
    private int          stringNum     = 4;

    /** 字体大小 */
    private int          fontSize      = 20;

    /**
     * 生成随机图片
     */
    public void createRandomCode(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        // 创建是具有缓冲区的Image对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);

        // 创建背景图
        Graphics g = image.getGraphics();
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, fontSize));
        g.setColor(getRandColor(110, 133));

        // 绘制干扰线
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }

        // 绘制随机字符
        String randomString = "";
        for (int i = 1; i <= stringNum; i++) {
            randomString = drawText(g, randomString, i);
        }

        // 保存验证码到会话中
        session.removeAttribute(KAPTCHA_KEY);
        session.setAttribute(KAPTCHA_KEY, randomString);

        // 将内存中的图片通过流的形式输出
        g.dispose();
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获得字体
     * 
     * @return 字体样式
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, fontSize);
    }

    /**
     * 获得颜色
     * 
     * @param fc 前景色
     * @param bc 背景色
     * @return 颜色
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /**
     * 绘制字符串
     * 
     * @param g 背景图
     * @param text 验证码
     * @param i 计数
     * @return 绘制字符
     */
    private String drawText(Graphics g, String text, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(RANDOM_STRING.length())));
        text += rand;
        g.translate(random.nextInt(2), random.nextInt(2));
        g.drawString(rand, 17 * i, 20);
        return text;
    }

    /**
     * 绘制干扰线
     * 
     * @param g 图对象
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 获取随机的字符
     * 
     * @param num 位置
     * @return 随机数字
     */
    public String getRandomString(int num) {
        return String.valueOf(RANDOM_STRING.charAt(num));
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLineSize() {
        return lineSize;
    }

    public void setLineSize(int lineSize) {
        this.lineSize = lineSize;
    }

    public int getStringNum() {
        return stringNum;
    }

    public void setStringNum(int stringNum) {
        this.stringNum = stringNum;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

}