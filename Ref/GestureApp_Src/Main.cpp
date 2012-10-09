// Main.cpp : Implementation of _Main
/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 09.11.2001 13:15:13
 Version: 1.0.0

*/

#include "stdafx.h"
#include "Main.h"
#include "GestureData.h"
#include "AboutBox.h"

_Main::_Main()
	: 
	m_net(3, NET_INPUT_SIZE, NET_INPUT_SIZE, NET_OUTPUT_SIZE),
	m_board(RANGE_SIZE),
	m_learn(m_net),
	m_test_stage(0)
{	
	m_net.set_transfer_function(GestureLearn::sigmoid);
	m_net.set_bias(0.5);
	m_net.set_minmax(.0, 1.);
}

_Main::~_Main()
{
}

LRESULT _Main::OnTrain(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)	
{
	if (IDOK != m_trainDlg.DoModal(m_hWnd))
		return 0;

	m_learn.stop();
	m_learn.start(m_hWnd, &m_board, m_trainDlg.m_cycles, 
		m_trainDlg.m_hi_rate, m_trainDlg.m_lo_rate, m_trainDlg.m_momentum, m_trainDlg.m_error);	

	UIUpdate();
	return 0;
}

LRESULT _Main::OnStop(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	if (m_test_stage)
	{
		StopTest();
	}
	else
	{
		m_learn.stop();		
	}

	m_board.Refresh();	
	UIUpdate();
	return 0;
}

bool _Main::RecognizeBoard()
{	
	MLNet::array_t v_in (NET_INPUT_SIZE);
	MLNet::array_t v_out(NET_OUTPUT_SIZE);

	std::copy(m_board.m_cosines.begin(), m_board.m_cosines.end(), v_in.begin());
	std::copy(m_board.m_sinuses.begin(), m_board.m_sinuses.end(), v_in.begin() + RANGE_SIZE);

	m_net.propagate(v_in, v_out);

	typedef MLNet::array_t::iterator iterator;

	// apply softmax to a net output vector and find winner
	
	double sum = std::accumulate(v_out.begin(), v_out.end(), 0.);

	for (unsigned n = 0; n < sizeof(m_board.m_winners)/sizeof(m_board.m_winners[0]); ++n)
	{
		iterator i = std::max_element(v_out.begin(), v_out.end());
		m_board.m_winners[n].m_id = i - v_out.begin();
		m_board.m_winners[n].m_probability = (float)(double(*i) / sum);
		*i = 0;
	}	

	// verify winner 

	#define MIN_PROBABILITY 0.25
	#define MIN_DIFFERENCE	0.25

	if (m_board.m_winners[0].m_probability > MIN_PROBABILITY && 
	   (m_board.m_winners[0].m_probability - m_board.m_winners[1].m_probability) > MIN_DIFFERENCE)
	{
		m_board.m_mode = Board::recognized_success;
		return true;
	}

	m_board.m_mode = Board::recognized_fail;

	UIUpdate();
	return false;
}

//////////////////////////////////////////////////////

