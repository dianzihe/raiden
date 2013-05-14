package com.raiden.test;

import com.raiden.game.Bullet;
import com.raiden.game.Collidable;
import com.raiden.game.Enemy;
import com.raiden.game.Ship;

import android.graphics.Point;
import android.test.AndroidTestCase;

public class PlayerTests extends AndroidTestCase {
	
	private static float scaleX = 1.0f;
	private static float scaleY = 1.0f;
	private static Point screenSize = new Point(720, 1280);
	private static final float TIMESLICE = 16;

	static {
		Collidable.setBounds(screenSize);
		Collidable.setScale(scaleX, scaleY);
	}

	public void testMovement() {
		//This is going to test if the ship moves correctly
		Ship hero = new Ship();
		Enemy[] enemies = new Enemy[0];
		hero.setTargets(enemies);
		
		int posX = hero.getX();
		int posY = hero.getY();
		int lastX, lastY;
		int heroSpeed = hero.getSpeed();
		// set a destination
		hero.move(heroSpeed * 3 + 1, heroSpeed * 3 + 1);
		for (int i = 0; i < 4; i++) {
			hero.update(TIMESLICE);
			lastX = hero.getX();
			lastY = hero.getY();
			// see if the hero moved
			assertNotSame(posX, lastX);
			assertNotSame(posY, lastY);
		}
		lastX = hero.getX();
		lastY = hero.getY();
		// check if the hero reached the exact spot
		assertEquals(heroSpeed * 3 + 1, lastX - posX);
		assertEquals(heroSpeed * 3 + 1, lastY - posY);
	}
	
	public void testBoundaries() {
		//This is going to test if the ship does not go out of bounds
		Ship hero = new Ship();
		Enemy[] enemies = new Enemy[0];
		hero.setTargets(enemies);
		
		int posX = hero.getX(); int posY = hero.getY();
		
		// check right side of the screen
		hero.move(screenSize.x * 2, 0);
		for (int i = 0; i < 50; i++) {
			hero.update(TIMESLICE);
		}
		posX = hero.getX(); posY = hero.getY();
		// right side -> maximum x
		assertEquals(screenSize.x-1, posX);
		
		// check down side of the screen
		hero.move(0, screenSize.y * 2);
		for (int i = 0; i < 50; i++) {
			hero.update(TIMESLICE);
		}
		posX = hero.getX(); posY = hero.getY();
		// down side -> maximum y
		assertEquals(screenSize.y-1, posY);
		
		// check left side of the screen
		hero.move(-(screenSize.x * 2), 0);
		for (int i = 0; i < 100; i++) {
			hero.update(TIMESLICE);
		}
		posX = hero.getX(); posY = hero.getY();
		// left side -> minimum x
		assertEquals(0, posX);
		
		// check top side of the screen
		hero.move(0, -(screenSize.y * 2) );
		for (int i = 0; i < 100; i++) {
			hero.update(TIMESLICE);
		}
		posX = hero.getX(); posY = hero.getY();
		// top side -> minimum y
		assertEquals(0, posY);
	}
	
	public void testShooting() {
		/* This is going to test if the player ship shoots correctly
		 * Should test shooting intervals
		 */
		Ship hero = new Ship();
		Enemy[] enemies = new Enemy[0];
		hero.setTargets(enemies);
		hero.shoot();
		
		int i;
		// check if the hero shoots 2 bullets
		for (i = 0; i < 2; i++) {
			Bullet bullet = hero.getShotsFired()[i];
			assertTrue(bullet.visible);
		}
		// these shots were not fired
		for(; i < hero.getShotsFired().length; i++){
			Bullet bullet = hero.getShotsFired()[i];
			assertFalse(bullet.visible);
		}
		
		// test the hero reload time
		hero.shoot(); hero.shoot(); hero.shoot();
		
		// see if no bullets were fired
		for (i = 0; i < 2; i++) {
			Bullet bullet = hero.getShotsFired()[i];
			assertTrue(bullet.visible);
		}
		for(; i < hero.getShotsFired().length; i++){
			Bullet bullet = hero.getShotsFired()[i];
			assertFalse(bullet.visible);
		}
		
		int reloadTime = 0;
		while (reloadTime < hero.RELOAD_DONE){
			reloadTime += TIMESLICE;
			hero.update(TIMESLICE);
		}
		
		hero.shoot();
		// check if now the hero shot 2 additional bullets
		for (i = 0; i < 4; i++) {
			Bullet bullet = hero.getShotsFired()[i];
			assertTrue(bullet.visible);
		}
		for(; i < hero.getShotsFired().length; i++){
			Bullet bullet = hero.getShotsFired()[i];
			assertFalse(bullet.visible);
		}
		
	}
	
	public void testTurrets() {
		/* This is going to test turret placement and bullet shoot trajectories */
		Ship hero = new Ship();
		Enemy[] enemies = new Enemy[0];
		hero.setTargets(enemies);
		
		hero.addTurret(180.0f);
		hero.shoot();
		// the first two bullets always start at 90.0f
		int i;
		for (i = 0; i < 2; i++) {
			Bullet bullet = hero.getShotsFired()[i];
			assertTrue(bullet.angle == 90.0f);
		}
		// the third bullet is from the new turret
		Bullet bullet = hero.getShotsFired()[i];
		// 180 degrees makes the bullet go left
		assertTrue(bullet.angle == 180.0f);
		
		int posX, posY, lastX, lastY;
		posX = bullet.getX(); posY = bullet.getY();
		bullet.update();
		lastX = bullet.getX(); lastY = bullet.getY();
		assertEquals(posX-bullet.getSpeed() , lastX);
		assertEquals(posY, lastY);
	}
	
	public void testPowerUps() {
		/* This should test possible powerups the player ship catches */
	}
	
	public void testDamageAndDestruction() {
		/* This should test player ship receiving damage and being destroyed in the process */
	}
	
}