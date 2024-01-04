/**
 * Akari Shooting Game
 * @version 1.0.0
 * @License MIT License (c) 2023 Takahashi Akari
 * @Author Takahashi Akari
 * @since 2023/12/22
 */
/*
 * JavaのSwingを使ってシューティングゲームを作ります。画面は、タイトル画面、ステージ1～6、ゲームオーバー画面、ゲームクリアー画面、ハイスコア表示画面があります。タイトル画面は、New Gameと、Score Rankingが選択できます。ハイスコアは、ファイルに保存されます。NewGameをクリックをすると、Stage 1と表示され、ステージ1が始まります。自機と敵機、ミサイルは、それぞれ画像として用意されています。敵は、7×3(横×縦)ぐらいの数でミサイルを飛ばしながら左右に往復して下に下がってきます。下には自機があり、左右ボタンで移動できます。スペースを押すとミサイルを打つことができます。すべての敵機を倒すと、ボスが出てきて、それを倒すと、ステージクリアで、次のステージまでいきます。ステージ6をクリアすると全クリアです。5回、自機がやられるとゲームオーバーです。ハイスコアは5位まで表示されます。スコアと残り自機数はゲーム中表示されています。敵機は倒されるとアイテムを落とすことがあり、自機数アップ、スコアアップ、移動スピードアップ、ミサイルの弾数アップなどがあります。敵機が下までくると、ゲームオーバーです。早く倒せば倒すほどスコアが上がりやすくなります。
 */

 // 画像からそれぞれの横幅・縦幅を取得する

import javax.swing.JFrame;
import javax.swing.Timer;

import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

enum GameState {
    TITLE,
    STAGE,
    GAME_OVER,
    GAME_CLEAR,
    HIGH_SCORE
}

enum ImageKey {
    PLAYER,
    PLAYER_BANG,
    PLAYER_MISSILE,
    ENEMY1,
    ENEMY2,
    ENEMY3,
    ENEMY4,
    ENEMY5,
    ENEMY6,
    ENEMY7,
    ENEMY8,
    ENEMY9,
    ENEMY10,
    ENEMY11,
    ENEMY_BANG,
    ENEMY_MISSILE,
    ITEM1,
    ITEM2,
    ITEM3,
    ITEM4,
    BOSS1,
    BOSS2,
    BOSS3,
    BOSS4,
    BOSS5,
    BOSS6,
    BOSS_BANG
}

// struct 星
class Star {
    private int x, y;
    private int speed = 5;
    private Color color;
    private int interval = 4;

    public Star(int startX, int startY, int speed) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        // colorは灰色でspeedが速いほど白に近づく
        this.color = new Color(200 - (10 - speed) * 10, 200 - (10 - speed) * 10, 200 - (10 - speed) * 10);
    }

    public void move() {
        if (interval == 0) {
            y += speed;
            interval = 4;
        } else {
            interval--;
        }
        if (y > Constants.SCREEN_HEIGHT) {
            y = 0;
        }
    }

    // 描画処理などその他のメソッド
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    // getSpeed
    public int getSpeed() {
        return speed;
    }
}

class Constants {
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    public static int missileFinalTime = (int) System.currentTimeMillis();
    public static int missileInterval = 1000;
    public static boolean missileStart = true;
    public static boolean isGameOver = false;
    public static boolean isGameClear = false;
    public static int stage = 1;
}

public class Game extends JFrame {
    private Timer timer;
    public Screen screen;


    public Game() {
        initUI();
    }

    private void initUI() {
        setTitle("Akari Shooting Game");
        setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // ウィンドウを画面中央に配置

        screen = new TitleScreen(this);
        // 背景は黒
        screen.setBackground(Color.BLACK);
        getContentPane().add(screen);


        // ゲームループの設定
        timer = new Timer(20, e -> gameLoop());
        timer.start();
    }

    private void gameLoop() {
        // ゲーム状態の更新と画面の再描画
        // StageScreenを表示している場合は、StageScreenのupdate()とrender()を呼び出す
        screen.update();
        screen.render();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            Game ex = new Game();
            ex.setVisible(true);
        });
    }

    // 画面の切り替え
    public void setScreen(Screen screen) {
        // 現在表示している画面を破棄
        getContentPane().removeAll();

        // 新しい画面を設定
        getContentPane().add(screen);

        // 新しい画面の初期化処理
        screen.init();

        // 画面の再描画
        repaint();
    }

}

abstract class Screen extends JPanel {
    protected Game game;
    // 星を100個生成
    protected List<Star> stars = new ArrayList<>();
    {
        for (int i = 0; i < 50; i++) {
            int x = (int) (Math.random() * Constants.SCREEN_WIDTH);
            int y = (int) (Math.random() * Constants.SCREEN_HEIGHT);
            int speed = (int) (Math.random() * 10) + 1;
            stars.add(new Star(x, y, speed));
        }
    }

    public Screen(Game game) {
        this.game = game;
    }

    public abstract void update(); // 画面の更新
    public abstract void render(); // 画面の描画