LRESULT _Main::OnFileOpen(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	OPENFILENAME ofn = {0};
	
	TCHAR buffer[MAX_PATH] = {0};
	
	if (m_filename.size())
		lstrcpy(buffer, m_filename.c_str());
	
	ofn.lStructSize	= sizeof OPENFILENAME;
	ofn.lpstrFile	= buffer;
	ofn.nMaxFile	= MAX_PATH;	
	ofn.hwndOwner	= m_hWnd;
	ofn.Flags		= OFN_EXPLORER|OFN_HIDEREADONLY;
	ofn.lpstrFilter = _T("Gesture network weights files (*.weights)\0*.weights\0")
					  _T("All files\0*.*\0");
	
	TCHAR dir[MAX_PATH];
	if (GetModuleFileName(GetModuleHandle(0), dir, MAX_PATH))
	{
		PathRemoveFileSpec(dir);
		ofn.lpstrInitialDir = dir;
	}
		
	if (!GetOpenFileName(&ofn))
		return 0;

	if (LoadNet(ofn.lpstrFile))
	{
		m_filename = ofn.lpstrFile;
		m_board.m_mode = Board::recognizing_mode;
		m_board.Refresh();
	}
	else
	{
		kb::MsgBox(MB_ICONWARNING, _T("GestureApp Open"), 
			_T("Unable to open file %s"), ofn.lpstrFile);
	}

	UIUpdate();
	return 0;
}
LRESULT _Main::OnFileSave(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	if (!m_filename.size())
	{
		OnFileSaveAs(wNotifyCode, wID, hWndCtl, bHandled);
	}
	else
	{
		if (!SaveNet(m_filename.c_str()))
		{
			kb::MsgBox(MB_ICONWARNING, _T("GestureApp Save"), 
				_T("Unable to save file %s"), m_filename.c_str());
		}			
	}

	UIUpdate();
	return 0;
}
LRESULT _Main::OnFileSaveAs(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	OPENFILENAME ofn = {0};
	
	TCHAR buffer[MAX_PATH] = {0};
	
	if (m_filename.size())
		lstrcpy(buffer, m_filename.c_str());
	
	ofn.lStructSize	= sizeof OPENFILENAME;
	ofn.lpstrFile	= buffer;
	ofn.nMaxFile	= MAX_PATH;	
	ofn.hwndOwner	= m_hWnd;
	ofn.Flags		= OFN_EXPLORER|OFN_HIDEREADONLY;
	ofn.lpstrFilter = _T("Gesture network weights files (*.weights)\0*.weights\0")
					  _T("All files\0*.*\0");
	
	TCHAR dir[MAX_PATH];
	if (GetModuleFileName(GetModuleHandle(0), dir, MAX_PATH))
	{
		PathRemoveFileSpec(dir);
		ofn.lpstrInitialDir = dir;
	}
		
	if (!GetSaveFileName(&ofn))
		return 0;
	
	PathAddExtension(ofn.lpstrFile, _T(".weights"));

	// to verify exists
	WIN32_FIND_DATA fd;
	HANDLE f = FindFirstFile(ofn.lpstrFile, &fd); 
	if (INVALID_HANDLE_VALUE != f)
	{
		FindClose(f);
		
		TCHAR msg[512] = {0};
		wsprintf(msg, _T("%s already exists.\nDo you want to replace it?"), ofn.lpstrFile);
		if (IDNO == ::MessageBox(m_hWnd, msg, _T("GestureApp Save"), MB_ICONWARNING|MB_YESNO))
			return 0;
	}
	
	m_filename.empty();
	if (SaveNet(ofn.lpstrFile))
	{
		m_filename = ofn.lpstrFile;		
	}		
	else
	{
		kb::MsgBox(MB_ICONWARNING, _T("GestureApp Save"), 
			_T("Unable to save file %s"), ofn.lpstrFile);
	}		

	UIUpdate();
	return 0;
}

bool _Main::SaveNet(const TCHAR* filename)
{	
	HANDLE file = CreateFile(filename, GENERIC_WRITE, FILE_SHARE_READ,
		0, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, 0);

	_ASSERTE(file != INVALID_HANDLE_VALUE);
	if (file == INVALID_HANDLE_VALUE) return false;

	DWORD data_size = m_net.size_weight() * sizeof(MLNet::array_t::value_type);
	MLNet::array_t::value_type* p = (MLNet::array_t::value_type*)_alloca(data_size);
		
	typedef MLNet::array_t::iterator iterator;
	MLNet::array_t::value_type* t = p;
	for (iterator i = m_net.begin_weight(); i != m_net.end_weight(); ++i, ++t)
		*t = *i;

	DWORD written = 0;	
	BOOL r = WriteFile(file, p, data_size, &written, 0);
	_ASSERTE(r);	
	VERIFY(CloseHandle(file));
	if (written != data_size)
		return false;
	return r != 0;
}

bool _Main::LoadNet(const TCHAR* filename)
{
	HANDLE file = CreateFile(filename, GENERIC_READ, FILE_SHARE_READ,
		0, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);

	_ASSERTE(file != INVALID_HANDLE_VALUE);
	if (file == INVALID_HANDLE_VALUE) return false;

	DWORD data_size = GetFileSize(file, 0);

	if (data_size != m_net.size_weight() * sizeof(MLNet::array_t::value_type))
		return false; // invalid size

	MLNet::array_t::value_type* p = (MLNet::array_t::value_type*)_alloca(data_size);
		
	DWORD number_of_read = 0;	
	BOOL r = ReadFile(file, p, data_size, &number_of_read, 0);
	_ASSERTE(r); r;
	VERIFY(CloseHandle(file));

	if (data_size != number_of_read)
		return false; // invalid size

	typedef MLNet::array_t::iterator iterator;
	MLNet::array_t::value_type* t = p;
	for (iterator i = m_net.begin_weight(); i != m_net.end_weight(); ++i, ++t)
		*i = *t;
	return true;
}

//////////////////////////////////////////////////////

LRESULT _Main::OnTest(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	StartTest();
	return 0;
}

