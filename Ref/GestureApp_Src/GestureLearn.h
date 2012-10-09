/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 02.11.2001 10:40:01
 Version: 1.0.0

*/

#ifndef _GestureLearn_66d49a78_bcd9_458f_9ab1_8bffd5ced75d
#define _GestureLearn_66d49a78_bcd9_458f_9ab1_8bffd5ced75d

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

#include "MLNet.h"
#include "Board.h"
#include "GestureData.h"

const double pi = 3.1415926535;
const double kk = pi / 180.;

class GestureLearn 
	: public MLNet::Learn
{	
	HWND		m_hMain;	
	Board	   *m_boardp;
	unsigned	m_period;
	bool		m_stop;
	unsigned	m_cycles;
	double		m_momentum;	
	double		m_learning_rate_hi;
	double		m_learning_rate_lo;
	double		m_error_lo;
	HANDLE		m_thread;
	unsigned	m_last_cycle;
	MLNet::array_t	m_vec;

public:
	GestureLearn(MLNet& net) 
		: 
		MLNet::Learn(net, d_sigmoid), 				
		m_vec(RANGE_SIZE),
		m_stop(false),
		m_thread(0)
	{}
	
	// hypertan
	// static float sigmoid(float f)	{return float(tanh(f) / 2. + .5);}
	// static float d_sigmoid(float f)	{return float((1.-f * f));}	

	// log-sigmoid
	static float sigmoid(float f)	{return float(1. / (1. + exp(-f)));}
	static float d_sigmoid(float f)	{return float(f *  (1. - f));}	
	
protected:
	
	virtual bool get(unsigned, unsigned z, MLNet::array_t& v_in, MLNet::array_t& v_out)
	{			
		_ASSERTE(v_in.size()  == m_net.get_input_size());
		_ASSERTE(v_out.size() == m_net.get_output_size());
		_ASSERTE((sizeof(pattern_data)/sizeof(pattern_data[0])) == m_net.get_output_size());
			
		unsigned x = rand() % m_net.get_output_size();
		
		unsigned n;	
		MLNet::array_t::iterator ic = v_in.begin();
		MLNet::array_t::iterator is = v_in.begin() + m_net.get_input_size() / 2;
		
		// prepare v_in		
		for (n = 0; n < m_vec.size();  ++n)
			m_vec[n] = (float)add_noise(z, m_cycles, pattern_data[x][n]);

		// cosines and sinuses
		for (n = 0; n < m_vec.size();  ++n, ++ic, ++is)
		{
			*ic = (float)shift(cos(m_vec[n] * kk));
			*is = (float)shift(sin(m_vec[n] * kk));
		}

		_ASSERTE(ic == (v_in.begin() + m_net.get_input_size() / 2));
		_ASSERTE(is == v_in.end());
						
		// prepate v_out				
		for (n = 0; n < v_out.size(); ++n) 		
			v_out[n] = m_net.get_min();	
		v_out[x] = m_net.get_max();
						
		if (m_boardp && z && ((z % m_period) == 0))
		{	
			// updates a board

			float error = compute_mean_square_error();
			m_boardp->UpdateTrainingError(error);
			m_boardp->UpdateTrainingVector(m_vec);
			m_boardp->UpdateTrainingWeight(m_net.begin_weight(), m_net.end_weight());
			m_boardp->Refresh();

			if (error < m_error_lo)
			{				
				m_stop = true;
			}
		}
	
		m_last_cycle = z;
		return !m_stop;
	}

 public:
	static double add_noise(unsigned z, unsigned cycles, double x)
	{		
		if ((rand() % 100) > 50)
		{
			// random change of an angle in range :
			// {x - 45 * gaussian_distance() .. x + 45 * gaussian_distance()}

			double gaussian = exp(- ((double)z * 2.) / cycles);
			double d_max =  45 * gaussian;
			double d_low = -d_max;			

			double d; // = (double)(rand() % 60) - 30.;
			d = d_low + ((double)rand() / RAND_MAX) * (d_max - d_low);

			x += d;
			if (x > 360.) x -= 360.;
			if (x < 0.)   x += 360.;

			_ASSERTE(x >= 0.);
			_ASSERTE(x <= 360.);
		}		
		return x;
	}
	// just in case
	static double shift(double x) {	return x; }

 public:
	float compute_mean_square_error()
	{		
		MLNet::array_t v_in;
		MLNet::array_t v_out;

		double error = 0.;
		
		for (unsigned x = 0; x < m_net.get_output_size(); ++x)
		{	
			v_in.resize(m_net.get_input_size());
			v_out.resize(m_net.get_output_size());
						
			unsigned n;			
			typedef MLNet::array_t::iterator iterator;
			iterator i = v_in.begin();
			
			// prepare v_in
				
			// add cosines			
			for (n = 0; n < v_in.size() / 2;  ++n, ++i)
				*i = (float)shift(cos(pattern_data[x][n] * kk));

			// add sinuses			
			for (n = 0; n < v_in.size() / 2;  ++n, ++i)
				*i = (float)shift(sin(pattern_data[x][n] * kk));
					
			_ASSERTE(i == v_in.end());
			
			// propagate and compute mean square error
			m_net.propagate(v_in, v_out);			
						
			for (n = 0; n < v_out.size(); ++n)
			{			
				double diff = v_out[n] - (n == x ? m_net.get_max() : m_net.get_min());
				error += diff * diff;				
			}
		}
	
		return (float)sqrt(error / (m_net.get_output_size() * m_net.get_output_size()));	
	//	return float(error / (m_net.get_output_size() * m_net.get_output_size()));
	}

	bool start(HWND	hMain, Board* boardp, unsigned cycles, 
		double lr_hi, double lr_lo, double momentum, double error_lo)
	{	
		_ASSERTE(m_thread == 0);
		_ASSERTE(::IsWindow(hMain));

		m_hMain				= hMain;		
		m_boardp			= boardp;
		m_cycles			= cycles;
		m_momentum			= momentum;	
		m_learning_rate_hi	= lr_hi;
		m_learning_rate_lo	= lr_lo;
		m_error_lo			= error_lo;
		m_stop				= 0;
		m_last_cycle		= 0;
				
		unsigned id;
		m_thread = (HANDLE)_beginthreadex(0, 0, thread_proc, this, CREATE_SUSPENDED, &id);
		_ASSERTE(m_thread);

		if (m_thread)
		{
			if (m_boardp)
			{
				unsigned l	= m_boardp->StartTraining();
				m_period	= cycles / l;
				m_boardp->Refresh();
			}
			VERIFY(ResumeThread(m_thread));			
		}
		return m_thread != 0;
	}

	bool stop()
	{
		if (!m_thread) return true;		
		DWORD r = (DWORD)-1;		
		if (!GetExitCodeThread(m_thread, &r) || r == STILL_ACTIVE)
		{
			m_stop = true;
			r = WaitForSingleObject(m_thread, INFINITE);
			_ASSERTE(WAIT_OBJECT_0 == r);
			if (WAIT_OBJECT_0 != r)		
				VERIFY(TerminateThread(m_thread, 0));
		}		
		VERIFY(CloseHandle(m_thread));
		m_thread = 0;
		if (m_boardp)
		{
			m_boardp->StopTraining(m_last_cycle);
		}
		return !r;
	}
 
 private:
	static unsigned __stdcall thread_proc(void* pv)
	{
		GestureLearn* this_ = reinterpret_cast<GestureLearn*>(pv);
		this_->run (this_->m_cycles, 
					this_->m_learning_rate_hi, 
					this_->m_learning_rate_lo, 
					this_->m_momentum);
		::PostMessage(this_->m_hMain, WM_COMMAND, MAKEWPARAM(ID_STOP, 0), 0);
		return 0;
	}
};

#endif //_GestureLearn_66d49a78_bcd9_458f_9ab1_8bffd5ced75d

