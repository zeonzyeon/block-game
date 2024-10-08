import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BlockGame {
	static class MyFrame extends JFrame{
		
		static int BALL_WIDTH=20; 
		static int BALL_HEIGHT=20;
		static int BLOCK_ROWS=5; 
		static int BLOCK_COLUMNS=10;
		static int BLOCK_WIDTH=40;
		static int BLOCK_HEIGHT=20;
		static int BLOCK_GAP=3;
		static int BAR_WIDTH=90;
		static int BAR_HEIGHT=15;
		static int CANVAS_WIDTH=420+(BLOCK_GAP*BLOCK_COLUMNS)-BLOCK_GAP*3;
		static int CANVAS_HEIGHT=600;

		static MyPanel myPanel=null;
		static int score=0;
		static Timer timer=null;
		static Block[][]blocks =new Block[BLOCK_ROWS][BLOCK_COLUMNS];
		static Bar bar=new Bar(); 
		static Ball ball=new Ball();
		static int barXTarget=bar.x;
		static int dir=0; // 0: Up-Right 1: Down-Right 2: Up-Left 3: Down-Left
		static int ballSpeed=5;
		static boolean isGameFinish = false;

		static class Ball{
			int x=CANVAS_WIDTH/2-BALL_WIDTH/2; 
			int y=CANVAS_HEIGHT/2-BALL_HEIGHT/2;
			int width=BALL_WIDTH;
			int height=BALL_HEIGHT;

			Point getCenter() {
				return new Point(x+(BALL_WIDTH/2),y+(BALL_HEIGHT/2));
			}
			Point getBottomCenter() {
				return new Point(x+(BALL_WIDTH/2),y+(BALL_HEIGHT));
			}
			Point getTopCenter() {
				return new Point(x+(BALL_WIDTH/2),y);
			}
			Point getLeftCenter() {
				return new Point(x,y+(BALL_HEIGHT/2));
			}
			Point getRightCenter() {
				return new Point(x+(BALL_WIDTH),y+(BALL_HEIGHT/2));
			}
		}
		static class Bar{
			int x=CANVAS_WIDTH/2-BAR_WIDTH/2;
			int y=CANVAS_HEIGHT-100;
			int width=BAR_WIDTH;
			int height=BAR_HEIGHT;
		}
		static class Block{
			int x;
			int y;
			int width=BLOCK_WIDTH;
			int height=BLOCK_HEIGHT;
			int color=0; 
			boolean isHidden=false;
		}
		static class MyPanel extends JPanel{ // VANVAS for Draw
			public MyPanel() {
				this.setSize(CANVAS_WIDTH,CANVAS_HEIGHT);
				this.setBackground(new Color(0,0,70));
			}
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d=(Graphics2D)g;

				drawUI(g2d);
			}
			private void drawUI(Graphics2D g2d) {
				// draw Blocks
				for(int i=0;i<BLOCK_ROWS;i++)
					for(int j=0;j<BLOCK_COLUMNS;j++) {
						if(blocks[i][j].isHidden) { 
							continue; 
						}
						if(blocks[i][j].color==0) {
							g2d.setColor(Color.WHITE);
						}
						else if(blocks[i][j].color==1) {
							g2d.setColor(new Color(0,255,171));
						}
						else if(blocks[i][j].color==2) {
							g2d.setColor(new Color(0,171,255));
						}
						else if(blocks[i][j].color==3) {
							g2d.setColor(new Color(171,0,255));
						}
						else if(blocks[i][j].color==4) {
							g2d.setColor(new Color(255,0,171));
						}
						g2d.fillRect(blocks[i][j].x,blocks[i][j].y,
								blocks[i][j].width,blocks[i][j].height);
					}
				
				// draw Score
				g2d.setColor(Color.WHITE);
				g2d.setFont(new Font("TimesRoman",Font.BOLD,20));
				g2d.drawString("score : "+ score,CANVAS_WIDTH/2-50,20);
				if(isGameFinish) {
					g2d.setColor(Color.RED);
					g2d.drawString("Game Finished!", CANVAS_WIDTH/2-75,50);
				}
				// draw Ball
				g2d.setColor(new Color(255,177,0));
				g2d.fillOval(ball.x,ball.y,BALL_WIDTH,BALL_HEIGHT);
				// draw Bar
				g2d.setColor(Color.WHITE);
				g2d.fillRect(bar.x,bar.y,bar.width,bar.height);
			}
		}
		public MyFrame(String title){ 
			super(title); 
			this.setVisible(true);
			this.setSize(CANVAS_WIDTH,CANVAS_HEIGHT);
			this.setLocation(400,50); 
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			initData();
			myPanel=new MyPanel();
			this.add("Center",myPanel);

			setKeyListener();
			startTimer();
		}
		public void initData() {
			for(int i=0;i<BLOCK_ROWS;i++)
				for(int j=0;j<BLOCK_COLUMNS;j++) {
					blocks[i][j]=new Block();
					blocks[i][j].x=BLOCK_WIDTH*j + BLOCK_GAP*j;
					blocks[i][j].y=100 + BLOCK_HEIGHT*i + BLOCK_GAP*i;
					blocks[i][j].width=BLOCK_WIDTH;
					blocks[i][j].height=BLOCK_HEIGHT;
					blocks[i][j].color=4-i;
					blocks[i][j].isHidden=false; 
				}
		}
		public void setKeyListener() {
			this.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) { // Key Event
					if(e.getKeyCode()==KeyEvent.VK_LEFT) {
						System.out.println("Pressed Left Key");
						barXTarget-=20;
						if(bar.x<barXTarget) { //repeat key pressed
							barXTarget=bar.x;
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
						System.out.println("Pressed Right Key");
						barXTarget+=20;
						if(bar.x>barXTarget) { //repeat key pressed
							barXTarget=bar.x;
						} 
					}
				}
			});
		}
		public void startTimer() {
			timer=new Timer(20,new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { // Timer Event
					movement();
					checkCollision(); // Wall, Bar
					checkCollisionBlock(); // Blocks
					myPanel.repaint(); // Redraw

					isGameFinish();
				}
			});
			timer.start(); // Start Timer!
		}
		public void isGameFinish() {
			// Game Success!
			int count=0;
			for(int i=0;i<BLOCK_ROWS;i++) {
				for(int j=0;j<BLOCK_COLUMNS;j++) {
					Block block = blocks[i][j];
					if(block.isHidden)
						count++;
				}
			}
			if(count==BLOCK_ROWS * BLOCK_COLUMNS) { // Game Finished!
				timer.stop();
				isGameFinish=true; 
			}
		}
		public void movement() {
			if(bar.x<barXTarget) {
				bar.x+=5;
			}else if(bar.x>barXTarget) {
				bar.x-=5;
			}
			if(dir==0) { // 0: Up-Right
				ball.x+=ballSpeed;
				ball.y-=ballSpeed;
			}else if(dir==1) { // 1: Down-Right
				ball.x+=ballSpeed;
				ball.y+=ballSpeed;
			}else if(dir==2) { // 2: Up-Left
				ball.x-=ballSpeed;
				ball.y-=ballSpeed;
			}else if(dir==3) { // 3: Down-Left
				ball.x-=ballSpeed;
				ball.y+=ballSpeed;
			}
		}
		public boolean duplRect(Rectangle rect1, Rectangle rect2) {
			return rect1.intersects(rect2); // check two Rect is Duplicated!
		}
		public void checkCollision() {
			if(dir==0) { // 0: Up-Right
				// Wall
				if(ball.y < 0) {
					dir=1;
				}
				if(ball.x > CANVAS_WIDTH-BALL_WIDTH*2) { 
					dir=2;
				}
				// Bar 
			}else if(dir==1) { // 1: Down-Right
				// Wall
				if(ball.y > CANVAS_HEIGHT-BALL_HEIGHT*3) {
					dir=0;
					// game reset
					dir=0;
					ball.x=CANVAS_WIDTH/2-BALL_WIDTH/2; 
					ball.y=CANVAS_HEIGHT/2-BALL_HEIGHT/2;
					score=0;
				}
				if(ball.x > CANVAS_WIDTH-BALL_WIDTH*2) {
					dir=3;
				}
				// Bar 
				if(ball.getBottomCenter().y>=bar.y) {
					if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
							new Rectangle(bar.x,bar.y,bar.width,bar.height))) {
						dir=0;
					}
				}
			}else if(dir==2) { // 2: Up-Left
				// Wall
				if(ball.y < 0) { 
					dir=3;
				}
				if(ball.x < 0) {
					dir=0;
				}
				// Bar 
			}else if(dir==3) { // 3: Down-Left
				// Wall
				if(ball.y > CANVAS_HEIGHT-BALL_HEIGHT) { 
					dir=2;
					// game reset
					dir=0;
					ball.x=CANVAS_WIDTH/2-BALL_WIDTH/2; 
					ball.y=CANVAS_HEIGHT/2-BALL_HEIGHT/2;
					score=0; 
				}
				if(ball.x < 0) { 
					dir=1;
				}
				// Bar 
				if(ball.getBottomCenter().y>=bar.y) {
					if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
							new Rectangle(bar.x,bar.y,bar.width,bar.height))) {
						dir=2;
					}
				}
			}
		}
		public void checkCollisionBlock() {
			// 0: Up-Right 1: Down-Right 2: Up-Left 3: Down-Left
			for(int i=0;i<BLOCK_ROWS;i++) {
				for(int j=0;j<BLOCK_COLUMNS;j++) {
					Block block=blocks[i][j];
					if(block.isHidden==false) {
						if(dir==0) { // 0: Up-Right
							if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
									new Rectangle(block.x,block.y,block.width,block.height))) {
								if(ball.x > block.x+2 && ball.getRightCenter().x<=block.x + block.width -2) {
									// block bottom collision
									dir=1;
								}else {
									// block left collision
									dir=2;
								}
								block.isHidden=true;
							}
						}
						else if(dir==1){ // 1: Down-Right
							if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
									new Rectangle(block.x,block.y,block.width,block.height))) {
								if(ball.x > block.x+2 && ball.getRightCenter().x<=block.x + block.width -2) {
									// block top collision
									dir=0;
								}else {
									// block left collision
									dir=3;
								}
								block.isHidden=true;
							}
						}
						else if(dir==2) { // 2: Up-Left
							if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
									new Rectangle(block.x,block.y,block.width,block.height))) {
								if(ball.x > block.x+2 && ball.getRightCenter().x<=block.x + block.width -2) {
									// block bottom collision
									dir=3;
								}else {
									// block right collision
									dir=0;
								}
								block.isHidden=true;
							}
						}
						else if(dir==3) { // 3: Down-Left
							if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
									new Rectangle(block.x,block.y,block.width,block.height))) {
								if(ball.x > block.x+2 && ball.getRightCenter().x<=block.x + block.width -2) {
									// block top collision
									dir=2;
								}else {
									// block right collision
									dir=1;
								}
								block.isHidden=true;
							}
						}
						if(block.isHidden==true) { 
							score+=10;
						}
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		new MyFrame("Block Game");
	}
}