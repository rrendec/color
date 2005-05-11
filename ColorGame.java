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
	protected PopupMenu popup;
	protected Vector undoStack = new Vector();
	protected int undoLevel = 0;

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
			undoStack.clear();
			undoLevel = 0;
			gameStarted = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected UndoMove __doMove(int dx, int dy) {
		UndoMove u = new UndoMove();
		u.mx = mx;
		u.my = my;
		u.dx = dx;
		u.dy = dy;
		int nx = mx + dx;
		int ny = my + dy;
		int px = nx + dx;
		int py = ny + dy;
		u.tile_m = board[mx][my];
		u.tile_n = board[nx][ny];
		u.incCnt = false;

		if(isSpace(board[nx][ny]))
			return null;
		do {
			if(isBackground(board[nx][ny]) || isFinish(board[nx][ny])) {
				// regular man movement
				u.tile_p = board[px][py];
				board[nx][ny] =
					board[nx][ny] == TILE_FINISH ? TILE_MAN_FINISH : TILE_MAN;
				break;
			}
			if(isColor(board[nx][ny]) && board[px][py] == board[nx][ny]) {
				// color join
				u.tile_p = board[px][py];
				colorCnt[board[nx][ny]]--;
				board[nx][ny] = TILE_MAN;
				u.incCnt = true;
				break;
			}
			if(isColor(board[nx][ny]) && isBackground(board[px][py])) {
				// color push
				u.tile_p = board[px][py];
				board[px][py] = board[nx][ny];
				board[nx][ny] = TILE_MAN;
				break;
			}
			if(isColor(board[nx][ny]) && isFinish(board[px][py]) &&
					colorCnt[board[nx][ny]] == 1) {
				// color over finish
				u.tile_p = board[px][py];
				colorCnt[board[nx][ny]]--;
				board[nx][ny] = TILE_MAN;
				u.incCnt = true;
				break;
			}
			if(isWaste(board[nx][ny]) && isBackground(board[px][py])) {
				// push waste
				u.tile_p = board[px][py];
				board[px][py] = board[nx][ny];
				board[nx][ny] = TILE_MAN;
				break;
			}
			if(isWaste(board[nx][ny]) && isSpace(board[px][py])) {
				// throw away waste
				u.tile_p = board[px][py];
				board[nx][ny] = TILE_MAN;
				break;
			}
			return null;
		} while(false);

		board[mx][my] =
			board[mx][my] == TILE_MAN ? TILE_BACKGROUND : TILE_FINISH;
		mx = nx;
		my = ny;
		return u;
	}

	protected void doMove(int dx, int dy) {
		UndoMove u;
		if(!gameStarted)
			return;
		if((u = __doMove(dx, dy)) == null)
			return;
		undoStack.setSize(undoLevel + 1);
		undoStack.set(undoLevel++, u);
		repaint();
		int sum = 0, i;
		for(i = 0; i < COLORS; i++)
			sum += colorCnt[i];
		if(sum == 0) {
			//FIXME Status bar <= "Press space for next level"
			gameStarted = false;
		}
	}

	protected void undoMove() {
		if(!gameStarted)
			return;
		if(undoLevel == 0)
			return;
		UndoMove u = (UndoMove)undoStack.elementAt(--undoLevel);
		mx = u.mx;
		my = u.my;
		int nx = mx + u.dx;
		int ny = my + u.dy;
		int px = nx + u.dx;
		int py = ny + u.dy;
		board[mx][my] = u.tile_m;
		board[nx][ny] = u.tile_n;
		board[px][py] = u.tile_p;
		if(u.incCnt)
			colorCnt[u.tile_n]++;
		repaint();
	}

	protected void redoMove() {
		UndoMove u;
		if(!gameStarted)
			return;
		if(undoLevel == undoStack.size())
			return;
		u = (UndoMove)undoStack.elementAt(undoLevel++);
		__doMove(u.dx, u.dy);
		repaint();
	}

	protected void nextLevel() {
		level++;
		loadLevel("level" + (level < 10 ? "0" : "") + level + ".txt");
	}

	protected void initMenu() {
		MenuItem mi;
		Menu m1, m2;
		int i, j;
		String s;
		
		popup = new PopupMenu();
		
		m1 = new Menu("Select Level");
		popup.add(m1);
		for(i = 0; i < 10; i++) {
			m2 = new Menu("Levels " + (i == 0 ? 1 : 10 * i) +
					"-" + (10 * i + 9));
			for(j = (i == 0 ? 1 : 10 * i); j < 10 * (i + 1); j++) {
				mi = new MenuItem("Level " + j);
				mi.setActionCommand("level" + (j < 10 ? "0" : "") + j);
				mi.addActionListener(this);
				m2.add(mi);
			}
			m1.add(m2);
		}

		this.add(popup);
	}

	public void init() {
		int i, j;
		addKeyListener(this);
		addMouseListener(this);
		initMenu();
		/*
		for(i = 0; i < BOARD_WIDTH; i++)
			for(j = 0; j < BOARD_HEIGHT; j++)
				board[i][j] = TILE_SPACE;
		*/
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
		//System.out.println("Event " + ke);
		int code = ke.getKeyCode();
		if(code == KeyEvent.VK_UP) {
			doMove(0, -1);
			return;
		}
		if(code == KeyEvent.VK_DOWN) {
			doMove(0, 1);
			return;
		}
		if(code == KeyEvent.VK_LEFT) {
			doMove(-1, 0);
			return;
		}
		if(code == KeyEvent.VK_RIGHT) {
			doMove(1, 0);
			return;
		}
		if(code == KeyEvent.VK_SPACE && !gameStarted) {
			nextLevel();
			repaint();
			return;
		}
		if(code == KeyEvent.VK_ESCAPE && gameStarted) {
			level--;
			nextLevel();
			repaint();
			return;
		}
		if(code == KeyEvent.VK_U || code == KeyEvent.VK_BACK_SPACE) {
			undoMove();
			return;
		}
		if(code == KeyEvent.VK_R) {
			redoMove();
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

class UndoMove {
	public int mx, my;
	public int dx, dy;
	public int tile_m;
	public int tile_n;
	public int tile_p;
	public boolean incCnt;
}
