import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.ImageIcon;

public class ColorGame extends Applet
implements KeyListener, MouseListener, ActionListener {
	protected final static int BOARD_WIDTH = 20;
	protected final static int BOARD_HEIGHT = 12;
	protected final static int TILE_SIZE = 40;
	protected final static int COLORS = 7;

	protected final static int TILE_BLACK			= 0;
	protected final static int TILE_BLUE			= 1;
	protected final static int TILE_GREEN			= 2;
	protected final static int TILE_RED				= 3;
	protected final static int TILE_VIOLET			= 4;
	protected final static int TILE_WHITE			= 5;
	protected final static int TILE_YELLOW			= 6;
	protected final static int TILE_WASTE_BLACK		= 7;
	protected final static int TILE_WASTE_BLUE		= 8;
	protected final static int TILE_WASTE_GREEN		= 9;
	protected final static int TILE_WASTE_RED		= 10;
	protected final static int TILE_WASTE_VIOLET	= 11;
	protected final static int TILE_WASTE_WHITE		= 12;
	protected final static int TILE_WASTE_YELLOW	= 13;
	protected final static int TILE_SPACE			= 14;
	protected final static int TILE_BACKGROUND		= 15;
	protected final static int TILE_MAN				= 16;
	protected final static int TILE_FINISH			= 17;
	protected final static int TILE_MAN_FINISH		= 18;

	protected static ImageIcon[] tiles = new ImageIcon[] {
		new ImageIcon("tiles/black.png"),
		new ImageIcon("tiles/blue.png"),
		new ImageIcon("tiles/green.png"),
		new ImageIcon("tiles/red.png"),
		new ImageIcon("tiles/violet.png"),
		new ImageIcon("tiles/white.png"),
		new ImageIcon("tiles/yellow.png"),
		new ImageIcon("tiles/waste_black.png"),
		new ImageIcon("tiles/waste_blue.png"),
		new ImageIcon("tiles/waste_green.png"),
		new ImageIcon("tiles/waste_red.png"),
		new ImageIcon("tiles/waste_violet.png"),
		new ImageIcon("tiles/waste_white.png"),
		new ImageIcon("tiles/waste_yellow.png"),
		new ImageIcon("tiles/space.png"),
		new ImageIcon("tiles/background.png"),
		new ImageIcon("tiles/man.png"),
		new ImageIcon("tiles/finish.png"),
		new ImageIcon("tiles/man_finish.png")
	};

	protected int board[][] = new int[BOARD_WIDTH][BOARD_HEIGHT];
	protected int colorCnt[] = new int[COLORS];
	protected int mx, my;
	protected int level = 0;
	protected boolean gameStarted = false;
	PopupMenu popup;

	/**
	 * Translate level file character into constant.
	 */
	protected static int xlateChar(char c) {
		switch(c) {
		case 'B':
			return TILE_BLACK;
		case 'U':
			return TILE_BLUE;
		case 'G':
			return TILE_GREEN;
		case 'R':
			return TILE_RED;
		case 'V':
			return TILE_VIOLET;
		case 'W':
			return TILE_WHITE;
		case 'Y':
			return TILE_YELLOW;
		case 'b':
			return TILE_WASTE_BLACK;
		case 'u':
			return TILE_WASTE_BLUE;
		case 'g':
			return TILE_WASTE_GREEN;
		case 'r':
			return TILE_WASTE_RED;
		case 'v':
			return TILE_WASTE_VIOLET;
		case 'w':
			return TILE_WASTE_WHITE;
		case 'y':
			return TILE_WASTE_YELLOW;
		case '#':
			return TILE_SPACE;
		case ' ':
			return TILE_BACKGROUND;
		case 'M':
			return TILE_MAN;
		case 'F':
			return TILE_FINISH;
		case 'X':
			return TILE_MAN_FINISH;
		}
		return -1;
	}

	protected static boolean isColor(int tile) {
		return tile >= TILE_BLACK && tile <= TILE_YELLOW;
	}

	protected static boolean isWaste(int tile) {
		return tile >= TILE_WASTE_BLACK && tile <= TILE_WASTE_YELLOW;
	}

	protected static boolean isSpace(int tile) {
		return tile == TILE_SPACE;
	}

	protected static boolean isBackground(int tile) {
		return tile == TILE_BACKGROUND;
	}

	protected static boolean isFinish(int tile) {
		return tile == TILE_FINISH;
	}

	public void loadLevel(String name) {
		name = "levels" + File.separatorChar + name;
		String line;
		int i = 0, j, t;
		for(i = 0; i < BOARD_WIDTH; i++)
			for(j = 0; j < BOARD_HEIGHT; j++)
				board[i][j] = TILE_SPACE;
		for(i = 0; i < COLORS; i++)
			colorCnt[i] = 0;
		try {
			BufferedReader r = new BufferedReader(new FileReader(name));
			j = 0;
			while((line = r.readLine()) != null && j < BOARD_HEIGHT) {
				if(line.length() < BOARD_WIDTH)
					continue;
				for(i = 0; i < BOARD_WIDTH; i++) {
					if((t = xlateChar(line.charAt(i))) == -1)
						break;
					board[i][j] = t;
					if(t == TILE_MAN || t == TILE_MAN_FINISH) {
						mx = i;
						my = j;
					}
					if(isColor(t))
						colorCnt[t]++;
				}
				j++;
			}
			r.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean __doMove(int dx, int dy) {
		int nx = mx + dx;
		int ny = my + dy;
		int px = nx + dx;
		int py = ny + dy;

		if(isSpace(board[nx][ny]))
			return false;
		do {
			if(isBackground(board[nx][ny]) || isFinish(board[nx][ny])) {
				// regular man movement
				board[nx][ny] =
					board[nx][ny] == TILE_FINISH ? TILE_MAN_FINISH : TILE_MAN;
				break;
			}
			if(isColor(board[nx][ny]) && board[px][py] == board[nx][ny]) {
				// color join
				colorCnt[board[nx][ny]]--;
				board[nx][ny] = TILE_MAN;
				break;
			}
			if(isColor(board[nx][ny]) && isBackground(board[px][py])) {
				// color push
				board[px][py] = board[nx][ny];
				board[nx][ny] = TILE_MAN;
				break;
			}
			if(isColor(board[nx][ny]) && isFinish(board[px][py]) &&
					colorCnt[board[nx][ny]] == 1) {
				// color over finish
				colorCnt[board[nx][ny]]--;
				board[nx][ny] = TILE_MAN;
				break;
			}
			return false;
		} while(false);

		board[mx][my] =
			board[mx][my] == TILE_MAN ? TILE_BACKGROUND : TILE_FINISH;
		mx = nx;
		my = ny;
		return true;
	}

	protected void doMove(int dx, int dy) {
		if(!gameStarted)
			return;
		if(!__doMove(dx, dy))
			return;
		repaint();
		int sum = 0, i;
		for(i = 0; i < COLORS; i++)
			sum += colorCnt[i];
		if(sum == 0)
			nextLevel();
	}

	protected void nextLevel() {
		level++;
		loadLevel("level" + (level < 10 ? "0" : "") + level + ".txt");
		gameStarted = true;
	}

	protected void initMenu() {
		MenuItem mi;
		Menu m;
		int i;
		String s;
		
		popup = new PopupMenu();
		
		m = new Menu("Select Level");
		popup.add(m);
		for(i = 1; i < 10; i++) {
			mi = new MenuItem("Level " + i);
			mi.setActionCommand("level" + (i < 10 ? "0" : "") + i);
			mi.addActionListener(this);
			m.add(mi);
		}

		this.add(popup);
	}

	public void init() {
		addKeyListener(this);
		addMouseListener(this);
		initMenu();
		nextLevel();
	}

	public void start() {
	}

	public void stop() {
	}

	public void destroy() {
	}

	public void keyTyped(KeyEvent ke) {
	}

	public void keyPressed(KeyEvent ke) {
		System.out.println("Event " + ke);
		switch(ke.getKeyCode()) {
		case KeyEvent.VK_UP:
			doMove(0, -1);
			return;
		case KeyEvent.VK_DOWN:
			doMove(0, 1);
			return;
		case KeyEvent.VK_LEFT:
			doMove(-1, 0);
			return;
		case KeyEvent.VK_RIGHT:
			doMove(1, 0);
			return;
		}
	}

	public void keyReleased(KeyEvent ke) {
	}

	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		if(cmd.indexOf("level") == 0) {
			level = Integer.parseInt(cmd.substring(5));
			loadLevel(cmd + ".txt");
			gameStarted = true;
			repaint();
			return;
		}
	}

	public void mouseClicked(MouseEvent me) {
		if((me.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
			popup.show(this, me.getX(), me.getY());
	}

	public void mousePressed(MouseEvent me) {
	}

	public void mouseReleased(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void paint(Graphics g) {
		int i, j;
		int dx, dy;
		int x, y;

		dx = (getWidth() - TILE_SIZE * BOARD_WIDTH) / 2;
		dy = (getHeight() - TILE_SIZE * BOARD_HEIGHT) / 2;

		for(i = 0; i < BOARD_WIDTH; i++)
			for(j = 0; j < BOARD_HEIGHT; j++) {
				x = dx + i * TILE_SIZE;
				y = dy + j * TILE_SIZE;
				tiles[board[i][j]].paintIcon(null, g, x, y);
			}
	}

	public void update(Graphics g) {
		paint(g);
	}
}
