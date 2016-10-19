package com.netfinworks.site;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class ImageTest {
	public static void main(String[] args) throws Exception {

		// 1.jpg是你的 主图片的路径
		InputStream is = new FileInputStream("E:/1.jpg");

		BufferedImage buffImg = ImageIO.read(is);

		// 得到画笔对象
		Graphics g = buffImg.getGraphics();

		// 创建你要附加的图象。
		// 2.jpg是你的小图片的路径
		// ImageIcon imgIcon = new ImageIcon("2.jpg");

		// 得到Image对象。
		// Image img = imgIcon.getImage();

		// 将小图片绘到大图片上。
		// 5,300 .表示你的小图片在大图片上的位置。
		// g.drawImage(img, 5, 330, null);

		// 设置颜色。
		g.setColor(Color.BLACK);

		// 最后一个参数用来设置字体的大小
		Font f = new Font("宋体", Font.BOLD, 30);

		g.setFont(f);

		// 10,20 表示这段文字在图片上的位置(x,y) .第一个是你设置的内容。
		g.drawString("默哀555555。。。。。。。", 10, 30);

		g.dispose();

		OutputStream os = new FileOutputStream("E:/union.jpg");

		// 创键编码器，用于编码内存中的图象数据。

		ImageIO.write(buffImg, "jpg", os);

		is.close();
		os.close();

		System.out.println("合成结束。。。。。。。。");

	}
}
