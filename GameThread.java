package cz.cuni.pedf.android.masekfilip.fucopter;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

	//https://www.androidauthority.com/android-game-java-785331/
	// Hlavní tutoriál použitý na celý projekt + tvorbu jeho základu a struktury
	//A.S. all, počítá pro výkres hry FPS, vykresluje v reálném čase konstantní frekvenci snímků
	private SurfaceHolder surfaceHolder;
	private GameView gameView;
	private boolean running;
	public static Canvas canvas;
	private final int targetFPS = 60;

	public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {

		super();
		this.surfaceHolder = surfaceHolder;
		this.gameView = gameView;
	}

	@Override
	public void run() {
		long startTime;
		long timeMillis;
		long waitTime;
		int frameCount = 0;
		long targetTime = 1000 / targetFPS;

		while (running) {
			startTime = System.nanoTime();
			canvas = null;

			try {
				canvas = this.surfaceHolder.lockCanvas();
				synchronized(surfaceHolder) {
					this.gameView.update();
					this.gameView.draw(canvas);
				}
			} catch (Exception e) {	   }
			finally {
				if (canvas != null)			{
					try {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			timeMillis = (System.nanoTime() - startTime) / 1000000;
			waitTime = targetTime - timeMillis;

			try {
				this.sleep(waitTime);
			} catch (Exception e) {}

			frameCount++;
			if (frameCount == targetFPS)		{
				frameCount = 0;		  }
		}

	}
	public void setRunning(boolean isRunning) {
		running = isRunning;
	}
}
