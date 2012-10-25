package kr.teamdeer.snap;

import java.util.ArrayList;

public class GestureElement {
	public int Id;
	public ArrayList<Point3> PointsAcc;
	public ArrayList<Point2> PointsOri;
	public ArrayList<Double> AccSource;
	public ArrayList<Double> OriSource;
	public ArrayList<Double> AccResult;
	public ArrayList<Double> OriResult;
	public int Size;
	public String Name;
	public String Action;
//	public String ThumbFileName;
	
	public GestureElement() {
		Id = 0;
		PointsAcc = new ArrayList<Point3>();
		PointsOri = new ArrayList<Point2>();
		AccSource = new ArrayList<Double>();
		OriSource = new ArrayList<Double>();
		AccResult = new ArrayList<Double>();
		OriResult = new ArrayList<Double>();
		Size = 0;
		Name = "No Name";
		Action = "";
		//ThumbFileName = "";
	}
}