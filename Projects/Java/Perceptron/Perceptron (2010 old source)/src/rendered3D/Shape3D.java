package rendered3D;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;
import java.util.ArrayList;

public interface Shape3D 
{
	public int setDepth(int i);
	public int setDepth();
	public int depth();
	public void draw(Graphics G, BufferedImage image_buffer);
	public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints);	
}
