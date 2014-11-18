package com.killercerealgames.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
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
	private HashMap<Body, Shape> shapes;

	private SpriteBatch batch;
	
	private HashMap<String, Body> boundaries;
	
	private Stage stage;
	
	private Body blue;
	private boolean debug = false;
	
	private RayHandler rayHandler;
	private PointLight pLight;
	
	private MyContactHandler myContactHandler;
	
	Array<Body> bodies;
	
	public static int numberOfReds = 0;
	
	private long timeStarted;
	private long timeNow;
	public static long timeTaken;

	
	boolean isLocAvailable;
	
	public static HashMap<String, String> savedData;
	
	public class Shape {
		private String type;
		public Body body;
		public Sprite sprite;
		private PointLight pLight;
		public Shape (Body body, Sprite sprite, PointLight pLight, String type) {
			this.body = body;
			this.sprite = sprite;
			this.pLight = pLight;
			this.type = type;
		}
		public void delete() {
			if (pLight != null)
				pLight.remove();
		}
		public String getType() {
			return type;
		}
		
	}
	
	public class MyContactHandler implements ContactListener {
		
		public MyContactHandler() {}

		@Override
		public void beginContact(Contact contact) {
			
			Fixture A = contact.getFixtureA();
			Fixture B = contact.getFixtureB();
			
			if (A.getBody().getUserData() != null && B.getBody().getUserData() != null) {
				
				if (A.getBody().getUserData().equals("blue") && B.getBody().getUserData().equals("red")) {
					B.getBody().setUserData("DELETE");
				}
				else if (B.getBody().getUserData().equals("blue") && A.getBody().getUserData().equals("red")) {
					A.getBody().setUserData("DELETE");
				}
				
			}
			
		}

		@Override
		public void endContact(Contact contact) {}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}
		
	}
	
	public class MySprite extends Sprite {
		public float myX;
		public float myY;
		
		public MySprite(Texture texture, float X1, float Y1) {
			super(texture);
			this.myX = X1;
			this.myY = Y1;
		}
	}

	public class MyActor extends Actor {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Table table = new Table(skin);
		
		Texture texture = new Texture(Gdx.files.internal("newSpaceBackground.png"));
		MySprite sprite1 = new MySprite(texture, 0, 0);
		MySprite sprite2 = new MySprite(texture, sprite1.myX + sprite1.getWidth(), 0);
		
        BitmapFont font = new BitmapFont();
        String stringTime;
        String bestTime;
		
		private MySprite currentSprite = sprite1;
		private MySprite nextSprite = sprite2;
		
		private Label numberOfRedsText = new Label("Red squares remaining: ", skin);
		private Label numberOfRedsNumber = new Label("", skin);
		private Label currentTimeText = new Label("Table elapsed: ", skin);
		private Label currentTimeNumber = new Label("", skin);
		private Label bestTimeText = new Label("Best time: ", skin);
		private Label bestTimeNumber = new Label("", skin);
		
		public void init() {
			table.setPosition(Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 50);

			numberOfRedsText.setColor(Color.RED);
			numberOfRedsNumber.setColor(Color.RED);
			table.add(numberOfRedsText);
			table.add(numberOfRedsNumber);
			table.row();
			
			currentTimeText.setColor(Color.YELLOW);
			currentTimeNumber.setColor(Color.YELLOW);
			table.add(currentTimeText);
			table.add(currentTimeNumber);
			table.row();
			
			bestTimeText.setColor(Color.CYAN);
			bestTimeNumber.setColor(Color.CYAN);
			table.add(bestTimeText);
			table.add(bestTimeNumber);
		}

		@Override
		public void draw(Batch batch, float alpha) {
		    batch.draw(sprite1, sprite1.myX, sprite1.myY, sprite1.getOriginX(), sprite1.getOriginY(), sprite1.getWidth(), sprite1.getHeight(), sprite1.getScaleX(), sprite1.getScaleY(), sprite1.getRotation());
		    batch.draw(sprite2, sprite2.myX, sprite2.myY, sprite2.getOriginX(), sprite2.getOriginY(), sprite2.getWidth(), sprite2.getHeight(), sprite2.getScaleX(), sprite2.getScaleY(), sprite2.getRotation());
		    
		    numberOfRedsNumber.setText(String.valueOf(MyEngineTest.numberOfReds));
		    currentTimeNumber.setText((MyEngineTest.timeTaken / 1000) + "." + ((MyEngineTest.timeTaken / 100) % 10));
		    bestTimeNumber.setText((Integer.parseInt(MyEngineTest.savedData.get("best_time")) / 1000) + "." + ((Integer.parseInt(MyEngineTest.savedData.get("best_time")) / 100) % 10));
		    table.draw(batch, alpha);
		}
		@Override
		public void act(float delta) {
			currentSprite.myX = currentSprite.myX - 1;
			nextSprite.myX = currentSprite.myX + currentSprite.getWidth();
			if (currentSprite.myX <= -currentSprite.getWidth()) {
				MySprite holder = currentSprite;
				currentSprite = nextSprite;
				nextSprite = holder;
			}

		}
	}
	
	@Override
	public void create () {
		
		isLocAvailable = Gdx.files.isLocalStorageAvailable();
		if (isLocAvailable) {
			savedData = new HashMap<String, String>();
			loadData();
		}
		
		myContactHandler = new MyContactHandler();
	
		world = new World(new Vector2(0,-9.81f), true);
		world.setContactListener(myContactHandler);
		
		loader = new RubeSceneLoader(world);
		scene = loader.loadScene(Gdx.files.internal("ball.json"));
		
		camera = new OrthographicCamera(Gdx.graphics.getWidth() / 50, Gdx.graphics.getHeight() / 50);
		camera.zoom = 0.5f;
		renderer = new Box2DDebugRenderer();
		
		shapes = new HashMap<Body, Shape>();

		
		rayHandler = new RayHandler(world);
		rayHandler.setShadows(false);
		
		generateShapesAndCenterCamera();
		
		batch = new SpriteBatch();
		
		stage = new Stage();
		MyActor myActor = new MyActor();
		myActor.init();
		stage.addActor(myActor);
		
		bodies = new Array<Body>();

	}

	private void loadData() {
		if (!Gdx.files.internal("data.sav").exists()) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(Gdx.files.internal("data.sav").file()));
				writer.write("best_time\n" + 60 * 1000);
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			loadData();
		}
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(Gdx.files.internal("data.sav").file()));
				String data = null;
				
			    while ((data = reader.readLine()) != null)
			    {
			        savedData.put(data, reader.readLine());
			    }
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void saveData() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Gdx.files.internal("data_temp.sav").file()));
			for(Entry<String, String> e : savedData.entrySet()) {
			        String key = e.getKey();
			        String value = e.getValue();
			        writer.append(key + "\n");
			        writer.append(value);
			}
			writer.close();
			if (Gdx.files.internal("data_temp.sav").exists()) {
				Gdx.files.internal("data.sav").file().delete();
				Gdx.files.internal("data_temp.sav").file().renameTo(Gdx.files.internal("data.sav").file());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void generateShapesAndCenterCamera() {
		texture = new Texture(Gdx.files.internal("index.png"));
		for (Body body : scene.getNamed(Body.class, "circle")) {
			Sprite sprite = new Sprite(texture);
			sprite.setScale(1/200f);
			sprite.setOriginCenter();
			
			pLight = new PointLight(rayHandler, 20, Color.RED, 0.90f, 0, 0);
			pLight.isXray();
			pLight.attachToBody(body);
			
			body.setUserData("red");
			
			shapes.put(body, new Shape(body, sprite, pLight, "red"));
			
			numberOfReds++;
		}
		
		

		
		texture = new Texture(Gdx.files.internal("blue.png"));
		blue = scene.getNamed(Body.class, "square").first();
		blue.setUserData("blue");
		Sprite blueSprite = new Sprite(texture);
		blueSprite.setScale(1/750f);
		blueSprite.setOriginCenter();
		
		pLight = new PointLight(rayHandler, 20, Color.CYAN, 2, 0, 0);
		pLight.isXray();
		pLight.attachToBody(blue);
		
		Filter filter = new Filter();
		filter.categoryBits = 0;
		filter.maskBits = 0;
		
		PointLight.setContactFilter(filter);
		
		shapes.put(blue, new Shape(blue, blueSprite, pLight, "blue"));
		
		boundaries = new HashMap<String, Body>();
		createBoundaries();
		centerCamera();
		
		timeStarted = System.currentTimeMillis();
		timeNow = System.currentTimeMillis();
		timeTaken = timeNow - timeStarted;
		

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act(Gdx.graphics.getDeltaTime());

		
		newTime = TimeUtils.millis() / 1000.0;
		frameTime = Math.min(newTime - currentTime, 0.25f);
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= step) {
			world.step(step, 6, 2);
			accumulator -= step;
			interpolate((float) accumulator / step);

		}

		stage.draw();

		batch.setProjectionMatrix(camera.combined);
		
		centerSprites();

		batch.begin();
		for (Shape shape : shapes.values()) {
			shape.sprite.draw(batch);
		}

		batch.end();
		
		checkInput();
		if (debug) {
			renderer.render(world, camera.combined);
			System.out.println(world.getBodyCount());
		}

		
		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();
		
		world.getBodies(bodies);
		for (Body body : bodies) {
			if (body.getUserData() != null && body.getUserData().equals("DELETE")) {
				Shape shape = shapes.get(body);
				if (shape.getType().equals("red")) {
					numberOfReds--;
				}
				shape.delete();
				shapes.remove(body);
				world.destroyBody(body);
			}
		}
		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
		timeNow = System.currentTimeMillis();
		timeTaken = timeNow - timeStarted;
		
		if (numberOfReds == 0) {
			if (timeTaken < Integer.parseInt((String)savedData.get("best_time"))) {
				savedData.replace("best_time", String.valueOf(timeTaken));
				saveData();
			}
			respawn();
		}
		
	}
	
	private void respawn() {
		numberOfReds = 0;
		world.clearForces();
		world.getBodies(bodies);
		for (Body body : bodies) {
			Shape shape = shapes.get(body);
			if (shape == null) {}
			else {
				shape.delete();
				shapes.remove(body);
			}

			world.destroyBody(body);
		}

		loader = new RubeSceneLoader(world);
		scene = loader.loadScene(Gdx.files.internal("ball.json"));
		generateShapesAndCenterCamera();
		
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
		for (Shape shape : shapes.values()) {
			shape.sprite.setPosition(shape.body.getPosition().x - shape.sprite.getWidth() / 2, shape.body.getPosition().y - shape.sprite.getWidth() / 2);
		}
	}
	
	private void interpolate (float alpha) {
		for (Shape shape : shapes.values()) {
			Sprite sprite = shape.sprite;
			Transform transform = shape.body.getTransform();
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
		saveData();
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {

		texture.dispose();
		rayHandler.dispose();
		world.dispose();
		
	}
	
	private void checkInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
			debug = !debug;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			blue.applyForceToCenter(new Vector2(0, 50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			blue.applyForceToCenter(new Vector2(0, -50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			blue.applyForceToCenter(new Vector2(-50, 0), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			blue.applyForceToCenter(new Vector2(50, 0), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			respawn();
		}
	}

}
