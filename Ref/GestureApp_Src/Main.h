// Main.h : Declaration of the _Main
/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 09.11.2001 13:15:07
 Version: 1.0.0

*/

#ifndef __MAIN_H_
#define __MAIN_H_

#include "resource.h"       // main symbols
#include "Board.h"       
#include "GestureLearn.h"
#include "MLNet.h"
#include "CustTestDlg.h"
#include "TrainOptDlg.h"

// Help and App commands
#define ID_APP_ABOUT                    0xE140
#define ID_APP_EXIT                     0xE141
#define ID_HELP                         0xE146

/////////////////////////////////////////////////////////////////////////////
// _Main
class _Main : 
	public CDialogImpl<_Main>
{
	MLNet			m_net;
	Board			m_board;
	GestureLearn	m_learn;
	TrainOptDlg		m_trainDlg;

	// test data
	CustTestDlg		m_testDlg;
	unsigned		m_test_stage;
	Board::path_t	m_path;
	RECT			m_rcBoard;
	unsigned		m_test_cycle;
	CustTestDlg::vector_t::iterator m_cur_pattern;	
	Board::path_t::iterator	m_cur_pos;

	// current weights filename (if any)
	std::basic_string<TCHAR> m_filename;		

	enum {TEST_TIMER_ID = 77};
	
public:
	_Main();
	~_Main();

	enum { IDD = IDD_MAIN };

 BEGIN_MSG_MAP(_Main)
	MESSAGE_HANDLER(WM_INITDIALOG,		OnInitDialog)
	MESSAGE_HANDLER(WM_DESTROY,			OnDestroy)
	MESSAGE_HANDLER(WM_SIZE,			OnSize)	
	MESSAGE_HANDLER(WM_TIMER,			OnTimer)
	COMMAND_ID_HANDLER(IDCANCEL,		OnCancel)
	COMMAND_ID_HANDLER(ID_TRAIN,		OnTrain)
	COMMAND_ID_HANDLER(ID_STOP,			OnStop)
	COMMAND_ID_HANDLER(ID_RECOGNIZE,	OnRecognize)
	COMMAND_ID_HANDLER(ID_FILEOPEN,		OnFileOpen)
	COMMAND_ID_HANDLER(ID_FILESAVE,		OnFileSave)	
	COMMAND_ID_HANDLER(ID_FILESAVEAS,	OnFileSaveAs)
	COMMAND_ID_HANDLER(ID_TEST,			OnTest)	
	COMMAND_ID_HANDLER(ID_HELP,			OnHelp)
	COMMAND_ID_HANDLER(ID_APP_ABOUT,	OnAbout)
	COMMAND_ID_HANDLER(ID_APP_EXIT,		OnCancel)
	COMMAND_ID_HANDLER(ID_APP_EMAIL,	OnSendEmail)
 END_MSG_MAP()

	LRESULT OnInitDialog(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		m_board.SubclassWindow(GetDlgItem(IDC_BOARD));
		UIUpdate();
		return 1;  // Let the system set the focus
	}
	LRESULT OnDestroy(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		StopTest();
		m_learn.stop();
		return 0;
	}
	LRESULT OnSize(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		if (wParam == SIZE_MINIMIZED) 
			return 0;
		int cx	= LOWORD(lParam); 
		int cy	= HIWORD(lParam);
		m_board.SetWindowPos(0, 0, 0, cx, cy, SWP_NOACTIVATE|SWP_NOMOVE);		
		return 0;
	}
	LRESULT OnCancel(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
	{
		DestroyWindow();
		return 0;
	}	
	LRESULT OnTrain(WORD, WORD, HWND, BOOL&);
	LRESULT OnStop(WORD, WORD, HWND, BOOL&);
	LRESULT OnRecognize(WORD, WORD, HWND, BOOL&)
	{
		RecognizeBoard();
		UIUpdate();
		return 0;
	}			
	LRESULT OnFileOpen(WORD, WORD, HWND, BOOL&);
	LRESULT OnFileSave(WORD, WORD, HWND, BOOL&);
	LRESULT OnFileSaveAs(WORD, WORD, HWND, BOOL&);
	LRESULT OnTest(WORD, WORD, HWND, BOOL&);	
	LRESULT OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);	
	LRESULT OnHelp(WORD, WORD, HWND, BOOL&);
	LRESULT OnAbout(WORD, WORD, HWND, BOOL&);
	LRESULT OnSendEmail(WORD, WORD, HWND, BOOL&);

	BOOL DispatchDialogMessage(MSG* pMsg)
	{
		if (!m_hWnd) return 0;

		if ((pMsg->message >= WM_KEYFIRST && pMsg->message <= WM_KEYFIRST) && 
				pMsg->wParam == VK_ESCAPE)
		{
			if (m_test_stage) 
			{
				StopTest();
				return TRUE;
			}			
		}

		if (IsDialogMessage(pMsg)) 		
		{		
			return TRUE;
		}			
		return FALSE;
	}		
	
	virtual void OnFinalMessage(HWND)
	{	
		PostQuitMessage(1);
	}

	void StartTest();
	void StopTest();

	bool OpenHLink(const TCHAR*);
	
private:
	bool SaveNet(const TCHAR*);
	bool LoadNet(const TCHAR*);
	bool RecognizeBoard();
	void UIUpdate();
};


#endif //__MAIN_H_
