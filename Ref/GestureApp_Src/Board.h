/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 01.11.2001 17:23:34
 Version: 1.0.0

*/

#ifndef _Board_6726ac43_b433_404f_928f_dcb951e922ed
#define _Board_6726ac43_b433_404f_928f_dcb951e922ed

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

class Board : 
	public CWindowImpl<Board, CStatic>
{
	friend class _Main;

public:
	typedef std::list<POINT>	path_t;
	typedef std::vector<float>	vector_real_t;
		
	struct winner
	{
		unsigned m_id;				// index of pattern in pattern_data array
		float    m_probability;
	};

private:
	enum mode
	{
		untrained_mode,
		training_mode,
		trained_mode,
		recognizing_mode,
		recognized_success,
		recognized_fail,
	};

	enum {WM_ASYNC_REFRESH = WM_USER + 101};
		
	const unsigned NUMBER_OF_ANGLES;
	const unsigned NUMBER_OF_ANCHOR_POINTS;

	bool		m_refresh;			// refresh flag
//	DWORD		m_timestamp;		// last redraw
	HDC			m_hMemDC;			
	HBITMAP		m_hBitmap;
	HBITMAP		m_hOldBitmap;
	HDC			m_hMemDC2;			
	HBITMAP		m_hBitmap2;
	HBITMAP		m_hOldBitmap2;
	HDC			m_hMemDC3;			
	HBITMAP		m_hBitmap3;
	HBITMAP		m_hOldBitmap3;
	HDC			m_hMemDC4;			
	HBITMAP		m_hBitmap4;
	HBITMAP		m_hOldBitmap4;
	RECT		m_rcMemDC;
	HBRUSH		m_hbrh1;
	HBRUSH		m_hbrh2;
	HPEN		m_hpen1;
	HPEN		m_hpen2;
	HPEN		m_hpen3;
	HPEN		m_hpen4;
	path_t		m_path;
	path_t		m_path2;		
	vector_real_t m_angles;
	vector_real_t m_cosines;
	vector_real_t m_sinuses;
	vector_real_t m_errors;	
	winner		m_winners[3];
	float		m_max_error;	
	unsigned	m_train_cycles;
	mode		m_mode;
	bool		m_update_top_desk;
	bool		m_update_bottom_desk;
	std::basic_string<TCHAR> m_info;
	std::vector<HPEN> m_hpdesk;

public:
	Board(unsigned range);
	~Board();

 DECLARE_WND_SUPERCLASS("Board", CStatic::GetWndClassName());

 BEGIN_MSG_MAP(_Main)	
	MESSAGE_HANDLER(WM_PAINT,		OnPaint)
	MESSAGE_HANDLER(WM_ERASEBKGND,	OnEraseBknd)
	MESSAGE_HANDLER(WM_SIZE,		OnSize)
	MESSAGE_HANDLER(WM_MOUSEMOVE,	OnMouseMove)	
	MESSAGE_HANDLER(WM_RBUTTONDOWN,	OnRButtonDown)
	MESSAGE_HANDLER(WM_RBUTTONUP,	OnRButtonUp)
	MESSAGE_HANDLER(WM_ASYNC_REFRESH, OnAsyncRefresh)
 END_MSG_MAP()

 private:
	LRESULT OnPaint(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnEraseBknd(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnSize(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnMouseMove(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnRButtonDown(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnRButtonUp(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	LRESULT OnAsyncRefresh(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);

	void CreateMemDC(HDC, RECT&);
	void CleanupMemDC();
	void Draw(HDC, RECT&);
	void DrawMemDC();
	void DrawPath(HDC hDC, RECT& rc);
	void DrawGraph(HDC hDC, RECT& rc);
	void DrawWinners(HDC hDC, RECT& rc);
	void DrawStatusInfo(HDC hDC, RECT& rc);
	void DrawTopDesc(HDC hDC, RECT& rc);
	void DrawBottomDesc(HDC hDC, RECT& rc);	
	void DrawVector(HDC, RECT& rc, vector_real_t&);
	bool TransfromPath();
	void Recognize();

 public:
	unsigned StartTraining();
	void UpdateTrainingError (float current_error);
	void UpdateTrainingVector(vector_real_t&);
	void UpdateTrainingWeight(vector_real_t::iterator, vector_real_t::iterator);
	void StopTraining(unsigned);

	void Refresh(bool immediately = false)
	{
		if (GetCurrentThreadId() != GetWindowThreadProcessId(m_hWnd, 0))		
		{
			PostMessage(WM_ASYNC_REFRESH);
			return;
		}
		
		m_refresh = true;
		if (immediately)
		{
			RECT rc;
			GetClientRect(&rc);
			
			HDC hDC = GetDC();
			Draw(hDC, rc);
			ReleaseDC(hDC);
		}
		else
		{
			Invalidate();
			UpdateWindow();
		}		
	}

		
 private:
	 void StartMouseRecord();
	 void AddMouseRecord(POINT);
	 void StopMouseRecord();

	void SetInfoStr(const TCHAR* p) {p ? m_info = p : m_info.erase();}

 public:
	 static vector_to_path(RECT& rc, vector_real_t& vec, path_t& path);	 
};

#endif //_Board_6726ac43_b433_404f_928f_dcb951e922ed

