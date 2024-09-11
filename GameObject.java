package cz.cuni.pedf.android.masekfilip.fucopter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class GameObject {
	private Bitmap image;
	//proměnná image typu Bitmap
	private int x, y; //pravý horní roh hitboxů objektů
	public boolean destroyed = false;

	public GameObject(Bitmap bmp) {
		image = bmp;
		x = 100; //default souřadnice spawnu
		y = 100; //default souřadnice spawnu
	}
	public void swapImageScaled(Bitmap bmp) {
		image = Bitmap.createScaledBitmap(bmp, getWidth(), getHeight(), false);
	}
	//změna obrázků jednoho GameObjektu, získá velikost aktuálního obrázku

	public void draw(Canvas canvas) {
		canvas.drawBitmap(image, x, y, null);
	}
	//vykreslí bitmapu GameObejktu

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public int getPositionX() {
		return x;
	}

	public int getPositionY() {
		return y;
	}

	//4x funkce vrací pozice po zeptání
}