    public void init() {
        // 画面の初期化処理
    }
}
class TitleScreen extends Screen {
    private InputHandler inputHandler;
    private int arrow = 0;
    private boolean isArrowPressed = false;

    public TitleScreen(Game game) {
        super(game);
        Constants.isGameOver = false;
        Constants.isGameClear = false;
        inputHandler = new InputHandler();
        game.addKeyListener(inputHandler);
    }
    public void update() {
        Constants.isGameOver = false;
        Constants.isGameClear = false;
        // タイトル画面の更新処理
        // 右矢印キーを押したらarrowを1増やす
        if (inputHandler.isRightPressed()) {
            arrow++;
            if (arrow > 1) {
                arrow = 1;
            }
            this.isArrowPressed = true;
        }
        // 左矢印キーを押したらarrowを1減らす
        else if (inputHandler.isLeftPressed()) {
            arrow--;
            if (arrow < 0) {
                arrow = 0;
            }
            this.isArrowPressed = true;
        }

        else {
            this.isArrowPressed = false;
        }

        // 星を移動
        stars.forEach(Star::move);

        if (inputHandler.isFirePressed() && !this.isArrowPressed) {
            if (arrow == 0) {
                // New Gameを選択したらステージ画面へ
                game.screen = new StageScreen(game);
                game.setScreen(game.screen);
                game.screen.setBackground(Color.BLACK);
                game.getContentPane().add(game.screen);
                // thisを破棄
                game.remove(this);
            } else if (arrow == 1) {
                // Score Rankingを選択したらハイスコア画面へ
                game.screen = new HighScoreScreen(game);
                game.setScreen(game.screen);
                game.screen.setBackground(Color.BLACK);
                game.getContentPane().add(game.screen);
                // thisを破棄
                game.remove(this);
            }
            this.isArrowPressed = true;
        }
    }
    public void render() {
        // タイトル画面の描画処理
        paint(game.getGraphics());
    }

    // paint
    public void paint(Graphics g) {
        // 画面を黒で塗りつぶす
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // 星の描画
        stars.forEach(star -> {
            g.setColor(star.getColor());
            g.fillRect(star.getX(), star.getY(), 1, 1);
        });

        // 白文字で「New Game」と「Score Ranking」を表示
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString("New Game", 400, 300);
        g.drawString("Score Ranking", 400, 400);
        // 右矢印で選択中の項目を表示
        if (arrow == 0) {
            g.drawString(">", 350, 300);
        } else if (arrow == 1) {
            g.drawString(">", 350, 400);
        }

        // タイトルを白文字で表示
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(50f));
        g.drawString("Akari Shooting Game", 200, 200);
    }
}
class StageScreen extends Screen {
    Player player;
    List<Enemy> enemies = new ArrayList<>();
    List<Missile> missiles = new ArrayList<>();
    List<Item> items = new ArrayList<>();
    Boss boss;
    ScoreManager scoreManager;
    ImageLoader imageLoader;
    InputHandler inputHandler;
    List<EnemyMissile> enemyMissiles = new ArrayList<>();
    int time;
    int displayStage = 50;
    int displayGameOver = 50;
    int displayGameClear = 50;

