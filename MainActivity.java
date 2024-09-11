package cz.cuni.pedf.android.masekfilip.fucopter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

	SharedPreferences savedScore;
	TextView scoreView;
	MediaPlayer themeMusic;
	//třída - proměnná.ost
	ImageView titleImage;
	//pozadí

	//A.S.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		scoreView = findViewById(R.id.bestScoreView);
		//proměnná = text pole
		Button howToPlayButton = findViewById(R.id.button2);
		Button playButton = findViewById(R.id.button);
		//proměnná typ button <= tlačítko přiřazené
		savedScore = this.getSharedPreferences("cz.cuni.pedf.android.masekfilip.fucopter", Context.MODE_PRIVATE);
		//najde kde hledat uložené skóre
		int bestScore = savedScore.getInt("bestScore", 0);
		//klíč pro zobrazení, set promenné

		scoreView.setText("Best Score: " + bestScore);
		Context context = this;

		Random rand = new Random();
		//tvorba generátoru náhodných čísel
		int titleBackground = rand.nextInt(5);
		//rand gimme random number (0-4)

		titleImage = findViewById(R.id.imageView);

		switch(titleBackground) {
			case 0: break; //výchozí png, preset v xml
			case 1: titleImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title2)); break;
			case 2: titleImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title3)); break;
			case 3: titleImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title4)); break;
			case 4: titleImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title5)); break;
			//čtení bitmapy z png 0-4 case
		}

		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				themeMusic.pause(); //stiknutí play zastavuje title ost
				Intent intent = new Intent(context, GameActivity.class); //příprava spustění GameActivity
				startActivityForResult(intent, 1); //spouští GameActivity
				}
		});

		howToPlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, HowToPlayActivity.class); //listener na tlačítko v how to play screenu
				startActivity(intent); //spouští HowToPlayActivity
			}
		});

		themeMusic = MediaPlayer.create(this, R.raw.theme); //odkazuje do raw adresáře s hudbou
		themeMusic.setLooping(true);
		themeMusic.start();
	}

	// Metoda pro řádné ukončení hry (aktualizuje best a spustí hudbu po zavření hry):
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); //volá funkci o třídu výš
		int bestScore = savedScore.getInt("bestScore", 0); //aktualizace výtahu bestscore záznamu
		scoreView.setText("Best Score: " + bestScore); //aplikace nového best
		themeMusic.seekTo(0); //reset title ost po pořážce
		themeMusic.start(); //here we go again
	}
}
