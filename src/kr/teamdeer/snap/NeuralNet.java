package kr.teamdeer.snap;

import java.util.ArrayList;
import java.util.Random;

public class NeuralNet {
	
	public int mNumInputs;
	public int mNumOutputs;
	public int mNumHiddenLayers;
	public int mNeuronsPerHiddenLayer;
	
	public double mLearningRate;
	public double mErrorSum;
	public boolean mTrained;
	
	public int mNumEpochs;
	
	public ArrayList<NeuronLayer> mLayers;

	public NeuralNet(int NumInputs, int NumOutputs,
			int HiddonNeurons, double LearningRate) {
		mNumInputs = NumInputs;
		mNumOutputs = NumOutputs;
		mNumHiddenLayers = 1;
		mNeuronsPerHiddenLayer = HiddonNeurons;
		mLearningRate = LearningRate;
		mErrorSum = 9999;
		mTrained = false;
		mNumEpochs = 0;
		mLayers = new ArrayList<NeuronLayer>();
		
		CreateNet();
	}
	
	public void CreateNet() {
		if (mNumHiddenLayers > 0) {
			mLayers.add(
				new NeuronLayer(
					mNeuronsPerHiddenLayer,
					mNumInputs));
			for (int i=0; i<mNumHiddenLayers-1; ++i) {
				mLayers.add(
					new NeuronLayer(
						mNeuronsPerHiddenLayer,
						mNeuronsPerHiddenLayer));
			}
			
			mLayers.add(
				new NeuronLayer(
					mNumOutputs,
					mNeuronsPerHiddenLayer));
		} else {
			mLayers.add(
					new NeuronLayer(
						mNumOutputs,
						mNumInputs));
		}
	}
	
	public void InitializeNetwork() {
		Random rand = new Random();
		for (NeuronLayer nl : mLayers) {
			for (Neuron n : nl.mNeurons) {
				for (int k=0; k<n.mInputs; ++k) {
					n.mWeight.set(k, rand.nextDouble()-rand.nextDouble()); 
				}
			}
		}
		
		mErrorSum = 9999;
		mNumEpochs = 0;
	}
	
	public boolean NetworkTrainingEpoch(
			ArrayList<ArrayList<Double>> SetIn,
			ArrayList<ArrayList<Double>> SetOut) {
		
		double WeightUpdate = 0;
		mErrorSum = 0;
		
		for (int vec=0; vec<SetIn.size(); ++vec) {
			ArrayList<Double> outputs = Update(SetIn.get(vec));
			if (outputs.size() == 0) return false;
			
			for (int op=0; op<mNumOutputs; ++op) {
				double err = (SetOut.get(vec).get(op)-outputs.get(op)) * 
							 outputs.get(op) * (1-outputs.get(op));
				mErrorSum += (SetOut.get(vec).get(op)-outputs.get(op)) *
							 (SetOut.get(vec).get(op)-outputs.get(op));
				
				mLayers.get(1).mNeurons.get(op).mError = err;
				
				int wcnt = mLayers.get(1).mNeurons.get(op).mWeight.size()-1;
				for (int wc=0; wc<wcnt-1; ++wc) {
					WeightUpdate = err * mLearningRate * mLayers.get(0).mNeurons.get(wc).mActivation;
					mLayers.get(1).mNeurons.get(op).mWeight.set(wc, Double.valueOf(
							mLayers.get(1).mNeurons.get(op).mWeight.get(wc)+WeightUpdate+
							mLayers.get(1).mNeurons.get(op).mPrevUpdate.get(wc)*0.9));
					mLayers.get(1).mNeurons.get(op).mPrevUpdate.set(wc, WeightUpdate);
				}
				
				WeightUpdate = err * mLearningRate * -1;
				mLayers.get(1).mNeurons.get(op).mWeight.set(wcnt, Double.valueOf(
						mLayers.get(1).mNeurons.get(op).mWeight.get(wcnt)+WeightUpdate+
						mLayers.get(1).mNeurons.get(op).mPrevUpdate.get(wcnt)*0.9));
				mLayers.get(1).mNeurons.get(op).mPrevUpdate.set(wcnt, WeightUpdate);
			}
			
			int n=0;
			for (Neuron ne : mLayers.get(0).mNeurons) {
				double err = 0;
				
				for (Neuron neo : mLayers.get(1).mNeurons) {
					err += neo.mError * neo.mWeight.get(n);
				}
				
				err *= ne.mActivation * (1-ne.mActivation); 
				
				for (int w=0; w<mNumInputs; ++w) {
					WeightUpdate = err * mLearningRate * 
								   SetIn.get(vec).get(w);
					ne.mWeight.set(w, Double.valueOf(
							ne.mWeight.get(w)+WeightUpdate+
							ne.mPrevUpdate.get(w)*0.9));
					ne.mPrevUpdate.set(w, WeightUpdate);
				}
				
				WeightUpdate = err * mLearningRate * -1;
				ne.mWeight.set(mNumInputs, Double.valueOf(
						ne.mWeight.get(mNumInputs)+WeightUpdate+
						ne.mPrevUpdate.get(mNumInputs)*0.9));
				ne.mPrevUpdate.set(mNumInputs, WeightUpdate);
				++n;
			}
		}
		
		return true;
	}
		
