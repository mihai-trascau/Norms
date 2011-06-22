package env;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	
	private Map map;
	private JLabel labelMap[][];
	private JPanel mapPanel;
	
	public GUI(Map map) throws IOException {
		super("Factory transport robots");
		
		this.setSize(1000, 1000);
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//this.setBounds(0,0,screenSize.width, screenSize.height);
		//this.setUndecorated(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.map = map;
		
		this.mapPanel = new JPanel(new GridLayout(map.getHeigth(), map.getWidth()));
		this.labelMap = new JLabel[map.getHeigth()][map.getWidth()];
		
		this.drawMap(null);
		
		this.getContentPane().add(mapPanel);
		this.setVisible(true);
	}
	
	public void drawMap(Hashtable<String,Position> agentPosition) {
		mapPanel.removeAll();
		
		for(int i = 0; i < map.getHeigth(); i++)
			for(int j = 0; j < map.getWidth(); j++)
			{
				if(map.getPosition(i, j) == 0) {
					labelMap[i][j] = new JLabel(i+","+j);
					labelMap[i][j].setOpaque(true);
					labelMap[i][j].setBackground(Color.WHITE);
					labelMap[i][j].setForeground(Color.LIGHT_GRAY);
					labelMap[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				}
				else if(map.getPosition(i, j) == 1) {
					labelMap[i][j] = new JLabel();
					labelMap[i][j].setOpaque(true);
					labelMap[i][j].setBackground(Color.GRAY);
					labelMap[i][j].setForeground(Color.LIGHT_GRAY);
				}
				else if(map.getPosition(i, j) == 2) {
					labelMap[i][j] = new JLabel();
					labelMap[i][j].setOpaque(true);
					labelMap[i][j].setBackground(Color.GRAY);
					labelMap[i][j].setIcon(getScaledIcon("res/img/crate.png", 0.17));
				}
				else if(map.getPosition(i, j) == 3) {
					labelMap[i][j] = new JLabel();
					labelMap[i][j].setOpaque(true);
					labelMap[i][j].setBackground(Color.CYAN);
					labelMap[i][j].setIcon(getScaledIcon("res/img/crate.png", 0.17));
				}
				
				labelMap[i][j].setHorizontalAlignment(JLabel.CENTER);
				labelMap[i][j].setVerticalTextPosition(JLabel.BOTTOM);
				labelMap[i][j].setHorizontalTextPosition(JLabel.CENTER);
			}
		
		if(agentPosition != null) {
			for(String agentName : agentPosition.keySet()){
				int i = agentPosition.get(agentName).getX();
				int j = agentPosition.get(agentName).getY();
				labelMap[i][j].setForeground(Color.RED);
				labelMap[i][j].setText(agentName);
				labelMap[i][j].setIcon(getScaledIcon("res/img/loaded_robot.png", 0.15));
			}
		}
		
		for(int i = 0; i < map.getHeigth(); i++)
			for(int j = 0; j < map.getWidth(); j++)
				this.mapPanel.add(labelMap[i][j]);
		
		this.setVisible(false);
		this.setVisible(true);
		//this.repaint();
	}
	
	private ImageIcon getScaledIcon(String path, double scaleFactor) {
		try {
			BufferedImage crate = ImageIO.read(new File(path));
			return this.scale(crate, (int)(crate.getWidth()*scaleFactor), (int)(crate.getHeight()*scaleFactor));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ImageIcon scale(BufferedImage image, int width, int height) {
		int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return new ImageIcon(resizedImage);
		}
}