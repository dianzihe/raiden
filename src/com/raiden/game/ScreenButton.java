package com.raiden.game;

import com.raiden.framework.Screen;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;


public class ScreenButton {
	
	public final static String GAME_FONT = "MYRIADPRO-REGULAR.OTF";
	public final static int GAME_FONT_COLOR = Color.rgb(255, 195, 0);
	
	public Rect hitbox;
	
	public int x;
	public int y;
	public int width;
	public int height;
	public boolean toggled;
	
	public String label;
	public int labelX;
	public int labelY;
	Paint paint;
	
	public Screen nextScreen;
	
	
	public ScreenButton(int x, int y, int width, int height, String label, int labelX, int labelY, Paint paint, Screen nextScreen, boolean toggled) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.label = label;
		this.labelX = labelX;
		this.labelY = labelY;
		this.paint = paint;
		this.nextScreen = nextScreen;
		
		hitbox = new Rect(x, y, x + width, y + height);
		
		this.toggled = toggled;
	}
	
	public ScreenButton(int x, int y, int width, int height, Screen nextScreen, boolean toggled) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.label = "";
		this.labelX = 0;
		this.labelY = 0;
		this.paint = null;
		this.nextScreen = nextScreen;
		
		hitbox = new Rect(x, y, x + width, y + height);
		
		this.toggled = toggled;
	}

}
