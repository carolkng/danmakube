package com.carolkng.danmakube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.PI;

public class GameScreen implements Screen {
    final Danmakube game;

    private final int NUM_BULLETS;
    private final int BULLET_MAX_ANGLE;
    private final int BULLET_SPEED = 1;
    private final int BOSS_RIGHT = 1;
    private final int BOSS_LEFT = -1;

    private TextureAtlas textureAtlas;

    private Animation<TextureRegion> shipAnimation;
    private Animation<TextureRegion> bossAnimation;
    private TextureRegion bulletImage;

    private float elapsedTime = 0;

    private Sound shipSound;
    private Sound bulletSound;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private Rectangle ship;
    private Rectangle bossShip;
    private Vector2 bossShipVel;
    private boolean bossShipInit;

    private Array<BulletRectangle> activeBullets = new Array<BulletRectangle>();
//    private final Array<Bullet> activeBullets = new Array<Bullet>();
//    private final Pool<Bullet> bulletPool = new Pool<Bullet>() {
//        @Override
//        protected Bullet newObject() {
//            return new Bullet();
//        }
//    };

    private long bulletTime;
    private int shipLives;

    // Initialized once to prevent reassignment
//    private Bullet newBullet;

    // Initialized once to prevent reassigning every time the screen is touched
    private Vector3 touchPos;

    public GameScreen(final Danmakube danmakube) {
        this.game = danmakube;

        NUM_BULLETS = 9;
        BULLET_MAX_ANGLE = 90;
        // HACK: Bootstrapped temporary list object since Arrays.asList can't return list of float
        java.util.List<Float> temp_velocities = new ArrayList<Float>();
        for (int bullet_i = 0; bullet_i < NUM_BULLETS; bullet_i++) {
            temp_velocities.add(
                    (float)Math.sin(bullet_i * (BULLET_MAX_ANGLE/(NUM_BULLETS-1)) * PI/180));
        }
        textureAtlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));

        TextureRegion[] shipFrames = new TextureRegion[2];
        TextureRegion[] bossFrames = new TextureRegion[2];

        shipFrames[0] = (textureAtlas.findRegion("ship1_on"));
        shipFrames[1] = (textureAtlas.findRegion("ship1_off"));
        bossFrames[0] = (textureAtlas.findRegion("boss_on"));
        bossFrames[1] = (textureAtlas.findRegion("boss_off"));

        shipAnimation = new Animation<TextureRegion>(0.1f, shipFrames);
        bossAnimation = new Animation<TextureRegion>(0.1f, bossFrames);
        bulletImage = textureAtlas.findRegion("ship3_off");
        bulletImage.setRegionHeight(20);
        bulletImage.setRegionWidth(20);

        bulletSound = Gdx.audio.newSound(Gdx.files.internal("bullet.wav"));
        shipSound = Gdx.audio.newSound(Gdx.files.internal("ship.wav"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("bgm.mp3"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.WIDTH, game.HEIGHT);

        ship = new Rectangle();
        ship.width = 64;
        ship.height = 64;
        ship.x = game.WIDTH/2 - ship.width/2;
        ship.y = 20;

        bossShip = new Rectangle();
        bossShip.width = 64;
        bossShip.height = 64;
        bossShip.x = game.WIDTH/2 - bossShip.width/2;
        bossShip.y = game.HEIGHT + 100; // Starts above the game screen

        bossShipVel = new Vector2();
        bossShipVel.set(0, -1);

        bossShipInit = true;

        shipLives = 3;

        touchPos = new Vector3();

    }

    @Override
    public void show() {
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        // OpenGL rendering
        Gdx.gl.glClearColor(0, 0, 0.2f, 1); // Sets clear color to blue, RGBA format
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);   // Actually clear the screen

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        elapsedTime += Gdx.graphics.getDeltaTime();
        game.batch.draw(shipAnimation.getKeyFrame(elapsedTime, true), ship.x - ship.width/2, ship.y);
        game.batch.draw(bossAnimation.getKeyFrame(elapsedTime, true), bossShip.x - bossShip.width/2, bossShip.y);
        for (BulletRectangle bullet : activeBullets) {
            game.batch.draw(bulletImage, bullet.x - bulletImage.getRegionWidth(), bullet.y - bulletImage.getRegionHeight());
        }

        game.font.draw(game.batch, "LIVES: " + shipLives, game.WIDTH - 100, game.HEIGHT - 250);
        game.batch.end();

        // Game logic:
        if (bossShipInit && bossShip.y < (game.HEIGHT - bossShip.height - 20)) {
            bossShipVel.set(BOSS_LEFT, 0);
            bossShipInit = false;
        }

        if (bossShip.x > game.WIDTH - bossShip.width) {
            bossShipVel.x = BOSS_LEFT;
        } else if (bossShip.x < 0) {
            bossShipVel.x = BOSS_RIGHT;
        }

        bossShip.x += bossShipVel.x;
        bossShip.y += bossShipVel.y;

        if (Math.abs(bulletTime - TimeUtils.nanoTime()) > 1000000000) {
            spawnBullets();
        }

//        if (Math.abs(bulletTime - TimeUtils.nanoTime()) > 500000000) {
//            for (int i = 0; i < NUM_BULLETS; i++) {
//                newBullet = bulletPool.obtain();
//                newBullet.init(bossShip.x, bossShip.y, 5, -90 + BULLET_MAX_ANGLE/2 + i * BULLET_MAX_ANGLE/(NUM_BULLETS-1), "degrees");
//                activeBullets.add(newBullet);
//            }
//            bulletTime = TimeUtils.nanoTime();
//        }

        // Object position updates
        Iterator<BulletRectangle> iter = activeBullets.iterator();
        while(iter.hasNext()) {
            BulletRectangle bullet = iter.next();
            bullet.setPosition(bullet.getPosition().add(bullet.velocity));
            if (bullet.y + bullet.height < 0) iter.remove();
            if (ship.overlaps(bullet)) {
                bulletSound.play();
                shipLives--;
                iter.remove();
            }
        }

        if (ship.x < 0)
            ship.x = 0;
        if (ship.x > game.WIDTH - ship.width)
            ship.x = game.WIDTH - ship.width;

        // User input:
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(),0);
            camera.unproject(touchPos);
            ship.x = touchPos.x - ship.width/2;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
            ship.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            ship.x += 200 * Gdx.graphics.getDeltaTime();

//        if (shipLives < 0) {
//            game.setScreen(new MainMenuScreen(game));
//            dispose();
//        }
    }

    private void spawnBullets() {
        for (int i = 0; i < NUM_BULLETS; i++) {
            BulletRectangle newBullet = new BulletRectangle();
            newBullet.width = 20;
            newBullet.height = 20;
            newBullet.x = bossShip.x - newBullet.width/2;
            newBullet.y = bossShip.y;
            newBullet.setVelocity(BULLET_SPEED, -90 - BULLET_MAX_ANGLE/2 + i * BULLET_MAX_ANGLE/(NUM_BULLETS - 1), "degrees");
            activeBullets.add(newBullet);
        }
        bulletTime = TimeUtils.nanoTime();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        textureAtlas.dispose();
        shipSound.dispose();
        bulletSound.dispose();
        backgroundMusic.dispose();

        game.dispose();
    }
}
