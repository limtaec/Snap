// AboutBox.h : Declaration of the AboutBox

#ifndef __ABOUTBOX_H_
#define __ABOUTBOX_H_

#include "resource.h"       // main symbols
#include <atlhost.h>

#include "AnimationCtrl.h"

// 

/////////////////////////////////////////////////////////////////////////////
// AboutBox
class AboutBox : 
	public CAxDialogImpl<AboutBox>
{	
	AnimationCtrl m_alogo;
	
public:
	AboutBox()
	{
	}

	~AboutBox()
	{
	}

	enum { IDD = IDD_ABOUTBOX };

BEGIN_MSG_MAP(AboutBox)
	MESSAGE_HANDLER(WM_INITDIALOG,		OnInitDialog)
	MESSAGE_HANDLER(WM_ERASEBKGND,		OnEraseBkgnd)
	MESSAGE_HANDLER(WM_CTLCOLORSTATIC,	OnCtlColorStatic)	
	MESSAGE_HANDLER(WM_DESTROY,			OnDestroy)	
	MESSAGE_HANDLER(WM_LBUTTONDOWN,		OnLButtonDown);
	COMMAND_ID_HANDLER(IDOK,			OnOK)
	COMMAND_ID_HANDLER(IDCANCEL,		OnCancel)
END_MSG_MAP()
	
	LRESULT OnInitDialog(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)	
	{
		kb::OSVersionInfo vi;
		if (vi.dwPlatformId == VER_PLATFORM_WIN32_WINDOWS) 	
		{
			// dummy Win9x platforms
			// no animation

			RECT rc;

			CWindow logo(GetDlgItem(IDC_LOGO));
			logo.ShowWindow(SW_HIDE);			
			logo.GetClientRect(&rc);
			logo.MapWindowPoints(m_hWnd, &rc);

			int dy = rc.bottom - rc.top;

			for (unsigned i = 0; i < 3; ++i)
			{
				CWindow label1(GetDlgItem(IDC_STATIC1 + i));
				label1.GetClientRect(&rc);
				label1.MapWindowPoints(m_hWnd, &rc);
				rc.top -= dy; rc.bottom -= dy; 
				label1.SetWindowPos(0, &rc, SWP_NOSIZE|SWP_NOACTIVATE);
			}			

			GetWindowRect(&rc);
			rc.bottom -= dy; 
			SetWindowPos(0, &rc, SWP_NOMOVE|SWP_NOACTIVATE);
		}
		else
		{
			// nice animation for Win2k and WinXP

			m_alogo.SubclassWindow(GetDlgItem(IDC_LOGO));
			m_alogo.Load(_T("RT_JPEG"), IDR_RT_JPEG1);							
			m_alogo.Start(0, 19, 0, 33, 120, 120, true);	// 21 frame, 120*120
		}		
		return 1;  // Let the system set the focus
	}

	LRESULT OnDestroy(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)	
	{	
		m_alogo.Stop();
		return 0;
	}

	LRESULT OnEraseBkgnd(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)	
	{
		HDC hdc = (HDC)wParam;
		RECT rc;
		GetClientRect(&rc);		
		FillRect(hdc, &rc, (HBRUSH)GetStockObject(BLACK_BRUSH));
		FrameRect(hdc, &rc, (HBRUSH)GetStockObject(GRAY_BRUSH));
		return 0;
	}	

	LRESULT OnCtlColorStatic(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)	
	{
		HDC hdc = (HDC)wParam;
		SetBkMode(hdc, TRANSPARENT);	
		SetTextColor(hdc, RGB(90, 100, 245));
		return (LRESULT)(HBRUSH)GetStockObject(BLACK_BRUSH);
	}
	
	LRESULT OnLButtonDown(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)	
	{
		EndDialog(IDOK);
		return 0;
	}	

	LRESULT OnOK(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		EndDialog(wID);
		return 0;
	}

	LRESULT OnCancel(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		EndDialog(wID);
		return 0;
	}	
};

#endif //__ABOUTBOX_H_
