package com.servlet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_core.Mat;

import cc.eguid.charsocr.PlateRecognition;

import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Label;

public class Ceshi {

	public static final JButton button = new JButton("ʶ����:");
	private static JFrame frame_1;
	public static String ret;

	public static void main(String[] args) {

		// TODO Auto-generated method stub
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFrame jf = new JFrameTest();
						jf.pack();
						jf.setVisible(true);
					}
				});
				button.setForeground(Color.BLACK);
				button.setFont(new Font("����", Font.PLAIN, 14));
				button.setBackground(Color.WHITE);
				button.setSize(120, 60);
				// TODO Auto-generated method stub
				JFrame frame = null;
				try {
					frame_1 = new ImageViewerFrame();
					frame_1.setTitle("\u6EC1\u5DDE\u5B66\u9662_\u8F66\u724C\u8BC6\u522B");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				frame_1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame_1.setVisible(true);
				frame_1.getContentPane().add(button, BorderLayout.SOUTH);

				Label label = new Label();
				label.setFont(new Font("Dialog", Font.PLAIN, 16));
				frame_1.getContentPane().add(label, BorderLayout.NORTH);
			}
		});
	}
}

class ImageViewerFrame extends JFrame {
	public ImageViewerFrame() throws IOException {
	
		setTitle("CHZU_����ʶ��");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		label = new JLabel();
		add(label, BorderLayout.CENTER);
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		JMenu menu = new JMenu("�˵�");
		menubar.add(menu);
		JMenuItem openItem = new JMenuItem("ѡ����ͼƬ");
		menu.add(openItem);
		JMenuItem exitItem = new JMenuItem("�ر�ϵͳ");
		menu.add(exitItem);
		openItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					String name = chooser.getSelectedFile().getPath();
					//���ѡ��ͼƬ��·��
					System.out.println(name);
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");// �������ڸ�ʽ
					// new Date()Ϊ��ȡ��ǰϵͳʱ��
					Calendar Cld = Calendar.getInstance();
					int YY = Cld.get(Calendar.YEAR);
					int MM = Cld.get(Calendar.MONTH) + 1;
					int DD = Cld.get(Calendar.DATE);
					int HH = Cld.get(Calendar.HOUR_OF_DAY);
					int mm = Cld.get(Calendar.MINUTE);
					int SS = Cld.get(Calendar.SECOND);
					int MI = Cld.get(Calendar.MILLISECOND);
					int shijianchuo = YY + MM + DD + HH + mm + SS + MI;
					String namepath = "res\\image\\xuanqu\\" + shijianchuo
							+ ".png";
					// ��ͼƬ���浽����
					copyFile(name, namepath);
					//
					String imgPath = namepath;
					Mat src = opencv_imgcodecs.imread(imgPath);
					PlateRecognition a = new PlateRecognition();
					Ceshi.ret = a.plateRecognise(src);
					// ��ʶ��Ľ����ֵ��button//���button����ʾ�ĳ��ƺ�ԤԼ����һ��
					Ceshi.button.setText(Ceshi.ret);
					label.setIcon(new ImageIcon(name));

				}
			}

		});

		exitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
	}

	private JLabel label;
	private JFileChooser chooser;
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 500;

	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldPath); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("���Ƶ����ļ���������");
			e.printStackTrace();
		}
	}
	
}
class JFrameTest extends JFrame{    
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 500;
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JButton jb1 = new JButton("��ѽ��û�����");  
	 Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    public JFrameTest(){    
    	setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
         this.setTitle("������");    
         this.add(jb1);  
         this.setLocation(500,500);
 
    }   
  
}