    public StageScreen(Game game) {
        super(game);
        scoreManager = new ScoreManager();
        imageLoader = new ImageLoader();
        inputHandler = new InputHandler();
        player = new Player(Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT - imageLoader.getImageHeight(ImageKey.PLAYER) - 10);
        player.setWidth(imageLoader.getImageWidth(ImageKey.PLAYER));
        player.setHeight(imageLoader.getImageHeight(ImageKey.PLAYER));
        game.addKeyListener(inputHandler);

        // 敵機の初期化
        switch (Constants.stage) {
            case 1:
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 2; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY1;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY2;
                    }
                    Enemy enemy = new Enemy(100 + i * 100, 100 + j * 100, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
            break;
            case 2:
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 2; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY3;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY4;
                    }
                    Enemy enemy = new Enemy(100 + i * 100, 100 + j * 100, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
            break;
            case 3:
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 3; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY5;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY6;
                    } else if (j == 2) {
                        key = ImageKey.ENEMY7;
                    }
                    Enemy enemy = new Enemy(100 + i * 100, 100 + j * 100, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
            break;
            case 4:
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 3; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY8;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY9;
                    } else if (j == 2) {
                        key = ImageKey.ENEMY10;
                    }
                    Enemy enemy = new Enemy(90 + i * 90, 90 + j * 90, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
            break;
            case 5:
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 4; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY11;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY8;
                    } else if (j == 2) {
                        key = ImageKey.ENEMY9;
                    } else if (j == 3) {
                        key = ImageKey.ENEMY10;
                    }
                    Enemy enemy = new Enemy(90 + i * 90, 90 + j * 90, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
            break;
            case 6:
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 5; j++) {
                    ImageKey key = null;
                    if (j == 0) {
                        key = ImageKey.ENEMY11;
                    } else if (j == 1) {
                        key = ImageKey.ENEMY8;
                    } else if (j == 2) {
                        key = ImageKey.ENEMY9;
                    } else if (j == 3) {
                        key = ImageKey.ENEMY10;
                    } else if (j == 4) {
                        key = ImageKey.ENEMY11;
                    }
                    Enemy enemy = new Enemy(90 + i * 90, 90 + j * 90, key);
                    enemy.setWidth(imageLoader.getImageWidth(key));
                    enemy.setHeight(imageLoader.getImageHeight(key));
                    enemies.add(enemy);
                }
            }
        }

        // ボス機の初期化
        // boss = new Boss(Constants.SCREEN_WIDTH / 2, 100);

        // アイテムの初期化
        // Item item = new Item(100, 100, ItemType.SCORE_UP);
        
        // time is now millisecobd
        time = (int) System.currentTimeMillis();
    }
    public void update() {
        if (displayStage > 0) {
            displayStage--;
            return;
        }
        if (Constants.isGameOver) {
            if (displayGameOver > 0) {
                displayGameOver--;
                return;
            }
            // spaceを押したらタイトル画面へ
            if (inputHandler.isFirePressed()) {
                game.screen = new TitleScreen(game);
                game.setScreen(game.screen);
                game.screen.setBackground(Color.BLACK);
                game.getContentPane().add(game.screen);
                // thisを破棄
                game.remove(this);
            }
        }
        if (Constants.isGameClear) {
            if (displayGameClear > 0) {
                displayGameClear--;
                return;
            }
            // spaceを押したらタイトル画面へ
            if (inputHandler.isFirePressed()) {
                game.screen = new TitleScreen(game);
                game.setScreen(game.screen);
                game.screen.setBackground(Color.BLACK);
                game.getContentPane().add(game.screen);
                // thisを破棄
                game.remove(this);
            }
        }
        // ゲームの状態を更新
        if (player.isAlive()) {
            if (inputHandler.isLeftPressed()) {
                if (player.getX() > 0) {
                    player.moveLeft();
                }
            }
            if (inputHandler.isRightPressed()) {
                if (player.getX() < Constants.SCREEN_WIDTH - player.getWidth()) {
                    player.moveRight();
                }
            }
            if (inputHandler.isFirePressed()) {
                if (Constants.missileStart || Math.abs(Constants.missileFinalTime - (int) System.currentTimeMillis()) > Constants.missileInterval) {
                    Missile missile = new Missile(player.getX() + player.getWidth() / 2, player.getY());
                    missile.setWidth(imageLoader.getImageWidth(ImageKey.PLAYER_MISSILE));
                    missile.setHeight(imageLoader.getImageHeight(ImageKey.PLAYER_MISSILE));
                    missiles.add(missile);
                    Constants.missileFinalTime = (int) System.currentTimeMillis();
                    Constants.missileStart = false;
                }
            }
        }

        // アイテムをランダムに生成
        if (Math.random() < 0.004) {
            int x = (int) (Math.random() * Constants.SCREEN_WIDTH);
            int type = (int) (Math.random() * 4);
            ImageKey key = null;
            ItemType itemType = null;
            switch(type) {
                case 0:
                    itemType = ItemType.SCORE_UP;
                    key = ImageKey.ITEM1;
                    break;
                case 1:
                    itemType = ItemType.LIFE_UP;
                    key = ImageKey.ITEM2;
                    break;
                case 2:
                    itemType = ItemType.SPEED_UP;
                    key = ImageKey.ITEM3;
                    break;
                case 3:
                    itemType = ItemType.MISSILE_UPGRADE;
                    key = ImageKey.ITEM4;
                    break;
            }
            int y = -imageLoader.getImageHeight(key);
            items.add(new Item(x, y, itemType));
        }

        // アイテムを上から下に移動
        items.forEach(item -> item.setY(item.getY() + item.getSpeed()));

        // 画面外に出たミサイルを削除
        missiles.removeIf(missile -> !missile.isVisible());

        // ミサイルの移動
        missiles.forEach(Missile::move);

        // 敵機の移動
        // このメソッド内で敵機の動きのロジックを実装、enemy.moveは使わない
        enemies.forEach(enemy -> {
            // 左端と右端に到達したら下に移動、左右の移動方向を反転
            if (enemy.getBang() > 0) {
                return;
            }
            if (enemy.getX() < 0 || enemy.getX() > Constants.SCREEN_WIDTH - enemy.getWidth()) {
                enemy.setY(enemy.getY() + 20);
                enemy.setSpeed(-enemy.getSpeed());
            }
            enemy.setX(enemy.getX() + enemy.getSpeed());
        });

        // 敵機のミサイルの発射
        // このメソッド内で敵機のミサイルの発射のロジックを実装
        enemies.forEach(enemy -> {
            if (!enemy.isAlive() || enemy.getBang() > 0) {
                return;
            }
            double percentange = 0.004;
            switch (Constants.stage) {
                case 1:
                    percentange = 0.003;
                    break;
                case 2:
                    percentange = 0.0035;
                    break;
                case 3:
                    percentange = 0.0040;
                    break;
                case 4:
                    percentange = 0.0045;
                    break;
                case 5:
                    percentange = 0.0050;
                    break;
                case 6:
                    percentange = 0.0055;
                    break;
            }
            // 一定の確率でミサイルを発射
            if (Math.random() < percentange) {
                enemyMissiles.add(new EnemyMissile(enemy.getX() + imageLoader.getImageWidth(ImageKey.ENEMY1) / 2, enemy.getY() + imageLoader.getImageHeight(ImageKey.ENEMY1)));
            }
        });

        // ボス機のミサイルの発射
        // このメソッド内でボス機のミサイルの発射のロジックを実装
        if (boss != null && boss.isAlive() && boss.getBang() == 0) {
            double percentange = 0.004;
            switch (Constants.stage) {
                case 1:
                    percentange = 0.003;
                    break;
                case 2:
                    percentange = 0.0035;
                    break;
                case 3:
                    percentange = 0.0040;
                    break;
                case 4:
                    percentange = 0.0045;
                    break;
                case 5:
                    percentange = 0.0050;
                    break;
                case 6:
                    percentange = 0.0055;
                    break;
            }
            ImageKey key = null;
            switch(Constants.stage) {
                case 1:
                    key = ImageKey.BOSS1;
                    break;
                case 2:
                    key = ImageKey.BOSS2;
                    break;
                case 3:
                    key = ImageKey.BOSS3;
                    break;
                case 4:
                    key = ImageKey.BOSS4;
                    break;
                case 5:
                    key = ImageKey.BOSS5;
                    break;
                case 6:
                    key = ImageKey.BOSS6;
                    break;
            }
            if (Math.random() < percentange) {
                enemyMissiles.add(new EnemyMissile(boss.getX() + imageLoader.getImageWidth(key) / 2, boss.getY() + imageLoader.getImageHeight(key)));
            }
        }

        // 画面外に出た敵機のミサイルを削除
        enemyMissiles.removeIf(missile -> !missile.isVisible());

        // 敵機のミサイルの移動
        enemyMissiles.forEach(EnemyMissile::move);

        // 敵機のミサイルと自機の当たり判定
        enemyMissiles.forEach(missile -> {
            if (missile.collidesWith(player)) {
                missile.hit();
                player.hit();
            }
        });

        // ボス機の移動
        if (boss != null && boss.isAlive() && boss.getBang() == 0) {
            boss.move();
        }

        // 衝突判定
        missiles.forEach(missile -> {
            enemies.forEach(enemy -> {
                if (enemy.isAlive() && missile.collidesWith(enemy)) {
                    missile.hit();
                    enemy.hit();
                    int time2 = (int) System.currentTimeMillis() - time;
                    scoreManager.addScore(1000 - time2 / 100);
                }
            });
            if (boss != null && missile.collidesWith(boss)) {
                missile.hit();
                boss.hit();
                int time2 = (int) System.currentTimeMillis() - time;
                scoreManager.addScore(1000 - time2 / 100);
            }
        });

        // 自機が敵機に当たったかどうかの判定
        enemies.forEach(enemy -> {
            if (player.collidesWith(enemy)) {
                player.setLife(1);
                player.hit();
            }
        });

        // 自機がボス機に当たったかどうかの判定
        if (boss != null && player.collidesWith(boss)) {
            player.hit();
            boss.hit();
        }

        // 敵機が画面外に出たかどうかの判定
        enemies.removeIf(enemy -> enemy.getY() > Constants.SCREEN_HEIGHT);

        // アイテムが画面外に出たかどうかの判定
        items.removeIf(item -> item.getY() > Constants.SCREEN_HEIGHT);

        // enemiesのisAliveがfalseのものを削除
        enemies.removeIf(enemy -> !enemy.isAlive() && enemy.getBang() == 0);

        // 敵機が全滅したかどうかの判定
        if (enemies.isEmpty() && boss == null) {
            // ボス機の出現
            ImageKey key = null;
            switch(Constants.stage) {
                case 1:
                    key = ImageKey.BOSS1;
                    break;
                case 2:
                    key = ImageKey.BOSS2;
                    break;
                case 3:
                    key = ImageKey.BOSS3;
                    break;
                case 4:
                    key = ImageKey.BOSS4;
                    break;
                case 5:
                    key = ImageKey.BOSS5;
                    break;
                case 6:
                    key = ImageKey.BOSS6;
                    break;
            }
            boss = new Boss(Constants.SCREEN_WIDTH / 2, 150, key);
            boss.setX(Constants.SCREEN_WIDTH / 2 - imageLoader.getImageWidth(key) / 2);
            boss.setHeight(imageLoader.getImageHeight(key));
            boss.setWidth(imageLoader.getImageWidth(key));
        }

        // 自機がやられたかどうかの判定
        if (!player.isAlive()) {
            // ゲームオーバー
            Constants.isGameOver = true;
            Constants.stage = 1;
        }

        // ボス機がやられたかどうかの判定
        if (boss != null && !boss.isAlive() && boss.getBang() == 0) {
            // ゲームクリア画面へ
            // game.setScreen(new GameClearScreen(game));
            if (Constants.stage < 6) {
                int time2 = (int) System.currentTimeMillis() - time;
                scoreManager.addScore(6000 - time2 / 100);
                Constants.stage++;

                // ステージ画面へ
                game.screen = new StageScreen(game);
                game.setScreen(game.screen);
                game.screen.setBackground(Color.BLACK);
                game.getContentPane().add(game.screen);
                // thisを破棄
                game.remove(this);
            } else {
                Constants.isGameClear = true;
            }


        // アイテムが自機に当たったかどうかの判定
        items.forEach(item -> {
            if(!item.isAlive()) {
                return;
            }
            if (player.collidesWith(item)) {
                switch(item.getType()) {
                    case SCORE_UP:
                        scoreManager.addScore(1000);
                        break;
                    case LIFE_UP:
                        player.setLife(player.getLife() + 1);
                        break;
                    case SPEED_UP:
                        player.setSpeed(player.getSpeed() + 1);
                        break;
                    case MISSILE_UPGRADE:
                        Constants.missileInterval -= 100;
                        break;
                }
                item.setAlive(false);
            }
        });

        // 星の移動
        stars.forEach(Star::move);

        // ハイスコアの更新
        scoreManager.updateHighScore();
        // 他のオブジェクトの更新処理
    }
    public void render() {
        paint(game.getGraphics());
    }

    // paint
    public void paint(Graphics g) {
        // 画面を黒で塗りつぶす
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        if (displayStage > 0) {
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(50f));
            g.drawString("Stage " + Constants.stage, 400, 400);
            return;
        }

        if (Constants.isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(50f));
            g.drawString("Game Over", 400, 400);
        }

        if (Constants.isGameClear) {
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(50f));
            g.drawString("Game Clear", 400, 400);
        }

        Image playerImage = ((ImageIcon) imageLoader.getImage(ImageKey.PLAYER)).getImage();

        // playerの描画
        if (player.getBang() > 0 && !player.isAlive()) {
            Image playerBangImage = ((ImageIcon) imageLoader.getImage(ImageKey.PLAYER_BANG)).getImage();
            g.drawImage(playerBangImage, player.getX(), player.getY(), this);
            player.setBang(player.getBang() - 1);
        } else if (player.isAlive()) {
            g.drawImage(playerImage, player.getX(), player.getY(), this);
        }
        // 敵機の描画
        enemies.forEach(enemy -> {
            if (enemy.isAlive()) {
                Image enemyImage = ((ImageIcon) imageLoader.getImage(enemy.getKey())).getImage();
                g.drawImage(enemyImage, enemy.getX(), enemy.getY(), this);
            } else if(enemy.getBang() > 0) {
                Image enemyBangImage = ((ImageIcon) imageLoader.getImage(ImageKey.ENEMY_BANG)).getImage();
                g.drawImage(enemyBangImage, enemy.getX(), enemy.getY(), this);
                enemy.setBang(enemy.getBang() - 1);
            }
        });

        // ボス機の描画
        if (boss != null) {
            if (boss.getBang() > 0) {
                Image bossBangImage = ((ImageIcon) imageLoader.getImage(ImageKey.BOSS_BANG)).getImage();
                g.drawImage(bossBangImage, boss.getX(), boss.getY(), this);
                boss.setBang(boss.getBang() - 1);
            } else if (boss.isAlive()) {
                Image bossImage = ((ImageIcon) imageLoader.getImage(boss.getKey())).getImage();
                g.drawImage(bossImage, boss.getX(), boss.getY(), this);
            }
        }

        // ミサイルの描画
        missiles.forEach(missile -> {
            if(!missile.isVisible()) {
                return;
            }
            Image missileImage = ((ImageIcon) imageLoader.getImage(ImageKey.PLAYER_MISSILE)).getImage();
            g.drawImage(missileImage, missile.getX(), missile.getY(), this);
        });

        // 敵機のミサイルの描画
        enemyMissiles.forEach(missile -> {
            Image missileImage = ((ImageIcon) imageLoader.getImage(ImageKey.ENEMY_MISSILE)).getImage();
            g.drawImage(missileImage, missile.getX(), missile.getY(), this);
        });

        // アイテムの描画
        items.forEach(item -> {
            if (!item.isAlive()) {
                return;
            }
            ImageKey key = null;
            switch(item.getType()) {
                case SCORE_UP:
                    key = ImageKey.ITEM1;
                    break;
                case LIFE_UP:
                    key = ImageKey.ITEM2;
                    break;
                case SPEED_UP:
                    key = ImageKey.ITEM3;
                    break;
                case MISSILE_UPGRADE:
                    key = ImageKey.ITEM4;
                    break;
            }
            Image itemImage = ((ImageIcon) imageLoader.getImage(key)).getImage();
            g.drawImage(itemImage, item.getX(), item.getY(), this);
        });

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString("SCORE: " + scoreManager.getScore(), 700, 60);

        // HISCORE
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString("HISCORE: " + scoreManager.getHighScore(), 400, 60);

        // 残り自機数の描画
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString("LIFE: " + player.getLife(), 80, 60);

        // 星の描画
        stars.forEach(star -> {
            g.setColor(star.getColor());
            g.fillRect(star.getX(), star.getY(), 1, 1);
        });
    }
}
class GameOverScreen extends Screen {
    public GameOverScreen(Game game) {
        super(game);
    }
    public void update() {
    }
    public void render() {
    }
}
class GameClearScreen extends Screen {
    public GameClearScreen(Game game) {
        super(game);
    }
    public void update() {
    }
    public void render() {
    }
}
class HighScoreScreen extends Screen {
    ScoreManager scoreManager = new ScoreManager();
    InputHandler inputHandler;

