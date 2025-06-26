package io.github.platform;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;


public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture background;
    private Texture playerTexture;
    private Texture platformTexture;
    private Array<Platform> platforms;
    private OrthographicCamera camera;
    private TextureRegion backgroundRegion;
    private Array<TrailPoint> trailPositions;


    // Player state
    private float playerX, playerY;
    private float velocityY = 0;
    private final float gravity = -0.5f;
    private final float jumpVelocity = 20;
    private final float moveSpeed = 300;
    private final float playerWidth = 32;
    private final float playerHeight = 32;
    private boolean isGrounded = false;
    private final int maxTrailLength = 15;

    

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("landscape.png"); // background image
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        backgroundRegion = new TextureRegion(background);
        backgroundRegion.setRegion(0, 0, 800, 480); // Initial region size

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1); // Red with full opacity
        pixmap.fillRectangle(0, 0, 32, 32);
        playerTexture = new Texture(pixmap);
        pixmap.dispose();
   
        platforms = new Array<>();
        generatePlatforms(20);  // Generate 20 platforms
        Pixmap platformPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        platformPixmap.setColor(0, 1, 0, 1);
        platformPixmap.fill();
        platformTexture = new Texture(platformPixmap);
        platformPixmap.dispose();

        camera = new OrthographicCamera(800, 480);
        camera.position.set(400, 240, 0);
        camera.update();

        trailPositions = new Array<>();
        
        playerX = 100;
        playerY = 150; // Start on "ground"
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput(deltaTime);
        applyPhysics();
        checkPlatformCollision();
        camera.position.x = playerX + playerWidth / 2;
        camera.update();

        if (playerY < -50) {  // Fell off screen
            playerX = 100;    // Reset to start X
            playerY = 150;    // Reset to start Y (ground level)
            velocityY = 0;
            isGrounded = false;
        }
        
        //adds trails
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.SPACE) || isGrounded == false) {
            float r = MathUtils.random();
            float g = MathUtils.random();
            float b = MathUtils.random();
            trailPositions.add(new TrailPoint(new Vector2(playerX, playerY), r, g, b));
            if (trailPositions.size > maxTrailLength) {
                trailPositions.removeIndex(0);
            }
        }
        
        // Render
        int scrollX = (int) camera.position.x;
        backgroundRegion.setRegion(scrollX, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);
        
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundRegion, camera.position.x - camera.viewportWidth / 2, 0);
        batch.setColor(1, 1, 1, 0.3f);  // Set transparency for trail
        for (int i = 0; i < trailPositions.size; i++) {
            TrailPoint tp = trailPositions.get(i);
            float alpha = 0.5f * ((float)(i + 1) / trailPositions.size);  // Faster fade max alpha 0.5
            batch.setColor(tp.r, tp.g, tp.b, alpha);
            batch.draw(playerTexture, tp.position.x, tp.position.y, playerWidth, playerHeight);
        }
        batch.setColor(1, 1, 1, 1);  // reset to opaque after
        // Now draw the actual player on top
        batch.draw(playerTexture, playerX, playerY);
        for (Platform platform : platforms) {
            batch.draw(platform.texture, platform.rect.x, platform.rect.y, platform.rect.width, platform.rect.height);
        }
        batch.end();
        

    }

    private void handleInput(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= moveSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += moveSpeed * deltaTime;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            velocityY = jumpVelocity;
            isGrounded = false;
        }
    }

    private void applyPhysics() {
        velocityY += gravity;
        playerY += velocityY;       
    }
    
    private void generatePlatforms(int count) {
        platforms.clear(); // Clear any existing platforms

        float x = 100;  // Start X position
        float y = 150;  // Start Y position (ground level)

        for (int i = 0; i < count; i++) {
            float width = MathUtils.random(100, 250);
            float height = 20;

            Rectangle rect = new Rectangle(x, y, width, height);

            // Create a 1x1 texture with a random color
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();

            platforms.add(new Platform(rect, texture));

            // Next position
            float gapX = MathUtils.random(150, 300);
            float gapY = MathUtils.random(-50, 50);
            x += width + gapX;
            y += gapY;
            y = MathUtils.clamp(y, 100, 400);
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
        pixmap.fill();
        pixmap.dispose();

    }

    
    private void checkPlatformCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        boolean onPlatform = false;

        for (Platform platform : platforms) {
            Rectangle rect = platform.rect;

            if (velocityY <= 0 && playerRect.overlaps(rect)) {
                playerY = rect.y + rect.height;
                velocityY = 0;
                onPlatform = true;
                break;
            }
        }


        if (onPlatform ) {
            isGrounded = true;
        } else {
            isGrounded = false;
        }
    }




    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        playerTexture.dispose();
        for (Platform platform : platforms) {
            platform.texture.dispose();
        }

    }
}
