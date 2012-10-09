/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 05.11.2001 17:37:02
 Version: 1.0.0

*/

#ifndef _CustTestDlg_47a760fa_dc01_41b3_8682_4f13cbd690eb
#define _CustTestDlg_47a760fa_dc01_41b3_8682_4f13cbd690eb

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

#include "GestureData.h"

class CustTestDlg :
	public CDialogImpl<CustTestDlg>
{
public:
	enum 
	{		
		LO_NOISE = 0,
		LO_SPEED = 1,
		HI_NOISE = 9,
		HI_SPEED = 10,
	};

public:	
	typedef std::vector<unsigned> vector_t;
	vector_t m_selected;
	bool	 m_use_all;
	unsigned m_speed;
	unsigned m_noise;
	unsigned m_repeat;

public:
	CustTestDlg() 
	{
		m_use_all	= true;
		m_speed		= 3;
		m_noise		= 0;
		m_repeat	= 1;
	}
	
	enum { IDD = IDD_CUSTOMIZE };

	BEGIN_MSG_MAP(CustTestDlg)
		MESSAGE_HANDLER(WM_INITDIALOG,		OnInitDialog)
		COMMAND_ID_HANDLER(IDOK,			OnOk)
		COMMAND_ID_HANDLER(IDCANCEL,		OnCancel)
		COMMAND_ID_HANDLER(IDC_CHECK_ALL,	OnCheckAll)
	END_MSG_MAP()

	LRESULT OnInitDialog(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		for (unsigned p = 0; p < NUMBER_OF_PATTERNS; ++p)	
		{
			SendDlgItemMessage(IDC_LIST_PATTERN, LB_ADDSTRING, 0, (LPARAM)pattern_names[p]);
		}

		::EnableWindow(GetDlgItem(IDC_LIST_PATTERN), !m_use_all);
		CheckDlgButton(IDC_CHECK_ALL, m_use_all ? BST_UNCHECKED : BST_CHECKED);

		if (!m_use_all)
		{
			typedef vector_t::iterator  iterator;
			for (iterator i = m_selected.begin(); i != m_selected.end(); ++i)
				VERIFY(LB_ERR != SendDlgItemMessage(IDC_LIST_PATTERN, LB_SETSEL, TRUE, *i));
		}
	

		SendDlgItemMessage(IDC_SPIN_REPEAT, UDM_SETRANGE, 0, MAKELONG(1, 100));
		SetDlgItemInt(IDC_EDIT_REPEAT,  m_repeat,	FALSE);

		SendDlgItemMessage(IDC_SLIDER_NOISE, TBM_SETRANGE, 0, MAKELONG(LO_NOISE, HI_NOISE));
		SendDlgItemMessage(IDC_SLIDER_SPEED, TBM_SETRANGE, 0, MAKELONG(LO_SPEED, HI_SPEED));

		SendDlgItemMessage(IDC_SLIDER_NOISE, TBM_SETPOS, TRUE, m_noise);
		SendDlgItemMessage(IDC_SLIDER_SPEED, TBM_SETPOS, TRUE, m_speed);

		return 1;
	}

	LRESULT OnCheckAll(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		::EnableWindow(GetDlgItem(IDC_LIST_PATTERN), IsDlgButtonChecked(IDC_CHECK_ALL) == BST_CHECKED);
		return 0;
	}

	LRESULT OnOk(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		m_use_all = IsDlgButtonChecked(IDC_CHECK_ALL) == BST_UNCHECKED;
		
		if (!m_use_all)
		{
			// get list items			
			int size = SendDlgItemMessage(IDC_LIST_PATTERN, LB_GETSELCOUNT);
			_ASSERTE(size != LB_ERR);
			if (size != LB_ERR)
			{
				int *buffer = (int *)_alloca(size * sizeof(int));
				int r = SendDlgItemMessage(IDC_LIST_PATTERN, LB_GETSELITEMS, size, (LPARAM)buffer);
				_ASSERTE(r != LB_ERR);
				if (r != LB_ERR)
				{
					m_selected.resize(r);
					for (int n = 0; n < r; ++n)
					{
						_ASSERTE(buffer[n] >= 0);
						_ASSERTE(buffer[n] < NUMBER_OF_PATTERNS);
						m_selected[n] = buffer[n];
					}						
				}			
			}				
		}
		else
		{
			int size = SendDlgItemMessage(IDC_LIST_PATTERN, LB_GETCOUNT);
			_ASSERTE(size != LB_ERR);			
			if (size != LB_ERR)
			{
				m_selected.resize(size);
				for (int n = 0; n < size; ++n)
					m_selected[n] = n;
			}			
		}

		m_repeat = GetDlgItemInt(IDC_EDIT_REPEAT, 0, FALSE);
		m_noise  = SendDlgItemMessage(IDC_SLIDER_NOISE, TBM_GETPOS);
		m_speed  = SendDlgItemMessage(IDC_SLIDER_SPEED, TBM_GETPOS);
		
		EndDialog(wID);
		return 0;
	}

	LRESULT OnCancel(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		EndDialog(wID);
		return 0;
	}
	
};

#endif //_CustTestDlg_47a760fa_dc01_41b3_8682_4f13cbd690eb