    public HighScoreScreen(Game game) {
        super(game);
        inputHandler = new InputHandler();
        game.addKeyListener(inputHandler);
    }
    public void update() {
        // スペースキーを押したらタイトル画面へ
        if (inputHandler.isFirePressed()) {
            game.screen = new TitleScreen(game);
            game.setScreen(game.screen);
            game.screen.setBackground(Color.BLACK);
            game.getContentPane().add(game.screen);
            // thisを破棄
            game.remove(this);
        }
    }
    public void render() {
        paint(game.getGraphics());
    }
    public void paint(Graphics g) {
        // 画面を黒で塗りつぶす
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // 星の描画
        stars.forEach(star -> {
            g.setColor(star.getColor());
            g.fillRect(star.getX(), star.getY(), 1, 1);
        });

        // タイトルを白文字で表示
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(50f));
        g.drawString("High Score", 400, 300);

        // 　ハイスコアを表示
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString("1st: " + scoreManager.getHighScores().get(0), 400, 400);
        g.drawString("2nd: " + scoreManager.getHighScores().get(1), 400, 450);
        g.drawString("3rd: " + scoreManager.getHighScores().get(2), 400, 500);
    }
}
class Player {
    private int x, y;
    private int speed = 5;
    private int life = 5;
    private int width;
    private int height;
    private boolean isAlive = true;
    // bang
    private int bang = 0;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void moveLeft() {
        x -= speed;
    }