	public ArrayList<Double> Update(ArrayList<Double> inputs) {
		Random rand = new Random();
		for (int k=0; k<inputs.size(); ++k) {
			inputs.set(k, Double.valueOf(
				inputs.get(k).doubleValue()*
				rand.nextFloat()*0.1));
		}
		
		ArrayList<Double> outputs = new ArrayList<Double>();
		int Weight = 0;
		
		for (NeuronLayer nl : mLayers) {
			outputs.clear();
			Weight = 0;
			for (Neuron n : nl.mNeurons) {
				double netinput = 0.0;
				int NumInputs = n.mInputs;
				for (int k=0; k<NumInputs-1; ++k) {
					netinput += n.mWeight.get(k) * inputs.get(Weight++);
				}
				netinput += n.mWeight.get(NumInputs-1) * -1;
				n.mActivation = Sigmoid(netinput, 1.0);
				outputs.add(n.mActivation);
				Weight = 0;
			}
			inputs.clear();
			inputs.addAll(outputs);
		}
		
		return outputs;
	}
	
	public boolean Train(GestureData gData, int type) {
		ArrayList<ArrayList<Double>> SetIn;
		ArrayList<ArrayList<Double>> SetOut;
		
		switch (type) {
		case 1: 
			SetIn = gData.GetAccInputSet();
			SetOut = gData.GetAccOutputSet();
			break;
		case 2:
			SetIn = gData.GetOriInputSet();
			SetOut = gData.GetOriOutputSet();
			break;
		default:
			return false;
		}
		
		InitializeNetwork();
		
		while(mErrorSum > 0.003) {
			if (!NetworkTrainingEpoch(SetIn, SetOut))
			{
				return false;
			}
			
			++mNumEpochs;
		}
		
		mTrained = true;
		
		return true;
	}
	
	public class NeuronLayer {
		public int mNeuronNum;
		public ArrayList<Neuron> mNeurons;
		
		public NeuronLayer(int NumNeurons, int NumInputsPerNeuron) {
			mNeuronNum = NumNeurons;
			mNeurons = new ArrayList<Neuron>(); 
			for (int i=0; i<NumNeurons; ++i) {
				mNeurons.add(new Neuron(NumInputsPerNeuron));
			}
		}
	}
	
	public class Neuron {
		public int mInputs;
		public ArrayList<Double> mWeight;
		public ArrayList<Double> mPrevUpdate;
		public double mActivation;
		public double mError;
		
		public Neuron(int NumInputs) {			
			mWeight = new ArrayList<Double>();
			mPrevUpdate = new ArrayList<Double>();
			mInputs = NumInputs+1;
			mActivation = 0;
			mError = 0;
			Random rand = new Random();
			for (int i=0; i<NumInputs+1; ++i) {
				mWeight.add(rand.nextDouble()-rand.nextDouble());
				mPrevUpdate.add(0.0);
			}
		}
	}
	
	public static double Sigmoid(double netinput, double response) {
		return ( 1 / ( 1 + Math.exp(-netinput / response)));
	}
}
