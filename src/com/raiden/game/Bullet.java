package com.raiden.game;

public class Bullet extends Collidable {
	private int speed;
	public boolean visible, hit;
	public float angle;
	
	private static int minX = 0;
	private static int minY = 0;
	private static int maxX = GameScreen.screenSize.x - 1;
	private static int maxY = GameScreen.screenSize.y - 1;
	
	private int moveX;
	private int moveY;
	
	private int damage;
	
	public Bullet(int x, int y, double angle) {
		
		this.radius = 10;
		this.x = x;
		this.y = y;
		this.angle = (float)angle;
		this.speed = 15;		
		this.visible = true;
		double radians = Math.toRadians(angle);
		this.moveX = (int)(speed * Math.cos(radians));
		this.moveY = (int)(speed * Math.sin(-radians));
		this.damage = 20;
		this.hit = false;
	}
	
	public void update(){
		
		if (!visible) return;
		
		x += moveX;
		y += moveY;

		if (x < minX || y < minY || x > maxX || y > maxY)
			visible = false;
	}
	
	public boolean isVisible(){
		return visible;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void accept(Collidable other) {
		other.visit(this);
	}
	
	@Override
	public void visit(Ship ship) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Bullet bullet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Enemy enemy) {
		enemy.health -= damage;
		this.visible = false;
		this.hit = true;
	}
}