    public void moveRight() {
        x += speed;
    }

    public void hit() {
        // 自機が攻撃を受けた時の処理
        if (life == 1) {
            isAlive = false;
            bang = 100;
        }
        if (life > 0) {
            life--;
        }
    }

    // getX
    public int getX() {
        return x;
    }

    // getY
    public int getY() {
        return y;
    }

    // collidesWith
    public boolean collidesWith(Enemy enemy) {
        // 自機と敵機の当たり判定
        return false;
    }

    // collidesWith
    public boolean collidesWith(Boss boss) {
        // 自機とボス機の当たり判定
        return false;
    }

    // collidesWith
    public boolean collidesWith(Item item) {
        // 自機とアイテムの当たり判定
        if(x < item.getX() + item.getWidth() && x + width > item.getX() && y < item.getY() + item.getHeight() && y + height > item.getY()) {
            return true;
        }
        return false;
    }

    // 描画処理などその他のメソッド

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    // getLife
    public int getLife() {
        return life;
    }

    // isAlive
    public boolean isAlive() {
        return isAlive;
    }
    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    // getSpeed
    public int getSpeed() {
        return speed;
    }

    // setSpeed
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setLife(int i) {
        life = i;
    }

    // bang
    public int getBang() {
        return bang;
    }

    public void setBang(int bang) {
        this.bang = bang;
    }
}