void _Main::StartTest()
{
	_ASSERTE(sizeof(pattern_data)/sizeof(pattern_data[0]) == NUMBER_OF_PATTERNS);

	if (IDOK != m_testDlg.DoModal(m_hWnd))
		return;

	if (m_testDlg.m_selected.empty())
	{
		MessageBox(_T("Unable to start test, need to select at least one pattern for testing"),
			_T("GestureApp"), MB_ICONWARNING);
		return;
	}
		
	m_test_cycle	= 0;
	m_test_stage	= 1;
	m_cur_pattern	= m_testDlg.m_selected.begin();
	m_cur_pos		= m_path.end();				
	
	m_board.GetClientRect(&m_rcBoard);		
	InflateRect(&m_rcBoard, -5, -5);
		
	int cx				= m_rcBoard.right - m_rcBoard.left;
	int cy				= m_rcBoard.bottom - m_rcBoard.top;
	int mv				= min(cx, cy);
	m_rcBoard.left		= m_rcBoard.left + (cx - mv) / 2;
	m_rcBoard.top		= m_rcBoard.top  + (cy - mv) / 2;
	m_rcBoard.right		= m_rcBoard.left + mv;
	m_rcBoard.bottom	= m_rcBoard.top  + mv;
	
	// wait 50 .. 500 ms			
	SetTimer(TEST_TIMER_ID, 500 / m_testDlg.m_speed);
}

void _Main::StopTest()
{
	if (m_test_stage)
	{
		KillTimer(TEST_TIMER_ID);
		m_board.SetInfoStr(0);
		m_board.Refresh();
		m_test_stage = 0;
		m_path.clear();
	}	
}

LRESULT _Main::OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{	
	_ASSERTE(wParam == TEST_TIMER_ID);
	_ASSERTE(m_test_stage != 0);

	typedef Board::path_t::iterator iterator;
	
	if (m_cur_pos == m_path.end())
	{
		if (m_test_stage == 1)
		{
			// wait 50 .. 500 ms			
			SetTimer(TEST_TIMER_ID, 500 / m_testDlg.m_speed);

			// start a next pattern cycle
			Board::vector_real_t vec(RANGE_SIZE);	
			for (unsigned n = 0; n < RANGE_SIZE; ++n)
			{				
				if (m_testDlg.m_noise == CustTestDlg::LO_NOISE) 
					vec[n] = pattern_data[*m_cur_pattern][n];
				else
					vec[n] = (float)GestureLearn::add_noise(
						CustTestDlg::HI_NOISE - m_testDlg.m_noise,
						CustTestDlg::HI_NOISE, 
						pattern_data[*m_cur_pattern][n]);			
			}				
			Board::vector_to_path(m_rcBoard, vec, m_path);
			m_cur_pos = m_path.begin();

			m_test_stage = 2;
		}
		else
		{
			// end of pattern cycle

			m_board.StopMouseRecord();										
			m_path.clear();
			
			if (m_board.m_mode != Board::untrained_mode && 
				m_board.m_winners[0].m_id != *m_cur_pattern)
			{
				KillTimer(TEST_TIMER_ID);

				if (IDNO == kb::MsgBox(MB_ICONWARNING|MB_YESNO, 
					_T("GestureApp, Test"),
					_T("Pattern \"%s\" is recognized incorrectly\nContinue ?"), 
					pattern_names[*m_cur_pattern]))
				{
					StopTest();
					return 0;
				}
			}

			// wait 100 .. 1000 ms			
			SetTimer(TEST_TIMER_ID, 1000 / m_testDlg.m_speed);

			++m_cur_pattern;

			if (m_cur_pattern == m_testDlg.m_selected.end())
			{
				++m_test_cycle;
				if (m_test_cycle == m_testDlg.m_repeat)
				{
					StopTest();
					return 0;
				}					

				m_path.clear();
				m_cur_pattern	= m_testDlg.m_selected.begin();				
				m_cur_pos		= m_path.end();				
			}
			
			m_test_stage = 1;
			return 0;
		}
	}							
	
	_ASSERTE(m_cur_pos != m_path.end());

	POINT pt;
	pt.x = min(m_rcBoard.right,  max(m_rcBoard.left, m_rcBoard.left + (*m_cur_pos).x));
	pt.y = min(m_rcBoard.bottom, max(m_rcBoard.top,  m_rcBoard.top  + (*m_cur_pos).y));
	
	if (m_cur_pos == m_path.begin())
	{
		TCHAR buf[256];	
		sprintf(buf, "test \"%s\" pattern #%u(%u), press ESC for canceling", 
			pattern_names[*m_cur_pattern], 
			m_cur_pattern - m_testDlg.m_selected.begin() + m_testDlg.m_selected.size() * m_test_cycle, 
			m_testDlg.m_selected.size() * m_testDlg.m_repeat);
		m_board.SetInfoStr(buf);

		m_board.StartMouseRecord();
		m_board.AddMouseRecord(pt);
	}					
	else
	{		
		m_board.AddMouseRecord(pt);
	}

	m_board.Refresh(true);	
	++m_cur_pos;	
	return 0;
}

