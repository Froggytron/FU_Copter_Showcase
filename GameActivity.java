package cz.cuni.pedf.android.masekfilip.fucopter;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends Activity {
	//beží když hra běží

	public boolean touch;
	public int touchX;
	public int touchY;
	public boolean gamePaused = false;
//A.S.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new GameView(this));
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		//ACTION_DOWN - prst na displeji detekce
		//ACTION_UP - detekce odstanění prstu z obrazovky
		//ACTION_MOVE - prst X,Y souřadnice se mění
		{
			touch = true;
			touchX = (int) event.getX();
			touchY = (int) event.getY();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		gamePaused = !gamePaused;
	}
	//mobile menu trojúhelník press (pause)
}