class Enemy {
    private int x, y;
    private boolean alive = true;
    private int width;
    private int height;
    private int speed = 5;
    // bang
    private int bang = 0;
    ImageKey key;

    public Enemy(int startX, int startY, ImageKey key) {
        this.x = startX;
        this.y = startY;
        this.key = key;
    }

    public void move() {
        // 敵機の動きのロジック

    }

    public void hit() {
        // 敵機が攻撃を受けた時の処理
        alive = false;
        bang = 50;
    }

    // 描画処理などその他のメソッド

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    // isAlive
    public boolean isAlive() {
        return alive;
    }

    // getX
    public int getX() {
        return x;
    }

    // setX
    public void setX(int x) {
        this.x = x;
    }

    // getY
    public int getY() {
        return y;
    }

    // setY
    public void setY(int y) {
        this.y = y;
    }

    // isVisible
    public boolean isVisible() {
        return true;
    }

    // getSpeed
    public int getSpeed() {
        return speed;
    }

    // setSpeed
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    // bang
    public int getBang() {
        return bang;
    }

    public void setBang(int bang) {
        this.bang = bang;
    }

    // key
    public ImageKey getKey() {
        return key;
    }
}

class EnemyMissile {
    private int x, y;
    private int speed = 3;
    private boolean visible;
    private int width;
    private int height;

    public EnemyMissile(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.visible = true;
    }

    public void move() {
        y += speed;
        if (y > Constants.SCREEN_HEIGHT) {
            visible = false;
        }
    }

    public void hit() {
        // ミサイルが攻撃を受けた時の処理
        visible = false;
    }

    // getX
    public int getX() {
        return x;
    }

    // getY
    public int getY() {
        return y;
    }

    // isVisible
    public boolean isVisible() {
        return visible;
    }

    // collidesWith
    public boolean collidesWith(Player player) {
        // ミサイルと自機の当たり判定
        if (x < player.getX() + player.getWidth() && x + width > player.getX() && y < player.getY() + player.getHeight() && y + height > player.getY()) {
            return true;
        }
        return false;
    }