//////////////////////////////////////////////////////

void _Main::UIUpdate()
{
	HMENU hMenu		= GetMenu();
	HMENU hMenuFile = GetSubMenu(hMenu, 0);
	HMENU hMenuNet	= GetSubMenu(hMenu, 1);	
	
	switch (m_board.m_mode)
	{
	case Board::untrained_mode: 
		EnableMenuItem(hMenuFile, ID_FILEOPEN,	MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuFile, ID_FILESAVE,	MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuFile, ID_FILESAVEAS,MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_TRAIN,		MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuNet,  ID_STOP,		MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_TEST,		MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_RECOGNIZE,	MF_BYCOMMAND|MF_GRAYED); 
		break;
		
	case Board::training_mode: 
		EnableMenuItem(hMenuFile, ID_FILEOPEN,	MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuFile, ID_FILESAVE,	MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuFile, ID_FILESAVEAS,MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_TRAIN,		MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_STOP,		MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuNet,  ID_TEST,		MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_RECOGNIZE,	MF_BYCOMMAND|MF_GRAYED); 
		break;

	case Board::trained_mode: 
	case Board::recognizing_mode: 
	case Board::recognized_success:
	case Board::recognized_fail:
		EnableMenuItem(hMenuFile, ID_FILEOPEN,	MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuFile, ID_FILESAVE,	MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuFile, ID_FILESAVEAS,MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuNet,  ID_TRAIN,		MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuNet,  ID_STOP,		MF_BYCOMMAND|MF_GRAYED); 
		EnableMenuItem(hMenuNet,  ID_TEST,		MF_BYCOMMAND|MF_ENABLED); 
		EnableMenuItem(hMenuNet,  ID_RECOGNIZE,	MF_BYCOMMAND|MF_ENABLED);
		break;
	
	default: _ASSERTE(0); break;
	}
	
	if (m_test_stage)
	{
		EnableMenuItem(hMenuNet,  ID_STOP,		MF_BYCOMMAND|MF_ENABLED); 
	}
}

/////////////////////////////////////////////////////

bool _Main::OpenHLink(const TCHAR* url)
{
	// use COM
		
	HRESULT hr;
	CComPtr<IUniformResourceLocator> spURL;
	hr = ::CoCreateInstance(CLSID_InternetShortcut, 0, CLSCTX_INPROC_SERVER, IID_IUniformResourceLocator, (void**)&spURL);
	if (SUCCEEDED(hr)) 
	{
		hr = spURL->SetURL(url, IURL_SETURL_FL_GUESS_PROTOCOL);
		if (SUCCEEDED(hr)) 
		{  
			URLINVOKECOMMANDINFO ivci;
			ivci.dwcbSize	= sizeof (URLINVOKECOMMANDINFO);
			ivci.dwFlags	= 0;
			ivci.hwndParent	= m_hWnd;
			ivci.pcszVerb	= _T("open"); 
			
			if (SUCCEEDED(spURL->InvokeCommand (&ivci)))
				return true;
		}
	}	
	
	// attempt to use Shell

	SHELLEXECUTEINFO sei = {0};
	sei.cbSize	= sizeof SHELLEXECUTEINFO;
	sei.fMask	= 0; //SEE_MASK_FLAG_NO_UI ;
	sei.hwnd	= m_hWnd;
	sei.lpVerb	= _T("open");
	sei.lpFile	= url;
	sei.nShow	= SW_SHOW;

	return ShellExecuteEx(&sei) == TRUE;
}

LRESULT _Main::OnSendEmail(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{		
	const TCHAR *url = _T("mailto:konstantin@mail.primorye.ru?subject=GestureApp");
	if (!OpenHLink(url))
	{
		kb::MsgBox(MB_ICONWARNING, _T("GestureApp"), _T("Unable to send e-mail to %s"), url);
	}
	return 0;
}

LRESULT _Main::OnAbout(WORD wNotifyCode, WORD wID, HWND hWndCtl, BOOL& bHandled)
{
	AboutBox dlg;
	dlg.DoModal(m_hWnd);
	return 0;
}

LRESULT _Main::OnHelp(WORD, WORD, HWND, BOOL&)
{			
	TCHAR url[1024];
	GetModuleFileName(GetModuleHandle(0), url, sizeof(url)/sizeof(url[0]));
	PathRemoveFileSpec(url);
	PathAddBackslash(url);
	lstrcat(url, _T("help\\gestureapp_help.htm"));

	if (!OpenHLink(url))
	{
		kb::MsgBox(MB_ICONWARNING, _T("GestureApp"), _T("Unable to open file %s"), url);
	}

	return 0;
}