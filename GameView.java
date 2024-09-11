package cz.cuni.pedf.android.masekfilip.fucopter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

//A.S.
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private GameThread thread;
	//fps gamethread

	private int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
	private int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
	//uložení šířky+výšky do proměnných, pro later práci

	private GameObject trollButton; //later swap obrázek
	private GameObject pauseScreen; //pause icon
	private Paint sky = new Paint(); //
	private Paint grass = new Paint(); //tráva + energybar
	private Paint texts = new Paint();
	private Paint texts2 = new Paint();
	private Paint texts3 = new Paint();
	private long frameCounter = 0; //time passed
	private int currentCopterImage = 1; //informace o zobr. obrázku copteru
	private int score = 0;
	private int fuel = 100;
	private GameActivity c; //budoucí reference na GameActivity
	private ArrayList<GameObject> bombs; //ArrayList = inteligentní pole
	private ArrayList<GameObject> enemyCastles; //-||-
	private ArrayList<GameObject> friendlyCastles; //-||-
	private boolean gameOver = false;
	private long nextEnemyCastle = 120; //zelený na samém začátku
	private long nextFriendlyCastle = 600; //green joinou za chvíli levelu1
	private Random rand = new Random();
	private int level = 1;
	private boolean stopCopter = false;
	private boolean newRecord = false;
	private boolean gameFinished = false; //když true, pozdějíší kód zabije GameView aktivitu
	private boolean copterSwitched = false; //otočení copteru
	private int previousBest; //pro porovnání s aktuálním
	private MediaPlayer copterSound, startSound, levelUpSound, bombReleaseSound, friendlyFireSound, gameOverSound, trollSound, superTuxSound, superTuxShootSound;
	//deklarace proměnné, později se do jí nacpe R.raw content
	private int trollState = 0;
	// -1 - (mezi offery)
	// 0 - nad 70% energie, nezobrazují se amazing offers
	// 1 - každých 10 a míň sekund nová nabídka
	// 2 - jen samý zelený hrady, stop nabídek
	private int currentTrollImage; // 0 - 13 offers marking (0 placeholder)
	private int bombsAI = 0; //počítá kolik bomb ještě hodit v 'free bombs' statu
	private int fireBullets = 0; //kolik zbývá tuxích fires
	private int redCastleWave = 0;
	private int greenCastleWave = 0;
	private Bitmap enemyCastleScaled; //bitmapa červenýho hradu, již pžizpůsobenýho screen resolution (dělá se níže)
	private Bitmap enemyCastleDestroyedScaled; //bitmapa červenýho ZNIČENÉHO hradu, již pžizpůsobenýho screen resolution (dělá se níže)
	private Bitmap friendlyCastleScaled; //bitmapa zelenýho hradu, již pžizpůsobenýho screen resolution (dělá se níže)
	private Bitmap friendlyCastleDestroyedScaled; //bitmapa zelenýho ZNIČENÉHO hradu, již pžizpůsobenýho screen resolution (dělá se níže)
	private Bitmap bombImageScaled; //b. bomby
	private Bitmap fireBulletImageScaled; //tuxí kulka bitmapa
	private Bitmap copterImage1Scaled; //vrr1
	private Bitmap copterImage2Scaled; //vrr 2
	private Bitmap copterImage3Scaled; //vrr zastavený
	private Bitmap copterImage4Scaled; //vrr 4 switched
	private Bitmap copterImage5Scaled; //vrr 5 switched
	private ArrayList<Bitmap> trollImagesScaled; //chytré pole bitmap offerů
	private final int copterWidth = 21, castleWidth = 12, bombWidth = 6, bulletWidth = 2, trollPopUpWidth = 25; //šířky bitmap, výšky se přizpůsobí VELIKOST COPTER (20 OG HODNOTA COPTERU)
	private GameObject copter; //VRR kterýmu jsou přiřazovaný (1-3, 4-5), později je 100% zakládaná jako objekt


	public GameView(Context context) {
		super(context);

		//A.S.
		getHolder().addCallback(this);
		thread = new GameThread(getHolder(), this);
		setFocusable(true);
		c = (GameActivity)context;
		// Jednorázově odkáže že v 'GameActivity' se skenují kliky na screen, odkaz nadále zůstává v proměnné 'c'
	}

	private void stackOverflow() {
		stackOverflow();
	} //32offer zlikviduje game a vyhodí title

	//A.S.
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//jednorázové scalnutí všech bitmap na resolution
		trollImagesScaled = new ArrayList<Bitmap>(); //tvorba chytrého pole bitmap offerů

		Bitmap enemyCastle = BitmapFactory.decodeResource(getResources(), R.drawable.red1); //bere skutečný .PNG obrázek do bitmapy, bitmapy se později přidělují objek
		enemyCastleScaled = Bitmap.createScaledBitmap(enemyCastle, castleWidth * displayWidth / 100, (int)(enemyCastle.getHeight() * ((castleWidth * displayWidth / 100.f) / enemyCastle.getWidth())), false); //matematický vzoreček na rescalování

		Bitmap enemyCastleDestroyed = BitmapFactory.decodeResource(getResources(), R.drawable.red2);
		enemyCastleDestroyedScaled = Bitmap.createScaledBitmap(enemyCastleDestroyed, castleWidth * displayWidth / 100, (int)(enemyCastleDestroyed.getHeight() * ((castleWidth * displayWidth / 100.f) / enemyCastleDestroyed.getWidth())), false); // -||-...

		Bitmap friendlyCastle = BitmapFactory.decodeResource(getResources(), R.drawable.green1);
		friendlyCastleScaled = Bitmap.createScaledBitmap(friendlyCastle, castleWidth * displayWidth / 100, (int)(friendlyCastle.getHeight() * ((castleWidth * displayWidth / 100.f) / friendlyCastle.getWidth())), false);

		Bitmap friendlyCastleDestroyed = BitmapFactory.decodeResource(getResources(), R.drawable.green2);
		friendlyCastleDestroyedScaled = Bitmap.createScaledBitmap(friendlyCastleDestroyed, castleWidth * displayWidth / 100, (int)(friendlyCastleDestroyed.getHeight() * ((castleWidth * displayWidth / 100.f) / friendlyCastleDestroyed.getWidth())), false);

		Bitmap bombImage = BitmapFactory.decodeResource(getResources(), R.drawable.bomb);
		bombImageScaled = Bitmap.createScaledBitmap(bombImage, bombWidth * displayWidth / 100, (int)(bombImage.getHeight() * ((bombWidth * displayWidth / 100.f) / bombImage.getWidth())), false);

		Bitmap fireBulletImage = BitmapFactory.decodeResource(getResources(), R.drawable.firebullet);
		fireBulletImageScaled = Bitmap.createScaledBitmap(fireBulletImage, bulletWidth * displayWidth / 100, (int)(fireBulletImage.getHeight() * ((bulletWidth * displayWidth / 100.f) / fireBulletImage.getWidth())), false);

		Bitmap copterImage1 = BitmapFactory.decodeResource(getResources(), R.drawable.copter1);
		copterImage1Scaled = Bitmap.createScaledBitmap(copterImage1, copterWidth * displayWidth / 100, (int)(copterImage1.getHeight() * ((copterWidth * displayWidth / 100.f) / copterImage1.getWidth())), false);

		Bitmap copterImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.copter2);
		copterImage2Scaled = Bitmap.createScaledBitmap(copterImage2, copterWidth * displayWidth / 100, (int)(copterImage2.getHeight() * ((copterWidth * displayWidth / 100.f) / copterImage2.getWidth())), false);

		Bitmap copterImage3 = BitmapFactory.decodeResource(getResources(), R.drawable.copter3);
		copterImage3Scaled = Bitmap.createScaledBitmap(copterImage3, copterWidth * displayWidth / 100, (int)(copterImage3.getHeight() * ((copterWidth * displayWidth / 100.f) / copterImage3.getWidth())), false);

		Bitmap copterImage4 = BitmapFactory.decodeResource(getResources(), R.drawable.copter4);
		copterImage4Scaled = Bitmap.createScaledBitmap(copterImage4, copterWidth * displayWidth / 100, (int)(copterImage1.getHeight() * ((copterWidth * displayWidth / 100.f) / copterImage4.getWidth())), false);

		Bitmap copterImage5 = BitmapFactory.decodeResource(getResources(), R.drawable.copter5);
		copterImage5Scaled = Bitmap.createScaledBitmap(copterImage5, copterWidth * displayWidth / 100, (int)(copterImage2.getHeight() * ((copterWidth * displayWidth / 100.f) / copterImage5.getWidth())), false);

		//{{{ optimalizovaný proces scalování všech offer obrázků
		Bitmap trollPopUp;
		for(int i = 0; i < 14; i++) {
			switch(i) {
				case 0: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.troll); break;
				case 1: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer1); break;
				case 2: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer2); break;
				case 3: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer3); break;
				case 4: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer4); break;
				case 5: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer5); break;
				case 6: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer6); break;
				case 7: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer7); break;
				case 8: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer8); break;
				case 9: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer9); break;
				case 10: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer10); break;
				case 11: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer11); break;
				case 12: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer12); break;
				default: trollPopUp = BitmapFactory.decodeResource(getResources(), R.drawable.offer13);
			}
			trollImagesScaled.add(Bitmap.createScaledBitmap(trollPopUp, trollPopUpWidth * displayWidth / 100, (int)(trollPopUp.getHeight() * ((trollPopUpWidth * displayWidth / 100.f) / trollPopUp.getWidth())), false));
		}
		//}}}

		pauseScreen = new GameObject(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.game_paused), displayWidth, displayHeight, false));
		// neoptimalizovaný pause button
		copter = new GameObject(copterImage1Scaled); //zbytek založení copter objektu
		trollButton = new GameObject(trollImagesScaled.get(0)); //zbytek založení chytrého políčka offerů
		sky.setColor(Color.rgb(0, 128, 255)); //dodatek barevnosti k Paint() oblohy
		grass.setColor(Color.rgb(0, 128, 0)); //dodatek barevnosti k Paint() země a energy baru
		pauseScreen.setPosition(0, 0);
		//3x typy game textů
		texts.setColor(Color.BLACK);
		texts.setTextSize(64);
		texts2.setColor(Color.BLACK);
		texts2.setTextSize(64);
		texts2.setTextAlign(Paint.Align.RIGHT);
		texts3.setColor(Color.WHITE);
		texts3.setTextSize(38);
		texts3.setTextAlign(Paint.Align.LEFT);
		copter.setPosition(displayWidth / 6, 0); //pozice vrtulníku od začátku druhé šestiny z leva
		trollButton.setPosition(3 * displayWidth / 4 - 32, 96); //pozice troll offeru v třetí čtvrtině - 32px
		//A.S.
		thread.setRunning(true);
		thread.start();
		//skutečná realizace chytrých polí
		bombs = new ArrayList<GameObject>();
		friendlyCastles = new ArrayList<GameObject>();
		enemyCastles = new ArrayList<GameObject>();

		SharedPreferences savedScore = c.getSharedPreferences("cz.cuni.pedf.android.masekfilip.fucopter", Context.MODE_PRIVATE); //umožní adresu na uložené best skóre
		previousBest = savedScore.getInt("bestScore", 0); //načtení přesného best

		copterSound = MediaPlayer.create(c, R.raw.copter);
		startSound = MediaPlayer.create(c, R.raw.start);
		levelUpSound = MediaPlayer.create(c, R.raw.levelup);
		bombReleaseSound = MediaPlayer.create(c, R.raw.bomb);
		friendlyFireSound = MediaPlayer.create(c, R.raw.friendlyfire);
		gameOverSound = MediaPlayer.create(c, R.raw.gameover);
		trollSound = MediaPlayer.create(c, R.raw.troll);
		superTuxSound = MediaPlayer.create(c, R.raw.fireflower);
		superTuxShootSound = MediaPlayer.create(c, R.raw.shoot);

		copterSound.setLooping(true);
		startSound.start();
	}


	//A.S.
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				thread.setRunning(false);
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			retry = false;
		}
	}


	public void update() { //updates každý tik
		if(c.gamePaused == true) { //pokus pause
			if(c.touch) { //odpausne při kliknutí
				c.touch = false;
				c.gamePaused = false;
			}
			return;
		}
		if(gameFinished == true) { //helikoptéra status 3
			if(c.touch) c.finish(); //kliknutí zavře hru zpět na MainActivity
			return;
		} // ---------  při hře --------------- //
		frameCounter++;
		if(newRecord == true) {
			previousBest = score; //best se aktualizuje s novým best score každý
		}
		if(fuel <= 75 && trollState == 0) { //stane se jen jednou
			trollState = 1;
			currentTrollImage = -1;
		}
		if((trollState == 1 && frameCounter % 600 == 0 && level < 5) || (trollState == 1 && frameCounter % 480 == 0 && level == 5) || (trollState == 1 && frameCounter % 360 == 0 && level == 6) || (trollState == 1 && frameCounter % 240 == 0 && level == 7)) { //snižující se duration offerů
			currentTrollImage = rand.nextInt(14); //chci náhodný offer po uplatnění/neuplatnění aktuálního
			trollButton.swapImageScaled(trollImagesScaled.get(currentTrollImage)); //náhodný číslo z chytrého pole a odkáže na číslo obrázku
		}
		/// Měnění bitmap vrtulníku pro iluzi letu (a switching offer)
		if(frameCounter == 30) copterSound.start(); //půl sekundy delay se startuje motor vrtulníku
		if(frameCounter % 10 == 0 && stopCopter == false) {
			if((currentCopterImage == 1) && (copterSwitched == false)) { //střídání obrázků vrtulníků
				currentCopterImage = 2;
				copter.swapImageScaled(copterImage2Scaled);
			} else if((currentCopterImage == 1) && (copterSwitched == true))  { //střídání obrázků vrtulníků
				currentCopterImage = 2;
				copter.swapImageScaled(copterImage4Scaled);
			} else if(copterSwitched == false) {
				currentCopterImage = 1;
				copter.swapImageScaled(copterImage1Scaled);
			} else {
				currentCopterImage = 1;
				copter.swapImageScaled(copterImage5Scaled);
			}
		}
		if(gameOver == true) { //****
			//přistává vrtulník
			if(copter.getPositionY() + copter.getHeight() < (6 * displayHeight / 7)) { //jestliže vrtulník ještě není na zemi
				copter.setPosition(copter.getPositionX() + copter.getWidth() / 70, copter.getPositionY() + copter.getHeight() / 40); //...pokračuje v přistávání
			} else {
				stopCopter = true;
				copter.swapImageScaled(copterImage3Scaled); //zastavené vrtule
				copterSound.stop(); //zastaví zvuk copteru
				//přenastavení předešlích stylů textů pro sedící styly na final skóre
				texts.setTextAlign(Paint.Align.CENTER);
				texts2.setTextSize(92);
				texts2.setTextAlign(Paint.Align.CENTER);
				texts3.setColor(Color.RED);
				texts3.setTextSize(72);
				texts3.setTextAlign(Paint.Align.CENTER);

				if(score == previousBest && !(trollState == 2 && redCastleWave > 0)) { //deklarace proměnné editor umožňující měnit blok trvalé paměti
					SharedPreferences savedScore = c.getSharedPreferences("cz.cuni.pedf.android.masekfilip.fucopter", Context.MODE_PRIVATE); //1 uložení nového Best (když je best)
					SharedPreferences.Editor editor = savedScore.edit(); //2 A.S. třída.třída proměnná
					editor.putInt("bestScore", score); //3
					editor.apply(); //vražení plánů 1,2,3 najednou do bloku dlouhodobé paměti
				}
				gameFinished = true;
				c.touch = false; //zrovna není prst na obrazovce
				return;
			}
		}
		if(frameCounter % 1800 == 0 && level < 7) { //co 30 sekund to level up do sedmičky
			level++;
			levelUpSound.start();
		}
		if((
				//jak rychle palivo ubývá každý level
			(frameCounter % 60 == 0 && level == 1) ||
			(frameCounter % 50 == 0 && level == 2) ||
			(frameCounter % 40 == 0 && level == 3) ||
			(frameCounter % 30 == 0 && level == 4) ||
			(frameCounter % 26 == 0 && level == 5) ||
			(frameCounter % 11 == 0 && level == 6) ||
			(frameCounter % 14 == 0 && level >  6)) && gameOver == false) {
			fuel--;
			//palivo gone case
			if(fuel <= 0) {
				fuel = 0;
				gameOverSound.start();
				gameOver = true; //faktor, při kterém vrtulník začne přistávat ****
				nextFriendlyCastle = 0;
				nextEnemyCastle = 0;
			}
		}
		if(c.touch && gameOver == false) {
			if(trollState == 1 &&
					//jestli se kliklo na offer hitbox
					currentTrollImage >= 0 &&
					c.touchX > trollButton.getPositionX() &&
					c.touchX < trollButton.getPositionX() + trollButton.getWidth() &&
					c.touchY > trollButton.getPositionY() &&
					c.touchY < trollButton.getPositionY() + trollButton.getHeight()) {
				if(currentTrollImage == 4) {
					currentTrollImage = rand.nextInt(13); //GAMBLING OFFER :7
				}
				if(currentTrollImage == 2) {
					currentTrollImage = -1;
					fuel = fuel - 45;
					if(fuel < 6) fuel = 6; //FREEEE energy ubere
				} else if(currentTrollImage == 3) { // + palivo
					fuel = fuel + 45;
					if(fuel > 100) fuel = 100;
					currentTrollImage = -1;
				} else if(currentTrollImage == 11) {
					currentTrollImage = -1; //jen zmizí
				} else if(currentTrollImage == 7) {
					currentTrollImage = -1;
					c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fcoc-vs-battles.fandom.com/wiki/FU_(Froggy_Universe)"))); //redirect na www.link
				} else if(currentTrollImage == 8) {
					currentTrollImage = -1;
					c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fcoc-vs-battles.fandom.com/wiki/Veneficaverse"))); //redirect na www.link
				} else if(currentTrollImage == 12) {
					currentTrollImage = -1;
					bombsAI = 30; /// FREE BOMBS DURATION
				} else if(currentTrollImage == 6) {
					stackOverflow(); //crash hry
				} else if(currentTrollImage == 9) {
					currentTrollImage = -1;
					fireBullets = fireBullets + 8; /// Tuxí projectiles
					superTuxSound.start(); //tututuTUu
				} else if(currentTrollImage == 5) { //snížení/zvýšení levelu offer
					currentTrollImage = -1;
					if(level < 7) {
						level = 7;
						levelUpSound.start();
					} else level = 5;
				} else if(currentTrollImage == 13) {
					currentTrollImage = -1;
					copterSwitched = !copterSwitched; //swapne copter
				} else { //MAIN TROLL
					trollState = 2;
					trollSound.start();
					nextFriendlyCastle = frameCounter + 1;
					nextEnemyCastle = 0;
				}
				c.touch = false; //vyrušení kliknutí pro možnost detekce dalšího
			} else {
				fuel = fuel - level; //FUEL BALANCING POINT 7 end
				c.touch = false;
				GameObject bomb; //každý klik mimo offer vytvoří GameObject bomby
				if(fireBullets == 0) { //FU energy bombs
					bombReleaseSound.seekTo(0);
					bombReleaseSound.start(); //zvuk házení drzz
					bomb = new GameObject(bombImageScaled); //dotvorba objektu bomby
					bomb.setPosition(copter.getPositionX() + copter.getWidth() / 2 - bomb.getWidth() / 4, copter.getPositionY() + copter.getHeight() / 2); //souřadnice odkud padají bomby
				} else { //Tuxí kulky
					superTuxShootSound.seekTo(0);
					superTuxShootSound.start();
					fireBullets--;
					bomb = new GameObject(fireBulletImageScaled);
					bomb.setPosition(copter.getPositionX() + copter.getWidth() / 2 + bomb.getWidth() / 4, copter.getPositionY() + copter.getHeight() / 2); //souřadnice pro tuxí kulky
				}
				bombs.add(bomb); //vytvořená bomba se přidá do <array> chytrého pole aktivně zobrazených bomb (bez řešení prvních/posledních/navazujících)
			}
		}
		if(bombsAI > 0 && frameCounter % 15 == 0 && gameOver == false) { //duplicita generace FU enrgy bomb
			bombsAI--;
			bombReleaseSound.seekTo(0);
			bombReleaseSound.start();
			GameObject bomb = new GameObject(bombImageScaled);
			bomb.setPosition(copter.getPositionX() + copter.getWidth() / 2 - bomb.getWidth() / 4, copter.getPositionY() + copter.getHeight() / 2);
			bombs.add(bomb);
		}

		//trails hradů po levelu 5
		/*
		if (trollState < 2 && level == 5 && frameCounter % 450 == 0) { //1800 celý level, 450 čtvrtina
			nextFriendlyCastle = frameCounter + 1; //zel hrady krátký spam

			if (trollState < 2 && level == 5 && frameCounter % 580 == 0) {
				nextFriendlyCastle = frameCounter + 70 / level + rand.nextInt(70 / level); //default rising spawn zel hradů
			}
		}
		*/
		//zahájení next wave of red castles in 'survive levels'
		if (level == 5 && frameCounter % 1000 == 0) {
			redCastleWave = 12;
		}

		if (level == 7 && frameCounter % 580 == 0) {
			redCastleWave = 6;
		}

		//MAIN BALANCING
		if(frameCounter == nextEnemyCastle) {
			GameObject castle = new GameObject(enemyCastleScaled);
			castle.setPosition(displayWidth, 3 * displayHeight / 4 - castle.getHeight() / 2);
			if(trollState < 2) { //dokud nás offer nevytrollil, budou se generovat
				switch(level) {
					case 1:
						//DALŠÍ ČERVENÝ HRAD   ZA JAK DLOUHO DALŠÍ (MÍŇ = VÍC HRADŮ)  ANTILUCK GENERATION
						nextEnemyCastle = frameCounter + 35 + rand.nextInt(35);
						break;
					case 2:
						nextEnemyCastle = frameCounter + 23 + rand.nextInt(23);
						break;
					case 3:
						nextEnemyCastle = frameCounter + 48 + rand.nextInt(42);
						break;
					case 4:
						nextEnemyCastle = frameCounter + 5 + rand.nextInt(10);

						break;
					case 5:
						if(redCastleWave > 0) {
							nextEnemyCastle = frameCounter + 1 + rand.nextInt(10);
							redCastleWave = redCastleWave - 1;
							break;
						}
						nextEnemyCastle = frameCounter + 32 + rand.nextInt(46);
						break;
					case 6:
						nextEnemyCastle = frameCounter + 4 + rand.nextInt(5);
						break;
					case 7:
						if(redCastleWave > 0) {
							nextEnemyCastle = frameCounter + 1 + rand.nextInt(10);
							redCastleWave = redCastleWave - 1;
							break;
						}
						nextEnemyCastle = frameCounter + 8 + rand.nextInt(34);
						break;
				}
				/* OG BALANCING DOWN*/
				//nextEnemyCastle = frameCounter + 70 / (8 - level) + rand.nextInt(70 / (8 - level));
			}
			enemyCastles.add(castle); //steně jak u bomb, hrad se přidá do chytrého pole momentálně zobrazených
		}
		if(frameCounter == nextFriendlyCastle) {
			GameObject castle = new GameObject(friendlyCastleScaled);
			castle.setPosition(displayWidth, 3 * displayHeight / 4 - castle.getHeight() / 2);
			if(trollState < 2) {
				/*
				switch(level) {
					case 1:
						nextFriendlyCastle = frameCounter + 35 + rand.nextInt(35);
						break;
					case 2:
						nextFriendlyCastle = frameCounter + 23 + rand.nextInt(23);
						break;
					case 3:
						nextFriendlyCastle = frameCounter + 14 + rand.nextInt(14);
						break;
					case 4:
						nextFriendlyCastle = frameCounter + 10 + rand.nextInt(10);
						break;
					case 5:
						nextFriendlyCastle = frameCounter + 7 + rand.nextInt(7);
						break;
					case 6:
						nextFriendlyCastle = frameCounter + 5 + rand.nextInt(5);
						break;
					case 7:
						nextFriendlyCastle = frameCounter + 1 + rand.nextInt(5);
						break;
				}
				*/
				nextFriendlyCastle = frameCounter + 70 / level + rand.nextInt(70 / level); //default rising spawn zel hradů
			} else {
				nextFriendlyCastle = frameCounter + 1 + rand.nextInt(20);
			}
			if(redCastleWave > 0)  {
			} else {
				friendlyCastles.add(castle);
			}
		}
		//POHYBUJE S CER HRADY (zmenšováním x souřadnice)
		for(int i = 0; i < enemyCastles.size(); i++) {
			GameObject castle = enemyCastles.get(i);
			castle.setPosition(castle.getPositionX() - level * (castle.getWidth() / 40), castle.getPositionY());
			if(castle.getPositionX() < -castle.getWidth()) enemyCastles.remove(i);
		}
		//POHYBUJE S ZEL HRADY (zmenšováním x souřadnice)
		for(int i = 0; i < friendlyCastles.size(); i++) {
			GameObject castle = friendlyCastles.get(i);
			castle.setPosition(castle.getPositionX() - level * (castle.getWidth() / 40), castle.getPositionY());
			if(castle.getPositionX() < -castle.getWidth()) friendlyCastles.remove(i);
		}
		for(int i = 0; i < bombs.size(); i++) { //projde všechny bomby v chytrém poli
			GameObject bomb = bombs.get(i);
			boolean removeBomb = false;

			// Rozlišují se FU energy bomby a Tuxí bomby mini-pixelovýn rozdílem X
			if(bomb.getPositionX() < (copter.getPositionX() + copter.getWidth() / 2)) {
				bomb.setPosition(bomb.getPositionX(), bomb.getPositionY() + bomb.getHeight() / 6);
			} else {
				bomb.setPosition(bomb.getPositionX(), bomb.getPositionY() + bomb.getHeight() / 2);
			}

			// Bomba vztoupila do zóny hradů
			if((bomb.getPositionY() + bomb.getHeight()) >= (3 * displayHeight / 4) && bomb.getPositionY() < 6 * displayHeight / 7) {
				int effectiveX = bomb.getPositionX() + bomb.getWidth() / 2; //x souřadnice prostředku bomby
				for(GameObject castle : enemyCastles) { //projde všechny červený hrady
					if(castle.destroyed == false) {
						if(castle.getPositionX() < effectiveX && (castle.getPositionX() + castle.getWidth()) > effectiveX) { //zásah plochy hradu
							castle.destroyed = true; //hrad hoří
							castle.swapImageScaled(enemyCastleDestroyedScaled); //set obrázek hrad hoří
							score = score + level * 10; //skóre +
							if(score > previousBest && newRecord == false) { //pokud nové best, nové best
								newRecord = true;
							}
							fuel = fuel + 2 * level; //přidání paliva za červený hrad (2 * level)
							if(fuel > 100) fuel = 100; //zarovná na 100 (101% a víc energie nesmí být)
							removeBomb = true; //uloží se chtíč smazat bombu ####
						}
					}
				}
				for(GameObject castle : friendlyCastles) { //projde všechny zelený hrady
					if(castle.destroyed == false) {
						if(castle.getPositionX() < effectiveX && (castle.getPositionX() + castle.getWidth()) > effectiveX) {
							friendlyFireSound.seekTo(0); //zelený hrad crash
							friendlyFireSound.start();
							castle.destroyed = true; //hrad hoří
							castle.swapImageScaled(friendlyCastleDestroyedScaled); //hrad hoří image
							score = score - level * 20; //skóre -
							fuel = fuel - 3 * level;
							if(fuel < 0) fuel = 0;
							removeBomb = true; //uloží se chtíč smazat bombu ####
						}
					}
				}
			}
			if(bomb.getPositionY() > displayHeight || removeBomb == true) bombs.remove(i); //bomba se smaže (propadla pod obrazovku nebo se přečte chtíč jí smazat) ####
		}
	}

	// VÝKRES OBJEKTŮ
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas); //volá metodu svého rodiče o vykreslení
		if (canvas != null) { //A.S.
			canvas.drawRect(0, 0, displayWidth, displayHeight, sky); //všude vykresli modrou, první zapsané pro zadní vrstvu
			canvas.drawRect(0, displayHeight - displayHeight / 4, displayWidth, displayHeight, grass); //grass pruh 1/4

			//každej tik hry se vykreslí červené. zelené hrady a bomby
			for(GameObject castle : enemyCastles) castle.draw(canvas);
			for(GameObject castle : friendlyCastles) castle.draw(canvas);
			for(GameObject bomb : bombs) bomb.draw(canvas);

			copter.draw(canvas);
			if(stopCopter == false) { //dokud vrtulník letí, zobrazují se score/best/level/energy stat TextViews
				canvas.drawText("Score: " + score, 32, 64, texts);
				canvas.drawText("Best: " + previousBest, 32, 104, texts3);
				canvas.drawText("Level: " + level, 32, 156, texts);
				canvas.drawText("Energy: ", 3 * displayWidth /4, 64, texts2);
				if(fuel > 0) {
					canvas.drawRect(3 * displayWidth / 4, 24, (19 * displayWidth / 20) - ((101 - fuel) * displayWidth / 500), 64, grass); //100/10 difficulty vzorec pro počet s barem
				}
				if(trollState == 1 && currentTrollImage != -1) {
					trollButton.draw(canvas);
				}
			} else { //pokud už vrtulník neletí, vykresluje game over TextViews
				if (redCastleWave > 0 && trollState == 2) {
					score = score + 2000;
					canvas.drawText("You Won! (all castles are gone - secret ending)", displayWidth / 2, displayHeight / 5, texts2);
				} else {
					canvas.drawText("Game Over!", displayWidth / 2, displayHeight / 5, texts2);
				}
				canvas.drawText("Your score: " + score, displayWidth / 2, displayHeight / 3, texts);
				if(newRecord == true) {
					canvas.drawText("New Record!", displayWidth / 2, displayHeight / 2, texts3);
				}
			}
			if(c.gamePaused) pauseScreen.draw(canvas); //při pause game se přes celou obrazovku vykreslí pause screen
		}
	}
}


/* OG BALANCING

switch(level) {
					case 1:
						//DALŠÍ ČERVENÝ HRAD      MIN ZA FRAMŮ       MAX ZA FRAMŮ (s frameCounter)
						nextEnemyCastle = frameCounter + 35 + rand.nextInt(35);
						break;
					case 2:
						nextEnemyCastle = frameCounter + 23 + rand.nextInt(23);
						break;
					case 3:
						nextEnemyCastle = frameCounter + 14 + rand.nextInt(14);
						break;
					case 4:
						nextEnemyCastle = frameCounter + 10 + rand.nextInt(10);
						break;
					case 5:
						nextEnemyCastle = frameCounter + 7 + rand.nextInt(7);
						break;
					case 6:
						nextEnemyCastle = frameCounter + 5 + rand.nextInt(5);
						break;
					case 7:
						nextEnemyCastle = frameCounter + 1 + rand.nextInt(5);
						break;
				}
 */