    // 描画処理などその他のメソッド
}

class Boss {
    private int x, y;
    private boolean alive = true;
    private int width;
    private int height;
    private int speed = 5;
    // speedX
    private int speedX = 5;
    // speedY
    private int speedY = 5;
    private int life = 10;
    // bang
    private int bang = 0;
    ImageKey key;

    public Boss(int startX, int startY, ImageKey key) {
        this.x = startX;
        this.y = startY;
        this.key = key;
    }

    public void move() {
        //System.out.println("boss move");
        // 敵機の動きのロジック
        // 横向きの８の字を描くように移動
        if (x < 0 || x > Constants.SCREEN_WIDTH - width) {
            speedX = -speedX;
        }
        if (y < 0 || y > Constants.SCREEN_HEIGHT - height - 150) {
            speedY = -speedY;
        }
        x += speedX;
        y += speedY;
    }

    public void hit() {
        // 敵機が攻撃を受けた時の処理
        if (life > 0) {
            life--;
        }
        if (life == 0) {
            alive = false;
            bang = 150;
        }
    }

    // 描画処理などその他のメソッド

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    // isAlive
    public boolean isAlive() {
        return alive;
    }

    // getX
    public int getX() {
        return x;
    }

    // setX
    public void setX(int x) {
        this.x = x;
    }

    // getY
    public int getY() {
        return y;
    }

    // setY
    public void setY(int y) {
        this.y = y;
    }

    // isVisible
    public boolean isVisible() {
        return true;
    }

    // getSpeed
    public int getSpeed() {
        return speed;
    }

    // setSpeed
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    // bang
    public int getBang() {
        return bang;
    }

    public void setBang(int bang) {
        this.bang = bang;
    }

    // key
    public ImageKey getKey() {
        return key;
    }
}
class Missile {
    private int x, y;
    private int speed = 10;
    private boolean visible;
    private int width;
    private int height;

    public Missile(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.visible = true;
        Constants.missileFinalTime = (int) System.currentTimeMillis();
    }

    public void move() {
        y -= speed;
        if (y < 0) {
            visible = false;
        }
    }

    public void hit() {
        // ミサイルが攻撃を受けた時の処理
        visible = false;
    }

    // getX
    public int getX() {
        return x;
    }

    // getY
    public int getY() {
        return y;
    }

    // isVisible
    public boolean isVisible() {
        return visible;
    }

    // collidesWith
    public boolean collidesWith(Enemy enemy) {
        // ミサイルと敵機の当たり判定
        if(x < enemy.getX() + enemy.getWidth() && x + width > enemy.getX() && y < enemy.getY() + enemy.getHeight() && y + height > enemy.getY()) {
            return true;
        }
        return false;
    }

    // collidesWith
    public boolean collidesWith(Boss boss) {
        // ミサイルとボス機の当たり判定
        if(x < boss.getX() + boss.getWidth() && x + width > boss.getX() && y < boss.getY() + boss.getHeight() && y + height > boss.getY()) {
            return true;
        }
        return false;
    }
    // 描画処理などその他のメソッド
    // setWidth
    public void setWidth(int width) {
        this.width = width;
    }

    // getWidth
    public int getWidth() {
        return width;
    }

    // setHeight
    public void setHeight(int height) {
        this.height = height;
    }

    // getHeight
    public int getHeight() {
        return height;
    }

}
class Item {
    private int x, y;
    private ItemType type; // アイテムの種類を示す列挙型
    private boolean visible;
    private int speed = 5;
    private int width;
    private int height;
    private boolean isAlive = true;

    public Item(int x, int y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void applyEffect(Player player) {
        // アイテムに応じた効果をプレイヤーに適用

    }

    // 描画処理などその他のメソッド

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    // getX
    public int getX() {
        return x;
    }

    // setX
    public void setX(int x) {
        this.x = x;
    }

    // getY
    public int getY() {
        return y;
    }
    // setY
    public void setY(int y) {
        this.y = y;
    }

    // isVisible
    public boolean isVisible() {
        return visible;
    }
    // getType
    public ItemType getType() {
        return type;
    }
    // getSpeed
    public int getSpeed() {
        return speed;
    }

    // isAlive
    public boolean isAlive() {
        return isAlive;
    }

    // setAlive
    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

}

// アイテムの種類を表す列挙型
enum ItemType {
    SCORE_UP,
    LIFE_UP,
    SPEED_UP,
    MISSILE_UPGRADE
}

class ScoreManager {
    // score.datは3行のテキストファイル
    private String fileName = "score.dat";
    private int score = 0;
    private List<Integer> highScores = new ArrayList<>();

    public ScoreManager() {
        // ハイスコアの読み込み処理
        loadHighScores();
    }

    public void updateHighScore() {
        // ハイスコアの更新処理
        checkHighScore();
    }

    public int getHighScore() {
        // ハイスコアを文字列で返す
        return highScores.get(0);
    }

    public void addScore(int value) {
        score += value;
    }

