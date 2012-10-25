package kr.teamdeer.snap;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public final class GestureData {
	
	private static final class GestureDataContainer {
		static final GestureData Instance = new GestureData();
	}
	
	private ArrayList<GestureElement> Gestures;
	private NeuralNet m_AccNeuralNet;
	private NeuralNet m_OriNeuralNet;
	
	public static GestureData Instance() {
		return GestureDataContainer.Instance;
	}

	public GestureElement getGesture(int index) {
		return Gestures.get(index);
	}
	
	public ArrayList<GestureElement> getGestures() {
		return Gestures;
	}
	
	public void insert(Context context, GestureElement newData) {
		GestureDBHelper DBHelper
		 = new GestureDBHelper(context, "Gestures.db", null, 1);
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		ContentValues row = new ContentValues();
		row.put("name", newData.Name);
		row.put("action", newData.Action);
		int cnt = Math.min(newData.PointsAcc.size(),newData.PointsOri.size());
		row.put("count", cnt);
		String data[] = new String[2];
		data[0] = data[1] = "";
		for (int i=0; i<cnt; i++)
		{
			data[0] += newData.PointsAcc.get(i).x+","+
					   newData.PointsAcc.get(i).y+","+
					   newData.PointsAcc.get(i).z+";";
			data[1] += newData.PointsOri.get(i).x+","+
					   newData.PointsOri.get(i).y+";";
		}
		row.put("pacc", data[0]);
		row.put("pori", data[1]);
		db.insert("gestures", null, row);
		DBHelper.close();
		load(context);
	}
	
	public void update(Context context, int Id) {
		GestureDBHelper DBHelper
		 = new GestureDBHelper(context, "Gestures.db", null, 1);
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		ContentValues row = new ContentValues();
		int pos = -1;
		for (int i=0; i<Gestures.size(); i++) {
			if (Id == Gestures.get(i).Id)
				pos = i;
		}
		if (pos == -1) return;
		row.put("name", Gestures.get(pos).Name);
		row.put("action", Gestures.get(pos).Action);
		int cnt = Math.min(Gestures.get(pos).PointsAcc.size(),Gestures.get(pos).PointsOri.size());
		row.put("count", cnt);
		String data[] = new String[2];
		data[0] = data[1] = "";
		for (int i=0; i<cnt; i++)
		{
			data[0] += Gestures.get(pos).PointsAcc.get(i).x+","+
					   Gestures.get(pos).PointsAcc.get(i).y+","+
					   Gestures.get(pos).PointsAcc.get(i).z+";";
			data[1] += Gestures.get(pos).PointsOri.get(i).x+","+
					   Gestures.get(pos).PointsOri.get(i).y+";";
		}
		row.put("pacc", data[0]);
		row.put("pori", data[1]);
		db.update("gestures", row, "_id"+"="+Gestures.get(pos).Id, null);
		DBHelper.close();
		load(context);
	}
	
	public void delete(Context context, int Id) {
		GestureDBHelper DBHelper
		 = new GestureDBHelper(context, "Gestures.db", null, 1);
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		db.delete("gestures", "_id"+"="+Id, null);
		DBHelper.close();
		load(context);
	}
	
	public void load(Context context) {
		GestureDBHelper DBHelper
		 = new GestureDBHelper(context, "Gestures.db", null, 1);
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM gestures", null);
		Gestures = new ArrayList<GestureElement>();
		while (cursor.moveToNext())
		{
			GestureElement nge = new GestureElement();
			nge.Id = cursor.getInt(0);
			nge.Name = cursor.getString(1);
			nge.Action = cursor.getString(2);
			int count = cursor.getInt(3);
			nge.Size = count;
			String pacc[] = cursor.getString(4).split(";");
			String pori[] = cursor.getString(5).split(";");
			String tmp[];
			for (int i=0; i<count; i++)
			{
				{
					tmp = pacc[i].split(",");
					Point3 tmpA = new Point3();
					tmpA.x = Double.parseDouble(tmp[0]);
					tmpA.y = Double.parseDouble(tmp[1]);
					tmpA.z = Double.parseDouble(tmp[2]);
					nge.PointsAcc.add(tmpA);
				}
				{
					tmp = pori[i].split(",");
					Point2 tmpO = new Point2();
					tmpO.x = Double.parseDouble(tmp[0]);
					tmpO.y = Double.parseDouble(tmp[1]);
					nge.PointsOri.add(tmpO);
				}
			}
			Gestures.add(nge);
		}
		DBHelper.close();
		
		CreateTrainingSet();
		
		m_AccNeuralNet = new NeuralNet(30*3, Gestures.size(), 30*3, 1.0);
		m_OriNeuralNet = new NeuralNet(30*2, Gestures.size(), 30*2, 1.0);
		m_AccNeuralNet.Train(this, 1);
		m_OriNeuralNet.Train(this, 2);
	}
	
	public class GestureDBHelper extends SQLiteOpenHelper {
		public GestureDBHelper(Context context, String name, 
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		public GestureDBHelper(Context context) {
			super(context, "Gestures.db", null, 1);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE gestures "
			+"(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+" name TEXT, action TEXT, count INTEGER,"
			+" pacc TEXT, pori TEXT);");
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS gestures");
			onCreate(db);
		}
	}
	
	public void CreateTrainingSet() {
		int n = 0;
		for (GestureElement ge : Gestures) {
			ge.AccSource.clear();
						
			ArrayList<Vector3d> accv = new ArrayList<Vector3d>();
			for (int i=1; i<ge.PointsAcc.size(); i++) {
				Vector3d tmp = new Vector3d(
								ge.PointsAcc.get(i),
								ge.PointsAcc.get(i-1));
				tmp.normalize();
				accv.add(tmp);
			}
			accv.add(new Vector3d(0,0,0)); // for match to total value
			
			for (Vector3d acc : accv) {
				ge.AccSource.add(acc.x);
				ge.AccSource.add(acc.y);
				ge.AccSource.add(acc.z);
			}
						
			ge.OriSource.clear();
						
			ArrayList<Vector2d> oriv = new ArrayList<Vector2d>();
			for (int i=1; i<ge.PointsOri.size(); i++) {
				Vector2d tmp = new Vector2d(
								ge.PointsOri.get(i),
								ge.PointsOri.get(i-1));
				tmp.normalize();
				oriv.add(tmp);
			}
			oriv.add(new Vector2d(0,0)); // for match to total value
			
			for (Vector2d ori : oriv) {
				ge.OriSource.add(ori.x);
				ge.OriSource.add(ori.y);
			}
			
			ge.AccResult.clear();
			ge.OriResult.clear();
			for (@SuppressWarnings("unused") GestureElement gee : Gestures) {
				ge.AccResult.add(0.0);
				ge.OriResult.add(0.0);
			}
			ge.AccResult.set(n, 1.0);
			ge.OriResult.set(n, 1.0);
			
			n++;
		}
		
	}
	
	public ArrayList<ArrayList<Double>> GetAccInputSet() {
		ArrayList<ArrayList<Double>> rt = new ArrayList<ArrayList<Double>>();
		for (GestureElement ge : Gestures) {
			rt.add(ge.AccSource);
		}
		return rt;
	}
	
	public ArrayList<ArrayList<Double>> GetOriInputSet() {
		ArrayList<ArrayList<Double>> rt = new ArrayList<ArrayList<Double>>();
		for (GestureElement ge : Gestures) {
			rt.add(ge.OriSource);
		}
		return rt;
	}
	
	public ArrayList<ArrayList<Double>> GetAccOutputSet() {
		ArrayList<ArrayList<Double>> rt = new ArrayList<ArrayList<Double>>();
		for (GestureElement ge : Gestures) {
			rt.add(ge.AccResult);
		}
		return rt;
	}
	
	public ArrayList<ArrayList<Double>> GetOriOutputSet() {
		ArrayList<ArrayList<Double>> rt = new ArrayList<ArrayList<Double>>();
		for (GestureElement ge : Gestures) {
			rt.add(ge.OriResult);
		}
		return rt;
	}
	
	public void ApplyAccOutputSet(ArrayList<ArrayList<Double>> result) {
		int n = 0;
		for (GestureElement ge : Gestures) {
			ge.AccResult.clear();
			ge.AccResult.addAll(result.get(n));
			n++;
		}
	}
	
	public void ApplyOriOutputSet(ArrayList<ArrayList<Double>> result) {
		int n = 0;
		for (GestureElement ge : Gestures) {
			ge.OriResult.clear();
			ge.OriResult.addAll(result.get(n));
			n++;
		}
	}
	
	public int NeuralNetLearning(Context context, GestureElement newData) {
		insert(context, newData);
		return NeuralNetRecognize(newData);
	}
	
	public int NeuralNetRecognize(GestureElement recvData) {
		recvData.AccSource.clear();
		
		ArrayList<Vector3d> accv = new ArrayList<Vector3d>();
		for (int i=1; i<recvData.PointsAcc.size(); i++) {
			Vector3d tmp = new Vector3d(
					recvData.PointsAcc.get(i),
					recvData.PointsAcc.get(i-1));
			tmp.normalize();
			accv.add(tmp);
		}
		accv.add(new Vector3d(0,0,0)); // for match to total value
		
		for (Vector3d acc : accv) {
			recvData.AccSource.add(acc.x);
			recvData.AccSource.add(acc.y);
			recvData.AccSource.add(acc.z);
		}
					
		recvData.OriSource.clear();
					
		ArrayList<Vector2d> oriv = new ArrayList<Vector2d>();
		for (int i=1; i<recvData.PointsOri.size(); i++) {
			Vector2d tmp = new Vector2d(
					recvData.PointsOri.get(i),
					recvData.PointsOri.get(i-1));
			tmp.normalize();
			oriv.add(tmp);
		}
		oriv.add(new Vector2d(0,0)); // for match to total value
		
		for (Vector2d ori : oriv) {
			recvData.OriSource.add(ori.x);
			recvData.OriSource.add(ori.y);
		}
		
		recvData.AccResult.clear();
		recvData.OriResult.clear();
		
		recvData.AccResult = m_AccNeuralNet.Update(recvData.AccSource);
		recvData.OriResult = m_OriNeuralNet.Update(recvData.OriSource);
		
		int Count = Math.min(recvData.AccResult.size(), recvData.OriResult.size());
		
		if (Count==0) {
			return -1;
		}		
		
		double HighestOutputA = 0;
		int BestMatchA = -1;
		int MatchA = -1;
		double HighestOutputO = 0;
		int BestMatchO = -1;
		int MatchO = -1;
		
		for (int i=0; i<Count; ++i) {
			if (recvData.AccResult.get(i) > HighestOutputA) {
				HighestOutputA = recvData.AccResult.get(i);
				BestMatchA = i;
				if (HighestOutputA > 0.96) {
					MatchA = BestMatchA;
				}
			}
			if (recvData.OriResult.get(i) > HighestOutputO) {
				HighestOutputO = recvData.AccResult.get(i);
				BestMatchO = i;
				if (HighestOutputO > 0.96) {
					MatchO = BestMatchO;
				}
			}
		}
		
		if (MatchA == MatchO) {
			return MatchA;
		} else if ((MatchA == BestMatchO)) {
			return MatchA;
		} else if ((MatchO == BestMatchA)) {
			return MatchO;
		} else {
			return -1;
		}
	}
	
}