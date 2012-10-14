package kr.teamdeer.snap;

import java.util.ArrayList;

public final class GestureData {
	
	private static final class GestureDataContainer {
		static final GestureData Instance = new GestureData();
	}
	
	private ArrayList<GestureElement> Gestures;
	
	public static GestureData Instance() {
		return GestureDataContainer.Instance;
	}

	public GestureElement getGesture(int index) {
		return Gestures.get(index);
	}
	
	public ArrayList<GestureElement> getGestures() {
		return Gestures;
	}

	public void setGestures(ArrayList<GestureElement> gestures) {
		Gestures = gestures;
	}
	
	private GestureData() {
		load();
	}
	
	public void save() {
		
	}
	
	public void load() {
		Gestures = new ArrayList<GestureElement>();
	}
}