    public void checkHighScore() {
        // 現在のスコアがハイスコアかどうか確認し、必要に応じて更新
        if (score > highScores.get(0)) {
            highScores.set(2, highScores.get(1));
            highScores.set(1, highScores.get(0));
            highScores.set(0, score);
            saveHighScores();
        } else if (score > highScores.get(1)) {
            highScores.set(2, highScores.get(1));
            highScores.set(1, score);
            saveHighScores();
        } else if (score > highScores.get(2)) {
            highScores.set(2, score);
            saveHighScores();
        }
    }

    public void saveHighScores() {
        // ハイスコアをファイルに保存
        // ファイルがなかったら作る
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("0\n0\n0");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileWriter fileWriter = new FileWriter(file);
                for (int i = 0; i < highScores.size(); i++) {
                    fileWriter.write(highScores.get(i) + "\n");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadHighScores() {
        // ハイスコアをファイルから読み込み
        // ファイルがなかったら作る
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("0\n0\n0");
                fileWriter.close();
                highScores.add(0);
                highScores.add(0);
                highScores.add(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    highScores.add(Integer.parseInt(line));
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ハイスコアの取得や表示に関連するメソッド
    // getScore
    public int getScore() {
        return score;
    }

    // getHighScores
    public List<Integer> getHighScores() {
        return highScores;
    }
}

class ImageLoader {
    private Map<ImageKey, ImageIcon> images = new HashMap<>();

    public ImageLoader() {
        // 画像の読み込み
        loadImages();
    }

    private void loadImages() {
        images.put(ImageKey.PLAYER, new ImageIcon("./images/player.png"));
        images.put(ImageKey.PLAYER_BANG, new ImageIcon("./images/player_bang.png"));
        images.put(ImageKey.PLAYER_MISSILE, new ImageIcon("./images/player_missile.png"));
        images.put(ImageKey.ENEMY1, new ImageIcon("./images/enemy1.png"));
        images.put(ImageKey.ENEMY2, new ImageIcon("./images/enemy2.png"));
        images.put(ImageKey.ENEMY3, new ImageIcon("./images/enemy3.png"));
        images.put(ImageKey.ENEMY4, new ImageIcon("./images/enemy4.png"));
        images.put(ImageKey.ENEMY5, new ImageIcon("./images/enemy5.png"));
        images.put(ImageKey.ENEMY6, new ImageIcon("./images/enemy6.png"));
        images.put(ImageKey.ENEMY7, new ImageIcon("./images/enemy7.png"));
        images.put(ImageKey.ENEMY8, new ImageIcon("./images/enemy8.png"));
        images.put(ImageKey.ENEMY9, new ImageIcon("./images/enemy9.png"));
        images.put(ImageKey.ENEMY10, new ImageIcon("./images/enemy10.png"));
        images.put(ImageKey.ENEMY11, new ImageIcon("./images/enemy11.png"));
        images.put(ImageKey.ENEMY_BANG, new ImageIcon("./images/enemy_bang.png"));
        images.put(ImageKey.ENEMY_MISSILE, new ImageIcon("./images/enemy_missile.png"));
        images.put(ImageKey.ITEM1, new ImageIcon("./images/item1.png"));
        images.put(ImageKey.ITEM2, new ImageIcon("./images/item2.png"));
        images.put(ImageKey.ITEM3, new ImageIcon("./images/item3.png"));
        images.put(ImageKey.ITEM4, new ImageIcon("./images/item4.png"));
        images.put(ImageKey.BOSS1, new ImageIcon("./images/boss1.png"));
        images.put(ImageKey.BOSS2, new ImageIcon("./images/boss2.png"));
        images.put(ImageKey.BOSS3, new ImageIcon("./images/boss3.png"));
        images.put(ImageKey.BOSS4, new ImageIcon("./images/boss4.png"));
        images.put(ImageKey.BOSS5, new ImageIcon("./images/boss5.png"));
        images.put(ImageKey.BOSS6, new ImageIcon("./images/boss6.png"));
        images.put(ImageKey.BOSS_BANG, new ImageIcon("./images/boss_bang.png"));

        // imagesの画像を全て1/2に縮小
        for (ImageKey key : images.keySet()) {
            // ImageKey.ENEMY_MISSILEとImageKey.PLAYER_MISSILEは縮小しない
            if (key == ImageKey.ENEMY_MISSILE || key == ImageKey.PLAYER_MISSILE) {
                continue;
            }
            ImageIcon icon = images.get(key);
            images.put(key, new ImageIcon(icon.getImage().getScaledInstance(icon.getIconWidth() / 2, icon.getIconHeight() / 2, Image.SCALE_SMOOTH)));
        }
    }

    public ImageIcon getImage(ImageKey key) {
        return images.get(key);
    }

    // 画像の横幅・縦幅を取得するメソッドなど
    public int getImageWidth(ImageKey key) {
        return images.get(key).getIconWidth();
    }

    public int getImageHeight(ImageKey key) {
        return images.get(key).getIconHeight();
    }

}

class InputHandler extends KeyAdapter {
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                firePressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                firePressed = false;
                break;
        }
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isFirePressed() {
        return firePressed;
    }

    // Getterメソッドなど
}