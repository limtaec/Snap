/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 06.11.2001 15:10:21
 Version: 1.0.0

*/

#ifndef _TrainOptDlg_7c1e836d_fa9c_4073_8f13_6cbb1fb2c792
#define _TrainOptDlg_7c1e836d_fa9c_4073_8f13_6cbb1fb2c792

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

#include "resource.h"
#include "SelectionSliderCtrl.h"

class TrainOptDlg :
	public CDialogImpl<TrainOptDlg>
{
public:
	unsigned	m_cycles;
	double		m_momentum;
	double		m_hi_rate;
	double		m_lo_rate;
	double		m_error;
	SelectionSliderCtrl	m_sliderRate;

public:
	TrainOptDlg() 
	{		
		m_cycles	= 7000;
		m_momentum	= 0.5;
		m_hi_rate	= 0.9;
		m_lo_rate	= 0.1;
		m_error		= 0.02;
	}
	
	enum { IDD = IDD_TRAINOPT};

	BEGIN_MSG_MAP(TrainOptDlg)
		MESSAGE_HANDLER(WM_INITDIALOG,		OnInitDialog)
		COMMAND_ID_HANDLER(IDOK,			OnOk)
		COMMAND_ID_HANDLER(IDCANCEL,		OnCancel)		
		REFLECT_NOTIFICATIONS()
	END_MSG_MAP()

	LRESULT OnInitDialog(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		SetDlgItemInt(IDC_EDIT_CYCLES,  m_cycles,	FALSE);
		SendDlgItemMessage(IDC_SPIN_CYCLES, UDM_SETRANGE, 0, MAKELONG(1, 20000));

		SendDlgItemMessage(IDC_SLIDER_MOMENTUM, TBM_SETRANGE, 0, MAKELONG(0, 10));
		SendDlgItemMessage(IDC_SLIDER_ERROR,	TBM_SETRANGE, 0, MAKELONG(0, 10));		
		SendDlgItemMessage(IDC_SLIDER_RATE,		TBM_SETRANGE, 0, MAKELONG(0, 10));		
		
		SendDlgItemMessage(IDC_SLIDER_MOMENTUM, TBM_SETPOS, TRUE, LPARAM(m_momentum * 10));
		SendDlgItemMessage(IDC_SLIDER_ERROR,    TBM_SETPOS, TRUE, LPARAM(m_error * 100));
		
		m_sliderRate.SubclassWindow(GetDlgItem(IDC_SLIDER_RATE));
		m_sliderRate.SetPos(int(m_hi_rate * 10));
		SendDlgItemMessage(IDC_SLIDER_RATE,     TBM_SETSEL, 0, MAKELONG(m_lo_rate * 10, m_hi_rate * 10));
	
		return 0;
	}

	LRESULT OnOk(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		m_cycles	= GetDlgItemInt(IDC_EDIT_CYCLES, 0, FALSE);
		m_momentum	= (double)SendDlgItemMessage(IDC_SLIDER_MOMENTUM, TBM_GETPOS) / 10.;
		m_error		= (double)SendDlgItemMessage(IDC_SLIDER_ERROR, TBM_GETPOS) / 100.;
		m_lo_rate	= (double)m_sliderRate.GetSelStart() / 10.;
		m_hi_rate	= (double)m_sliderRate.GetSelEnd() / 10.;

		EndDialog(wID);
		return 0;
	}

	LRESULT OnCancel(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		EndDialog(wID);
		return 0;
	}
};

#endif //_TrainOptDlg_7c1e836d_fa9c_4073_8f13_6cbb1fb2c792

