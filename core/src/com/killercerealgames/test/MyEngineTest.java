package com.killercerealgames.test;

import java.util.HashMap;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.RubeSceneLoader;

public class MyEngineTest implements ApplicationListener{
	
	private static final float step = 1/60f;
	
	private RubeSceneLoader loader;
	private RubeScene scene;
	private double accumulator = 0;
	private World world;
	double newTime;
	double currentTime;
	double frameTime;
	
	private OrthographicCamera camera;
	private Box2DDebugRenderer renderer;
	
	private Texture texture;
	private HashMap<Sprite, Body> sprites;

	private SpriteBatch batch;
	
	private HashMap<String, Body> boundaries;
	
	private Stage stage;
	
	private Body blue;
	private boolean debug = true;
	
	private RayHandler rayHandler;
	private PointLight pLight;

	public class MyActor extends Actor {
		private float X = 0;
		private float Y = 0;
		Texture texture = new Texture(Gdx.files.internal("index.png"));
		@Override
		public void draw(Batch batch, float alpha) {
			batch.draw(texture, X, Y);
		}
		@Override
		public void act(float delta) {
			X = X + 1;
			Y = Y + 1;
		}
	}
	
	@Override
	public void create () {
		
	
		world = new World(new Vector2(0,-9.81f), true);
		
		loader = new RubeSceneLoader(world);
		scene = loader.loadScene(Gdx.files.internal("ball.json"));
		
		camera = new OrthographicCamera(Gdx.graphics.getWidth() / 50, Gdx.graphics.getHeight() / 50);
		camera.zoom = 0.5f;
		renderer = new Box2DDebugRenderer();
		
		sprites = new HashMap<Sprite, Body>();
		texture = new Texture(Gdx.files.internal("index.png"));
		for (Body body : scene.getNamed(Body.class, "circle")) {
			Sprite sprite = new Sprite(texture);
			sprite.setScale(1/200f);
			sprite.setOriginCenter();
			
			sprites.put(sprite, body);
		}
		
		texture = new Texture(Gdx.files.internal("blue.png"));
		blue = scene.getNamed(Body.class, "square").first();
		Sprite sprite = new Sprite(texture);
		sprite.setScale(1/750f);
		sprite.setOriginCenter();
		sprites.put(sprite, blue);
		
		boundaries = new HashMap<String, Body>();
		createBoundaries();
		centerCamera();
		
		batch = new SpriteBatch();
		
		stage = new Stage();
		MyActor myActor = new MyActor();
		stage.addActor(myActor);
		
		rayHandler = new RayHandler(world);
		rayHandler.setShadows(false);
	
		pLight = new PointLight(rayHandler, 100, Color.CYAN, 5,
				(boundaries.get("right").getPosition().x + boundaries.get("left").getPosition().x) / 2,
				boundaries.get("up").getPosition().y - 1);
		pLight.isXray();
		
		Filter filter = new Filter();
		filter.categoryBits = 0;
		filter.maskBits = 0;
		
		PointLight.setContactFilter(filter);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		
		newTime = TimeUtils.millis() / 1000.0;
		frameTime = Math.min(newTime - currentTime, 0.25f);
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= step) {
			world.step(step, 6, 2);
			accumulator -= step;
			interpolate((float) accumulator / step);

		}
		



		batch.setProjectionMatrix(camera.combined);
		
		centerSprites();

		batch.begin();
		for (Sprite sprite : sprites.keySet()) {
			sprite.draw(batch);
		}

		batch.end();
		
		checkInput();
		if (debug)
		renderer.render(world, camera.combined);
		
		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();
		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
		
	}
	
	private void moveCamera(float alpha) {

		float CAMERA_SPEED = .45f;
		float delta = Gdx.graphics.getDeltaTime();
		
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			camera.position.x -= CAMERA_SPEED * delta;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			camera.position.x += CAMERA_SPEED * delta;

		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			camera.position.y += CAMERA_SPEED * delta;

		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			camera.position.y -= CAMERA_SPEED * delta;
		}
		
		camera.update();
	}

	private void centerSprites() {
		for (Sprite sprite : sprites.keySet()) {
			sprite.setPosition(sprites.get(sprite).getPosition().x - sprite.getWidth() / 2, sprites.get(sprite).getPosition().y - sprite.getWidth() / 2);
		}
	}
	
	private void interpolate (float alpha) {
		for (Sprite sprite : sprites.keySet()) {
			Transform transform = sprites.get(sprite).getTransform();
			Vector2 bodyPosition = transform.getPosition();
			Float rotation = MathUtils.radiansToDegrees * transform.getRotation();
			
			sprite.setX(bodyPosition.x * alpha + sprite.getX() * (1.0f - alpha));
			sprite.setY(bodyPosition.y * alpha + sprite.getY() * (1.0f - alpha));
			sprite.setRotation(rotation * alpha + sprite.getRotation() * (1.0f - alpha));
			
		}
		moveCamera(alpha);

	}
	
	private void createBoundaries() {
		boundaries.put("up", scene.getNamed(Body.class, "up").first());
		boundaries.put("down", scene.getNamed(Body.class, "down").first());
		boundaries.put("left", scene.getNamed(Body.class, "left").first());
		boundaries.put("right", scene.getNamed(Body.class, "right").first());
		
	}
	
	private void centerCamera() {
		float centerX = (boundaries.get("left").getPosition().x + boundaries.get("right").getPosition().x) / 2;
		float centerY = (boundaries.get("up").getPosition().y + boundaries.get("down").getPosition().y) / 2;
		camera.position.set(centerX, centerY, 0);
		camera.update();
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {

		texture.dispose();
		rayHandler.dispose();
		
	}
	
	private void checkInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
			debug = !debug;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
			blue.applyForceToCenter(new Vector2(0, 50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			blue.applyForceToCenter(new Vector2(0, -50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			blue.applyForceToCenter(new Vector2(-50, 0), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			blue.applyForceToCenter(new Vector2(50, 0), true);
		}
	}

}
